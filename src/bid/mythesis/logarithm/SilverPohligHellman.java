package bid.mythesis.logarithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Silver-Pohlig-Hellman method
 * @author David Salac
 */
public class SilverPohligHellman extends DiscreteLogarithm {
    private BigInteger q;
    private BigInteger qSubractOne;
    private BigInteger a;
    private BigInteger b;
    private BigInteger [] prime;
    private int [] exp;
    
    /**
     * Use brute force method for factorization of (modulus - 1)
     */
    public final void factorizeQSubtract1() {
        BigInteger qSubtract1 = q.subtract(BigInteger.ONE);
        BigInteger upperBound = bigIntegerSqrt(qSubtract1);
        
        List<BigInteger> piList = new ArrayList<>();
        List<Integer> alphaiList = new ArrayList<>();
        
        for(BigInteger p = new BigInteger("2"); p.compareTo(upperBound) < 1; p = p.add(BigInteger.ONE)) {
            if(qSubtract1.mod(p).compareTo(BigInteger.ZERO) == 0) {
                piList.add(p);
                for(int alphai = 1; alphai < 256; alphai++) {
                    if( (qSubtract1.mod(p.pow(alphai))).compareTo(BigInteger.ZERO) != 0 ) {
                        alphai -= 1; 
                        alphaiList.add(alphai);
                        qSubtract1 = qSubtract1.divide(p.pow(alphai));
                        
                        if(qSubtract1.isProbablePrime(100) || qSubtract1.compareTo(BigInteger.ONE) == 0) {
                            if(qSubtract1.compareTo(BigInteger.ONE) != 0) {
                                piList.add(qSubtract1);
                                alphaiList.add(1);
                            }
                            
                            this.prime = new BigInteger[piList.size()];
                            this.exp = new int[piList.size()];
                            
                            for(int i = 0; i < piList.size(); i++) {
                                prime[i] = piList.get(i);
                                exp[i] = alphaiList.get(i);
                            }
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Find value of j of congruence: val = a^(j.qSubractOne/pi) (mod q)
     * @param pi Argument of congruence: val = a^(j.qSubractOne/pi) (mod q)
     * @param val Argument of congruence: val = a^(j.qSubractOne/pi) (mod q)
     * @return Null or value of j solving congruence: val = a^(j.qSubractOne/pi) (mod q)
     */
    private BigInteger findJ(BigInteger pi, BigInteger val) {
        if(pi.bitLength() < 22) {
            for(BigInteger j = BigInteger.ZERO; j.compareTo(pi) < 1; j = j.add(BigInteger.ONE)) {
                BigInteger powRes = a.modPow(j.multiply( qSubractOne.divide(pi) ), q);
                if(powRes.compareTo(val) == 0) {
                    return j;
                }
            }
        } else {
            BabyStepGiantStep tryJ = new BabyStepGiantStep(a, val, q);
            BigInteger jFinded = tryJ.commitMethod();
            return jFinded.multiply(pi).divide(qSubractOne);
        }
        
        return null;
    }
    
    /**
     * Compute set of congruence using Chinese reminder theorem
     * @param congruences Set of congruence of form: x_i = congruence[i][0] (mod congruence[i][1])
     * @return Solution, value of x: x_i = congruence[i][0] (mod congruence[i][1])
     */
    private BigInteger chineseRemainderTheorem(BigInteger [][] congruences) {
        
        BigInteger solution = new BigInteger("0");
        BigInteger M = new BigInteger("1");
        for(int i = 0; i < congruences.length; i ++) { M = M.multiply(congruences[i][1]); }
        
        for(int i = 0; i < congruences.length; i ++) { 
            BigInteger Mi = M.divide(congruences[i][1]); 
            BigInteger MiOverline = Mi.modInverse(congruences[i][1]);
            solution = solution.add(Mi.multiply(MiOverline).multiply(congruences[i][0]));
        }
        return solution.mod(M);
    }
    
    /**
     * Implementation of Silver-Pohlig-Hellman method
     * @return Integer solving equation g^x = a (mod p)
     */
    private BigInteger silverPohligHellmanMethod() {
        //-------- Finding set of congruences --------
        BigInteger [][] congruences = new BigInteger[prime.length][2]; //First is value second modulus: x = value (modulus)
        for(int i = 0; i < this.prime.length; i++) {
            BigInteger x = new BigInteger("0");
            BigInteger pi = this.prime[i];
            int exponent = this.exp[i];
            BigInteger bi = this.b;
            BigInteger aInv = a.modInverse(q);
            for(int e = 1; e <= exponent; e++) {
                BigInteger val = bi.modPow(qSubractOne.divide(pi.pow(e)), q);
                BigInteger j = findJ(pi, val);
                x = x.add(j.multiply(pi.pow(e-1)));
                bi = this.b.multiply(aInv.modPow(x, q)).mod(q);
            }
            congruences[i][0] = x;
            congruences[i][1] = pi.pow(exponent);
        }
        //---------------------------------------------
        
        //Use the congruence for computation
        return this.chineseRemainderTheorem(congruences);
    }
    
    /**
     * Create instance of class
     * @param g left site of congruence g^x = a (mod n)
     * @param a right site of congruence g^x = a (mod n)
     * @param n prime modulus of congruence g^x = a (mod n)
     */
    public SilverPohligHellman(BigInteger g, BigInteger a, BigInteger n) {
        super(g, a ,n);
        this.a = g; this.b = a; this.q = n; this.qSubractOne = q.subtract(BigInteger.ONE);
        this.factorizeQSubtract1();
    }
    
    @Override
    public BigInteger commitMethod() {
        return this.silverPohligHellmanMethod();
    }
    
}
