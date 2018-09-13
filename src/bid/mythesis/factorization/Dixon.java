package bid.mythesis.factorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Implementation of Dixon factorization method
 * @author David Salac
 */
public class Dixon extends Factorization {
    //Size of factor base (FB) = number of primes in FB
    private int factorBaseSize;
    //Minimal dimension of null space of parity matrix
    private int parityMatrixOffset;
    
    /**
     * Create instance with values FB size = 10 + n.bitCount() and parity matrix offset = 3
     * @param n Integer to be factorized
     */
    public Dixon(BigInteger n) {
        super(n);
        this.factorBaseSize = 10 + 
                (n.toString().length() * n.toString().length() * n.toString().length() * n.toString().length())
                / 192; //Factor base size is empirically set to |n_10|^4/192
        this.parityMatrixOffset = 5; //Minimal null space (Kernel) dimension
    }
    
    /**
     * Find factor base consists of first k (k=factorBaseSize) prime numbers
     * @return Factor base of size factorBaseSize
     */
    private BigInteger[] getSimpleFactorBase() {
        BigInteger [] fb = new BigInteger[this.factorBaseSize];
        int index = 0;
        for(BigInteger pr = new BigInteger("2"); index < fb.length; pr = pr.add(BigInteger.ONE)) {
            if(pr.isProbablePrime(100)) {
                fb[index] = pr;
                index ++;
            }
        }
        return fb;
    }
    
    /**
     * Implementation of Dixon's factorization method
     * @return Two factors of n or null if does not find anything
     */
    private BigInteger[] dixonMethod() {
        //Factor base:
        BigInteger [] fb = this.getSimpleFactorBase();
        //Square root of n (later lower bound for x)
        BigInteger sqrtN = Factorization.bigIntegerSqrt(this.getN());
        
        Random randomGenerator = new SecureRandom();
        //Exponents of x2 over FB
        Integer[][] exponentsOverFB = new Integer[factorBaseSize + parityMatrixOffset][factorBaseSize];
        //Represents matrix of parity bits
        MatrixGF2 parityMatrix = new MatrixGF2(factorBaseSize + parityMatrixOffset, factorBaseSize);
        //List of all finded x values
        BigInteger [] xList = new BigInteger[factorBaseSize + parityMatrixOffset];
        //Map of x:
        Set<BigInteger> xOverview = new HashSet<>();
        //Index of array and counter of successful matches
        int index = 0;
        //Check maximal number of iterations
        long iteration = 0;
        
        final BigInteger randomUpperBound = this.getN().subtract(sqrtN);
        
        while (index < (factorBaseSize + parityMatrixOffset)) {
            //Checking if it is not infinity loop
            if(++iteration > this.maxIterations) return null;
            //Generating random x in range (sqrtN, N)
            BigInteger x = new BigInteger(this.getN().bitLength()*2, randomGenerator).mod(randomUpperBound);
            x = x.add(sqrtN); if(x.mod(Factorization.BIGINTEGER_TWO).compareTo(BigInteger.ZERO) == 0) x = x.add(BigInteger.ONE);
            
            //Compute x^2 value and factorization of x2 over FB
            BigInteger x2 = x.modPow(Factorization.BIGINTEGER_TWO, this.getN());
            exponentsOverFB[index] = Factorization.getFactorBaseCoeficients(x2, fb);
            if(exponentsOverFB[index] == null) continue; // In case that x2 is not smooth over x2
            //Check if x is not allready in list 
            if(xOverview.contains(x)) {
                exponentsOverFB[index] = null; 
                continue;
            }
            
            //Insert parity of x^2 factorization over FB to parity matrix
            parityMatrix.insertRow(exponentsOverFB[index], index);
            //Save the x value
            xList[index] = x;
            xOverview.add(x);
            
            index++;
        }
        
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
                y = y.multiply(fb[i].pow(yFactorization[i] / 2));
            }
            if(this.getN().gcd(x.add(y)).compareTo(BigInteger.ONE) != 0 
                    && this.getN().gcd(x.add(y)).compareTo(this.getN()) != 0 ) {
                return new BigInteger[]{ this.getN().gcd(x.add(y)), this.getN().gcd(x.subtract(y)) };
            }
        }
        
        return null;
    }
    
    /**
     * Find factors using Dixon's method in iteration
     * @return Factors that algorithm has found
     */
    @Override
    public BigInteger[] commitMethod() {
        BigInteger[] factors = this.dixonMethod();
        int count = 0;
        while(factors == null) { 
            if(++count % 10 == 0) {
                this.factorBaseSize *= 2;
            }
            factors = this.dixonMethod();
        }
        return factors;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //Following commented part is based on different Factor base searching algorithm
    /* Data collection phase of Dixon's algorithm: */
    /*private BigInteger[] findFactorBaseDixon() {
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
            if(currentFBsize < factorBaseSize) {
                factorBase[currentFBsize] = set.getKey();
                currentFBsize++;
            } else break;
        }
        return factorBase;
    }*/
    
    /* Data processing phase of Dixon's algorithm: */
    /*public BigInteger[] dixon() {
        int matrixOffset = 3; //Also minimal dimension of nullspace
        BigInteger sqrtN = Factorization.bigIntegerSqrt(this.getN()); 
        
        Random randomGenerator = new SecureRandom();
        BigInteger [] factorBase = this.getSimpleFactorBase();
        if(factorBase == null) { return null; }
        int factorBaseSize = factorBase.length;
                
        //Save x, x^2, factors of x^2 (mod n):
        List<FermatCongruence> valuesOfCongruence = new ArrayList<>();
        
        MatrixGF2 matrix = new MatrixGF2(factorBase.length + matrixOffset, factorBaseSize);
                
        BigInteger x, x2;
        //Finding x, x^2 pairs on factor base
        for (int i = 0; i < factorBaseSize + matrixOffset; i++) {
            //Generate odd x (pseudorandom x^e mod n for random x, res. is in interval sqrt(n) ... n)
            x = new BigInteger(this.getN().bitCount(), randomGenerator);
            for(int e = 3; e < 100; e++) {
                x = x.modPow(new BigInteger(Integer.toString(e)), this.getN());
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
            
            FermatCongruence fc = new FermatCongruence(x, x2, factors);
            boolean duplicite = false;
            for(FermatCongruence test : valuesOfCongruence) {
                if(test.equalsTo(fc)) {
                    duplicite = true;
                }
            }
            byte matrixRow [] = fc.wrapToFactorBase(factorBase);
            
            if(matrixRow == null) {
                i--;
                continue;
            }
            else if(duplicite) {
                i--;
                continue;
            }
            else {
                valuesOfCongruence.add(fc);
                //System.out.println(fc.toString(factorBase));
                matrix.insertRow(matrixRow, i);
            }
        }
        
        //Primes exponent parity = row, number (vector) = column => TRANSPOSE
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
    
}
