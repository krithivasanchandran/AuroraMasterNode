package AmazonEC2.Cluster;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AmazonEc2SpinUp {

    private static final AWSCredentials credentials;

    private static final Map<String,InstanceDetails> createdInstanceCredentials = new ConcurrentHashMap<String,InstanceDetails>();

    static {
        // put your accesskey and secretkey here
        credentials = new BasicAWSCredentials(
                "AKIAI6H7CTPVGIVJBMVA",
                "U/lm/bD2TcOkv26aJfwJ77SBxBMK0yyr3jTD60oX"
        );
    }

    private static final String securityGroupName = "challenge";
    private static final String securityGroupDescription = "Coding hard for hackathon challenge";
    private static final String keypairname = "mysqlaurora";
    private String instanceId="";

    private AmazonEC2 spinUp(){
        AmazonEC2 ec2Client = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        return (ec2Client != null) ? ec2Client : null;
    }

    private CreateSecurityGroupResult createSecurityGroup(AmazonEC2 ec2Client){

        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
                .withGroupName(securityGroupName)
                .withDescription(securityGroupDescription);

        if(spinUp() != null){
            CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);
            return createSecurityGroupResult;
        }else{
            System.out.println("Error creating ec2 instance with the given credentials" + this.getClass().getName());
        }

        return null;
    }

    /*
     * Since security groups don’t allow any network traffic by default,
     * we’ll have to configure our security group to allow traffic.
     * Let’s allow HTTP traffic coming from any IP address:
     */
    private IpPermission allowHTTPTraffic(){

        IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
        IpPermission ipPermission = new IpPermission()
                .withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange }))
                .withIpProtocol("tcp")
                .withFromPort(80)
                .withToPort(80);
        return ipPermission;
    }

    private void authorizeInBoundConnections(AmazonEC2 rootec2,IpPermission ipallow){

        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest
                = new AuthorizeSecurityGroupIngressRequest()
                .withGroupName(securityGroupName)
                .withIpPermissions(ipallow);
        rootec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
    }

    private String createKeyPair(AmazonEC2 rootec2){
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
     */

    private AmazonEC2 launchinstance(){

        AmazonEC2 rootec2 = spinUp();

        CreateSecurityGroupResult securitygroupres  = createSecurityGroup(rootec2);
        IpPermission ipallowtraffic = allowHTTPTraffic();
        authorizeInBoundConnections(rootec2,ipallowtraffic);
        final String privkeypair = createKeyPair(rootec2);

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId("ami-976020ed")
                .withInstanceType("t2.micro")
                .withKeyName(keypairname)
                .withMinCount(1)
                .withMaxCount(1)
                .withSecurityGroups(securityGroupName)
                .setUserData(https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html);

        instanceId = rootec2.runInstances(runInstancesRequest).getReservation().getInstances().get(0).getInstanceId();

        System.out.println("Created Instance Id is " + instanceId);

        StartInstancesRequest startInstancesRequest = new StartInstancesRequest()
                .withInstanceIds(instanceId);

        rootec2.startInstances(startInstancesRequest);

        System.out.println("Started the Instance !!!!");
        return rootec2;
    }

    /*
     * Monitor Instance
     */

    private void MonitorInstance(AmazonEC2 ec2Client,String yourInstanceId){

        // Monitor Instances
        MonitorInstancesRequest monitorInstancesRequest = new MonitorInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.monitorInstances(monitorInstancesRequest);

    }

    /*
     * Unmonitor Instance Request
     */

    private void unMonitorInstance(AmazonEC2 ec2Client,String yourInstanceId){
        UnmonitorInstancesRequest unmonitorInstancesRequest = new UnmonitorInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.unmonitorInstances(unmonitorInstancesRequest);
    }

    /*
     * Rebooting Instance
     */

    private void rebootInstance(AmazonEC2 ec2Client,String yourInstanceId){
        RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest()
                .withInstanceIds(yourInstanceId);

        ec2Client.rebootInstances(rebootInstancesRequest);
    }

    /*
     * Stopping Instance
     */

    private void stopInstance(String yourInstanceId,AmazonEC2 ec2Client){
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

    private void describeInstance(AmazonEC2 ec2client){

        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        DescribeInstancesResult response = ec2client.describeInstances(describeInstancesRequest);

        List<Instance> listOfInstances = response.getReservations().get(0).getInstances();

        for(Instance instance : listOfInstances){
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

    private static void createinstance() throws InterruptedException {
        AmazonEc2SpinUp spin = new AmazonEc2SpinUp();
        AmazonEC2 rootec2 = spin.launchinstance();

        spin.describeInstance(rootec2);

    }

}
