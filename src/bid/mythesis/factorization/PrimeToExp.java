package bid.mythesis.factorization;

import java.math.BigInteger;

/**
 * Represent the tuple of prime number and it's exponent
 * @author David Salac
 */
public class PrimeToExp {
    BigInteger p;
    int exp;
    byte expParity;

    /**
     * Get p value
     * @return Value of prime number p
     */
    public BigInteger getP() {
        return p;
    }

    /**
     * Get value of exponent
     * @return Return exponent
     */
    public int getExp() {
        return exp;
    }

    /**
     * Get parity of exponent
     * @return Parity of exponent
     */
    public byte getExpParity() {
        return expParity;
    }
    
    /**
     * Create new instance of class (the tuple of number and exponent)
     * @param prime Prime number in factorization
     * @param exponent Exponent of prime number
     */
    public PrimeToExp(BigInteger prime, int exponent) {
        this.p = new BigInteger(prime.toString());
        this.exp = exponent;
        this.expParity = (byte)(exponent % 2 > 0 ? exponent % 2 : (-1)*exponent % 2); //Little error here before = cca 21 h of time :-(
    }
}
