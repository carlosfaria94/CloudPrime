import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class AutoScaling 
{
	//Demora em media 2.30 minutos para comecar uma instancia, entao para o grace period iremos dar um valor de 3 minutos
	//para cobrir casos em que possa demorar mais tempo a comecar uma instancia
	public final static long GRACE_PERIOD = 180000; //3minutos = 180000ms
	
	public final static int LOW_CPU_MAX_COUNTS = 11;
	public final static long TIMEOUT_BETWEEN_CPU_CHECKS = 60000;
	public final static double MAX_CPU_USAGE = 75;
	public final static double MIN_CPU_USAGE = 20;

	public static InstanceObject startInstance(AmazonEC2 ec2)
	{
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.withImageId("ami-f6ed6785")
		.withMonitoring(true)
        .withInstanceType("t2.micro")
        .withMinCount(1)
        .withMaxCount(1)
        .withKeyName("CNV-lab-AWS")
        .withSecurityGroups("CNV-ssh+http");
		RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		DescribeInstancesResult describeInstancesResult  = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesResult.getReservations();
		ArrayList<Instance> auxListOfInstances = new ArrayList<Instance>();
        for (Reservation reservation : reservations) {
        	auxListOfInstances.addAll(reservation.getInstances());
        }
        
        InstanceObject instanceObject = null;
        for (Instance instance : auxListOfInstances) {
        		if(instance.getInstanceId().equals(runInstancesResult.getReservation().getInstances().get(0).getInstanceId())) //id do load balancer, nao adicionamos este as instancias 
        			instanceObject = new InstanceObject(instance);      		
        }
		
        //retornamos a instance criada
        return instanceObject;
	}
	
   public static GetMetricStatisticsRequest createGetMetricStatisticsRequest(String namespace, String metricName, String statisticsType, List<Dimension> dimensions)
   {             
	  GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest() 
	    .withStartTime(DateTime.now(DateTimeZone.UTC).minusMinutes(1).toDate()).withNamespace(namespace).withDimensions(dimensions) 
	    .withPeriod(60).withMetricName(metricName).withStatistics(statisticsType) 
	    .withEndTime(DateTime.now(DateTimeZone.UTC).minusMinutes(0).toDate()); 
	  return getMetricStatisticsRequest; 
   }
   
   public static InstanceObject healthCheck(InstanceObject instance, boolean needsGracePeriod)
   {
	 //se uma instancia acabou de ser criada, o needsGracePeriod vem a true
   	//isto e necessario pois temos que dar tempo para a instancia arrancar antes de comecar a efetuar os health checks
   	if(needsGracePeriod)
   	{
   		try {
				Thread.sleep(AutoScaling.GRACE_PERIOD);
			} catch (InterruptedException e) {}
   	}
   	int unHealthyThreshold = 0;
	   while(true)
	   {
		   try {
			   //vemos se a instancia esta viva ou nao, durante 2 segundos
			   try (Socket socket = new Socket()) {
			        socket.connect(new InetSocketAddress(instance.getInstance().getPublicIpAddress(),22), 3000);
			    	unHealthyThreshold = 0; //se a instancia responder, fazemos reset no unhealthyThreshold*/
			    } catch (IOException e) {
			    	unHealthyThreshold++;
			    }
			   //System.out.println("instance " + instance.getInstance().getInstanceId() + " unhealthy threshold " + unHealthyThreshold);

			   if(unHealthyThreshold == 3) //se chegarmos a 3 unHealthyThresholds entao damos a instancia como crashed
				   return instance;

			   Thread.sleep(30000); //intervalo entre cada health check
		} catch (InterruptedException e) {}
	   }
   }
   
   public static void removeInstance(InstanceObject instance,AmazonEC2 ec2 )
   {
	   TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
       termInstanceReq.withInstanceIds(instance.getInstance().getInstanceId());
       ec2.terminateInstances(termInstanceReq);
   }
   
   public static boolean checkInstanceState(Instance instance)
   {
	   String state = instance.getState().getName();
	   if (state.equals("running")) 
       { 
           	return true;
       }
       else 
       {
           return false;
       }
   }
   
   public static void setupChecksNewInstance(InstanceObject newInstance)
   {
	   //como uma instancia foi criada, lancamos novas Threads para voltar a analisar a utilizacao de CPU
		//criamos uma que e necessario grace period (dai o true) porque a instancia primeiro tem que ser inicializada
		//antes de comecarmos a analisar a utilizacao de cpu
		Thread thread = new LoadBalancer.ThreadScalingIndividual(newInstance,true, GRACE_PERIOD);
	    thread.start();
	    
	    //precisamos tambem de iniciar a monitorizacao atraves de health checks a esta nova instancia
	    //aplicando tambem o grace period
	    Thread thread1 = new LoadBalancer.ThreadHealthCheckIndividual(newInstance, true);
	    thread1.start();
   }
   
 //metodo responsavel por averiguar se compensa criar instancia ou nao, devolvendo true para criar instancia, devolvendo false em caso oposto
   public static boolean createInstanceOrNot(ArrayList<InstanceObject> listOfActiveInstances)
   {
	   int underUtilizedInstances = 0;
	   
	   for(InstanceObject instance : listOfActiveInstances)
	   {
		   if(instance.getCurrentCPU_Usage() <= MIN_CPU_USAGE)
			   underUtilizedInstances++;
	   }
	   
	   //so permitimos que seja criada uma nova instancia se nao houver nenhuma com baixa utilizacao ou uma
	   if(underUtilizedInstances < 2)
	   {
		   System.out.println("It compensates to create a new instance");
		   return true;
	   }
	   else
	   {
		   System.out.println("It doesnt compensate to create a new instance");
		   return false;
	   }
   }

   /* Algoritmo:
    *  Primeiro verificamos se ha alguma instancia com utilizacao elevada de CPU se nao existir entao podemos remover esta instancia
    *  Se houver uma instancia com elevada utilizacao de CPU entao pode nao compensar remover esta instancia fazemos entao uma segunda verificacao
    *  Vemos se existe alguma instancia com baixa utilizacao, se houver, entao podemos remover esta instancia caso contrario nao compensa remover esta instancia
    */
   public static boolean removeInstanceOrNot(ArrayList<InstanceObject> listOfActiveInstances, InstanceObject instanceToBeRemoved)
   {
	   int underUtilizedInstances = 0;
	   int overUtilizedInstances = 0;
	   
	   for(InstanceObject instance : listOfActiveInstances)
	   {
		   if((instance.getInstance().getInstanceId().equals(instanceToBeRemoved.getInstance().getInstanceId()))) //se for a propria nao conta
			   continue;
		   		   
		   if(instance.getCurrentCPU_Usage() <= MIN_CPU_USAGE )
			   underUtilizedInstances++;
		   else if(instance.getCurrentCPU_Usage() >= MAX_CPU_USAGE)
			   overUtilizedInstances++;
	   }
	   
	   if(overUtilizedInstances == 0)
	   {
		   System.out.println("It compensates to remove the instance");
		   return true;
	   }
	   else
	   {
		   if(underUtilizedInstances > 0)
		   {
			   System.out.println("It compensates to remove the instance");
			   return true;
		   }
		   else
		   {
			   System.out.println("It doesnt compensate to remove the instance");
			   return false;
		   }
	   }
   }
   
   //metodo responsavel por transferir pedidos de uma maquina que crashou ou que ira ser removida por baixa utilizacao de CPU
   public static void transferRequests(InstanceObject instanceTerminated)
   {
	   ExecutorService service = Executors.newCachedThreadPool();

	   System.out.println("Transfering requests from faulty instance");
	   
	   for(Request request : instanceTerminated.getRequestsBeingProcessed())
	   {
		   //temos que envia-los para o scheduler que ira decidir para onde reencaminhar os pedidos
		   CompletableFuture.runAsync(()->LoadBalancer.scheduler(request.getRequest(), request.getParameter(), request.getRank()),service);
	   }
   }
}
