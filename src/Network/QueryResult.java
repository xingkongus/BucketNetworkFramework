package Network;

import java.util.List;

import com.google.gson.JsonObject;

import Common.Tool;
import Network.BucketObject.BucketCommand;
import Network.BucketObject.Command.Client.ClientCommand;

public class QueryResult {

	public int count;
	public List<JsonObject> results;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void setResults(List<JsonObject> results) {
		this.results = results;
	}
	
	public int getCount() {
		return count;
	}
	
	public List<JsonObject> getResults() {
		return results;
	}
	
	public BucketCommand toClientCommand(int Sign)
	{
		ClientCommand clm= new ClientCommand();
		clm.setCommand(this.getClass().getSimpleName());
		clm.setValues(this);
		clm.setSign(Sign);
		return clm;
	}
	
	public String toJSON()
	{
		return Tool.toJson(this);
	}
}
