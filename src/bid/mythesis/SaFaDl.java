/**
 * The main package of application
 */
package bid.mythesis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import bid.mythesis.cryptanalysis.*;

/**
 * Class where the application starts
 * @author David Salac
 */
public class SaFaDl {
    
    /**
     * Starting method of application
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String URL = Configuration.RECEIVE_URL + "?id=" + Configuration.STATION_ID;
        CryptanalysisTask task = null;
        Thread thread = null;
        
        DateFormat df = DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale ("en", "EN"));
        String formattedDate = df.format (new Date ());
        System.out.println("Application started at: " + formattedDate);
        
        while(!Thread.currentThread().isInterrupted()) {
            try {
                ReceiveData incomeData = new ReceiveData();
                if(incomeData.isEmpty()) continue;
                
                //First iteration:
                if(task == null) {
                    task = incomeData.getTask(); incomeData.sendACK();
                    if(task == null) continue;
                    
                    df = DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale ("en", "EN"));
                    formattedDate = df.format (new Date ());
                    System.out.println("New data arrived at " + formattedDate);
                    System.out.println(task.toString());
                    
                    thread = new Thread(task);
                    thread.start();
                }
                
                CryptanalysisTask newTask = incomeData.getTask();
                if(newTask == null) continue;
                incomeData.sendACK();
                
                if(!newTask.equals(task)) {
                    if(!thread.isInterrupted() && thread.isAlive())
                        thread.interrupt();
                    if(thread.isInterrupted() || !thread.isAlive()) {
                        task = newTask;
                        
                        df = DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale ("en", "EN"));
                        formattedDate = df.format (new Date ());
                        System.out.println("New data arrived at " + formattedDate);
                        System.out.println(task.toString());
                        
                        thread = new Thread(task);
                        thread.start();
                    }
                }
                
                Thread.currentThread().sleep(3000);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
