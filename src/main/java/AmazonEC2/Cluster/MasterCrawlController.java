package AmazonEC2.Cluster;

import UrlLoader.LoadUrls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MasterCrawlController {

    public void sendInstanceDetailsCrawl(Map<String,InstanceDetails> instanceDetailsContainer){


       try {
           LoadUrls loader = new LoadUrls();
           Queue<String> urlQueue = loader.readFile();

           Set<String> instanceKeySet = instanceDetailsContainer.keySet();

           /*
            * Crawl Functionality - Client Posts the URL's to be crawled from the queue
            * to the Public IP address of the t2.micro instances.
            * example: Url would be like: https://123.4.63.16:8080/startcrawl?url=https://amazon.com
            */

           int totalQueueURLS = urlQueue.size();

           while(urlQueue.size() != 0){

               if(totalQueueURLS == urlQueue.size()){
                   //First phase of URL Submission skip it.
                   continue;
               }else{
                   /* Wait for the Thread for 33 seconds to go for second Iteration of
                    * Crawl Seed URL Submission . In this way the Master Node honours
                    * The client SLA.
                    */
                   Thread.sleep(33000);
               }

               for(String instanceId : instanceKeySet){

                   System.out.println("The Instance ID to which URL is going to submitted for crawl is" + instanceId);
                   InstanceDetails instancedetails = instanceDetailsContainer.get(instanceId);

                   final String IPAddress = instancedetails.getPublicIPaddress();
                   StringBuilder urlBuilder = new StringBuilder();

                   /*
                    * Removes the URL from the head of the Queue.
                    */
                    if(urlQueue.size() == 0){
                        break;
                    }

                   final String seedUrl = urlQueue.remove();

                    /*
                   * example: Url would be like: https://123.4.63.16:8080/startcrawl?url=https://amazon.com
                   */

                   urlBuilder.append("https://").append(IPAddress).append(":8080/startCrawl?url=").append(seedUrl);

                   URL url = new URL(urlBuilder.toString());

                   final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                   conn.setRequestMethod("GET");

                   /*
                    * The Slave Crawler API takes time to return the http response code
                    * Status 412 - 412 Precondition Failed client error response code
                    * indicates that access to the target resource has been denied.
                    *
                    * Crawler has a soft hit of 30,000 milliseconds before it accepts
                    * any other URL for crawling.
                    * If else it print response code 200 Ok.
                    */

                   if(conn.getResponseCode() == 200){

                       System.out.println("Success returned a 200 Ok Response");

                   }else if(conn.getResponseCode() == 412){

                       System.out.println(" Crawler SLA has surpassed below the 30 seconds limit !!" +
                               " The Instance should wait for the next crawling queue");
                   }
               }
           }

           GraceFulClientShutdown graceFulMicroServiceSlaveShutdown = new GraceFulClientShutdown();
           graceFulMicroServiceSlaveShutdown.handleGraceFulShutdown(instanceDetailsContainer);

        } catch (IOException e) {

            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        } catch (InterruptedException ex) {

           System.out.println(ex.getMessage());
           System.out.println(ex.getLocalizedMessage());
       }
    }
}
