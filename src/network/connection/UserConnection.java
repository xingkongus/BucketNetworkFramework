package network.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import Common.Tool;
import Database.DatabaseManager;
import network.bucketobject.DeleteQuery;
import network.bucketobject.Message;
import network.bucketobject.Query;
import network.bucketobject.QueryResult;
import network.bucketobject.USER;
import network.command.client.ClientCommand;
import network.listener.BucketListener;
import network.listener.LoginListener;
import network.listener.PoolListener;

public class UserConnection extends Connection {

	public String username;
	private boolean isServer;
	private LoginListener loginListener;
	private PoolListener poolListener;

	public void setLoginListener(LoginListener loginListener) {
		this.loginListener = loginListener;
	}

	public LoginListener getLoginListener() {
		return loginListener;
	}

	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}
	
	public boolean isServer() {
		return isServer;
	}

	public UserConnection(Socket socket,DatabaseManager db,PoolListener pl, BucketListener messageListener) throws IOException {
		this(socket,db,pl, messageListener, false);
	}
	
	public UserConnection(Socket socket, BucketListener messageListener) throws IOException {
		this(socket,null,null, messageListener, false);
	}


	public UserConnection(Socket socket,DatabaseManager db,PoolListener pl, BucketListener messageListener, boolean isServer) throws IOException {
		super(socket,db, messageListener);
		this.poolListener = pl;
		this.isServer = isServer;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public void startListen() throws IOException {
		if (isServer) {
			socket.setSoTimeout(2000);

			if (check(readLine())) {
				socket.setSoTimeout(1000 * 60 * 30);
				super.startListen();
			} else {
				socket.close();
			}
		} else {
			super.startListen();
		}

	}

	public void login(USER user, LoginListener listener) throws IOException {
		writeLine(Checker.createLogin(user).toJSON());
		loginListener = listener;
	}
	
	public void Signin(USER user, LoginListener listener) throws IOException {
		writeLine(Checker.createSignin(user).toJSON());
		loginListener = listener;
	}

	private boolean check(String str) throws UnsupportedEncodingException, IOException {

		Checker checker = Tool.JSON2E(str, Checker.class);
		if (checker == null)
			return false;
		
		ClientCommand cc = new ClientCommand();

		USER checkerUser = checker.doCheck(db);
		if( checkerUser == null)
		{
			cc.setCommand("CONNECT");
			cc.setValues("FAIL");
			send(cc);
			return false;
		}else{
			
			if(poolListener != null)
				poolListener.push(checkerUser.getUsername(), this);

			
			cc.setCommand("CONNECT");
			cc.setValues("SUCCESS");
			
			username = checkerUser.username;
			send(cc);
			
			//����������Ϣ
			List<Message> msgList = getUnreadMessage();
			for(Message msg : msgList){
				send(msg.toClientCommand());
			}

			return true;
		}

	}
	
	//��ȡ���������ݿ��е�������Ϣ����ɾ��
	private List<Message> getUnreadMessage(){
		ArrayList<Message> array = new ArrayList<Message>();
		Query query = new Query();
		query.setTable_name(Message.class.getSimpleName());
		query.addQuery("receiver", "=\'" + username + "\'");
		
		QueryResult result = db.Query(query);
		for(JsonObject obj : result.getResults()){
			Message msg = Tool.object2E(obj,Message.class);
			array.add(msg);
		}
		
		DeleteQuery dquery = new DeleteQuery();
		dquery.setTable_name(Message.class.getSimpleName());
		dquery.addQuery("receiver", "=\'" + username + "\'");
		db.Delete(dquery);
		return array;
	}
}
