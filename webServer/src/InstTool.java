import BIT.highBIT.*;
import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;


public class InstTool {

public static class Metricas {
	  BigInteger i_count, b_count, loadCount, fieldLoadCount;
	  boolean last = false;

	public Metricas(BigInteger i, BigInteger b, BigInteger l, BigInteger f, boolean bool) {
		i_count = i;
		b_count = b;
		loadCount = l;
		fieldLoadCount = f;
		last = bool;
	}

	public boolean getLast() {
		return last;
	}

	public void setLast(boolean bool) {
		last = bool;
	}
}

	static HashMap<Long, Metricas> metricasThread = new HashMap<Long, Metricas>(); // metricas por thread
	static HashMap<Long, Integer> contabilizar = new HashMap<Long, Integer>();
	
	public static void main(String argv[]) {
		String fileName = new String(argv[0]); // classe que queremos instrumentar
		String outputFileName = new String(argv[1]); // output classe instrumentada
		ClassInfo ci = new ClassInfo(fileName);
		Vector routines = ci.getRoutines();

		// este auxiliar serve para identificar a ultima rotina a ser executada para apenas esta instrucao, enviarmos as metricas para os logs
		Enumeration auxiliar = routines.elements();
		Routine aux = (Routine) auxiliar.nextElement();
		aux.addBefore("InstTool", "init", "");

		// loop through all the routines
		for (Enumeration e = routines.elements(); e.hasMoreElements();) {
			Routine routine = (Routine) e.nextElement();
			for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();) {
				BasicBlock bb = (BasicBlock) b.nextElement();
				bb.addBefore("InstTool", "count", bb.size());
			}
			for (Enumeration instrs = routine.getInstructionArray().elements(); instrs.hasMoreElements();) {
				Instruction instr = (Instruction) instrs.nextElement();
				int opcode = instr.getOpcode();
				if (opcode == InstructionTable.getfield)
					instr.addBefore("InstTool", "LoadCount", new Integer(0));
				else {
					short instr_type = InstructionTable.InstructionTypeTable[opcode];
					if (instr_type == InstructionTable.LOAD_INSTRUCTION)
						instr.addBefore("InstTool", "LoadCount", new Integer(1));
				}
			}
			if (!e.hasMoreElements())
				routine.addAfter("InstTool", "writeLog", "");
		}
		ci.write(outputFileName);
	}

	public static synchronized void init(String a) {
		BigInteger zero = new BigInteger("0");
		metricasThread.put(new Long(Thread.currentThread().getId()), new Metricas(zero, zero, zero, zero, false));
		contabilizar.put(new Long(Thread.currentThread().getId()), new Integer("0"));
	}

	public static synchronized void writeLog(String foo) {
		Metricas aux = metricasThread.get(new Long(Thread.currentThread().getId()));

		if (!aux.getLast()) {
			AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
			dynamoDB.setRegion(Region.getRegion(Regions.EU_WEST_1));

			System.out.println("Thread " + Thread.currentThread().getId() + ": " + aux.i_count + " instructions in "
					+ aux.b_count + " basic blocks were executed " + " field loads: " + aux.fieldLoadCount
					+ " load counts : " + aux.loadCount);
			aux.setLast(true);
			contabilizar.put(new Long(Thread.currentThread().getId()), new Integer("1"));
			try {
				FileInputStream logFile = new FileInputStream("logIntermedio.txt");
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(logFile));

				String currentThreadId = "Thread " + Thread.currentThread().getId();
				String line;

				while ((line = bufReader.readLine()) != null) {
					String[] parts = line.split(":");
					String parameter = "";
					if (parts[0].equals(currentThreadId)) 
					{
						String[] parameterParts = parts[1].split("=");
						parameter = parameterParts[1];

						// efetuamos uma query para ir buscar a entrada existente no Dynamo deste parametro
						// se o estado de Running for true, entao atualizamos a entrada no Dynamo para este parametro
						// e metemos Running a false. Se o estado retornado pela query for False entao nao fazemos nada
						// pois isso significa que as metricas para este parametro ja foram calculados. Se for null entao nao
						// existe entrada e temos que cria-la
						QueryResult result = getEntryDynamo(parameter, dynamoDB);

						if (result.getCount() == 0) // se a query nao retora nada significa que nao	 temos nada no dynamo				// sobre este parametro
						{
							putDynamo(parameter, aux.i_count, aux.b_count, aux.fieldLoadCount, aux.loadCount, dynamoDB,
									false, "", null);
						} else {
							Map<String, AttributeValue> queryResult = result.getItems().get(0);
							// runningValue fica a true se Running estiver a true.
							boolean runningValue = queryResult.values().contains(new AttributeValue().withBOOL(true));
							if (runningValue) {
								putDynamo(parameter, aux.i_count, aux.b_count, aux.fieldLoadCount, aux.loadCount,
										dynamoDB, false, "", null);
							} 
						}
						break;
					}
				}
			} catch (Exception e) {}
		} else if (contabilizar.get(new Long(Thread.currentThread().getId())).intValue() == 0) {
			contabilizar.put(new Long(Thread.currentThread().getId()), new Integer("1"));
			aux.setLast(false);
		}
	}

	public static synchronized QueryResult getEntryDynamo(String parameter, AmazonDynamoDBClient dynamoDB) {
		HashMap<String, Condition> keyConditions = new HashMap<String, Condition>();
		keyConditions.put("Parameter", new Condition().withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue(parameter)));

		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setKeyConditions(keyConditions);
		queryRequest.withTableName("MSS");

		QueryResult result = dynamoDB.query(queryRequest);
		return result;
	}

	public static synchronized void putDynamo(String parameter, BigInteger instCount, BigInteger basicBlocksCount,
			BigInteger fieldLoadsCount, BigInteger loadsCount, AmazonDynamoDBClient dynamoDB, boolean runningState,
			String threadID, QueryResult queryResult) 
	{
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("Parameter", new AttributeValue(parameter));
		item.put("Instructions", new AttributeValue().withN(instCount.toString()));
		item.put("Basic Blocks", new AttributeValue().withN(basicBlocksCount.toString()));
		item.put("Field Loads", new AttributeValue().withN(fieldLoadsCount.toString()));
		item.put("Loads Stack", new AttributeValue().withN(loadsCount.toString()));
		item.put("Running", new AttributeValue().withBOOL(runningState));

		// calculamos o valor do rank
		BigDecimal rank = (new BigDecimal(instCount).multiply(new BigDecimal("0.40")))
				.add(new BigDecimal(basicBlocksCount).multiply(new BigDecimal("0.15")))
				.add(new BigDecimal(fieldLoadsCount).multiply(new BigDecimal("0.05")))
				.add(new BigDecimal(loadsCount).multiply(new BigDecimal("0.40")));
		item.put("Rank", new AttributeValue().withN(rank.toBigInteger().toString()));

		// se o runningState vier a true, ou seja e' uma metrica intermedia que vamos atualizar no Dynamo,
		// temos que tambem introduzir o ThreadID que esta a tratar este parametro e a instancia.
		// estas duas informacoes sao necessarios para evitar o seguinte caso. Imaginando que duas instancias diferentes
		// estao a processar o mesmo pedido longo e ambos retiram metricas intermedias. Imaginando que a instancia A comecou
		// 1 minuto antes, entao nao podemos deixar que a instancia B nao faca overwrite na entrada no Dynamo pois a instancia A tem os
		// os dados mais recentes. O mesmo se aplica a threads dentro da mesma instancia que processam o mesmo pedido dai precisarmos do ThreadID
		if (runningState) {
			// tal como referido acima temos que verificar se esta e' a thread que deve atualizar as metricas intermedias
			String[] id = threadID.split(" ");

			if (queryResult != null) {
				Map<String, AttributeValue> result = queryResult.getItems().get(0);
				// vamos ver se o id desta thread corresponde ao que deve atualizar as metricas
				boolean containsID = result.values().contains(new AttributeValue().withN(id[1]));

				if (containsID) {
					// se sim temos de ver agora se correspondem 'a mesma instancia pois podem haver threads com o mesmo ID em
					// maquinas diferentes a processar o mesmo request
					boolean containsInstance = result.values().contains(new AttributeValue(checkInstanceId()));
					if (containsInstance) // se sim entao podemos atualizar as	 metricas
					{
						item.put("InstanceID", new AttributeValue(checkInstanceId()));
						item.put("ThreadID", new AttributeValue().withN((id[1])));
					} else //caso contrario nao fazemos nada, o mesmo para o else abaixo
						return;
				} else 
					return;
			} 
			else // este else e' o caso em que nao temos metricas internas deste parametro
			{
				item.put("InstanceID", new AttributeValue(checkInstanceId()));
				item.put("ThreadID", new AttributeValue().withN((id[1])));
			}
		}
		PutItemRequest putItemRequest = new PutItemRequest().withTableName("MSS").withItem(item);
		dynamoDB.putItem(putItemRequest);
	}

	static class ThreadIntermedia extends Thread {
		String threadID;
		Metricas metricas;

		public ThreadIntermedia(String threadID, Metricas metricas) {
			this.threadID = threadID;
			this.metricas = metricas;
		}

		@Override
		public void run() {
			try {
				FileInputStream logFile = new FileInputStream("logIntermedio.txt");
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(logFile));

				String line;

				while ((line = bufReader.readLine()) != null) {
					String[] parts = line.split(":");

					if (parts[0].equals(threadID)) {
						AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
						dynamoDB.setRegion(Region.getRegion(Regions.EU_WEST_1));

						String[] parameterParts = parts[1].split("=");
						String parameter = parameterParts[1];

						QueryResult result = getEntryDynamo(parameter, dynamoDB);

						if (result.getCount() == 0) // se a query nao retornar nada significa que nao temos nada no dynamo sobre este parametro
						{
							putDynamo(parameter, metricas.i_count, metricas.b_count, metricas.fieldLoadCount,
									metricas.loadCount, dynamoDB, true, threadID, null);
						} else { //caso contrario vamos buscar as metricas que temos para atualiza las
							Map<String, AttributeValue> queryResult = result.getItems().get(0);
							// runningValue fica a true se Running estiver a true, se ja estiver a false entao ja temos as metricas finais para este parametro portanto nao atualizamos o dynamo
							boolean runningValue = queryResult.values().contains(new AttributeValue().withBOOL(true));
							if (runningValue) {
								putDynamo(parameter, metricas.i_count, metricas.b_count, metricas.fieldLoadCount,
										metricas.loadCount, dynamoDB, true, threadID, result);
							}
						}
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void count(int incr) {

		Metricas aux = metricasThread.get(new Long(Thread.currentThread().getId()));
		BigInteger intermedio = new BigInteger(Integer.toString(incr));

		aux.i_count = aux.i_count.add(intermedio);
		aux.b_count = aux.b_count.add(BigInteger.ONE);
		metricasThread.put(new Long(Thread.currentThread().getId()), aux);

		// de 100400724 em 100400724 basic blocks guardamos metricas intermedias
		BigInteger modulo = aux.b_count.mod(new BigInteger("100400724"));
		if (modulo.equals(new BigInteger("0"))) {
			String currentThreadId = "Thread " + Thread.currentThread().getId();
			// fazemos uma thread para colocar as metricas intermedias no Dynamo. utilizamos uma thread para este proposito pois o processo de
			// colocar no Dynamo ainda leva algum tempo e portanto poderia influenciar o rank final atribuido ao pedido
			// pois nao iria contar com este overhead de colocar no dynamo
			Thread threadIntermedia = new ThreadIntermedia(currentThreadId, aux);
			threadIntermedia.start();
		}
	}

	public static synchronized void LoadCount(int type) {

		Metricas aux = metricasThread.get(new Long(Thread.currentThread().getId()));
		if (type == 0)
			aux.fieldLoadCount = aux.fieldLoadCount.add(BigInteger.ONE);
		else
			aux.loadCount = aux.loadCount.add(BigInteger.ONE);
		metricasThread.put(new Long(Thread.currentThread().getId()), aux);
	}

	public static synchronized String checkInstanceId() {
		URL url;
		String instanceId = "";
		try {
			url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
			URLConnection conn = url.openConnection();
			Scanner s = new Scanner(conn.getInputStream());
			if (s.hasNext()) {
				instanceId = s.next();
			} else {
				return "no response";
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return instanceId;
	}
}
