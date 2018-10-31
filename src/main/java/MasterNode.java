import UrlLoader.LoadUrls;

import java.io.IOException;
import java.util.Scanner;

public class MasterNode{

    public static void main(String args[]) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the Slave Hostname along with Port , example: 192.21.12.12:8080");

        String hostName = scanner.nextLine();

            LoadUrls loadFile = new LoadUrls();
            loadFile.SetHostname(hostName);
            loadFile.readFile();
            loadFile.createThread();

    }

}
