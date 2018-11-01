import AmazonEC2.Cluster.AmazonEc2SpinUp;
import UrlLoader.LoadUrls;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;

public class MasterNode{

    public static void main(String args[]) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println(" You can run this Application in two Modes - Local Setup or AWS Cloud Setup");
        System.out.println("                          ");
        System.out.println(" Press 1. Local Setup " + "\n");
        System.out.println(" Press 2. AWS Cloud Setup " + "\n");

        int choice = scanner.nextInt();

        System.out.println((choice == 1) ? " Local Setup You have chosed " : " AWS Cloud Setup you have chosen");

        switch (choice){
            case 1:
                System.out.println("Make sure the Slave Application is Up and running");
                System.out.println("Enter the only the hostname:port : example : localhost:8080 ");
                scanner.nextLine();
                String hostname = scanner.nextLine();
                LoadUrls loadFile = new LoadUrls();
                loadFile.SetHostname(hostname);
                Queue<String> urlQueue = loadFile.readFile();
                loadFile.fireThreads(urlQueue);

            case 2:
                System.out.println("You have chosen to Run 120 micro instances in the AWS ");
                System.out.println("Enter Y/N to continue");
                scanner.nextLine();
                String awschoice = scanner.nextLine();

                if(awschoice.equalsIgnoreCase("Y")){
                    System.out.println("You have pressed yes hence creating the instances");
                    final AmazonEc2SpinUp ec2CriticalSpinUp = new AmazonEc2SpinUp();
                    ec2CriticalSpinUp.launchinstance();
                }else{
                    System.out.println("You have pressed No hence exiting the application");
                    System.out.println(" Thinking of rerunning the application in local please rerun this applications");
                    scanner.close();
                    System.exit(0);
                }

            default:
                System.out.println("You havent selected either of those 1 or 2 choices");
                System.out.println(" Thinking of rerunning the application in local please rerun this applications");
                scanner.close();
                System.exit(0);

        }

    }

}
