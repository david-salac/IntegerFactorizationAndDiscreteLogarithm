package bid.mythesis;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Class for sending data to selected URL
 * @author David Salac
 */
public class SendData implements Runnable {
    private Map<String,String> dataToSend;
    
    /**
     * Initialize instance
     * @param dataSet Data set that will be sand
     */
    public SendData(Map<String, String> dataSet) {
        this.dataToSend = dataSet;
    }
    /**
     * Create data line for POST method
     * @return data line
     */
    private String getDataLine() {
        String dataLine = "";
        dataLine = dataToSend.entrySet().stream().map((entry) -> "&"+entry.getKey()+"="+entry.getValue()).reduce(dataLine, String::concat);
        dataLine = dataLine.substring(1);
        return dataLine;
    }
    
    /**
     * Function for sending data to URL using POST method
     * @return Code of operation
     * @throws IOException If there is an error with connection
     */
    private int sendData() throws IOException {
        //URL is obtained from configuration class
        String url = Configuration.TRANSMIT_URL;
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = this.getDataLine();
        connection.setDoOutput(true);
        DataOutputStream write = new DataOutputStream(connection.getOutputStream());
        write.writeBytes(urlParameters);
        write.flush();
        write.close();
        return connection.getResponseCode();
    }

    /**
     * Sending data to server
     */
    @Override
    public void run() {
        boolean sendCheck = true;
        while(sendCheck) {
            try {
                sendCheck = (sendData() != 200);
                Thread.currentThread().sleep(3000);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
