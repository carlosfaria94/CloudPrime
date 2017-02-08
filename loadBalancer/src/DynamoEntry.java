import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

public class DynamoEntry {

	static AmazonDynamoDBClient  dynamoDB;
	
	private BigInteger rank;
	private boolean runningState;
	
	public DynamoEntry(BigInteger rank, boolean runningState)
	{
		this.rank = rank;
		this.runningState = runningState;
	}
	
	public static DynamoEntry getFromMSS(String parameter)
	{
		dynamoDB = new AmazonDynamoDBClient(new ProfileCredentialsProvider().getCredentials());
		dynamoDB.setRegion(Region.getRegion(Regions.EU_WEST_1));
		
		HashMap<String, Condition> keyConditions = new HashMap<String, Condition>();
		keyConditions.put("Parameter", new Condition().withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue(parameter)));
		
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setKeyConditions(keyConditions);
		queryRequest.withTableName("MSS");

		QueryResult result = dynamoDB.query(queryRequest);
		//se houver um pedido ja tratado no MSS getCount > 0, caso contrario nao existe
		if(result.getCount() == 0)
			return null;
		else
		{
			//vamos buscar o rank
			Map<String, AttributeValue> queryResult = result.getItems().get(0);
			String rank = "";
			boolean runningState = false;
			
	        for (Map.Entry<String, AttributeValue> entry  : queryResult.entrySet()) 
	        {
	        	if(entry.getKey().equals("Rank"))
	        		rank = entry.getValue().getN();
	        	else if(entry.getKey().equals("Running"))
	        		runningState = entry.getValue().getBOOL();
	        }
			DynamoEntry rankResult = new DynamoEntry(new BigInteger(rank), runningState);			
			return rankResult;
		}
	}


	public BigInteger getRank() {
		return rank;
	}


	public boolean isRunningState() {
		return runningState;
	}
}
