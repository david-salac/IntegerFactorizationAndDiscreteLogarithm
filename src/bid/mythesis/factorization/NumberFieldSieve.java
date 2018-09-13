package bid.mythesis.factorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Partial implementation of GNFS algorithm
 * @author David Salac
 */
public class NumberFieldSieve extends Factorization {
    //Size of factor base (FB) = this.getN() of primes in FB
    private int factorBaseSize;
    //Minimal dimension of null space of parity matrix
    private int parityMatrixOffset;
    //Maximal limit for a and b in sieving process
    BigInteger aAndBinterval;
    
    /**
     * Initialize problem
     * @param n integer that will be factorized
     */
    public NumberFieldSieve(BigInteger n) {
        super(n);
        
        this.factorBaseSize = 10 + 
                (n.toString().length() * n.toString().length() * n.toString().length() * n.toString().length())
                / 1024; //Factor base size is empirically set to |n_10|^4/1024
        this.parityMatrixOffset = 3; //Minimal null space (Kernel) dimension
        this.aAndBinterval = new BigInteger("1024");
    }
    
    /**
     * Find factor base consists of first k (k=size) prime numbers
     * @param size Length of returned factor base
     * @return Factor base of size expressed in argument
     */
    public BigInteger[] generateGNFSRFB(int size) {
        BigInteger [] fb = new BigInteger[size];
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
     * Generate a list of first k primes (k = size)
     * @param size Size of list
     * @return List of k primes (k=size)
     */
    public int [] generatePrimeList(int size) {
        int[] primes = new int[size];
        BigInteger [] primeList = this.generateGNFSRFB(size);
        for(int i = 0; i < size; i++) {
            primes[i] = primeList[i].intValue();
        }
        return primes;
    }
    /**
     * Generate prime ideals of form ri + pi*phi
     * @param polynomial Polynomial used in algorithm
     * @return Algebraic factor base
     */
    public Pair<Pair<BigInteger[], BigInteger[]>, IdealSet[]> generateGNFSIdealFactorBase(BigInteger[] polynomial) {
        List<IdealSet> primeIdealSet = new ArrayList<>();
        int j = 0;
        int index = 0;
        int [] primeList = generatePrimeList(factorBaseSize*3);
        List<BigInteger> pList = new ArrayList<>();
        List<BigInteger> rList = new ArrayList<>();
        for(int i = 0; i < factorBaseSize; i++) {
            BigInteger np = new BigInteger(Integer.toString(primeList[j]));
            boolean added = false;
            for(int r = 1; r < primeList[j]; r++) {
                if(polyVal(new BigInteger(Integer.toString(r)), polynomial, np).compareTo(BigInteger.ZERO) == 0 ) {
                    if(!added) {
                        primeIdealSet.add(new IdealSet(np));
                        index ++;
                        added = true;
                    }
                    primeIdealSet.get(index - 1).InsertR(new BigInteger(Integer.toString(r)));
                    pList.add(np);
                    rList.add(new BigInteger(Integer.toString(r)));
                }
            }
            if(!added) { i--; }
            j++;
            if(index == factorBaseSize) break;
        }
        IdealSet [] ideals = new IdealSet[primeIdealSet.size()];
        for(int i = 0; i < primeIdealSet.size(); i++) {
            ideals[i] = primeIdealSet.get(i);
        }
        BigInteger[] p = new BigInteger[pList.size()];
        BigInteger[] r = new BigInteger[pList.size()];
        for(int i = 0; i < pList.size(); i++) {
            p[i] = pList.get(i);
            r[i] = rList.get(i);
        }
        Pair<BigInteger[], BigInteger[]> pairOfPAndR = new Pair<BigInteger[], BigInteger[]>(p,r);
        return new Pair<>(pairOfPAndR, ideals);
    }
    
    /**
     * Represents one iteration of bisection method for finding k root of n
     * @param n Base of root
     * @param k Degree of root
     * @param pivot Actual position
     * @param pivotUp Upper bound
     * @param pivotDown Lower bound
     * @return k-root of n
     */
    public static BigInteger kRootOfNByBisectionMethod(BigInteger n, int k, BigInteger pivot, BigInteger pivotUp, BigInteger pivotDown) {
        //Finding pivot^k
        BigInteger find = pivot.pow(k);
        //---------------
        if(pivotUp.subtract(pivotDown).compareTo(BigInteger.ONE) <= 0)
            return pivot;
        if( find.compareTo(n) > 0 ) {
            BigInteger newPivot = (pivot.add(pivotDown)).divide(new BigInteger("2"));
            return kRootOfNByBisectionMethod(n, k, newPivot,pivot, pivotDown);
        }
        else if(find.compareTo(n) < 0) {
            BigInteger newPivot = (pivot.add(pivotUp)).divide(new BigInteger("2"));
            return kRootOfNByBisectionMethod(n, k, newPivot, pivotUp, pivot);
        }
        else
            return pivot;
    }
    /**
     * Compute k root of n
     * @param n Base of root
     * @param k Degree of root
     * @return k root of n
     */
    public static BigInteger kRootOfN(BigInteger n, int k) {
        return kRootOfNByBisectionMethod(n, k, n.divide(new BigInteger("2")), n, BigInteger.ZERO);
    }
    /**
     * Generate polynomial f(x) with some m where f(m) = 0 mod n
     * @param d Degree of polynomial
     * @return polynomial with m value
     */
    public Pair<BigInteger, BigInteger []> baseMMethod(int d) {
        Random randomGenerator = new SecureRandom();
        BigInteger lowerBound = kRootOfN(this.getN(), d + 1);
        BigInteger upperBound = kRootOfN(this.getN(), d);
        //Generating of random m:
        BigInteger m = new BigInteger(this.getN().bitLength() + 1, randomGenerator);
        while(m.compareTo(upperBound) > 0 || m.compareTo(lowerBound) < 0) {
            m = m.modPow(new BigInteger(Integer.toString(randomGenerator.nextInt(3) + 3)), upperBound);
        }
        //-----------------------
        BigInteger r = new BigInteger(this.getN().toString());
        BigInteger [] coef = new BigInteger[d + 1];
        for(int i = d; i >= 0; i--) {
            BigInteger ai = r.divide(m.pow(i));
            r = r.mod(m.pow(i));
            coef[i] = ai;
        }
        return new Pair<>(m, coef);
    }
    
    /**
     * Norm of polynomial
     * @param polynomial Input polynomial
     * @param a Argument a of Norm(a + b*phi) = b^deg(f)*f(a/b)
     * @param b Argument b of Norm(a + b*phi) = b^deg(f)*f(a/b)
     * @return Norm of polynomial f(x) for input a and b
     */
    public BigInteger polyNorm(BigInteger [] polynomial, BigInteger a, BigInteger b) {
        BigInteger value = new BigInteger("0");
        for(int i = 0; i < polynomial.length; i++) {
            value = value.add( (polynomial[i].multiply(a.pow(i))).multiply(b.pow( polynomial.length - 1 - i )).mod(this.getN()) );
        }
        return value.mod(this.getN());
    }
    
    /**
     * Partial implementation of General Number Field Sieve method
     * @return Factors of n or null if does not succeed in searching.
     */
    public BigInteger [] GNFS() {
        //Offset of matrices (minimal dimension of nullspace)
        int factorBaseOffset = this.parityMatrixOffset;
        Pair<BigInteger, BigInteger[]> mAndPolynomial = baseMMethod(2); //Fixed degree: 3
        //Polynomial coefficients and m value: f(m) = 0 (mod n)
        BigInteger[] polynomialCoefficient = mAndPolynomial.getRight(); //a_1 x^n + a2 x^{n-1} + ... + a_{n-1}x + a_n
        BigInteger m = mAndPolynomial.getLeft();
        
        Pair<Pair<BigInteger[], BigInteger[]>, IdealSet[]> idealFactorBasePair = generateGNFSIdealFactorBase(polynomialCoefficient);
        IdealSet[] idealFactorBase = idealFactorBasePair.getRight();
        Pair<BigInteger[], BigInteger[]> pAndR = idealFactorBasePair.getLeft();
        BigInteger[] p = pAndR.getLeft();
        BigInteger[] r = pAndR.getRight();
        
        BigInteger [] idealSetOfP = new BigInteger[idealFactorBase.length];
        for(int i = 0; i < idealFactorBase.length; i++) {
            idealSetOfP[i] = idealFactorBase[i].getP();
        }
        
        //Define maximal interval for a and b
        BigInteger aInterval = this.aAndBinterval;
        BigInteger bInterval = this.aAndBinterval;
        
        //Parity for nullspace computation (we save both norm and g(x) value)
        int parityVectorCount = 2*(factorBaseOffset + p.length);
        MatrixGF2 parity = new MatrixGF2( parityVectorCount , p.length);
        
        MatrixGF2 parityIdeal = new MatrixGF2( factorBaseOffset + p.length , p.length);
        MatrixGF2 parityPrime = new MatrixGF2( factorBaseOffset + p.length , p.length);
        
        /*BigInteger [] aValues = new BigInteger[factorBaseOffset + p.length];
        BigInteger [] bValues = new BigInteger[factorBaseOffset + p.length];*/
        
        BigInteger [] rationalFactorBase = generateGNFSRFB(p.length);
        
        List<Integer[]> primeExp = new ArrayList<>();
        List<Integer[]> idealExp = new ArrayList<>();
        
        int saveIndex = 0; int temp = 0;
        
        for(BigInteger a = aInterval.multiply(new BigInteger("0")); a.compareTo(aInterval) <= 0; a = a.add(BigInteger.ONE)) {
            for(BigInteger b = new BigInteger("-1"); b.compareTo(bInterval) <= 0; b = b.add(BigInteger.ONE)) {
                //a and b has to be coprime:
                if(a.gcd(b).compareTo(BigInteger.ONE) != 0) continue;
                
                Integer[] normExponents = getFactorBaseCoeficients(polyNorm(polynomialCoefficient, a,b), idealSetOfP);
                
                if(normExponents == null) continue;
                
                List<BigInteger> piSet = new ArrayList<>();
                List<BigInteger> riSet = new ArrayList<>();
                //For check that pi | a+b*rj
                boolean checkIdealSmoothness = true;
                for(int pIndex = 0; (pIndex < normExponents.length) && checkIdealSmoothness; pIndex++) {
                    if( (normExponents[pIndex] != 0)) {
                        //We also needs to know exact value of exponents of ideal
                        int rIncidence = 0;
                        for (BigInteger rj : idealFactorBase[pIndex].getR()) {
                            if (a.add(b.multiply(rj)).mod(idealFactorBase[pIndex].getP()).compareTo(BigInteger.ZERO) == 0) {
                                riSet.add(rj);
                                piSet.add(idealFactorBase[pIndex].getP());
                                rIncidence++;
                            }
                        }
                        if(rIncidence != normExponents[pIndex]) { checkIdealSmoothness = false; }
                    }
                }
                if(riSet.isEmpty() && !checkIdealSmoothness) continue;
                
                //For parity matrix (would be save):
                Integer[] idExp = new Integer[p.length];
                byte [] idealExponents = new byte[p.length];
                for(int i = 0; i < piSet.size(); i++) {
                    for(int j = 0; j < p.length; j++) {
                        idExp[j] = 0;
                        if(p[j].compareTo(piSet.get(i)) == 0 && r[j].compareTo(riSet.get(i)) == 0 ) {
                            idealExponents[j] = 1; idExp[j] = 1;
                        }
                    }
                }
                
                Integer fRFBExponents [] = getFactorBaseCoeficients(a.add(b.multiply(m)), rationalFactorBase);
                if(fRFBExponents == null) { continue; }
                
                /*aValues[saveIndex / 2] = a;
                bValues[saveIndex / 2] = b;*/
                
                idealExp.add(idExp);
                primeExp.add(fRFBExponents);
                
                parity.insertRow(fRFBExponents, saveIndex++);
                parity.insertRow(idealExponents, saveIndex++);
                
                parityPrime.insertRow(fRFBExponents, temp);
                parityIdeal.insertRow(idealExponents, temp++);
                
                if(saveIndex >= parityVectorCount) break;
            } 
            if(saveIndex >= parityVectorCount) break;
        }
        
        MatrixGF2 nullspace = (parityPrime.rightJoin(parityIdeal)).transpose().getNullspace();
        for(int col = 0; col < nullspace.getColsCount(); col ++) {
            int RFBexponents [] = new int[p.length];
            int IFBexponents [] = new int[p.length];
            for ( int i = 0; i < nullspace.getRowsCount(); i++) {
                if((int)nullspace.getElement(i, col) > 0) {
                    for(int j = 0; j < p.length; j++) {
                        int RFBexp = primeExp.get(i)[j];
                        int IFBexp = idealExp.get(i)[j];
                        //System.out.println(factorBase[j]+"/"+Integer.toString(exp)+" -- " +valuesOfCongruence.get(i).toString());
                        RFBexponents[j] += RFBexp;
                        IFBexponents[j] += IFBexp;
                    }
                }
            }
            BigInteger x = new BigInteger("1");
            BigInteger y = new BigInteger("1");
            for(int i = 0; i < p.length; i++) {
                RFBexponents[i] /= 2;
                x = x.multiply(rationalFactorBase[i].pow(RFBexponents[i]));
                IFBexponents[i] /= 2;
                y = y.multiply((r[i].add(p[i].multiply(m))).pow(IFBexponents[i]));
            }
            if( this.getN().gcd(x.add(y)).compareTo(BigInteger.ONE) != 0 && this.getN().gcd(x.subtract(y)).compareTo(this.getN()) != 0) {
                return new BigInteger[] { this.getN().gcd(x.add(y)), this.getN().gcd(x.subtract(y)) };
            }
        }
        
        return null;
    }
    
    /**
     * GNFS implemented in this class is only for showing how it does work
     * @return Does not return no useful result
     */
    @Override
    public BigInteger[] commitMethod() {
        throw new UnsupportedOperationException("This is only partial implementation of General Number Field Sieve method.");
    }
    
}
