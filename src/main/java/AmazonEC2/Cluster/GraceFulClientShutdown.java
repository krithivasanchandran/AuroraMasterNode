package AmazonEC2.Cluster;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class GraceFulClientShutdown {

    /* Handles gracefull shutdown of JVM - if the Slave Crawler is still crawling when you invoke this method
     * then , it would handle it gracefully by completing all the remaining child URLs crawls and then
     * shutdown after that.
     */

    public void handleGraceFulShutdown(Map<String,InstanceDetails> instanceDetailsContainer) throws MalformedURLException {

       Set<Map.Entry<String,InstanceDetails>> entries = instanceDetailsContainer.entrySet();

       for(Map.Entry<String,InstanceDetails> k : entries){
           final String instanceId = k.getKey();
           final InstanceDetails instanceDetails = k.getValue();

           StringBuilder gracefulUrlBuilder = new StringBuilder();
           gracefulUrlBuilder.append("http://").append(instanceDetails.getPublicIPaddress()).append("/shutdown");

           URL url = new URL(gracefulUrlBuilder.toString());

           final HttpURLConnection conn;
           try {
               conn = (HttpURLConnection) url.openConnection();
               conn.setRequestMethod("GET");

               if(conn.getResponseCode() == 200){
                   System.out.println("Success returned a 200 Ok Response");
                   System.out.println("Now Gracefully shutting down the crawl microservice running on Instance ID: "+ instanceId);
               }else{
                   System.out.println("Make sure the instance is running , Graceful shutdown of microservice failed");
               }

           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }

}
