package bid.mythesis.factorization;

import java.math.BigInteger;
import java.util.List;

/**
 * Represents the values for computation of Legendere congruence
 * @author David Salac
 */
public class LegendereCongruence {
    private BigInteger x;
    private BigInteger x2;
    private List<PrimeToExp> factors;

    /**
     * Initialize congruence class
     * @param x Default value of x
     * @param x2 Value of x^2 mod n
     * @param factors List of all prime factors with exponents
     */
    public LegendereCongruence(BigInteger x, BigInteger x2, List<PrimeToExp> factors) {
        this.x = x;
        this.x2 = x2;
        this.factors = factors;
    }
    
    /**
     * Returns value of x
     * @return get value of x
     */
    public BigInteger getX() {
        return x;
    }
    
    /**
     * Returns value of x^2 mod n
     * @return * @return Returns value of x^2 mod n
     */
    public BigInteger getX2() {
        return x2;
    }
    
    /**
     * Find the exponent for integer in factor base
     * @param factor Element of integer factorization
     * @return Exponents in factorization of x^2 mod n
     */
    public int getExponentOfFactor(BigInteger factor) {
        for(PrimeToExp f : factors) {
            if(f.getP().compareTo(factor) == 0) {
                return f.exp;
            }
        }
        return 0;
    }
    
    /**
     * Returns list of all factors
     * @return get all factors of x^2 mod n
     */
    public List<PrimeToExp> getFactors() {
        return factors;
    }
    /**
     * Returns parity of exponent in selected factor base
     * @param base Chosen factor base
     * @return 1 if parity of exponent is odd otherwise zero
     */
    public byte [] wrapToFactorBase(BigInteger base[]) {
        byte [] newBase = new byte[base.length];
        boolean findIt;
        for(PrimeToExp p : factors) {
            findIt = false;
            for(int i =0; i < base.length; i++) {
                if(p.getP().compareTo(base[i]) == 0) {
                    newBase[i] = p.getExpParity();
                    findIt = true;
                    break;
                }
            }
            if(!findIt)
                return null;
        }
        return newBase;
    }
    
    /**
     * For debug purposes
     * @return Return formated output of stored data
     */
    @Override
    public String toString() {
        String fb = "";
        for(PrimeToExp p : factors) {
            fb += p.getP()+"^"+p.getExp()+" * ";
        }
        return "x:"+x.toString()+",x^2:"+x2.toString()+", factors: " + fb; //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Return string consists of exponent over FB
     * @param fbase Factor base
     * @return Exponents of current factor base
     */
    public String toString(BigInteger fbase[]) { 
        String output = this.getX2()+"\t";
        for(int i = 0; i < fbase.length; i++) {
            int exp = 0;
            for(int q = 0; q < this.getFactors().size(); q++) {
                if(getFactors().get(q).getP().compareTo(fbase[i]) == 0) {
                    exp = getFactors().get(q).getExp();
                    break;
                }
            }
            output += Integer.toString(exp) + "\t";
        }
        return output;
    }
    
    /**
     * Comparison with another congruence class
     * @param m Compared element
     * @return Returns true if m stores same data like current class
     */
    public boolean equalsTo(LegendereCongruence m) {
        return (m.getX().compareTo(x) == 0) && (m.getX2().compareTo(x2) == 0);
    }
    
}
