package bid.mythesis.factorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implementation of Quadratic Sieve method for integer factorization
 * @author David Salac
 */
public class QuadraticSieve extends Factorization {
    private final BigInteger sqrtN;
    //Interval for x
    private BigInteger xInterval;
    //Size of factor base (FB) = number of primes in FB
    private int factorBaseSize;
    //Minimal dimension of null space of parity matrix
    private int parityMatrixOffset;
    
    private BigInteger[] factorBaseDixon;
    private BigInteger[] factorBaseQS;
    
    /**
     * Create instance of class prepared for factorization
     * @param n Number to be factorized
     */
    public QuadraticSieve(BigInteger n) {
        super(n);
        int fbMinimalSize = 384;
        BigInteger xIntervalDivisor = new BigInteger("16384");
        if(n.bitLength() <= 55) {
            fbMinimalSize = 256;
            xIntervalDivisor = new BigInteger("256");
        }
        else if(n.bitLength() <= 65) {
            fbMinimalSize = 512;
            xIntervalDivisor = new BigInteger("256");
        }
        this.factorBaseSize = fbMinimalSize + 
                (n.toString().length() * n.toString().length() * n.toString().length() * n.toString().length())
                / 1024; //Factor base size is empirically set to |n_10|^4/4096
        this.parityMatrixOffset = 5; //Minimal null space (Kernel) dimension
        //Set interval for m as 8*|FB|
        this.xInterval = new BigInteger("512").add(Factorization.bigIntegerSqrt(this.getN()).divide(xIntervalDivisor));
        //Square root of n
        this.sqrtN = bigIntegerSqrt(this.getN());
        
        this.factorBaseDixon = this.getDixonFactorBaseForQS();
        this.factorBaseQS = this.getQSFactorBaseForQS();
    }
    
    /**
     * Find factor base consists of first k (k=factorBaseSize) prime numbers
     * @return Factor base of size factorBaseSize
     */
    private BigInteger[] getDixonFactorBaseForQS() {
        BigInteger [] fb = new BigInteger[this.factorBaseSize];
        fb[0] = new BigInteger("2");
        int index = 1;
        for(BigInteger pr = new BigInteger("3"); index < fb.length; pr = pr.add(Factorization.BIGINTEGER_TWO)) {
            if(pr.isProbablePrime(100)/* && legendereSymbolModPrime(this.getN(), pr) == 1*/) {
                fb[index] = pr;
                index ++;
            }
        }
        return fb;
    }
    
    /**
     * Generate factor base usable for Shank-Tonelli algorithm
     * @return Factor base
     */
    private BigInteger[] getQSFactorBaseForQS() {
        List<BigInteger> fb = new ArrayList<>();
        int index = 0;
        for(BigInteger pr : this.factorBaseDixon) {
            if(index++ == 0) continue;
            if(legendereSymbolModPrime(this.getN(), pr) == 1) {
                fb.add(pr);
            }
        }
        if(fb.isEmpty()) return null;
        BigInteger [] biFactorBase = new BigInteger[fb.size()];
        for(int i = 0; i < fb.size(); i++) {
            biFactorBase[i] = fb.get(i);
        }
        return biFactorBase;
    }
    
    /**
     * Base of polynomial for Quadratic Sieve Q(x)=x+floor(sqrt(n))
     * @param x Variable of polynomial
     * @return Result of polynomial
     */
    private BigInteger quadraticSievePolynomial(BigInteger x) {
        return x.add(sqrtN); //Simplest possible polynomial
    }
    /**
     * Complete polynomial for Quadratic Sieve method (Q(x) := (x + ceil(sqrt(n)))^2 - n )
     * @param x Variable of polynomial
     * @return Result of polynomial
     */
    private BigInteger quadraticSievePolynomialComplete(BigInteger x) {
        return (x.add(sqrtN)).multiply(x.add(sqrtN)).subtract(this.getN()); //Simplest possible polynomial
    }
    
    /**
     * Quadratic Sieve method for integer factorization
     * @return Factors of n or null if does not found anything
     */
    private BigInteger[] quadraticSieveMethod() {
        //Exponents of Q(x) over FB
        Integer[][] exponentsOverFB = new Integer[factorBaseSize + parityMatrixOffset][factorBaseSize]; //System.out.println(factorBaseSize + parityMatrixOffset);
        //Represents matrix of parity bits
        MatrixGF2 parityMatrix = new MatrixGF2(factorBaseSize + parityMatrixOffset, factorBaseSize);
        //List of all finded x with indices values
        Map<BigInteger, Integer> findX = new HashMap<>();
        //Index of array and counter of successful matches
        int index = 0;
        //Following part use Tonelli-Shank algorithm for finding x:
        for(int i = this.factorBaseQS.length - 1; i >= 0; i--) {
            BigInteger pi = factorBaseQS[i];
            BigInteger s1 = tonelliShanksAlgorithm(this.getN().mod(pi), pi);
            if(s1 != null) {
                BigInteger s2 = pi.subtract(s1);
                BigInteger x1 = s1; BigInteger x2 = s2;
                
                //Compute all multiple of x1 and x2:
                boolean x1OutOfRange = false;
                boolean x2OutOfRange = false;
                for(BigInteger k = BigInteger.ONE; !(x1OutOfRange && x2OutOfRange); k = k.add(BigInteger.ONE)) {
                    //Find value of Q(x1)
                    BigInteger x = x1.add(k.multiply(pi));
                    if(x.compareTo(xInterval) > 0) x1OutOfRange = true;
                    if(!findX.containsKey(x)) {
                        BigInteger Qx = quadraticSievePolynomialComplete( x ); 
                        Integer [] xFactorizationOverFB = Factorization.getFactorBaseCoeficients(Qx, this.factorBaseDixon);
                        if(xFactorizationOverFB != null) {
                            exponentsOverFB[index] = xFactorizationOverFB;
                            parityMatrix.insertRow(xFactorizationOverFB, index);
                            findX.put(x, index++);// System.out.println(Integer.toString(index) + " " +pi);
                            if(index >= factorBaseSize + parityMatrixOffset) break;
                        }
                    }
                    
                    //Find value of Q(x2)
                    x = x2.add(k.multiply(pi));
                    if(x.compareTo(xInterval) > 0) x2OutOfRange = true;
                    if(!findX.containsKey(x)) {
                        BigInteger Qx = quadraticSievePolynomialComplete( x ); 
                        Integer [] xFactorizationOverFB = Factorization.getFactorBaseCoeficients(Qx, this.factorBaseDixon);
                        if(xFactorizationOverFB != null) {
                            exponentsOverFB[index] = xFactorizationOverFB;
                            parityMatrix.insertRow(xFactorizationOverFB, index);
                            findX.put(x, index++); //System.out.println(Integer.toString(index) + " " +pi);
                            if(index >= factorBaseSize + parityMatrixOffset) break;
                        }
                    }
                }
                if(index >= factorBaseSize + parityMatrixOffset) break;
            }
        }
        
        //For situation where method does not succeed
        if(index != factorBaseSize + parityMatrixOffset) return null;
        
        //Find null space of (transposed) parity matrix 
        MatrixGF2 nullspace = parityMatrix.transpose().getNullspace();
        if(nullspace == null) return null;
        
        //Extract values from hash map findX:
        BigInteger [] xi = new BigInteger[factorBaseSize + parityMatrixOffset];
        findX.keySet().forEach((key) -> {
            xi[findX.get(key)] = this.quadraticSievePolynomial(key);
        });
        
        //Finding values of legendere congruence:
        for(int col = 0; col < nullspace.getColsCount(); col++) {
            //Find value of x (left side of Legendere congruence) and factorization of y (right side of legendere congruence)
            BigInteger x = new BigInteger("1");
            //Exponents of factorization of right side of legendere congruence
            int [] yFactorization = new int[factorBaseSize];
            for(int row = 0; row < nullspace.getRowsCount(); row++) {
                //This find whether vector is relevant (1) or not (0)
                if(nullspace.getElement(row, col) == 1) {
                    x = x.multiply(xi[row]);
                    Integer [] yVecFactors = exponentsOverFB[row];
                    //Append exponets of vector to yFactorization
                    for(int i = 0; i < factorBaseSize; i++) { yFactorization[i] += yVecFactors[i]; }
                }
            }
            //Find y value
            BigInteger y = new BigInteger("1");
            for(int i = 0; i < factorBaseSize; i++) {
                y = y.multiply(this.factorBaseDixon[i].pow(yFactorization[i] / 2));
            }
            if(this.getN().gcd(x.add(y)).compareTo(BigInteger.ONE) != 0 
                    && this.getN().gcd(x.add(y)).compareTo(this.getN()) != 0 ) {
                return new BigInteger[]{ this.getN().gcd(x.add(y)), this.getN().gcd(x.subtract(y)) };
            }
        }
        
        //If there is no relevant results:
        return null;
    }
    
    /**
     * Computes Legendere symbol of n mod p
     * @param n Natural number
     * @param p Prime number
     * @return Legendere symbol of n mod p
     */
    public static int legendereSymbolModPrime(BigInteger n, BigInteger p) {
        if(n.mod(p).compareTo(BigInteger.ZERO) == 0) return 0;
        BigInteger res = n.modPow( (p.subtract(BigInteger.ONE)).divide(new BigInteger("2")), p);
        if(res.compareTo(BigInteger.ONE) == 0) return 1;
        else if(res.compareTo(BigInteger.ONE) > 0) return -1;
        return 0;
    }
    
    /**
     * Algorithm for solving congruence of form x^2 = n (mod p)
     * @param n Integer of congruence: x^2 = n (mod p)
     * @param p Prime number (IMPORTANT), modulus of congruence: x^2 = n (mod p)
     * @return Plausible x of congruence: x^2 = n (mod p)
     */
    public static BigInteger tonelliShanksAlgorithm(BigInteger n, BigInteger p) {
        if (legendereSymbolModPrime(n, p) != 1) {
            return null;
        }
        BigInteger S = null;
        BigInteger Q = p.subtract(BigInteger.ONE);
        int exp = 1;
        while (Q.mod(new BigInteger("2")).compareTo(BigInteger.ZERO) == 0) {
            Q = Q.divide(new BigInteger("2"));
            exp++;
        } exp--;
        S = new BigInteger(Integer.toString(exp));
        if (S.compareTo(BigInteger.ONE) == 0) {
            return n.modPow((p.add(BigInteger.ONE)).divide(new BigInteger("4")), p);
        }
        BigInteger z = null;
        //Finding z such that Legender's symbol: (z / p) = -1
        for (int zInt = 2; zInt < 1000000000; zInt++) {
            z = new BigInteger(Integer.toString(zInt));
            if (legendereSymbolModPrime(z, p) == -1) {
                break;
            }
        }
        BigInteger c = z.modPow(Q, p);
        BigInteger R = n.modPow((Q.add(BigInteger.ONE)).divide(new BigInteger("2")), p);
        BigInteger t = n.modPow(Q, p);
        BigInteger M = new BigInteger(S.toString());
        
        while (t.compareTo(BigInteger.ONE) != 0) {
            int i = 1;
            for (i = 1; i <= 1000000000; i++) {
                if (t.modPow(new BigInteger("2").pow(i), p).compareTo(BigInteger.ONE) == 0) {
                    break;
                }
            }
            BigInteger b = c.modPow(new BigInteger("2").pow(M.intValue() - i - 1), p);
            R = R.multiply(b).mod(p);
            t = t.multiply(b.pow(2)).mod(p);
            c = b.modPow(new BigInteger("2"), p);
            M = new BigInteger(Integer.toString(i));
        }
        return R;
    }
    
    /**
     * Find factors using Quadratic Sieve method
     * @return Factors that algorithm has found
     */
    @Override
    public BigInteger[] commitMethod() {
        BigInteger[] factors = this.quadraticSieveMethod();
        int count = 0;
        while(factors == null) {
            this.xInterval = this.xInterval.multiply(new BigInteger("2"));
            if(++count % 10 == 0) {
                this.factorBaseSize *= 2;
            }
            factors = this.quadraticSieveMethod();
        }
        return factors;
    }
    
    /*private BigInteger[] findFactorBaseQS() {
        Random randomGenerator = new SecureRandom();

        //Prime factors numerosity:
        Map<BigInteger, Integer> numerosity = new HashMap<>();
                
        BigInteger sqrtN = Factorization.bigIntegerSqrt(number); 
        BigInteger x, x2;
        
        for(int i = 0; i < randomSetSize; i++) { 
            //Generate odd x (pseudorandom x^e mod n for random x, res. is in interval sqrt(n) ... n)
            x = new BigInteger(number.bitCount(), randomGenerator);
            for(int e = 3; e < 100; e++) {
                x = x.modPow(new BigInteger(Integer.toString(e)), number);
                if(x.compareTo(sqrtN) > 0) {
                    break;
                }
            }
            if(x.compareTo(BigInteger.ONE) == 0) {
                i--; continue;
            }
            if(x.mod(new BigInteger("2")).compareTo(BigInteger.ZERO) == 0) 
                x = x.add(BigInteger.ONE);
            //------------------
            
            //Find x^2 mod n:
            x2 = x.modPow(new BigInteger("2"), number);
            
            //Find factors of x^2 mod n (partial brute force with fixed factor base):
            Factorization xf = new Factorization(x2);
            List<PrimeToExp> factors = xf.bruteForcePartial();
            if(factors == null) { i --; continue; }
            
            for(PrimeToExp factorExp : factors) {
                BigInteger factor = factorExp.getP();
                if(numerosity.containsKey(factor)) {
                    int currentCount = numerosity.get(factor);
                    currentCount++;
                    numerosity.put(factor, currentCount);
                }
                else {
                    numerosity.put(factor, 1);
                }
            }
        }

        //Factor base (FB):
        int factorBaseSize = Integer.min(maximalFBsize, numerosity.size());
        BigInteger factorBase [] = new BigInteger[factorBaseSize];
        numerosity = sortByValue(numerosity);
        int currentFBsize = 0;
        for(Map.Entry<BigInteger, Integer> set : numerosity.entrySet()) {
            //Factor base primes has to be odd:
            //if(set.getKey().compareTo(new BigInteger("2")) == 0) { continue; }
            //Quadratic residue problem:
            if( legendereSymbolModPrime(number, set.getKey()) != 1 ) { continue; }
            
            if(currentFBsize < factorBaseSize) {
                factorBase[currentFBsize] = set.getKey();
                currentFBsize++;
            } else break;
        }
        return factorBase;
    }*/
    
    
    
    /*
    public BigInteger quadraticPolynomial(BigInteger x, BigInteger sqrtN) {
        return (x.add(sqrtN));
    }
    public BigInteger[] quadraticSieve() {
        int matrixOffset = 3; //Also minimal dimension of nullspace
        BigInteger [] factorBase = this.findFactorBaseQS();        
        int factorBaseSize = factorBase.length;
        
        int iM = 200;
        BigInteger M = new BigInteger(Integer.toString(iM)); //defines interval for integer x in [0, M]
        
        
        BigInteger sqrtN = Factorization.bigIntegerSqrt(number); 
        if(sqrtN.pow(2).compareTo(number) > 0) {
            sqrtN = sqrtN.subtract(BigInteger.ONE);
        }
        
        
        List<FermatCongruence> valuesOfCongruence = new ArrayList<>();
        MatrixGF2 matrix = new MatrixGF2(factorBase.length + matrixOffset, factorBase.length);
        
        int rowIndex = 0;
        
        for(int i = 0; i < factorBase.length; i++) {
            if(factorBase[i].compareTo(new BigInteger("2")) == 0 ) continue;
            BigInteger s = TonelliShanksAlgorithm(number.mod(factorBase[i]), factorBase[i]);
            
            if(s != null) {
                BigInteger x = s.add(sqrtN);
                for(int j = 0; j < iM; j++) {
                    BigInteger x1 = x.add(factorBase[i].multiply(new BigInteger(Integer.toString(j))));
                    BigInteger s1 = quadraticPolynomial(x1, sqrtN);
                    BigInteger s2 = s1.modPow(new BigInteger("2"), number);
                    
                    Factorization factorK = new Factorization(s2);
                    List<PrimeToExp> factors = factorK.bruteForcePartial();
                    if(factors == null) continue;
                    
                    FermatCongruence fc = new FermatCongruence(s1, s2, factors);
                    boolean duplicite = false;
                    for(FermatCongruence test : valuesOfCongruence) {
                        if(test.equalsTo(fc)) {
                            duplicite = true;
                        }
                    }
                    byte matrixRow [] = fc.wrapToFactorBase(factorBase);

                    if(matrixRow == null) {
                        continue;
                    }
                    else if(duplicite) {
                        continue;
                    }
                    else {
                        valuesOfCongruence.add(fc);
                        //System.out.println("a: " + fc.toString(factorBase));
                        matrix.insertRow(matrixRow, rowIndex++);
                        if(valuesOfCongruence.size() >= maximalFBsize + matrixOffset) {break;}
                    }
                }
            }
            if(valuesOfCongruence.size() >= maximalFBsize + matrixOffset) {break;}
        }
        
        matrix = matrix.transpose();
        
        MatrixGF2 nullsp = matrix.getNullspace();
        if(nullsp == null) { return null; }
        
        for(int nspI = 0; nspI < nullsp.getColsCount(); nspI ++) {
            //FERMAT CONGRUENCE
            BigInteger xFC = new BigInteger("1"); //x of left side of congruence x^2 = y^2 (mod n)
            BigInteger yFC = new BigInteger("1"); //y of left side of congruence x^2 = y^2 (mod n)

            int fbExponents [] = new int[factorBaseSize];
            for ( int i = 0; i < valuesOfCongruence.size(); i++) {
                if((int)nullsp.getElement(i, nspI) > 0) {
                    for(int j = 0; j < factorBaseSize; j++) {
                        int exp = valuesOfCongruence.get(i).getExponentOfFactor(factorBase[j]);
                        //System.out.println(factorBase[j]+"/"+Integer.toString(exp)+" -- " +valuesOfCongruence.get(i).toString());
                        fbExponents[j] += exp;
                    }
                    xFC = xFC.multiply(valuesOfCongruence.get(i).getX());
                }
            }
            for(int i = 0; i < factorBaseSize; i++) {
                yFC = yFC.multiply(factorBase[i].pow(fbExponents[i] / 2));
            }
            if( (xFC.subtract(yFC)).gcd(number).compareTo(number) != 0 && (xFC.subtract(yFC)).gcd(number).compareTo(BigInteger.ZERO) != 0 ) {
                return new BigInteger[] { (xFC.subtract(yFC)).gcd(number) , (xFC.add(yFC)).gcd(number) };
            }
        }
        
        return null;
    }*/

    /*
    private BigInteger[] quadraticSieveMethod() {
        Random randomGenerator = new SecureRandom();
        //Exponents of x2 over FB
        Integer[][] exponentsOverFB = new Integer[factorBaseSize + parityMatrixOffset][factorBaseSize]; 
        //Represents matrix of parity bits
        MatrixGF2 parityMatrix = new MatrixGF2(factorBaseSize + parityMatrixOffset, factorBaseSize);
        //List of all finded x values
        BigInteger [] xList = new BigInteger[factorBaseSize + parityMatrixOffset];
        //Index of array and counter of successful matches
        int index = 0;
        for(BigInteger prime : this.factorBaseQS) {
            BigInteger s1 = tonelliShanksAlgorithm(this.getN().mod(prime), prime);
            if(s1 != null) {
                BigInteger s2 = prime.subtract(s1).abs();
                BigInteger x1 = s1.multiply(BigInteger.ONE);
                BigInteger x2 = s2.multiply(BigInteger.ONE);
                for(BigInteger k = BigInteger.ONE; k.compareTo(kInterval) < 1 && x1.compareTo(xInterval) < 1 && x2.compareTo(xInterval) < 1; k = k.add(BigInteger.ONE)) {
                    x1 = s1.multiply(k.multiply(prime));
                    x2 = s2.multiply(k.multiply(prime));
                    BigInteger Q1 = quadraticSievePolynomial(x1).modPow(Factorization.BIGINTEGER_TWO, this.getN());
                    Integer[] exponentsX1 = Factorization.getFactorBaseCoeficients(Q1, this.factorBaseDixon);
                    if(exponentsX1 != null) {
                        //Check if x is not allready in list
                        boolean dupliciteXExists = false;
                        for(int i = 0; i < index; i++) { 
                            if(Q1.compareTo(xList[i]) == 0) 
                            { dupliciteXExists = true; break; } 
                        } 
                        if(dupliciteXExists) {
                            exponentsOverFB[index] = null; 
                            continue;
                        }
                        //----------------------------------
                        exponentsOverFB[index] = exponentsX1;
                        xList[index] = quadraticSievePolynomial(x1);
                        parityMatrix.insertRow(exponentsX1, index);
                        index++;
                    }
                    if(index >= factorBaseSize + parityMatrixOffset) break;

                    BigInteger Q2 = quadraticSievePolynomial(x2).modPow(Factorization.BIGINTEGER_TWO, this.getN());
                    Integer[] exponentsX2 = Factorization.getFactorBaseCoeficients(Q2, this.factorBaseDixon);
                    if(exponentsX2 != null) {
                        //Check if x is not allready in list
                        boolean dupliciteXExists = false;
                        for(int i = 0; i < index; i++) { 
                            if(Q2.compareTo(xList[i]) == 0) 
                            { dupliciteXExists = true; break; } 
                        } 
                        if(dupliciteXExists) {
                            exponentsOverFB[index] = null; 
                            continue;
                        }
                        //----------------------------------
                        exponentsOverFB[index] = exponentsX2;
                        xList[index] = quadraticSievePolynomial(x2);
                        parityMatrix.insertRow(exponentsX2, index);
                        index++;
                    }
                    if(index >= factorBaseSize + parityMatrixOffset) break;
                }
            }
            if(index >= factorBaseSize + parityMatrixOffset) break;
        } 
        
        while (index < (factorBaseSize + parityMatrixOffset)) {
            //Generating random x in range (sqrtN, N)
            BigInteger x = new BigInteger(this.getN().bitCount()*2, randomGenerator).mod(xInterval);
            BigInteger Qx = this.quadraticSievePolynomial(x);
            BigInteger Qx2 = Qx.modPow(Factorization.BIGINTEGER_TWO, this.getN());
            
            exponentsOverFB[index] = Factorization.getFactorBaseCoeficients(Qx2, this.factorBaseDixon);
            if(exponentsOverFB[index] == null) continue; // In case that x2 is not smooth over x2
            //Check if x is not allready in list
            boolean dupliciteXExists = false;
            
            for(int i = 0; i < index; i++) { 
                if(Qx.compareTo(xList[i]) == 0) 
                { dupliciteXExists = true; break; } 
            } 
            if(dupliciteXExists) {
                exponentsOverFB[index] = null; 
                continue;
            }
            
            //Insert parity of x^2 factorization over FB to parity matrix
            parityMatrix.insertRow(exponentsOverFB[index], index);
            //Save the x value
            xList[index] = x;
            
            index++;
        }
        
        
        if(index != factorBaseSize + parityMatrixOffset) return null;
        //Find null space of (transposed) parity matrix 
        MatrixGF2 nullspace = parityMatrix.transpose().getNullspace();
        
        for(int col = 0; col < nullspace.getColsCount(); col++) {
            BigInteger x = new BigInteger("1");
            BigInteger y = new BigInteger("1");
            int [] yFactorization = new int[factorBaseSize];
            for(int row = 0; row < nullspace.getRowsCount(); row++) {
                //This say whether vector is relevant (1) or not (0)
                if(nullspace.getElement(row, col) == 1) {
                    x = x.multiply(xList[row]);
                    Integer [] yVecFactors = exponentsOverFB[row];
                    for(int i = 0; i < factorBaseSize; i++) { yFactorization[i] += yVecFactors[i]; }
                }
            }
            //Find y value
            for(int i = 0; i < factorBaseSize; i++) {
                y = y.multiply(this.factorBaseDixon[i].pow(yFactorization[i] / 2));
            }
            if(this.getN().gcd(x.add(y)).compareTo(BigInteger.ONE) != 0 
                    && this.getN().gcd(x.add(y)).compareTo(this.getN()) != 0 ) {
                return new BigInteger[]{ this.getN().gcd(x.add(y)), this.getN().gcd(x.subtract(y)) };
            }
        }
        
        return null;
    }*/
    
}
