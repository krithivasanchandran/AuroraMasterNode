package AmazonEC2.Cluster;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AmazonEc2SpinUp {

    private static final AWSCredentials credentials;

    private static final Map<String, InstanceDetails> createdInstanceCredentials = new ConcurrentHashMap<String, InstanceDetails>();

    static {
        // put your accesskey and secretkey here
        credentials = new BasicAWSCredentials(
                "",
                ""
        );
    }

    private static final String securityGroupName = "aurorachallenge";
    private static final String securityGroupDescription = "Coding hard for hackathon challenge";
    private static final String keypairname = "mysqlaurora";
    private String instanceId = "";

    /*
     * US_WEST_1("us-west-1", "US West (N. California)"),
     */

    private AmazonEC2 spinUp() {
        AmazonEC2 ec2Client = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_WEST_1)
                .build();

        return (ec2Client != null) ? ec2Client : null;
    }

    private CreateSecurityGroupResult createSecurityGroup(AmazonEC2 ec2Client) {

        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
                .withGroupName(securityGroupName)
                .withDescription(securityGroupDescription);

        if (spinUp() != null) {
            CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);
            return createSecurityGroupResult;
        } else {
            System.out.println("Error creating ec2 instance with the given credentials" + this.getClass().getName());
        }

        return null;
    }

    /*
     * Since security groups don’t allow any network traffic by default,
     * we’ll have to configure our security group to allow traffic.
     * Let’s allow HTTP traffic coming from any IP address: 8080
     * Open up Port 8080 for external traffic
     */
    private IpPermission allowHTTPTraffic() {

        IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
        IpPermission ipPermission = new IpPermission()
                .withIpv4Ranges(Arrays.asList(new IpRange[]{ipRange}))
                .withIpProtocol("tcp")
                .withFromPort(8080)
                .withToPort(8080);
        return ipPermission;
    }

    private void authorizeInBoundConnections(AmazonEC2 rootec2, IpPermission ipallow) {

        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest
                = new AuthorizeSecurityGroupIngressRequest()
                .withGroupName(securityGroupName)
                .withIpPermissions(ipallow);
        rootec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
    }

    private String createKeyPair(AmazonEC2 rootec2) {
        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest()
                .withKeyName(keypairname);
        CreateKeyPairResult createKeyPairResult = rootec2.createKeyPair(createKeyPairRequest);
        final String privateKey = createKeyPairResult
                .getKeyPair()
                .getKeyMaterial();

        DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
        DescribeKeyPairsResult describeKeyPairsResult = rootec2.describeKeyPairs(describeKeyPairsRequest);
        System.out.println(" Printing keypair results" + describeKeyPairsResult.toString());

        return privateKey;
    }

    /*
     * - Image Id: ami-976020ed , AMI Name - Alpine-3.7-r2-Hardened-EC2
     *
     * - Heart of the Ec2 Instance Creation -
     *
     * -
     */

    private AmazonEC2 launchinstance() throws UnsupportedEncodingException {

        AmazonEC2 rootec2 = spinUp();

        CreateSecurityGroupResult securitygroupres = createSecurityGroup(rootec2);
        IpPermission ipallowtraffic = allowHTTPTraffic();
        authorizeInBoundConnections(rootec2, ipallowtraffic);
        final String privkeypair = createKeyPair(rootec2);

        /*
         * UserData - Init Scripts - That Installs Java 8 and runs the java program with the following
         * memory optimized JVM arguments -
         * -XX:MaxGCPauseMillis=80 - For high availability - Reduces the GC Pauses to lesser value
         * –XX:GCTimeRatio=19 - 5% of the total time for GC and throughput goal of 95%
         * -XX:InitiatingOccupancyFraction - Estimates the Amount of Garbage heap
         * and start Garbage collection as late as possible to avoid issues.
         *
         * nohup – use for terminal output goes into the nohup.out file.
         */

        StringBuilder userDataBuilder = new StringBuilder();
        userDataBuilder.append("#!/bin/bash" + "\n");
        userDataBuilder.append("yum update -y" + "\n");
        userDataBuilder.append("yum install java-1.8.0 -y" + "\n");
        userDataBuilder.append("aws s3 cp s3://aurorachallenge/amazonaurora-1.0.jar --region=us-east-1a" + "\n");
        userDataBuilder.append("nohup java -Xms256m -Xmx850m -XX:MaxGCPauseMillis=80 –XX:GCTimeRatio=19" +
                " -XX:InitiatingOccupancyFraction -jar amazonaurora-1.0.jar" + "\n");
        byte[] userDataFinal = Base64.encodeBase64(userDataBuilder.toString().getBytes("UTF-8"));
        String userDataEncodedwithBase64 = new String(userDataFinal, "UTF-8");

        /*
         * Spins UP 120 Instances - Requires special Capacity Provisional Request.
         * For testing you could use 20 t2.micro instances in local.
         * Image Id: ami-976020ed , AMI Name - Alpine-3.7-r2-Hardened-EC2 - Lesser weight
         * If you are using different
         */
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId("ami-976020ed");
        runInstancesRequest.withInstanceType("t2.micro");
        runInstancesRequest.withKeyName(keypairname);
        runInstancesRequest.withMinCount(100);
        runInstancesRequest.withMaxCount(120);
        runInstancesRequest.withSecurityGroups(securityGroupName);
        runInstancesRequest.setUserData(userDataEncodedwithBase64);

        /*
         * Returns the List of instances to run.
         */
        List<Instance> listOfInstances = rootec2.runInstances(runInstancesRequest).getReservation().getInstances();

        /*
          Critical Block Spins up 100 Instances .

         */
        synchronized (this) {

            for (Instance instanceObject : listOfInstances) {

                final String instanceId = instanceObject.getInstanceId();

                final StartInstancesRequest startInstancesRequest = new StartInstancesRequest()
                        .withInstanceIds(instanceId);


                rootec2.startInstances(startInstancesRequest);

            }
        }

        System.out.println("Started the Instance !!!!");
        return rootec2;
    }

    /*
     * Monitor Instance
     */

    private void MonitorInstance(AmazonEC2 ec2Client, String yourInstanceId) {

        // Monitor Instances
        MonitorInstancesRequest monitorInstancesRequest = new MonitorInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.monitorInstances(monitorInstancesRequest);

    }

    /*
     * Unmonitor Instance Request
     */

    private void unMonitorInstance(AmazonEC2 ec2Client, String yourInstanceId) {
        UnmonitorInstancesRequest unmonitorInstancesRequest = new UnmonitorInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.unmonitorInstances(unmonitorInstancesRequest);
    }

    /*
     * Rebooting Instance
     */

    private void rebootInstance(AmazonEC2 ec2Client, String yourInstanceId) {
        RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.rebootInstances(rebootInstancesRequest);
    }

    /*
     * Stopping Instance
     */

    private void stopInstance(String yourInstanceId, AmazonEC2 ec2Client) {
        // Stop an Instance
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.stopInstances(stopInstancesRequest)
                .getStoppingInstances()
                .get(0)
                .getPreviousState()
                .getName();

    }

    /*
     * Describe Instance
     */

    private void describeInstance(AmazonEC2 ec2client) {

        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        DescribeInstancesResult response = ec2client.describeInstances(describeInstancesRequest);

        List<Instance> listOfInstances = response.getReservations().get(0).getInstances();

        for (Instance instance : listOfInstances) {

            if (instance.getState().getName().equalsIgnoreCase("pending") ||
                    instance.getState().getName().equalsIgnoreCase("0")) {

                System.out.println("These are the pending EC2 instances with pending state " +
                        "even after giving a grace time of 2 minutes" + instance.getInstanceId());

                continue;
            }

            if (instance.getState().getName().equalsIgnoreCase("running") ||
                    instance.getState().getName().equalsIgnoreCase("0")) {

                String instanceId = instance.getInstanceId();
                String publicdnsname = instance.getPublicDnsName();
                String publicipaddress = instance.getPublicIpAddress();


                System.out.println(" Created Instance Id is " + instanceId);
                System.out.println(" Public DNS name is " + publicdnsname);
                System.out.println(" Public IP adderss is " + publicipaddress);

                InstanceDetails iddetails = new InstanceDetails();
                iddetails.setPublicDnsname(publicdnsname);
                iddetails.setPublicIPaddress(publicipaddress);

                createdInstanceCredentials.put(instanceId, iddetails);
            }
        }
    }

    private static void createinstance() throws InterruptedException, UnsupportedEncodingException {

        AmazonEc2SpinUp spin = new AmazonEc2SpinUp();

        /*
         * Critical launches 100 micro instances
         * To change the number of instances to 20
         * navigate to this method and set runInstancesRequest.withMaxCount(20);
         */
        final AmazonEC2 rootec2 = spin.launchinstance();

        // Wait for 4 minutes for the instances to start and change its state
        // from pending to running state.
        Thread.sleep(240000);

        /*
         * Descibes the Instance ID , public DNS Name
         * and Public IP Address.
         */
        spin.describeInstance(rootec2);

        /*
         * Send the Instances for scheduled crawling.
         */
        final MasterCrawlController masterCrawler = new MasterCrawlController();
        masterCrawler.sendInstanceDetailsCrawl(createdInstanceCredentials);

        spin.shutdowninstance(rootec2, createdInstanceCredentials);
    }

    /*
     * Shutting down the EC2Instance.
     */

    public void shutdowninstance(final AmazonEC2 rootec2, Map<String, InstanceDetails> createdInstanceCredentials) {

        Set<Map.Entry<String, InstanceDetails>> entries = createdInstanceCredentials.entrySet();

        for (Map.Entry<String, InstanceDetails> k : entries) {
            final String instanceId = k.getKey();
            final InstanceDetails instanceDetails = k.getValue();

            StopInstancesRequest stopInstancesRequest = new StopInstancesRequest()
                    .withInstanceIds(instanceId);

           System.out.println("Instances with the following Instance ID has been shutdown " + instanceId);
        }

    }
}