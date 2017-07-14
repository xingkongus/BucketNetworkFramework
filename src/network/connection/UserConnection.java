package network.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import Common.Gobal;
import Common.Tool;
import network.bucketobject.Query;
import network.bucketobject.QueryResult;
import network.bucketobject.USER;
import network.command.client.ClientCommand;
import network.listener.BucketListener;
import network.listener.LoginListener;

public class UserConnection extends Connection {

	public String username;
	private boolean isServer;
	private LoginListener loginListener;

	public void setLoginListener(LoginListener loginListener) {
		this.loginListener = loginListener;
	}

	public LoginListener getLoginListener() {
		return loginListener;
	}

	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}

	public UserConnection(Socket socket, BucketListener messageListener) throws IOException {
		this(socket, messageListener, false);
	}

	public UserConnection(Socket socket, BucketListener messageListener, boolean isServer) throws IOException {
		super(socket, messageListener);
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
				socket.setSoTimeout(0);
				super.startListen();
			} else {
				socket.close();
			}
		} else {
			super.startListen();
		}

	}

	public void login(USER user, LoginListener listener) throws IOException {

		writeLine((Tool.toJson(user)));
		loginListener = listener;

	}

	private boolean check(String str) throws UnsupportedEncodingException, IOException {

		USER usr = Tool.JSON2E(str, USER.class);
		if (usr == null)
			return false;

		Query query = new Query(USER.class.getSimpleName(), -1);
		query.addQuery("username", "=\'" + usr.username + "\'");
		query.addQuery("password", "=\'" + usr.password + "\'");
		QueryResult result = Gobal.db.Query(query);
		ClientCommand cc = new ClientCommand();

		if (result.count != 0) {
			USER get = Tool.object2E(result.getResults().get(0), USER.class);

			if (get != null) {
				UserConnection conn;
				while ((conn = Gobal.getPool().getUserConnection(get.getUsername())) != null) {
					conn.finish();
				}

			}
			
			username = usr.username;
			cc.setCommand("LOGIN");
			cc.setValues("SUCCESS");
			send(cc);
			return true;

		}

		cc.setCommand("LOGIN");
		cc.setValues("FAIL");

		send(cc);
		return false;

	}

}
