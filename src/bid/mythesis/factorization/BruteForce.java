package bid.mythesis.factorization;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Brute force factorization method
 * @author David Salac
 */
public class BruteForce extends Factorization {
    private final long upperBound;
    
    /**
     * Create instance of class BruteForce
     * @param n Number to be factorized
     * @param upperBound Method check all numbers less than this bound
     */
    public BruteForce(BigInteger n, long upperBound) {
        super(n);
        this.upperBound = upperBound;
    }

    /**
     *
     * @param n
     */
    public BruteForce(BigInteger n) {
        this(n, 1000000000L);
    }
    
    /**
     * Implementation of simple brute force method for integer factorization with exponents of each prime
     * @return Prime factors of number with exponents or null if does not find anything
     */
    public PrimeToExp [] bruteForceFactorizationIncExp() {
        List<PrimeToExp> factorList = new ArrayList<>();
        
        BigInteger n = new BigInteger(this.getN().toString());
        long boundCheck = 0;
        
        for(BigInteger p = new BigInteger("2"); p.compareTo(n) <= 0 && n.compareTo(BigInteger.ONE) >= 0 && boundCheck < this.upperBound; p = p.add(BigInteger.ONE) ) {
            if(n.mod(p).compareTo(BigInteger.ZERO) == 0) {
                int exp = 1;
                for(exp = 1; exp < 4096; exp++) {
                    if(n.mod(p).compareTo(BigInteger.ZERO) == 0) {
                        n = n.divide(p);
                    }
                    else break;
                }
                factorList.add(new PrimeToExp(p, exp - 1));
                if(n.isProbablePrime(1)) {
                    factorList.add(new PrimeToExp(n, 1));
                    break;
                }
            }
            boundCheck++; 
        }
        if(factorList.isEmpty()) return null;
        PrimeToExp [] factors = new PrimeToExp[factorList.size()];
        for(int i = 0; i < factorList.size(); i++) {
            factors[i] = factorList.get(i);
        }
        return factors;
    }
    
    /**
     * Implementation of simple brute force method for integer factorization
     * @return Factors of number or null if does not find anything
     */
    public BigInteger [] bruteForceFactorization() {
        List<BigInteger> factorList = new ArrayList<>();
        
        BigInteger n = new BigInteger(this.getN().toString());
        long boundCheck = 0;
        
        for(BigInteger p = new BigInteger("2"); p.compareTo(n) <= 0 && n.compareTo(BigInteger.ONE) >= 0 && boundCheck < this.upperBound; p = p.add(BigInteger.ONE) ) {
            if(n.mod(p).compareTo(BigInteger.ZERO) == 0) {
                factorList.add(p);
                for(int exp = 1; exp < 4096; exp++) {
                    if(n.mod(p).compareTo(BigInteger.ZERO) == 0) {
                        n = n.divide(p);
                    }
                    else break;
                }
                if(n.isProbablePrime(1)) {
                    factorList.add(n);
                    break;
                }
            }
            boundCheck++; 
        }
        if(factorList.isEmpty()) return null;
        BigInteger [] factors = new BigInteger[factorList.size()];
        for(int i = 0; i < factorList.size(); i++) {
            factors[i] = factorList.get(i);
        }
        return factors;
    }

    /**
     * Find factors using Brute force method
     * @return Factors that algorithm has found
     */
    @Override
    public BigInteger[] commitMethod() {
        return this.bruteForceFactorization();
    }
    
}
