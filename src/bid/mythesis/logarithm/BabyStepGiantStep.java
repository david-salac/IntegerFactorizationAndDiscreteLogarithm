package bid.mythesis.logarithm;

import static bid.mythesis.logarithm.DiscreteLogarithm.bigIntegerSqrt;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Implementation of Baby step Giant step method
 * @author David Salac
 */
public class BabyStepGiantStep extends DiscreteLogarithm {

    /**
     * Create instance of class
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n modulus of congruence g^x = a (mod n)
     * @param maxBigIntegerMapSize Algorithm using Map of BigInteger, because of memory limits specify maximum size
     */
    public BabyStepGiantStep(BigInteger g, BigInteger a, BigInteger n, int maxBigIntegerMapSize) {
        super(g, a, n, maxBigIntegerMapSize);
    }
    
    /**
     * Create instance of class with default value of maximal map size
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n modulus of congruence g^x = a (mod n)
     */
    public BabyStepGiantStep(BigInteger g, BigInteger a, BigInteger n) {
        this(g, a, n, 4000000);
    }
    
    /**
     * Implementation of Baby step / Giant step algorithm
     * @return Returns solution k of g^k = a (mod n) or null if does not succeed.
     */
    private BigInteger babyStepGiantStep() {
        BigInteger m = bigIntegerSqrt(n);
        Map<BigInteger,BigInteger> gjA = new HashMap<>();
        BigInteger maximumIterations = new BigInteger(Integer.toString(this.maxBigIntegerMapSize));
        
        if(m.compareTo(maximumIterations) <= 0) {
            for(BigInteger j = new BigInteger("0"); j.compareTo(m) < 0 && !Thread.currentThread().isInterrupted(); j = j.add(BigInteger.ONE)) {
                BigInteger tuple =  g.modPow( j , n);
                gjA.put(tuple, j);
            }
        } else {
            Random randomGenerator = new SecureRandom();
            for(long ik = 0; ik < maximumIterations.longValue() && !Thread.currentThread().isInterrupted(); ik++) {
                BigInteger j = new BigInteger(m.bitLength(), randomGenerator);
                BigInteger tuple =  g.modPow( j , n);
                gjA.put(tuple, j);
            }
        }
        
        BigInteger gInv = g.modInverse(n);
        BigInteger gInvPowerToM = gInv.modPow(m, n);
        BigInteger J = new BigInteger(a.toString());
        
        for(BigInteger i = new BigInteger("0"); i.compareTo(m) <= 0 && !Thread.currentThread().isInterrupted(); i = i.add(BigInteger.ONE)) {
            BigInteger j = gjA.get(J); 
            if(j != null) {
                BigInteger res = i.multiply(m);
                return new BigInteger(res.add(j).toString());
            }
            J = J.multiply(gInvPowerToM).mod(n);
        }
        return null;
    }

    /**
     * Commit Baby step / Giant step method
     * @return Result of algorithm (x that solve congruence g^x mod n)
     */
    @Override
    public BigInteger commitMethod() {
        BigInteger solution = babyStepGiantStep();
        
        //Probabilistic version of BS/GS algorithm
        if(bigIntegerSqrt(n).compareTo(new BigInteger(Integer.toString(maxBigIntegerMapSize))) >= 0) {
            while(solution == null && !Thread.currentThread().isInterrupted()) {
                solution = this.commitMethod();
            }
            return solution;
        }
        
        return solution;
    }
}
