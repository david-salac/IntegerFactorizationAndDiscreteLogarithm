package bid.mythesis;

import bid.mythesis.cryptanalysis.CryptanalysisTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for receiving of data from server
 * @author David Salac
 */
public final class ReceiveData {
    private String receiveURL;
    private String transmitURL;
    private String JSONline;
    private CryptanalysisTask task;
    
    /**
     * Get content of web site on selected URL
     * @param url URL of selected web site
     * @return Content of web site
     * @throws Exception if there is anything wrong
     */
    public static String getURLcontent(String url) throws Exception {
        URL site = new URL(url);
        URLConnection con = site.openConnection();
        BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder res = new StringBuilder();
        String inLine;
        while ((inLine = input.readLine()) != null) {
            res.append(inLine);
        }
        input.close();
        return res.toString();
    }
    
    /**
     * Create an instance of this class
     * @param receiveURL The URL of file where the data are stored
     * @param transmitURL The URL of file where the ACK should be sent
     */
    public ReceiveData(String receiveURL, String transmitURL) {
        this.receiveURL = receiveURL;
        this.transmitURL = transmitURL;
        try {
            this.JSONline = getURLcontent(receiveURL);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        if(!isEmpty())
            this.task = CryptanalysisTask.initInstance(JSONline);
    }

    /**
     * Create an instance of this class
     */
    public ReceiveData() {
        this(Configuration.RECEIVE_URL + "?id=" + Configuration.STATION_ID, Configuration.TRANSMIT_URL);
    }
    
    /**
     * Encapsulate the task variable
     * @return value of task variable
     */
    public CryptanalysisTask getTask() {
        return task;
    }
    
    /**
     * Send the positive acknowledgement to server
     */
    public void sendACK() {
        //Sending ACK
        Map<String, String> ack = new HashMap<>();
        ack.put("ACK", "true");
        ack.put("taskId", task.getTaskId());
        //Data are sended in independent thread
        SendData output = new SendData(ack);
        Thread outputThread = new Thread(output);
        outputThread.start();
        //-----------
    }
    
    /**
     * Check whether the task is not empty
     * @return true if the task is empty
     */
    public boolean isEmpty() {
        return this.JSONline == null || JSONline.length() < 10;
    }
}
