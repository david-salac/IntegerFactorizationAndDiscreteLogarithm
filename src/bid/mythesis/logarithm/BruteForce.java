package bid.mythesis.logarithm;

import java.math.BigInteger;

/**
 * Implementation of brute force for solving of discrete logarithm
 * @author David Salac
 */
public class BruteForce extends DiscreteLogarithm {

    /**
     * Create instance of class
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n modulus of congruence g^x = a (mod n)
     */
    public BruteForce(BigInteger g, BigInteger a, BigInteger n) {
        super(g, a, n);
    }
    
    /**
     * Simple brute force method for solving of discrete logarithm problem
     * @return 
     */
    private BigInteger bruteForceMethod() {
        for(BigInteger x = BigInteger.ZERO; x.compareTo(n) <= 1 && !Thread.currentThread().isInterrupted(); x = x.add(BigInteger.ONE)) {
            if((g.modPow(x, n)).compareTo(a) == 0) 
                return x;
        }
        return null;
    }

    /**
     * Commit Brute force method
     * @return Result of algorithm (x that solve congruence g^x mod n)
     */
    @Override
    public BigInteger commitMethod() {
        return bruteForceMethod();
    }
}
