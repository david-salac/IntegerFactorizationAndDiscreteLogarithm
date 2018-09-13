package bid.mythesis.factorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Implements the Pollard's Rho method for integer factorization
 * @author David Salac
 */
public class PollardRho extends Factorization {
    private final static int [] POLLARD_PRIME = {2,3,5,7,11,13,17,19,23,29};
    
    /**
     * Create instance of class prepared for factorization
     * @param n Number to be factorized
     */
    public PollardRho(BigInteger n) {
        super(n);
    }
    
    /**
     * One iteration of Pollard's Rho
     * @param nr Number to be factorized
     * @return Returns results of one step of Pollard's rho algorithm
     */
    private BigInteger polardRhoEngine(BigInteger nr ) {
        BigInteger n = new BigInteger(nr.toString());
        Random randomGenerator = new SecureRandom();
        BigInteger x = new BigInteger(Integer.toString(randomGenerator.nextInt(33) + 2));
        BigInteger y = new BigInteger("2");
        BigInteger d = new BigInteger("1");
        
        long freeze = 0;
        while (d.compareTo(BigInteger.ONE) == 0 && freeze++ < maxIterations) {
            x = polyVal(x, new BigInteger[] { BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE } ,n);
            y = polyVal(polyVal(y, new BigInteger[] { BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE } ,n), new BigInteger[] { BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE }, n);
            d = n.gcd(x.subtract(y).abs());
        }
        if(d.compareTo(n) == 0) {
            return null;
        }
        return d;
    }
    /**
     * Standard Pollard's rho method for integer factorization
     * @return List of factors of number
     */
    private List<BigInteger> polardRho() {
        BigInteger n = new BigInteger(this.getN().toString());
        
        Set<BigInteger> prime = new HashSet<>();
        Queue<BigInteger> composite = new LinkedList<>();
        
        /* ---- BRUTE FORCE SECTION ---- */
        int exp = 1;
        //PolardRho works wrong for small factors, lets check them:

        for(int f : POLLARD_PRIME) {
            BigInteger fBI = new BigInteger(Integer.toString(f));
            if(fBI.isProbablePrime(100)) {
                for(exp = 1; exp < 4096; exp++) {
                    if(n.mod(fBI.pow(exp)) != BigInteger.ZERO) {
                        exp -= 1;
                        break;
                    }
                }
                if(exp > 0) {
                    n = n.divide(fBI.pow(exp));
                    prime.add(fBI);
                }
            }
            
        }
        if(n.isProbablePrime(100)) {
            prime.add(n);
            n = BigInteger.ONE;
        }
        if(n.compareTo(BigInteger.ONE) == 0) {
            List<BigInteger> factors = new ArrayList<>();
            factors.addAll(prime);
            return factors;
        }
        
        
        /* ---- POLARD RHO FORCE SECTION ---- */
        for(long freeze = 0; freeze < maxIterations; freeze ++) {
            BigInteger factor = this.polardRhoEngine(n);
            if(factor == null) continue;
            if(factor.isProbablePrime(100)) {
                prime.add(factor);
            } else {
                composite.add(factor);
            }
            
            //Finding max exponent of factor
            for(exp = 1; exp < 4096; exp++) {
                if(n.mod(factor.pow(exp)) != BigInteger.ZERO) {
                    exp -= 1;
                    break;
                }
            }
            
            n = n.divide(factor.pow(exp));
            
            if(n.isProbablePrime(100)) {
                prime.add(n);
                break;
            }
            
            if(n.compareTo(BigInteger.ONE) == 0) break;
        }
        long freeze = 0;
        while(!composite.isEmpty() && freeze ++ < maxIterations) {
            BigInteger c = composite.poll();
            BigInteger factor = this.polardRhoEngine(c);
            if(factor == null) {
                composite.add(c);
                continue;
            }
            if(factor.isProbablePrime(100)) {
                prime.add(factor);
            } else {
                composite.add(factor);
            }
            //Finding max exponent of factor
            for(exp = 1; exp < 4096; exp++) {
                if(c.mod(factor.pow(exp)) != BigInteger.ZERO) {
                    exp -= 1;
                    break;
                }
            }
            c = c.divide(factor.pow(exp));
            if(c.isProbablePrime(100)) {
                prime.add(c);
                break;
            }
            if(c.compareTo(BigInteger.ONE) != 0) composite.add(c);
        }
        
        List<BigInteger> factors = new ArrayList<>();
        factors.addAll(prime);
        return factors;
    }
    
    /**
     * Find factors using Pollard's Rho method
     * @return Factors that algorithm has found
     */
    @Override
    public BigInteger[] commitMethod() {
        List<BigInteger> res = this.polardRho();
        //Not every iteration is successful
        while(res == null || res.isEmpty()) {
            res = this.polardRho();
        }
        
        BigInteger[] toReturn = new BigInteger[res.size()];
        for(int i = 0; i < res.size(); i++)
            toReturn[i] = res.get(i);
        return toReturn; //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}