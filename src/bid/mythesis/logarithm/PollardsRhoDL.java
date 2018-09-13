package bid.mythesis.logarithm;

import java.math.BigInteger;

/**
 * Implementation of Pollard's rho algorithm for discrete logarithms
 * @author David Salac
 */
public class PollardsRhoDL extends DiscreteLogarithm {
    BigInteger phiN, a1, b1, A1, B1, x1, X1;
    private static final BigInteger BIGINTEGER_TWO = new BigInteger("2");
    private static final BigInteger BIGINTEGER_THREE = new BigInteger("3");
    
    /**
     * Create instance of class
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n prime modulus of congruence g^x = a (mod n)
     */
    public PollardsRhoDL(BigInteger g, BigInteger a, BigInteger n) {
        super(g, a, n);
        this.phiN = n.subtract(BigInteger.ONE);
        this.a1 = BigInteger.ZERO; this.b1 = BigInteger.ZERO; 
        this.A1 = a1.multiply(BigInteger.ONE); this.B1 = b1.multiply(BigInteger.ONE);
        this.x1 = BigInteger.ONE; this.X1 = x1.multiply(BigInteger.ONE);
    }
    /**
     * One iteration of algorithm (operates with x1, a1, b1)
     */
    private void pollardStepLittle() {
        int sel = x1.mod(BIGINTEGER_THREE).intValue();
        switch (sel) {
            case 0:
                x1 = x1.modPow(BIGINTEGER_TWO, n);
                a1 = a1.multiply(BIGINTEGER_TWO).mod(phiN);
                b1 = b1.multiply(BIGINTEGER_TWO).mod(phiN);
                break;
            case 1:
                x1 = x1.multiply(g).mod(n);
                a1 = a1.add(BigInteger.ONE).mod(phiN);
                break;
            case 2:
                x1 = x1.multiply(this.a).mod(n);
                b1 = b1.add(BigInteger.ONE).mod(phiN);
                break;
            default:
                break;
        }
    }
    /**
     * One iteration of algorithm (operates with X1, A1, B1)
     */
    private void pollardStepLarge() {
        int sel = X1.mod(BIGINTEGER_THREE).intValue();
        switch (sel) {
            case 0:
                X1 = X1.modPow(BIGINTEGER_TWO, n);
                A1 = A1.multiply(BIGINTEGER_TWO).mod(phiN);
                B1 = B1.multiply(BIGINTEGER_TWO).mod(phiN);
                break;
            case 1:
                X1 = X1.multiply(g).mod(n);
                A1 = A1.add(BigInteger.ONE).mod(phiN);
                break;
            case 2:
                X1 = X1.multiply(this.a).mod(n);
                B1 = B1.add(BigInteger.ONE).mod(phiN);
                break;
            default:
                break;
        }
    }

    /**
     * Method for solving discrete logarithm problem
     * @return Proper x of congruence g^x = a (mod n) or null if does not succeed
     */
    @Override
    public BigInteger commitMethod() {
        //Maximal iterations (algorithm could fall into infinity loop)
        long currentIteration = 0;
        long iterationLimit = 1000000000;
        
        //Algorithm loop:
        do {
            pollardStepLittle();
            pollardStepLarge();
            pollardStepLarge();
            currentIteration++;
        } while(x1.compareTo(X1) != 0 && iterationLimit >= currentIteration);
                
        if(currentIteration == iterationLimit) return null;
        
        BigInteger a1SubtractA1 = a1.subtract(A1);
        BigInteger b1SubtractB1 = B1.subtract(b1); 
        if(b1SubtractB1.compareTo(BigInteger.ZERO) == 0) return null;
        
        //Compute GCD for further purpose
        BigInteger gcd = a1SubtractA1.gcd(b1SubtractB1.gcd(phiN));
        a1SubtractA1 = a1SubtractA1.divide(gcd);
        b1SubtractB1 = b1SubtractB1.divide(gcd);
        phiN = phiN.divide(gcd);
        
        return a1SubtractA1.multiply(b1SubtractB1.modInverse(phiN)).mod(phiN);
    }
}