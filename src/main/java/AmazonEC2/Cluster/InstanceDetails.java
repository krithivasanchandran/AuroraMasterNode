package AmazonEC2.Cluster;

public class InstanceDetails {

    private String publicDnsname;
    private String publicIPaddress;

    public String getPublicDnsname() {
        return publicDnsname;
    }

    public void setPublicDnsname(String publicDnsname) {
        this.publicDnsname = publicDnsname;
    }

    public String getPublicIPaddress() {
        return publicIPaddress;
    }

    public void setPublicIPaddress(String publicIPaddress) {
        this.publicIPaddress = publicIPaddress;
    }

}
