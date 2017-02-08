import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

public class Request {
	//classe que carateriza um pedido

		private HttpExchange request; //contem o request do cliente
		private BigInteger rank;
		private String parameter;
		
		public Request(HttpExchange request, BigInteger rank, String parameter)
		{
			this.request = request;
			this.rank = rank;
			this.parameter = parameter;
		}
		
		public String getParameter()
		{
			return parameter;
		}
		public BigInteger getRank()
		{
			return rank;
		}

		public HttpExchange getRequest() {
			return request;
		}

		public void setRequest(HttpExchange request) {
			this.request = request;
		}
		
		public static String calculateParameters(HttpExchange t)
		{
			String numeroFatorizar = "";
    		String query = t.getRequestURI().getQuery(); //guardamos o que o user introduziu em string
    		
    		if(query != null) //se a query for null entao e um ping do load balancer
    		{
    			String[] parts = query.split("="); //vamos buscar o numero que o user introduziu que fica guardado em [1]
    			numeroFatorizar = parts[1];		
    		}
    		
    		return numeroFatorizar;
		}
		
		public static HashMap<String, BigInteger> getRanksFromPersistentMemory()
		{
			HashMap<String, BigInteger> historyOfRanks = new HashMap<String, BigInteger>();
			
			try {
				FileInputStream logFile = new FileInputStream("logRanks.txt");
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(logFile));
				
				String line;

				while ((line = bufReader.readLine()) != null) 
				{
					String[] parts = line.split(":"); //[0] contem o parametro, [1] o rank
					historyOfRanks.put(parts[0], new BigInteger(parts[1]));
				}

			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {}
			return historyOfRanks;
		}
		
		public static void writeHistory(String parameter, String rank)
		{
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("logRanks.txt", true)));
				out.println(parameter + ":" + rank); 
				out.close();
			} catch (IOException e) {}
		}			
}
