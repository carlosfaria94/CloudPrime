
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Scanner;

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
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoadBalancer{

	protected LoadBalancer() throws RemoteException {}

	//o MAX_RANK sera' utilizado para filtrar pedidos grandes. Quando um pedido tem um Rank igual ou superior ao MAX_RANK
	//entao so' deixamos um desses pedidos por instancia
	public final static BigInteger MAX_RANK = new BigInteger("1272110814");

	/* O INTERMEDIATE_RANK sera' utilizado para filtrar ranks intermedios, ou seja que nao sejam pedidos curtos nem demasiado longos
	 * Pedidos com rank menor que o INTERMEDIATE_RANK serao considerados pedidos pequenos e poderemos envia-los para instancias
	 * em que estejam a ser tratados pedidos com rank >= MAX_RANK. Ja pedidos com rank => INTERMEDIATE_RANK apenas poderemos enviar
	 * x pedidos para uma maquina a tratar pedidos com rank >= IMTERMEDIATE_RANK e < MAX_RANK */
	public final static BigInteger INTERMEDIATE_RANK = new BigInteger("161751155");
	public final static int LIMIT_INTERMEDIATE_REQUESTS = 20;
	public final static int LIMIT_MAX_REQUESTS = 10;
	public final static int MAX_PENDING_LIST = 20; //se houver que MAX_PENDING_LIST 'a espera entao criamos uma instancia extra
	private static int pendingList = 0;
	
    static AmazonEC2 ec2;
	static AmazonDynamoDBClient  dynamoDB;
    static AmazonCloudWatchClient cloudWatch;
    public static Dimension instanceDimension;
    public static DescribeInstancesResult describeInstancesResult;
    public static ArrayList<InstanceObject> listOfActiveInstances;
	private static HashMap<String, BigInteger> internalStorage;
    public static List<Reservation> reservations;
    static AWSCredentials credentials = null;
    
    //variavel de controlo que permite que so' uma instancia esteja a ser criada de cada vez
    private static boolean instanceBeingCreated = false;
    
    private static Lock lockCreateInstances = new ReentrantLock(); //lock utilizado para controlar os acessos 'a variavel de controlo para criar instancias
    private static Lock lockListOfActiveInstances = new ReentrantLock(); //lock utilizado para controlar as atualizacoes do listOfActiveInstances
    private static Lock lockScheduling = new ReentrantLock();
    private static Lock lockPendingRequests = new ReentrantLock();
    
    public static void main(String[] args) throws Exception
    {        
    	init();
    }

    static class ThreadScaling extends Thread
    {
    	public ThreadScaling(){}

    	@Override
    	public void run()
    	{
    		checkCPU_Utilization();
    	}

    	public void checkCPU_Utilization()
    	{
    		//verificacao da cpu utilization de cada instancia sera feita assincronamente:
			lockListOfActiveInstances.lock();
			for(InstanceObject instance : listOfActiveInstances) //analisamos todas as instancias atualmente ativas
			{
				Thread thread = new ThreadScalingIndividual(instance, false, 0);
    			thread.start();
			}
			lockListOfActiveInstances.unlock();
		}
    }

    //metodo que sera o callback das funcoes assincronas responsaveis por verificar a utilizacao de cpu
    public static void scalingDecision(InstanceObject instanceDecision)
    {
		ExecutorService service = Executors.newCachedThreadPool();
		
		/*temos que ver se vale mesmo a pena remover uma instancia ou nao porque de momento podemos ter uma instancia com baixa utilizacao de CPU
		 * e querermos criar uma nova instancia e uma instancia estar em vias de ser terminada
		 * temos que impedir que essa seja terminada pois iriamos terminar uma para comecar outra o que nao permite uma boa eficiencia do sistema (removeInstanceOrNot)
		 */	
		//so removemos a instancia se realmente compensar, o que e' verificado por removeInstanceOrNot
		lockListOfActiveInstances.lock();
		if(listOfActiveInstances.contains(instanceDecision)) //Fazemos esta verificacao porque a instancia pode ter falhado um health check entretanto, para nao removermos/transferir-mos duas vezes
		{
			if(AutoScaling.removeInstanceOrNot(listOfActiveInstances, instanceDecision))
			{
				listOfActiveInstances.remove(instanceDecision);  //tiramos a instancia da lista de activas
				System.out.println("Removing instance " + instanceDecision.getInstance().getInstanceId() + " due to low CPU usage");
				lockListOfActiveInstances.unlock();
				CompletableFuture.runAsync(()->AutoScaling.removeInstance(instanceDecision, ec2),service);
				CompletableFuture.runAsync(()->AutoScaling.transferRequests(instanceDecision),service);
			}
			else
			{
				lockListOfActiveInstances.unlock();
				//se a instancia nao for removida, temos que voltar a fazer os checks de utilizacao de CPU desta instancia
				Thread thread2 = new LoadBalancer.ThreadScalingIndividual(instanceDecision, false, 0);
			    thread2.start();
			}			
		}
    }

	static class ThreadHealthChecks extends Thread
	{
		@Override
    	public void run()
    	{
			//health checks de cada instancia sera feita assincronamente:
    		lockListOfActiveInstances.lock();
			for(InstanceObject instance : listOfActiveInstances) //analisamos todas as instancias atualmente ativas
			{
				Thread thread = new ThreadHealthCheckIndividual(instance,false);
				thread.start();
			}
			lockListOfActiveInstances.unlock();
    	}
	}

	public static void crashedInstance(InstanceObject instanceCrashed)
	{
		try {
			ExecutorService service = Executors.newCachedThreadPool();
			lockListOfActiveInstances.lock();
			if(listOfActiveInstances.contains(instanceCrashed)) //Fazemos esta verificacao porque a instancia pode ter sido terminada por low CPU , para nao removermos/transferir-mos duas vezes
			{
				listOfActiveInstances.remove(instanceCrashed); //tiramos a instancia da lista de activas
				System.out.println("Instancia missed all health checks");
	
				//caso seja um falso positivo, terminamos a instancia a mesma pois assim evitamos ficar com instancia a mais pois iremos comecar outra instancia para compensar a falha desta
				CompletableFuture.runAsync(() -> AutoScaling.removeInstance(instanceCrashed,ec2), service);			
				CompletableFuture.runAsync(()->AutoScaling.transferRequests(instanceCrashed),service);
				
				/*criamos uma instancia nova para compensar a falha da anterior
				 * 
				 * temos que ver se vale mesmo a pena criar uma nova instancia ou nao
				 * porque de momento podemos ter uma instancia com baixa utilizacao de CPU portanto nao compensa comecar outra nova
				 *
				 * O que vamos fazer e' entao o seguinte:
				 * Imaginando o caso em que temos uma instancia a 90% e outra a 20%, permitimos que seja criada mais uma pois a segunda
				 * instancia pode derepente ficar flooded com pedidos (pois a primeira nao os pode atender) mas nao permitimos que seja criado mais do que uma,
				 * ou seja, o maximo de instancias que teremos com baixa utilizacao quando temos uma com muita utilizacao e' 2.
				 */
	
				//so criamos a instancia se realmente compensar, o que e' verificado por createInstanceOrNot, se retonar true entao compensa		 
				if(AutoScaling.createInstanceOrNot(listOfActiveInstances))
					listOfActiveInstances.add(AutoScaling.startInstance(ec2));
			}

		} catch (Exception e) {}
		lockListOfActiveInstances.unlock();	
	}

	static class ThreadHealthCheckIndividual extends Thread
	{
		InstanceObject instanceBeingChecked;
		boolean needsGracePeriod;

		public ThreadHealthCheckIndividual(InstanceObject instance, boolean needsGracePeriod)
		{
			this.needsGracePeriod = needsGracePeriod;
			instanceBeingChecked = instance;
		}

		@Override
		public void run()
		{
			crashedInstance(AutoScaling.healthCheck(instanceBeingChecked, needsGracePeriod));
		}
	}

    private static void init() throws Exception
    {
    	//vamos buscar um historico de metricas guardados na memoria persistente do Load Balancer e guardamos na memoria interna para acesso mais rapido
    	internalStorage = Request.getRanksFromPersistentMemory();
    	if(internalStorage == null)
    		internalStorage = new HashMap<String,BigInteger>();
    	
    	Connection.setupServer();
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = new AmazonEC2Client(credentials);
        cloudWatch= new AmazonCloudWatchClient(credentials);

        try {
            /* Using AWS Ireland. Pick the zone where you have AMI, key and secgroup */
            ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
            cloudWatch.setEndpoint("monitoring.eu-west-1.amazonaws.com");

            describeInstancesResult = ec2.describeInstances();
            reservations = describeInstancesResult.getReservations();
            listOfActiveInstances = new ArrayList<InstanceObject>();

    		//funcionamento:vamos buscar todas as instancias e guardamos numa lista auxiliar, depois metemos na lista definitiva
    		//apenas as instancias que estao a correr

    		ArrayList<Instance> auxListOfInstances = new ArrayList<Instance>();
            for (Reservation reservation : reservations) {
            	auxListOfInstances.addAll(reservation.getInstances());
            }
    		lockListOfActiveInstances.lock();
            for (Instance instance : auxListOfInstances) {
            	if(AutoScaling.checkInstanceState(instance)) //se a instancia estiver running, adicionamo-la
            	{
            		if(!instance.getInstanceId().equals(Connection.checkInstanceId())) //id do load balancer, nao adicionamos este as instancias
            		{
            			InstanceObject instanceObject = new InstanceObject(instance);
            			listOfActiveInstances.add(instanceObject);
            		}
            	}
            }
    		lockListOfActiveInstances.unlock();

            instanceDimension = new Dimension();
            instanceDimension.setName("InstanceId");
            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(instanceDimension);

        } catch (AmazonServiceException ase) {    		
        		lockListOfActiveInstances.unlock();
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }

        Thread thread = new ThreadScaling();
        thread.start(); //thread responsavel pela monitorizacao das instancias (Fara o papel de Auto Scaling)

        //vamos ter outra thread que e responsavel por fazer os health checks das instancias iniciais
        Thread threadHealth = new ThreadHealthChecks();
        threadHealth.start();
    }

    static class ThreadScalingIndividual extends Thread
    {
    	InstanceObject instance; //instancia a ser analisada
    	boolean needsGracePeriod;
    	long periodToSleep;

    	public ThreadScalingIndividual(InstanceObject instance, boolean needsGracePeriod, long periodToSleep)
    	{
    		this.instance = instance;
    		this.needsGracePeriod = needsGracePeriod;
    		this.periodToSleep = periodToSleep;
    	}

    	@Override
    	public void run()
    	{
    		scalingDecision(needsNewInstance(instance, needsGracePeriod, periodToSleep));
    	}
    }

    public static InstanceObject needsNewInstance(InstanceObject instance, boolean needsGracePeriod, long periodToSleep)
	{
    	//se uma instancia acabou de ser criada, o needsGracePeriod vem a true
    	//isto e necessario pois temos que dar tempo para a instancia arrancar antes de comecar a verificar a utilizacao de cpu
    	if(needsGracePeriod)
    	{
    		try {
				Thread.sleep(periodToSleep);
			} catch (InterruptedException e) {}
    	}
    	/*
    	 * Isto sera um contador que se tiver a 11 indicara' que devemos remover esta instancia devido a low cpu usage.
    	 * Este contador corresponde a checks ate 10 minutos, para evitar
    	 * casos em que uma instancia e' inicializada e depois logo terminada por falta de carga, por exemplo
    	 */
    	int lowCPUCounts = 0;

    	Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(instanceDimension);
        String name = instance.getInstance().getInstanceId();
        instanceDimension.setValue(name);

		while(lowCPUCounts != AutoScaling.LOW_CPU_MAX_COUNTS)
		{
           GetMetricStatisticsRequest request = AutoScaling.createGetMetricStatisticsRequest("AWS/EC2", "CPUUtilization", "Average", dims);
           GetMetricStatisticsResult result = cloudWatch.getMetricStatistics(request);
           List<Datapoint> datapoints = result.getDatapoints();

           for (Datapoint dp : datapoints)
           {
    	   	//atualizamos a utilizacao de CPU desta instancia
       		listOfActiveInstances.get(listOfActiveInstances.indexOf(instance)).setCurrentCPU_Usage(dp.getAverage());

	       	 if (dp.getAverage() < AutoScaling.MIN_CPU_USAGE)
	       	 {
	       		 lockListOfActiveInstances.lock();

	       		 if(listOfActiveInstances.size() > 2)//porventura so removemos uma instancia se se estiver mais que duas ativas
	       		 {
	 	     		lockListOfActiveInstances.unlock();
	       			 if(lowCPUCounts > 0)
	       				lowCPUCounts++;
		       		 else
		       		 {
		       			lowCPUCounts = 0;
		       			lowCPUCounts++;
		       		 }

	       			 if(lowCPUCounts == AutoScaling.LOW_CPU_MAX_COUNTS)
	       				 return instance; //se atingiu o limite de low cpu counts retornamos esta instancia para ser eliminada
	       			else
						try {
							Thread.sleep(AutoScaling.TIMEOUT_BETWEEN_CPU_CHECKS);
						} catch (InterruptedException e) {}
 	       		 }
	       		 else
	 	     		 lockListOfActiveInstances.unlock();
	       	 }
	       	 else	       	 
	       		 lowCPUCounts = 0;	       	
           }          
           try 
           {
        	   Thread.sleep(69000); //fazemos um sleep de 1.15 minutos porque nao vale a pena estar sempre a pedir CPU
        	   						// pois com detailed monitoring o CPU so' se atualiza de minuto a minuto
			} catch (InterruptedException e) {}
		}
		return instance;
	}

	static class MyHandler implements HttpHandler
	{		
        @Override
        public void handle(HttpExchange t) throws IOException 
        {   	        	        	 	        	
        	String numeroFatorizar = Request.calculateParameters(t);     	  	     			
            //cada request recebido sera tratado por uma thread diferente, passamos como parametro, o request recebido
        	 Thread thread = new MyThread(t, numeroFatorizar);
        	 thread.start();        	     
        }
    }
	
	static class MyThread extends Thread 
    {    	
    	private String numeroFatorizar;
    	HttpExchange t;

    	public MyThread(HttpExchange t, String parameter)
    	{
    		this.t = t;
    		this.numeroFatorizar = parameter;
    	}	
    	//codigo corrido quando fazemos start na thread
    	@Override
    	public void run()
    	{	
    		
    		/* Quando chega um pedido temos que associa-lo a um rank. Em primeiro lugar vamos 'a memoria interna do load balancer
    		 * ver se existe algum pedido igual, caso exista atribuimos esse rank ao pedido recebido, caso contrario vamos ao MSS
    		 * ver se existe algum pedido igual, se existir atribuimos esse rank, caso contrario o rank sera' o proprio valor.
    		 * Optamos por atribuir o proprio valor do pedido ao rank pois de acordo com alguns testes que fizemos, observamos
    		 * que o rank nao difere muito significativamente do numero do pedido portanto achamos que e' uma boa aproximacao
    		 * inicial quando nao temos informacao acerca do pedido
    		 */
    		BigInteger rank = calculateRank(numeroFatorizar); 		
    		scheduler(t, numeroFatorizar, rank); 		
    	}	
    }
	
	public static void scheduler(HttpExchange t, String numeroFatorizar, BigInteger rank)
	{
		int sizeInstancesAnalyzed = 0;
		ArrayList<InstanceObject> instancesAnalyzed = new ArrayList<InstanceObject>();
		while(true)
		{				
			if(instancesAnalyzed != null )
				sizeInstancesAnalyzed = instancesAnalyzed.size();
	
			//quando a condicao abaixo se verificar significa que ja percorremos todas as instancias e nao conseguimos encaixar
			//o pedido em nenhuma portanto criamos uma.
			lockListOfActiveInstances.lock();
			if(listOfActiveInstances.size() == sizeInstancesAnalyzed)
			{			
				lockListOfActiveInstances.unlock();
				lockCreateInstances.lock();
				if(!instanceBeingCreated) //se nao estiver nenhuma instancia a ser criada entao podemos criar uma
				{
					instanceBeingCreated = true;
					lockCreateInstances.unlock();
	
					System.out.println("We could not put the request in any instance, creating new instance...");
					
					createInstanceThroughScheduler(t, rank, numeroFatorizar, 1);
					return;
				}
				else //se uma estiver a ser criada, temos que esperar que ela acabe de ser criada 
				{
					lockCreateInstances.unlock();
					lockPendingRequests.lock();
					//se a lista pending for maior que MAX_PENDING_LIST criamos uma extra
					if(pendingList == MAX_PENDING_LIST)
					{
						pendingList = 0; //pomos de volta a 0 para que se houver mais pedidos possa ser preciso criar uma nova instancia
						lockPendingRequests.unlock();
						System.out.println("Pending list max reached, creating new instance...");
						
						createInstanceThroughScheduler(t, rank, numeroFatorizar, 2);				
						return;
					}
					else
					{
						pendingList++;
						lockPendingRequests.unlock();
					}				
					try 
					{	
						Thread.sleep(AutoScaling.GRACE_PERIOD + 3000); //damos algum tempo extra para as duas threads que criaram as duas instancias ou uma, libertarem a condicao que mais nao podem ser criadas
						lockPendingRequests.lock();
						if(pendingList > 0 )
							pendingList--;
						lockPendingRequests.unlock();

					} catch (InterruptedException e) {}
				}			
			}
			else
				lockListOfActiveInstances.unlock();
		
			InstanceObject instanceToSendRequest = null;
	
			lockListOfActiveInstances.lock();
			ArrayList<InstanceObject> listOfInstances = listOfActiveInstances;
			lockListOfActiveInstances.unlock();
	
			//vamos buscar a instancia a tratar menos pedidos atualmente
			for(InstanceObject instance : listOfInstances)
			{	
				lockScheduling.lock();
				boolean containsInstance = false;
				if(instancesAnalyzed != null)
					containsInstance = instancesAnalyzed.contains(instance);
				
				if(containsInstance) //se ja analisamos, passamos para a proxima
				{
					lockScheduling.unlock();
					continue;
				}	
				if(instanceToSendRequest == null) 
					instanceToSendRequest = instance;
				else if(instanceToSendRequest.getRequestsBeingProcessed().size() > instance.getRequestsBeingProcessed().size())
					instanceToSendRequest = instance;
				
				lockScheduling.unlock();			
			}
	
			lockScheduling.lock();
			//comparamos o numero a fatorizar com o intermedio, se for menos, entao enviamos logo para esta instancia pois 
			//e' um pedido rapido. Podemos simplesmente enviar porque as condicos abaixo que restringem o scheduling de pedidos
			//intermedios e grandes permite que as instancias tenham "sempre espaco" para pedidos rapidos
			int compare = rank.compareTo(INTERMEDIATE_RANK);
			if(compare == -1)
			{
				lockScheduling.unlock();
				InstanceObject instanceToSend = instanceToSendRequest;
				ExecutorService service = Executors.newCachedThreadPool();
				
				lockListOfActiveInstances.lock();
				//fazemos esta verificacao pois a instancia pode ter crashado ou ter sido terminada a meio do scheduling
				if(listOfActiveInstances.contains(instanceToSend))
				{
					Request request = new Request(t, rank, numeroFatorizar);
					CompletableFuture.runAsync(()->listOfActiveInstances.get(listOfActiveInstances.indexOf(instanceToSend)).addRequestToRequestProcessingList(request),service);
					CompletableFuture.runAsync(()->sendToInstance(instanceToSend, numeroFatorizar, t),service);
					lockListOfActiveInstances.unlock();
					return;
				}else  //se instancia crashou ou foi terminada entretanto, fazemos o reschedule
				{
					lockListOfActiveInstances.unlock();
				}		
			}
			//vamos agora ver a carga de utilizacao de CPU da instancia que atualmente esta a tratar menos pedidos
			//se for menor que 75% entao talvez possamos enviar para esta instancia 
			else if(instanceToSendRequest.getCurrentCPU_Usage() < AutoScaling.MAX_CPU_USAGE)
			{				
				//temos que ver se o nosso pedido e' MAX ou INTERMEDIATE para saber para onde vamos manda-lo ja que nao permitimos
				//que pedidos INTERMEDIATE coincidam que pedidos MAX
				int compare2 = rank.compareTo(MAX_RANK);
				if(compare2 == 0 || compare2 == 1) //Se for >= MAX_RANK
				{
					//vamos ver se esta instancia esta a tratar algum pedido categorizado MAX_RANK ou INTERMEDIATE
	    			if(instanceToSendRequest.getRequests_MAX_RANK() < LIMIT_MAX_REQUESTS && instanceToSendRequest.getRequests_INTERMEDIATE_RANK() == 0)
	    			{					    				
	    				InstanceObject instanceToSend = instanceToSendRequest;
	    				ExecutorService service = Executors.newCachedThreadPool();
	
	    				//atualizamos os pedidos que esta instancia esta a tratar
	    				Request request = new Request(t, rank, numeroFatorizar);
	    				lockListOfActiveInstances.lock();
	    				
	    				//fazemos esta verificacao pois a instancia pode ter crashado ou ter sido terminada a meio do scheduling
	    				if(listOfActiveInstances.contains(instanceToSend))
	    				{
	    					CompletableFuture.runAsync(()->listOfActiveInstances.get(listOfActiveInstances.indexOf(instanceToSend)).addRequestToRequestProcessingList(request),service);
	        				CompletableFuture.runAsync(()->sendToInstance(instanceToSend, numeroFatorizar, t),service);
	        				lockListOfActiveInstances.unlock();
	        				lockScheduling.unlock();        			
	        				return;
	    				}
	    				else //se instancia crashou ou foi terminada, fazemos o reschedule
	    				{
	        				lockListOfActiveInstances.unlock();
	        				lockScheduling.unlock();
	    				}  							
	    			}
					else
					{
						//vamos analisar outra instancia disponivel para ver se e' possivel esta tratar este pedido
						instancesAnalyzed.add(instanceToSendRequest);
	    				lockScheduling.unlock();
					}
				}
				else
				{
					//so enviamos se nao tiver sido atingido o limite de requests Intermedios e se esta instancia nao estiver a tratar pedidos grandes
					if(instanceToSendRequest.getRequests_INTERMEDIATE_RANK() < LIMIT_INTERMEDIATE_REQUESTS && instanceToSendRequest.getRequests_MAX_RANK() == 0)
					{    				
	    				InstanceObject instanceToSend = instanceToSendRequest;
						ExecutorService service = Executors.newCachedThreadPool();
	
	    				lockListOfActiveInstances.lock();
	    				//atualizamos os pedidos que esta instancia esta a tratar
	    				Request request = new Request(t, rank, numeroFatorizar);
	    				
	    				//fazemos esta verificacao pois a instancia pode ter crashado ou ter sido terminada a meio do scheduling
	    				if(listOfActiveInstances.contains(instanceToSend))
	    				{
	    					CompletableFuture.runAsync(()->listOfActiveInstances.get(listOfActiveInstances.indexOf(instanceToSend)).addRequestToRequestProcessingList(request),service);
	        				lockListOfActiveInstances.unlock();
	    					CompletableFuture.runAsync(()->sendToInstance(instanceToSend, numeroFatorizar, t),service);
	        				lockScheduling.unlock();
	    	    			return;
	    				}
	    				else  //se instancia crashou ou foi terminada entretanto, fazemos o reschedule
	    				{	
	        				lockListOfActiveInstances.unlock();
	    					lockScheduling.unlock();
	    				}   				
					}
					else
					{
						//vamos analisar outra instancia disponivel para ver se e' possivel esta tratar este pedido
						instancesAnalyzed.add(instanceToSendRequest);
	    				lockScheduling.unlock();
					}
				}
			}
			else //se o CPU desta instancia esta acima de 75% temos que analisar uma proxima instancia a ver se pode tratar este pedido
			{
				//vamos analisar outra instancia disponivel para ver se e' possivel esta tratar este pedido
				instancesAnalyzed.add(instanceToSendRequest);
				lockScheduling.unlock();
			}
		}
	}
	
	public static void createInstanceThroughScheduler(HttpExchange t, BigInteger rank, String numeroFatorizar, int identifier)
	{
		ExecutorService service = Executors.newCachedThreadPool();
		
		InstanceObject instanceCreated = AutoScaling.startInstance(ec2);
		System.out.println("Instance created with id : " + instanceCreated.getInstance().getInstanceId());
		
		lockListOfActiveInstances.lock();
		listOfActiveInstances.add(instanceCreated);
		//atualizamos este pedido como um que instancia que acabou de ser criada esta a tratar
		Request request = new Request(t, rank, numeroFatorizar);
		CompletableFuture.runAsync(()->listOfActiveInstances.get(listOfActiveInstances.indexOf(instanceCreated)).addRequestToRequestProcessingList(request),service);
		lockListOfActiveInstances.unlock();
		
		//metodo responsavel por iniciar os health checks e a verificacao de CPU da nova instancia criada
		//e tambem de reinicializar a verificacao de CPU da instancia que ultrapassou o threshold de utilizacao de CPU
		CompletableFuture.runAsync(()->AutoScaling.setupChecksNewInstance(instanceCreated),service);
		//antes de enviarmos o pedido para a instancia temos que dar tempo para a instancia comecar

		try {
			Thread.sleep(AutoScaling.GRACE_PERIOD);
			if(identifier == 1) //serve para identificar quem chamou este metodo e consoante aplicamos condicoes diferenteswa
			{
				lockCreateInstances.lock();
				instanceBeingCreated = false; //instancias ja podem ser criadas
				lockCreateInstances.unlock();
			}			
		} catch (InterruptedException e) {} 
		CompletableFuture.runAsync(()->sendToInstance(instanceCreated, numeroFatorizar, t),service);

	}
	
	public static BigInteger calculateRank(String parameter)
	{
		BigInteger rank;
		
		if(internalStorage.containsKey(parameter))
		{
			rank = internalStorage.get(parameter);
		}
		else
		{
			DynamoEntry dynamoEntry = DynamoEntry.getFromMSS(parameter);
			try
			{
				rank = dynamoEntry.getRank();

				/*se for uma metrica intermedia, ou seja, Running = true (no dynamo) , entao nao guardamos na memoria 
				interna e persistente pois este valor ira mudar em breve.
				Como e' um metrica intermedia, vamos assumir o worst case, ou seja, que e' um pedido longo, portanto
				vamos atribuir-lhe o rank MAX.
			  */
				if(dynamoEntry.isRunningState())
				{
					rank = MAX_RANK;
				}
				else //caso contrario nao sao metricas intermedias
				{
					rank = dynamoEntry.getRank();	
					//guardamos no internal storage para da proxima vez nao termos que ir ao MSS
					String rankToSave = rank.toString();
					
					//quando vamos buscar um rank ao Dynamo, guardamos tambem em memoria persistente para quando o load balancer 
					//for reiniciado (por ex.) ter algum historico para poupar no overhead de ter que ir buscar informacao ao MSS
					ExecutorService service = Executors.newCachedThreadPool();
					CompletableFuture.runAsync(()-> Request.writeHistory(parameter, rankToSave), service);
					
					internalStorage.put(parameter, rank);
				}	
			}catch(NullPointerException e) //se nao existir no MSS
			{
				//o rank e' o numero do pedido
				rank = new BigInteger(parameter);		
			}
		}		
		return rank;
	}
	
	public static void sendToInstance(InstanceObject instance, String numeroFatorizar, HttpExchange t)
	{
		 String response = ""; //resposta de volta para o user
        try {
        	//reencaminha para a instancia
    		URL url = new URL("http://"+instance.getInstance().getPublicIpAddress()+":8000/f.html/numeroFatorizar?="+numeroFatorizar);
    		URLConnection connection = url.openConnection();
    		Scanner s = new Scanner(connection.getInputStream());
			while(s.hasNext()) {
				response += s.next() + " ";
			}
			s.close();
		} catch (IOException e1) {
			//se ocorrer algum problema fazemos o reschedule
			scheduler(t, numeroFatorizar, calculateRank(numeroFatorizar));
		}		
		//enviamos o resultado do pedido para o user
		try
		{
			ExecutorService service = Executors.newCachedThreadPool();

			lockListOfActiveInstances.lock();
			//removemos o pedido que esta instancia estava a tratar
			
			Request request = new Request(t, calculateRank(numeroFatorizar), numeroFatorizar);
			CompletableFuture.runAsync(()->listOfActiveInstances.get(listOfActiveInstances.indexOf(instance)).removeRequestFromRequestProcessingList(request),service);
			lockListOfActiveInstances.unlock();
						
    		t.sendResponseHeaders(200, response.length());
   		 	OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
		} 
		catch (IOException e) {}
		catch(NullPointerException e){}
	}	
}
