# AuroraMasterNode
Aurora Challenge - Master Node Web Crawler

# Master - Submits the URLS to the Slave Crawlers - either runs locally or runs in 120 t2.micro instances across AWS.

How to run:
Make sure you have java 1.8 and maven installed. 

Master Web Crawler –
•	Git clone https://github.com/krithivasanchandran/AuroraMasterNode.git
•	Please give the path of 20,000 URLS here - https://github.com/krithivasanchandran/AuroraMasterNode/blob/master/src/main/java/UrlLoader/LoadUrls.java#L33
•	You can download the 20,000 URLS from https://s3.amazonaws.com/aurorachallenge/20000websites.xlsx. 
•	mvn clean install
•	In IDE – MasterNode.java – Right Click Run as Java application or in command line navigate to the AuroraMasterNode/ and run java MasterNode.java.


Make sure the Master and Slave are in different windows of IDE as they are 2 different threads or process in a java application.

#Running in Scalable Mode – AWS Cloud: 

I prefer running the Master Web Crawler in the local as it has got the AWS deploy scripts. What’s required: 
•	AWS Access Keys – Define it here:  https://github.com/krithivasanchandran/AuroraMasterNode/blob/master/src/main/java/AmazonEC2/Cluster/AmazonEc2SpinUp.java#L34
•	Amazon MySQL Aurora Instance – SpinUp – db.t2.large instance with writer enabled.

Make sure you give these details: 

•	db instance identifier - crawldb

•	master username: krithivasan
•	master password: Springboot1234

•	availability zone- us-east-1a

•	dbcluster identifier - crawldb

•	tcp ip port : 3306

It spins up the DB cluster with - crawldb.c9nylpylkekg.us-east-1.rds.amazonaws.com and port as 3306. 

Run the Table Scripts: https://github.com/krithivasanchandran/amazonaurora/blob/master/MySQLDDL - 2 Tables - HomePage and ChildPage.

# Make sure you add the security group name ==> aurorachallenge to Amazon MyRDS ==> to accept incoming traffic (inbound) from t2.micro instances.

In the IDE or Java console : 
Enter option 2 to deploy to the AWS instances. 

You can see your crawled results in the MYSQL databases. The crawler gracefully shuts-down after the crawl is completed and terminates the VM instances. 


Note: 
#	Do contact me at c.krithivasan@gmail.com – if you are stuck with the setup. 
#	Slave crawlers can be deployed across 1000 t2 micro instances and more – make sure your master web crawler has 6.542 = 7 GB java heap space to load all the 1 million url’s.
#	It can be used as a load and testing tool for your aurora mysql product. You can test it for very high parallel writes. 
