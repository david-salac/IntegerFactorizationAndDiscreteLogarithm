package bid.mythesis.factorization;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents ideal of form r + p*phi
 * @author David Salac
 */
public class IdealSet {
    private BigInteger p;
    private List<BigInteger> r;
    
    /**
     * Returns value of p
     * @return p value
     */
    public BigInteger getP() { return p; }
    
    /**
     * Returns array of all r values
     * @return r values
     */
    public BigInteger[] getR() { 
        if(r.isEmpty()) return null;
        BigInteger ri[] = new BigInteger[r.size()];
        for(int i = 0; i < r.size(); i++) {
            ri[i] = r.get(i);
        }
        return ri; 
    }
    
    /**
     * Initialize problem
     * @param p Prime number
     * @param r First ri value
     */
    public IdealSet(BigInteger p, BigInteger r) {
        this.p = p; this.r = new ArrayList<>();
        if(r != null) { this.r.add(r); }
    }
    
    /**
     * Initialize problem
     * @param p prime number
     */
    public IdealSet(BigInteger p) {
        this(p, null);
    }
    
    /**
     * Insert new value of r
     * @param r value of r
     */
    public void InsertR(BigInteger r) {
        this.r.add(r);
    }
}
