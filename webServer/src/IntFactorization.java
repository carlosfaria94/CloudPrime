import java.math.BigInteger;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.*;


public class IntFactorization 
{	
	private BigInteger zero = new BigInteger("0");
	private BigInteger one = new BigInteger("1");
	private BigInteger divisor = new BigInteger("2");
	private ArrayList<BigInteger> factors = new ArrayList<BigInteger>();

	public String calculateFactor(String numeroFatorizar)
	{
	    int i = 0;

		try
		{ 		
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("logIntermedio.txt", true)));
			out.println("Thread " + Thread.currentThread().getId() + ": Parameter=" + numeroFatorizar); 
			out.close();
		}catch(Exception e)	{}
		
	    ArrayList<BigInteger> factors = calcPrimeFactors(new BigInteger(numeroFatorizar));
	    //resposta a enviar de volta ao utilizador
   	    String response = "The prime factors of " + numeroFatorizar + " are ";     
	    	    
	    for (BigInteger bi: factors) 
	    {
	      i++;
	      response += bi.toString();
	      
	      if (i == factors.size())
	      {
	        response += ".";
	      } 
	      else 
	      {
	        response += ", ";
	        
	      }
	    }
	    return response; //enviamos a resposta de volta ao utilizador
	 }
	
	//metodo de fatorizacao
	public ArrayList<BigInteger> calcPrimeFactors(BigInteger num) 
	{		 		 	
	    if (num.compareTo(one)==0) {
	      return factors;
	    }
	    while(num.remainder(divisor).compareTo(zero)!=0) {
	      divisor = divisor.add(one);
	    }
	    factors.add(divisor);
	    return calcPrimeFactors(num.divide(divisor));
	 }	
}
