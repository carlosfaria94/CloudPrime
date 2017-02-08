import java.math.BigInteger;
import java.util.LinkedList;
import com.amazonaws.services.ec2.model.Instance;

public class InstanceObject {

	public final static BigInteger MAX_RANK = new BigInteger("1272110814");
	public final static BigInteger INTERMEDIATE_RANK = new BigInteger("161751155");

	private Instance instance;
	private double currentCPU_Usage;
	private LinkedList<Request> requestsBeingProcessed = new LinkedList<Request>();
	private int requests_MAX_RANK = 0;  //numero de pedidos MAX_RANK que esta instancia esta a tratar
	private int requests_INTERMEDIATE_RANK = 0; //numero de pedidos INTERMEDIATE_RANK que esta instancia esta a tratar
	
	public int getRequests_MAX_RANK() {
		return requests_MAX_RANK;
	}

	public int getRequests_INTERMEDIATE_RANK() {
		return requests_INTERMEDIATE_RANK;
	}

	public InstanceObject(Instance instance)
	{
		this.instance = instance;
	}

	public Instance getInstance() {
		return instance;
	}
	
	public LinkedList<Request> getRequestsBeingProcessed()
	{
		return requestsBeingProcessed;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public double getCurrentCPU_Usage() {
		return currentCPU_Usage;
	}

	public void setCurrentCPU_Usage(double usage) {
		this.currentCPU_Usage = usage;
	}
	
	public void addRequestToRequestProcessingList(Request request)
	{
		
		BigInteger parameter = request.getRank();
		int compare = parameter.compareTo(MAX_RANK);

		if(compare == 1 || compare == 0) //se for 1 ou 0 entao o rank deste pedido e' um pedido grande
			requests_MAX_RANK++;
		else
		{
			compare = parameter.compareTo(INTERMEDIATE_RANK);
			if(compare == 1 || compare == 0)
			{
				requests_INTERMEDIATE_RANK++;

			}
		}	
		requestsBeingProcessed.add(request);
	}
	
	public void removeRequestFromRequestProcessingList(Request request)
	{
		BigInteger parameter = request.getRank();
		int compare = parameter.compareTo(MAX_RANK);
		if(compare == 1 || compare == 0) //se for 1 ou 0 entao o rank deste pedido e' um pedido grande
			requests_MAX_RANK--;
		else
		{
			compare = parameter.compareTo(INTERMEDIATE_RANK);
			if(compare == 1 || compare == 0)
				requests_INTERMEDIATE_RANK--;
		}	
		
		for(int i = 0; i < requestsBeingProcessed.size(); i++)
		{
			if(requestsBeingProcessed.get(i).getParameter().equals(request.getParameter()))
			{
				requestsBeingProcessed.remove(i);
				break;
			}
		}
	}
}
