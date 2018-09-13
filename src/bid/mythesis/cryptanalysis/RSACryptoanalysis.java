package bid.mythesis.cryptanalysis;

import bid.mythesis.Configuration;
import static bid.mythesis.Configuration.STATION_ID;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import bid.mythesis.factorization.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Analyze RSA encryption problem
 * @author David Salac
 */
public class RSACryptoanalysis extends CryptanalysisTask {
    private final BigInteger n;
    private final BigInteger c;
    private final BigInteger e;
    private BigInteger m;
    private BigInteger d;

    /**
     * Initialize instance for solving of problem
     * @param taskId Task ID
     * @param n modulus
     * @param c encrypted message
     * @param e encryption exponent
     */
    public RSACryptoanalysis(String taskId, BigInteger n, BigInteger c, BigInteger e) {
        super(taskId);
        this.n = n.multiply(BigInteger.ONE); this.c = c.multiply(BigInteger.ONE); this.e = e.multiply(BigInteger.ONE); this.m = null; this.d = null;
    }

    /**
     * Solve the problem
     * @return Map of values that will be send to server
     */
    @Override
    public Map<String, String> analyse() {
        long startTime = System.currentTimeMillis() / 1000L;
        Factorization factor = Factorization.initInstance(n);
        BigInteger [] factors = null;
        Map<String, String> res = new HashMap<>();
        if(factor == null) {
            try { 
                Process msieve = new ProcessBuilder(Configuration.MSIEVE_MOD_BIN, Configuration.MSIEVE_MOD_PARAM, this.n.toString()).start();
                BufferedReader input = new BufferedReader(new InputStreamReader(msieve.getInputStream()));
                String inputLine;
                
                while( (inputLine = input.readLine()) != null ) {
                    if(JSONparser(inputLine, "task") != null) {
                        String [] keys = JSONkeys(inputLine);
                        factors = new BigInteger[2];
                        int pIndex = 0;
                        for (String key : keys) {
                            if(key.contains("p_")) {
                                factors[pIndex++] = new BigInteger(JSONparser(inputLine, key));
                            }
                        }
                    } else {
                        System.out.println(inputLine);
                    }
                }
            }
            catch(Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
        } else {
            factors = factor.commitMethod();
        }
        if(factors.length == 2 && this.n.compareTo( factors[0].multiply(factors[1]) ) == 0) {
            long totalTime = (System.currentTimeMillis() / 1000L) - startTime;
            BigInteger phi = (factors[0].subtract(BigInteger.ONE)).multiply(factors[1].subtract(BigInteger.ONE));
            BigInteger dVal = e.modInverse(phi);
            this.d = dVal;
            this.m = c.modPow(d, n);
            res.put("type", "RSA");
            res.put("stationId", STATION_ID);
            res.put("taskId", this.getTaskId());
            res.put("m", this.m.toString(16));
            res.put("d", this.d.toString(16));
            res.put("p", factors[0].toString(16));
            res.put("q", factors[1].toString(16));
            res.put("time", Long.toString(totalTime));
        }
        return res;
    }

    /**
     * Compare values of each variable of problem
     * @param obj Data set to comparison
     * @return Information about identity of problems
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RSACryptoanalysis) {
            RSACryptoanalysis nObj = (RSACryptoanalysis)obj;
            return this.getTaskId().equals(nObj.getTaskId()) && nObj.n.compareTo(this.n) == 0 && nObj.c.compareTo(this.c) == 0 && nObj.e.compareTo(this.e) == 0;
        }
        return false;
    }

    /**
     * Compute hash code of current task
     * @return Hash code of task
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.n);
        hash = 79 * hash + Objects.hashCode(this.c);
        hash = 79 * hash + Objects.hashCode(this.e);
        return hash;
    }
    
    /**
     * Transform problem to readable string
     * @return String consisted of current problem data
     */
    @Override
    public String toString() {
        return "Solve: RSA, TaskID: " + this.getTaskId() + ", n: " + this.n.toString(16) + ", c: " + this.c.toString(16) + ", e: " + this.e.toString(16) + (this.m == null ? "" : ", m: " + this.m.toString(16));
    }
}
