/**
 * Package for solving of cryptanalysis problem
 */
package bid.mythesis.cryptanalysis;

import bid.mythesis.SendData;
import java.math.BigInteger;
import java.util.Map;

/**
 * Encapsulation of single system task
 * @author David Salac
 */
public abstract class CryptanalysisTask implements Runnable {
    
    private final String taskId;

    /**
     * Fundamental method for obtaining of results
     * @return Map of results (key and value)
     */
    public abstract Map<String, String> analyse();
    
    /**
     * Get the value from JSON line for some key
     * @param JSON JSON line { ... }
     * @param key Key you want to find
     * @return Return value in JSON line that match key in argument
     */
    protected static String JSONparser(String JSON, String key) {
        String JSONedit = JSON.replace("{", "").replace("}", "").replace(" ", "").replace("\n", "").replace("\"", "");
        String [] dataJson = JSONedit.split(",");
        
        for(String dataPair : dataJson) {
            String [] dataSet = dataPair.split(":");
            String findedKey = dataSet[0].trim();
            String value = dataSet[1].trim();
            if(findedKey.equals(key)) {
                return value;
            }
        }
        return null;
    }
    
    /**
     * Returns key set of JSON line
     * @param JSON JSON is one JSON line { ... }
     * @return Returns list of keys in JSON
     */
    protected static String [] JSONkeys(String JSON) {
        String JSONedit = JSON.replace("{", "").replace("}", "").replace(" ", "").replace("\n", "").replace("\"", "");
        String [] dataJson = JSONedit.split(",");
        String keysSet = "";
        int count = 0;
        for(String dataPair : dataJson) {
            String [] dataSet = dataPair.split(":");
            String findedKey = dataSet[0].trim();
            keysSet += findedKey;
            count ++;
            if(count < dataJson.length) {
                keysSet += ",";
            }
        }
        if(count > 0) {
            return keysSet.split(",");
        }
        return null;
    }
    
    /**
     * Get the value of task ID
     * @return value task ID
     */
    public String getTaskId() {
        return this.taskId;
    }
    
    /**
     * Initialize instance
     * @param taskId Value of task ID
     */
    protected CryptanalysisTask(String taskId) {
        this.taskId = taskId;
    }
    
    /**
     * Select right solver for current task
     * @param JSONline Values of JSON line (parameters of task)
     * @return Right solver for current task
     */
    public static CryptanalysisTask initInstance(String JSONline) {
        try {
            if(JSONparser(JSONline, "type").equals("RSA")) {
                String taskId = JSONparser(JSONline, "taskId");
                BigInteger n = new BigInteger(JSONparser(JSONline, "n"), 16);
                BigInteger c = new BigInteger(JSONparser(JSONline, "c"), 16);
                BigInteger e = new BigInteger(JSONparser(JSONline, "e"), 16);
                return new RSACryptoanalysis(taskId, n, c, e);
            }
            else if(JSONparser(JSONline, "type").equals("DH")) {
                String taskId = JSONparser(JSONline, "taskId");
                BigInteger p = new BigInteger(JSONparser(JSONline, "p"), 16);
                BigInteger g = new BigInteger(JSONparser(JSONline, "g"), 16);
                BigInteger gPowA = new BigInteger(JSONparser(JSONline, "gPowA"), 16);
                BigInteger gPowB = new BigInteger(JSONparser(JSONline, "gPowB"), 16);
                return new DHCryptoanalysis(taskId, p, g, gPowA, gPowB);
            }
            else if(JSONparser(JSONline, "type").equals("ElGamal")) {
                String taskId = JSONparser(JSONline, "taskId");
                BigInteger p = new BigInteger(JSONparser(JSONline, "p"), 16);
                BigInteger g = new BigInteger(JSONparser(JSONline, "g"), 16);
                BigInteger h = new BigInteger(JSONparser(JSONline, "h"), 16);
                BigInteger c1 = new BigInteger(JSONparser(JSONline, "c1"), 16);
                BigInteger c2 = new BigInteger(JSONparser(JSONline, "c2"), 16);
                return new ElGamalCryptoanalysis(taskId, c1, c2, p, g, h);
            }
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
        
    }
    
    /**
     * Find solution and send it to server
     */
    @Override
    public void run() {
        Map<String, String> res = this.analyse();
        if(res != null) {
            System.out.print("System find solution! ");
            res.keySet().forEach((s) -> {
                System.out.print(s + ": " + res.get(s) + ", ");
            });
            System.out.println();
            //Data are sended in independent thread
            SendData output = new SendData(res);
            Thread outputThread = new Thread(output);
            outputThread.start();
        }
    }
    
}
