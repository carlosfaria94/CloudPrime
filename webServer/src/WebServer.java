import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.io.*;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.util.List;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class WebServer {

	public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/f.html", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();    
    }

	
    static class MyHandler implements HttpHandler {
    	
    	//este metodo e quem trata dos requests 
        @Override
        public void handle(HttpExchange t) throws IOException {     
            //cada request recebido sera tratado por uma thread diferente, passamos como parametro, o request recebido
           Thread thread = new MyThread(t);
           thread.start();         
        }
    }
    
    static class MyThread extends Thread
    {
    	
    	private String numeroFatorizar;
    	HttpExchange t;
    	String query;

    	public MyThread(HttpExchange t)
    	{
    		this.t = t;
    		query = t.getRequestURI().getQuery(); //guardamos o que o user introduziu em string
    		if(query != null) //se a query for null entao e um ping do load balancer
			{
				String[] parts = query.split("="); //vamos buscar o numero que o user introduziu que fica guardado em [1]
				numeroFatorizar = parts[1];		
    		}
    	}
    	//codigo corrido quando fazemos start na thread
    	@Override
    	public void run()
    	{	
			if(query != null){
				IntFactorization factorizator = new IntFactorization();	
				String response = factorizator.calculateFactor(numeroFatorizar); //calculamos a fatorizacao		
				responseToUser(response); //enviamos a resposta de volta para o load balancer
			}else responseToUser("");
    	}
    	
    	public void responseToUser(String response) 
    	{
    	   try 
           {
    		 t.sendResponseHeaders(200, response.length());
    		 OutputStream os = t.getResponseBody();
             os.write(response.getBytes());
             os.close();
			} catch (IOException e) {}	
    	}	
    }
}
