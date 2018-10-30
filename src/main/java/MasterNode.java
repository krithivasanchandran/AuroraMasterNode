import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

public class MasterNode extends Thread{

    private static final long MEGABYTE = 1024L * 1024L;
    private static Set<String> bucket1 = new HashSet<String>();
    private static Set<String> bucket2 = new HashSet<String>();

    public static void main(String[] args) throws IOException {

        File excelFile = new File("C:\\Users\\Dell\\Documents\\MasterWebCrawler\\20000websites.xlsx");
        FileInputStream fis = new FileInputStream(excelFile);

        // we create an XSSF Workbook object for our XLSX Excel File
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        // we get first sheet
        XSSFSheet sheet = workbook.getSheetAt(0);

        // we iterate on rows
        Iterator<Row> rowIt = sheet.iterator();

        int rowcounter = 0;

        while(rowIt.hasNext()) {
            Row row = rowIt.next();

            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                System.out.print(cell.toString());
                if(rowcounter % 2 == 0){
                    bucket1.add(cell.toString());
                }else{
                    bucket2.add(cell.toString());
                }
                break;
            }

            rowcounter++;

            System.out.println();
        }

        workbook.close();
        fis.close();

        final Runtime runtime = Runtime.getRuntime();

        long memory = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is megabytes: "+ bytesToMegabytes(memory));

        MasterNode master = new MasterNode();
        MasterNode master2 = new MasterNode();

        CloseableHttpResponse response=null;
        try{
            CloseableHttpClient httpclient = HttpClients.createSystem();

            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(180000)
                    .setConnectTimeout(180000)
                    .setConnectionRequestTimeout(180000)
                    .setCircularRedirectsAllowed(false)
                    .setRedirectsEnabled(false)
                    .build();

            bucket1.forEach((t1) -> {
                try {
                    master.run(t1);
                } catch (IOException e) {
                    e.getMessage();
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            });

            bucket2.forEach((t2) -> {
                try{
                    master2.run(t2);
                }catch (IOException e) {
                    e.getMessage();
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            });

        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }


    }

    public void run(String url1) throws IOException, InterruptedException {

        URL url = new URL("http://localhost:8080/startCrawl?url="+url1);//your url i.e fetch data from .
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == 412) {

            System.out.println("Failed : HTTP Error code : "
                    + conn.getResponseCode());
            Thread.sleep(25000);

        }else if(conn.getResponseCode() == 200){

            System.out.println("Success returned a 200 Ok Response");
        }


    }

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }



}
