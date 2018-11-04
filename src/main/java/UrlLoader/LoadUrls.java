package UrlLoader;

/*
 * Loads the CSV which is present here - https://s3.amazonaws.com/aurorachallenge/20000websites.xlsx
 * Loads the Alexa 1 million URLs file from here - https://s3.amazonaws.com/aurorachallenge/top-1m.csv
 */


import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/*
 * Thread runs on 4 CPU core - Initiates 2 Threads for execution.
 * bucket 1 contains 10,000 urls , bucket 2 contains 10,000 Urls.
 */

public class LoadUrls{

    private static final String file_name = "C:\\Users\\Dell\\Documents\\MasterWebCrawler\\20000websites.xlsx";

    private static final Queue<String> q = new LinkedList<String>();

    private static String hostnamePort=null;

    public static void SetHostname(String hostname){
        hostnamePort = hostname;
    }

    public String getHostName(){return hostnamePort; }

    public Queue<String> readFile() throws IOException {

        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;

        try {
            // we create an XSSF Workbook object for our XLSX Excel File
            workbook = new XSSFWorkbook(new FileInputStream(new File(file_name)));
            // we get first sheet
            sheet = workbook.getSheetAt(0);

            // we iterate on rows
            Iterator<Row> rowIt = sheet.iterator();
            int rowcounter = 0;

            while (rowIt.hasNext()) {
                Row row = rowIt.next();

                // iterate on cells for the current row
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    System.out.println(cell.toString());
                    String fetchedURL = cell.toString();
                   q.add(fetchedURL);
                    break;
                }

                rowcounter++;
            }


            System.out.println(" Total URls in Queue --------------> " + q.size());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{

            if(workbook != null){ workbook.close();}
        }
        return q;
    }


    public void fireThreads(Queue<String> q,final String host){

       while(q.size() != 0){
            try {
                LoadUrls threadOne = new LoadUrls();
                String str = q.remove();
                System.out.println(" URL removed from the queue is " + str);
                threadOne.initiatecrawl(str,host);
            } catch (IOException e) {
                e.getMessage();
            } catch (InterruptedException e) {
                e.getMessage();
            }
        }


    }

    private void initiatecrawl(String url1,String host) throws IOException, InterruptedException {


        URL url = new URL("http://"+host+"/startCrawl?url="+url1);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

        if (conn.getResponseCode() == 412) {

            System.out.println("Failed : HTTP Error code : "
                    + conn.getResponseCode());
            Thread.sleep(30000);

        }else if(conn.getResponseCode() == 200){

            System.out.println("Success returned a 200 Ok Response");
        }
    }

}
