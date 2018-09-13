/**
 * Package for solving of discrete logarithm problem
 */
package bid.mythesis.logarithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Class for solving of discrete logarithm problem
 * @author David Salac
 */
public abstract class DiscreteLogarithm implements Runnable {

    /**
     * Generator of group
     */
    protected BigInteger g;

    /**
     * Right side of congruence g^x = a (mod n)
     */
    protected BigInteger a;

    /**
     * Modulus of congruence g^x = a (mod n)
     */
    protected BigInteger n;

    /**
     * Maximal size of map
     */
    protected int maxBigIntegerMapSize;
    
    /**
     * Create instance of class
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n prime modulus of congruence g^x = a (mod n)
     * @param maxBigIntegerMapSize Algorithm using Map of BigInteger, because of memory limits specify maximum size
     */
    protected DiscreteLogarithm(BigInteger g, BigInteger a, BigInteger n, int maxBigIntegerMapSize) {
        this.g = new BigInteger(g.toString());
        this.a = new BigInteger(a.toString());
        this.n = new BigInteger(n.toString());
        this.maxBigIntegerMapSize = maxBigIntegerMapSize;
    }

    /**
     * Create instance of class with default value of maximal map size
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n prime modulus of congruence g^x = a (mod n)
     */
    protected DiscreteLogarithm(BigInteger g, BigInteger a, BigInteger n) {
        this(g, a, n, 10000000);
    }
    
    /**
     * Newton method for finding of square root of n
     * @param n Integer to extract
     * @param poly Polynomial coefficient
     * @param precision Precision of computation
     * @return 
     */
    private static BigDecimal computeBigIntegerSqrt(BigDecimal n, BigDecimal poly, BigDecimal precision) {
        BigDecimal digits = new BigDecimal(150);
        BigDecimal funcMult = poly.multiply(new BigDecimal(2));
        BigDecimal func = poly.pow(2).add(n.negate());
        BigDecimal polynomial = func.divide(funcMult, 2 * digits.intValue(), RoundingMode.HALF_DOWN);
        polynomial = poly.add(polynomial.negate());
        BigDecimal sqrt = polynomial.pow(2);
        BigDecimal sqrtPrec = sqrt.subtract(n);
        sqrtPrec = sqrtPrec.abs();
        if (sqrtPrec.compareTo(precision) <= -1) {
            return polynomial;
        }
        return computeBigIntegerSqrt(n, polynomial, precision);
    }
    
    /**
     * Method for computation of square root of n
     * @param n Integer to extract
     * @return Square root of n
     */
    protected static BigInteger bigIntegerSqrt(BigInteger n){
        BigDecimal digits = new BigDecimal(150);
        BigDecimal prepare = new BigDecimal(10).pow(digits.intValue());
        return new BigInteger(computeBigIntegerSqrt(new BigDecimal(n.toString()),new BigDecimal(1),new BigDecimal(1).divide(prepare)).setScale(0, RoundingMode.CEILING).toString());
    }
    
    /**
     * Find largest factor of factorization n in some special cases
     * @param n Number to be factorized
     * @return Bit size of largest factor of n
     */
    public static int largestFactorSize(BigInteger n) {
        int size = 0;
        BigInteger temp = new BigInteger(n.toString());
        for(BigInteger i = new BigInteger("2"); i.compareTo(new BigInteger("1000000")) < 0; i = i.add(BigInteger.ONE) ) {
            if(temp.mod(i).equals(BigInteger.ZERO)) {
                for(int exp = 2; exp < 4096; exp++) {
                    if( ! temp.mod(i.pow(exp)).equals(BigInteger.ZERO)) {
                        exp -= 1; 
                        temp = temp.divide(i.pow(exp));
                        size = i.bitLength();
                        
                        if(temp.isProbablePrime(100)) { 
                            if(temp.bitLength() > size) {
                                return temp.bitLength();
                            }
                            return size;
                        }
                        
                        break;
                    }
                }
            }
        }
        return n.bitLength();
    }
    
    /**
     * Create suitable instance of class DiscreteLogarithm
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n modulus of congruence g^x = a (mod n)
     * @return Instance of class DiscreteLogarithm with implemented proper method
     */
    public static DiscreteLogarithm initInstance(BigInteger g, BigInteger a, BigInteger  n) {
        int nBitCount = n.bitLength(); //For further purposes
        DiscreteLogarithm method = new SilverPohligHellman(g, a, n);
        if(nBitCount < 20) {
            method = new BruteForce(g, a, n);
        } else if(nBitCount < 35) {
            method = new BabyStepGiantStep(g, a, n);
        }/* else {
            int largestFactorBitSize = largestFactorSize(n);
            if(largestFactorBitSize > 26) {
                method = new BabyStepGiantStep(g, a, n);
            }
        }*/
        return method;
    }
    
    /**
     * Method for solving discrete logarithm problem
     * @return Proper x of congruence g^x = a (mod n) or null if does not succeed
     */
    public abstract BigInteger commitMethod();
    
    /**
     * Proceed computation and show the result
     */
    @Override
    public void run() {
        BigInteger solution = this.commitMethod();
        /* if sqrt(n) > maxBigIntegerMapSize */
        if(bigIntegerSqrt(n).compareTo(new BigInteger(Integer.toString(maxBigIntegerMapSize))) > 0 && solution == null) {
            while(solution == null && !Thread.currentThread().isInterrupted()) {
                solution = this.commitMethod();
            }
        } else {
            solution = this.commitMethod();
        }
        if(solution != null) {
            System.out.println(solution.toString());
        }
    }
}
