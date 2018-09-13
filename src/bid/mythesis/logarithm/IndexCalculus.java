package bid.mythesis.logarithm;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Simple implementation of index calculus method
 * @author David Salac
 */
public class IndexCalculus extends DiscreteLogarithm {
    private final static BigInteger BIG_INTEGER_TWO = new BigInteger("2");
    private final static BigInteger BIG_INTEGER_MINUS_ONE = new BigInteger("-1");
    private final static int maximalNumberOfIteration = 1000000;
    private final int factorBaseSize;
    private final BigInteger a, p, g;
    private BigInteger[] factorBase;
    
    /**
     * Generate factor base of first "factorBaseSize" elements
     */
    public final void generateFactorBase() {
        factorBase = new BigInteger[factorBaseSize];
        int index = 0;
        BigInteger temp = new BigInteger("2");
        while(index < factorBaseSize) {
            if(temp.isProbablePrime(100)) {
                this.factorBase[index] = temp;
                index++;
            }
            temp = temp.add(BigInteger.ONE);
        } 
    }
    
    /**
     * Initialize instance
     * @param g Generator of group
     * @param a Right side of congruence g^x = a (mod p)
     * @param p Order of group
     */
    public IndexCalculus(BigInteger g, BigInteger a, BigInteger p) {
        super(g, a, p, 1000000);
        this.factorBaseSize = 190; //160
        this.generateFactorBase();
        this.g = g.multiply(BigInteger.ONE);
        this.a = a.multiply(BigInteger.ONE);
        this.p = p.multiply(BigInteger.ONE);
    }
    
    /**
     * Factorize number over factor base
     * @param nr Number to be factorized over FB
     * @return Factor over factor base or null
     */
    private BigInteger[] factorOverFB(BigInteger nr) {
        BigInteger [] coef = new BigInteger[factorBaseSize];
        BigInteger temp = nr.multiply(BigInteger.ONE);
        for(int i = 0; i < factorBaseSize; i ++) {
            coef[i] = new BigInteger("0");
            if(temp.mod(factorBase[i]).equals(BigInteger.ZERO)) {
                for(int exp = 1; exp < 4096; exp++) {
                    if( ! (temp.mod(factorBase[i].pow(exp))).equals(BigInteger.ZERO) ) {
                        exp = exp - 1;
                        coef[i] = new BigInteger(Integer.toString(exp));
                        temp = temp.divide(factorBase[i].pow(exp));
                        if(temp.equals(BigInteger.ONE)) {
                            for(int j = i+1; j < factorBaseSize; j++) {
                                coef[j] = new BigInteger("0");
                            }
                            return coef;
                        }
                        break;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * One iteration of index calculus
     * @return Return null if does not succeed or x in g^x = a (mod p)
     */
    private BigInteger indexCalculusIteration() {
        MatrixGFn matrix = new MatrixGFn(factorBaseSize, factorBaseSize + 1, p.subtract(BigInteger.ONE));
        Random randomGenerator = new SecureRandom();
        
        Set<BigInteger> eSet = new HashSet<>();
        int row = 0;
        while(row < factorBaseSize) {
            //Obtain random exponent in [0,p-2);
            BigInteger e = new BigInteger( p.bitLength(), randomGenerator).modPow(BIG_INTEGER_TWO.multiply(new BigInteger(5,randomGenerator)), p.subtract(BIG_INTEGER_TWO));
            if( ! eSet.contains(e)) {
                if(matrix.insertRowOverFactorBase(row, g.modPow(e, p), e, factorBase)) {
                    row++;
                    System.out.println("DEBUG: " + Integer.toString(row));
                    eSet.add(e);
                }
            }
        }
        
        BigInteger[] solution = null;
        try {
            solution = matrix.solve();
        } catch(Exception e) {
            return null;
        }
        
        int iteration = 0;
        while(iteration < maximalNumberOfIteration) {
            //Obtain random exponent in [0,p-2);
            BigInteger e = new BigInteger( p.bitLength(), randomGenerator).modPow(BIG_INTEGER_TWO, p.subtract(BIG_INTEGER_TWO));
            BigInteger gPowE = g.modPow(e, p);
            
            BigInteger[] factorsOverFB = factorOverFB((gPowE.multiply(a)).mod(p)); 
            
            if( factorsOverFB != null ) {
                BigInteger res = e.multiply(BIG_INTEGER_MINUS_ONE);
                for(int j = 0; j < factorBaseSize; j++) {
                    res = res.add( factorsOverFB[j].multiply(solution[j]) );
                }
                BigInteger result = (res.mod(p.subtract(BigInteger.ONE)));
                if(g.modPow(result, p).equals(a)) {
                    return result;
                }
            }
            iteration++;
        }
        return null;
    }
    /**
     * Encapsulate index calculus iteration
     * @return Return x in congruence g^x = a (mod p)
     */
    public BigInteger indexCalculusMethod() {
        BigInteger res = indexCalculusIteration();
        while(res == null) {
            res = indexCalculusIteration();
        }
        return res;
    }
    
    /**
     * Commit Index calculus method
     * @return Result of algorithm (x that solve congruence g^x mod n)
     */
    @Override
    public BigInteger commitMethod() { 
        return this.indexCalculusMethod();
    }
    
    
}
