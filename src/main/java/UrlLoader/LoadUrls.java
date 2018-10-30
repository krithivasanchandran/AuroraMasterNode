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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * Thread runs on 4 CPU core - Initiates 2 Threads for execution.
 * bucket 1 contains 10,000 urls , bucket 2 contains 10,000 Urls.
 */

public class LoadUrls extends Thread{

    private static final String file_name = "C:\\Users\\Dell\\Documents\\MasterWebCrawler\\20000websites.xlsx";

    private static final Set<String> bucket1 = new HashSet<String>();
    private static final Set<String> bucket2 = new HashSet<String>();

    private static String hostnamePort=null;

    public static void SetHostname(String hostname){
        hostnamePort = hostname;
    }

    public void readFile() throws IOException {

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

                    if(rowcounter % 2 == 0){
                        bucket1.add(cell.toString().trim());
                    }else{
                        bucket2.add(cell.toString().trim());
                    }
                    break;
                }

                rowcounter++;
            }


            System.out.println(" Total URls in bucket1 --------------> " + bucket1.size());
            System.out.println(" Total URls in bucket2 --------------> " + bucket2.size());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{

            if(workbook != null){ workbook.close();}
        }

    }


    public void createThread(){

        LoadUrls threadOne = new LoadUrls();
        LoadUrls threadTwo = new LoadUrls();
        CloseableHttpResponse response=null;

        fireThreads(threadOne,threadTwo);
    }

    private void fireThreads(LoadUrls threadOne, LoadUrls threadTwo){

        bucket1.forEach((t1) -> {
            try {
                threadOne.run(t1);
            } catch (IOException e) {
                e.getMessage();
            } catch (InterruptedException e) {
                e.getMessage();
            }
        });

        bucket2.forEach((t2) -> {
            try{
                threadTwo.run(t2);
            }catch (IOException e) {
                e.getMessage();
            } catch (InterruptedException e) {
                e.getMessage();
            }
        });
    }

    public void run(String url1) throws IOException, InterruptedException {


        URL url = new URL("http://"+hostnamePort+"/startCrawl?url="+url1);
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
            Thread.sleep(25000);

        }else if(conn.getResponseCode() == 200){

            System.out.println("Success returned a 200 Ok Response");
        }
    }

}
