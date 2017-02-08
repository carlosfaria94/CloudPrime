import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.sun.net.httpserver.HttpServer;

public class Connection {
	
	public static void setupServer() throws IOException
	{		
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    	server.createContext("/f.html", new LoadBalancer.MyHandler());
    	server.setExecutor(null); // creates a default executor
    	server.start();
    	System.out.println("Load Balancer ready");
	}
	
	public static String checkInstanceId() {
		URL url;
		String instanceId = "";
		try {
			url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
			URLConnection conn = url.openConnection();
			Scanner s = new Scanner(conn.getInputStream());
			if (s.hasNext()) {
				instanceId = s.next();
			} else {
				s.close();
				return "no answer";
			}
			s.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}	
		return instanceId;
	}
	
	public static String getOwnIP()
	{
		AWSCredentials credentials = null;
		try
		{
			credentials = new ProfileCredentialsProvider().getCredentials();
		}
		catch(Exception e){}
		
		AmazonEC2 ec2 = new AmazonEC2Client(credentials);		

		ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
		DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesResult.getReservations();
		ArrayList<Instance> listOfInstances = new ArrayList<Instance>();
		for(Reservation reservation : reservations)
			listOfInstances.addAll(reservation.getInstances());
		
		String ownIP = null;
		String ownInstanceID = checkInstanceId();
		for(Instance instance: listOfInstances)
		{
			if(instance.getInstanceId().equals(ownInstanceID))
				ownIP = instance.getPublicIpAddress();
		}
		
		return ownIP;
	}
}
