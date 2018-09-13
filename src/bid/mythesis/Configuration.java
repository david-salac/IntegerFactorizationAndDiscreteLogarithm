package bid.mythesis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Application configuration class
 * @author David Salac
 */
public class Configuration {

    /**
     * URL where the tasks are generated
     */
    public static String RECEIVE_URL;

    /**
     * URL to which the tasks are submitted
     */
    public static String TRANSMIT_URL;

    /**
     * ID of this station
     */
    public static String STATION_ID;

    /**
     * Path to binary file of modified MSIEVE program
     */
    public static String MSIEVE_MOD_BIN;

    /**
     * Arguments of msieve program (usually -q)
     */
    public static String MSIEVE_MOD_PARAM;
    
    /**
     * Get the value from JSON line for some key
     * @param JSON JSON line { ... }
     * @param key Key you want to find
     * @return Return value in JSON line that match key in argument
     */
    public static String JSONparser(String JSON, String key) {
        String JSONedit = JSON.replace("://", "///").replace("{", "").replace("}", "").replace(" ", "").replace("\n", "").replace("\"", "");
        String [] dataJson = JSONedit.split(",");
        
        for(String dataPair : dataJson) {
            String [] dataSet = dataPair.split(":");
            String findedKey = dataSet[0].trim();
            String value = dataSet[1].trim();
            if(findedKey.equals(key)) {
                return value.replace("///", "://");
            }
        }
        return null;
    }
    
    static {
        try {
            String configInfo = readFile("config.json");
            RECEIVE_URL = JSONparser(configInfo, "RECEIVE_URL");
            TRANSMIT_URL = JSONparser(configInfo, "TRANSMIT_URL");
            STATION_ID = JSONparser(configInfo, "STATION_ID");
            MSIEVE_MOD_BIN = JSONparser(configInfo, "MSIEVE_MOD_BIN");
            MSIEVE_MOD_PARAM = JSONparser(configInfo, "MSIEVE_MOD_PARAM");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        } 
        //For testing purposes:
        /*
        RECEIVE_URL  = "http://www.mythesis.bid/task.php";
        TRANSMIT_URL = "http://www.mythesis.bid/solution.php";
        STATION_ID = "1";
        MSIEVE_MOD_BIN = "/home/addmin/Downloads/Diplomka/modBuild/msieve";
        MSIEVE_MOD_PARAM = "-q";
        */
    }
    /**
     * Read whole content of file
     * @param file File that will be read
     * @return Content of file
     * @throws IOException If there is any trouble with file
     */
    private static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader (file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }
}
