package bid.mythesis.cryptanalysis;

import static bid.mythesis.Configuration.STATION_ID;
import bid.mythesis.logarithm.DiscreteLogarithm;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Analyze ElGamal encryption problem
 * @author David Salac
 */
public class ElGamalCryptoanalysis extends CryptanalysisTask {
    private final BigInteger c1;
    private final BigInteger c2;
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger h;
    
    private BigInteger x;
    private BigInteger m;
    
    /**
     * Initialize instance for solving of problem
     * @param taskId Task ID
     * @param c1 Encrypted message (first element of vector)
     * @param c2 Encrypted message (second element of vector)
     * @param p Modulus of encrypting congruence
     * @param g Group generator 
     * @param h g^x mod p
     */
    public ElGamalCryptoanalysis(String taskId, BigInteger c1, BigInteger c2, BigInteger p, BigInteger g, BigInteger h) {
        super(taskId);
        
        this.c1 = c1;
        this.c2 = c2;
        this.p = p;
        this.g = g;
        this.h = h;
        
        this.x = null; this.m = null;
    }

    /**
     * Solve the problem
     * @return Map of values that will be send to server
     */
    @Override
    public Map<String, String> analyse() {
        long startTime = System.currentTimeMillis() / 1000L;
        DiscreteLogarithm DLsolver = DiscreteLogarithm.initInstance(g, h, p);
        this.x = DLsolver.commitMethod();
        Map<String, String> res = new HashMap<>();
        if(x != null) {
            long totalTime = (System.currentTimeMillis() / 1000L) - startTime;
            BigInteger c1x = c1.modPow(x, p);
            BigInteger c1xInv = c1x.modInverse(p);
            this.m = c2.multiply(c1xInv).mod(p);
            res.put("type", "ElGamal");
            res.put("stationId", STATION_ID);
            res.put("taskId", this.getTaskId());
            res.put("x", this.x.toString(16));
            res.put("m", this.m.toString(16));
            res.put("time", Long.toString(totalTime));
            return res;
        }
        
        return null;
    }
    
    /**
     * Transform problem to readable string
     * @return String consisted of current problem data
     */
    @Override
    public String toString() {
        return "Solve: ElGamal, TaskID: " + this.getTaskId() + ", p: " + this.p.toString(16) + ", g: " + this.g.toString(16) + ", h: " + this.h.toString(16) + ", c1: " + this.c1.toString(16) + ", c2: " + this.c2.toString(16);
    }
    
    /**
     * Compare values of each variable of problem
     * @param obj Data set to comparison
     * @return Information about identity of problems
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ElGamalCryptoanalysis) {
            ElGamalCryptoanalysis nObj = (ElGamalCryptoanalysis)obj;
            return this.getTaskId().equals(nObj.getTaskId()) && nObj.p.compareTo(this.p) == 0 && nObj.g.compareTo(this.g) == 0 && nObj.h.compareTo(this.h) == 0 && nObj.c1.compareTo(this.c1) == 0 && nObj.c2.compareTo(this.c2) == 0;
        }
        return false;
    }

    /**
     * Compute hash code of current task
     * @return Hash code of task
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.c1);
        hash = 89 * hash + Objects.hashCode(this.c2);
        hash = 89 * hash + Objects.hashCode(this.p);
        hash = 89 * hash + Objects.hashCode(this.g);
        hash = 89 * hash + Objects.hashCode(this.h);
        return hash;
    }
    
}
