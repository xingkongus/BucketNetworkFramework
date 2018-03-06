package Test;

import bucket.network.Server;
import bucket.network.connection.ServerConnection;
import bucket.network.protocol.BucketProtocol;
import bucket.network.protocol.HttpProtocol;
import bucket.network.protocol.WebSocketProtocol;

/**
 * 测试
 * 
 * @author Hansin1997
 *
 */
public class ServerTest {

	public static void main(String[] args) throws Throwable {

		ServerConnection.ProtocolList.add(WebSocketProtocol.class.getName());
		ServerConnection.ProtocolList.add(HttpProtocol.class.getName());
		ServerConnection.ProtocolList.add(BucketProtocol.class.getName());

		Server s = new Server(TestApplication.class.getName(),8080);
		s.start();

	}
}
