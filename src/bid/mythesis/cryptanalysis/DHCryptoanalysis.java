package bid.mythesis.cryptanalysis;

import static bid.mythesis.Configuration.STATION_ID;
import bid.mythesis.logarithm.DiscreteLogarithm;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Analyze Diffie-Hellman key exchange problem
 * @author David Salac
 */
public class DHCryptoanalysis extends CryptanalysisTask {
    private final BigInteger g;
    private final BigInteger p;
    
    private final BigInteger gPowA;
    private final BigInteger gPowB;
    
    private BigInteger a;
    private BigInteger sharedKey;
    
    /**
     * Initialize instance for solving of problem
     * @param taskId Task ID
     * @param p modulus p of congruence
     * @param g group generator g
     * @param gPowA g^a mod p
     * @param gPowB g^b mod p
     */
    public DHCryptoanalysis(String taskId, BigInteger p, BigInteger g, BigInteger gPowA, BigInteger gPowB) {
        super(taskId);
        this.p = p; this.g = g; this.gPowA = gPowA; this.gPowB = gPowB;
        this.a = null; this.sharedKey = null;
    }
    
    /**
     * Transform problem to readable string
     * @return String consisted of current problem data
     */
    @Override
    public String toString() {
        return "Solve: DH, TaskID: " + this.getTaskId() + ", p: " + this.p.toString(16) + ", g: " + this.g.toString(16) + ", gPowA: " + this.gPowA.toString(16) + ", gPowB: " + this.gPowB.toString(16);
    }

    /**
     * Solve the problem
     * @return Map of values that will be send to server
     */
    @Override
    public Map<String, String> analyse() {
        long startTime = System.currentTimeMillis() / 1000L;
        DiscreteLogarithm solver = DiscreteLogarithm.initInstance(g, gPowA, p);
        this.a = solver.commitMethod();
        Map<String, String> res = new HashMap<>();
        if(a != null && g.modPow(a, p).compareTo(gPowA) == 0) {
            long totalTime = (System.currentTimeMillis() / 1000L) - startTime;
            this.sharedKey = gPowB.modPow(a, p);
            res.put("type", "DH");
            res.put("stationId", STATION_ID);
            res.put("taskId", this.getTaskId());
            res.put("a", this.a.toString(16));
            res.put("sharedKey", this.sharedKey.toString(16));
            res.put("time", Long.toString(totalTime));
            return res;
        }
        return null;
    }
    
    /**
     * Compare values of each variable of problem
     * @param obj Data set to comparison
     * @return Information about identity of problems
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DHCryptoanalysis) {
            DHCryptoanalysis nObj = (DHCryptoanalysis)obj;
            return this.getTaskId().equals(nObj.getTaskId()) && nObj.p.compareTo(this.p) == 0 && nObj.g.compareTo(this.g) == 0 && nObj.gPowA.compareTo(this.gPowA) == 0 && nObj.gPowB.compareTo(this.gPowB) == 0;
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
        hash = 67 * hash + Objects.hashCode(this.g);
        hash = 67 * hash + Objects.hashCode(this.p);
        hash = 67 * hash + Objects.hashCode(this.gPowA);
        hash = 67 * hash + Objects.hashCode(this.gPowB);
        return hash;
    }
    
}
