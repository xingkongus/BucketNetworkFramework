package network.command.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import Common.Gobal;
import Common.Tool;
import network.bucketobject.Message;
import network.bucketobject.Query;
import network.bucketobject.QueryResult;
import network.bucketobject.USER;
import network.command.BucketCommand;
import network.connection.UserConnection;

public class MessageCommand extends BucketCommand {

	public Message message;

	public void setMessage(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public void execute() {

		message.setSendTime(new Date());

		if (client.username != null && !client.equals("")) {
			message.setSender(client.username);

		}

		UserConnection r = Gobal.getPool().getUserConnection(message.receiver);
		if (r != null) {
			try {
				r.send(message.toClientCommand());
			} catch (IOException e) {
			}
		}else{
			Query query = new Query();
			query.setTable_name(USER.class.getSimpleName());
			query.addQuery("username", "=\'" + message.receiver + "\'");
			query.setJustCount(true);
			QueryResult result = Gobal.getDb().Query(query);
			if(result.getCount() > 0){
				ArrayList<Message> array = new ArrayList<Message>();
				array.add(message);
				
				DataSaver ds = new DataSaver();
				ds.setTable(Tool.object2Table(array.get(0)));
				ds.setValues(Tool.List2JsonArray(array));
				ds.execute();
			}
		}

	}

}
