import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

	Socket socket;

	static boolean connected;

	public Client(String hostname, int port) {

		connected = false;
		connect(hostname, port);

	}

	public void connect(String hostname, int port) {
		// System.out.println("In connect function");
		try {

			while (connected != true) {
				try {

					socket = new Socket(hostname, port);

					connected = true;
					// if (connected == true)
					// System.out.println("Connected to client at port: " +
					// port);
				} catch (IOException ex) {

				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void write(int requestingNode, NodeInfo node) {

		OutputStream output;
		try {
			System.out.println(node.nodeid + " got client req from " + requestingNode);
			output = socket.getOutputStream();
			System.out.println("am i reaching here aftr socket");
			ObjectOutputStream oos = new ObjectOutputStream(output);
			System.out.println("am i reaching here aftr output object stream");
			oos.writeObject(node);
			System.out.println("after sendng node to server");
			// oos.writeObject(requestingNode);

			// not closing the client socket as the connection needs to be up
			// always
			// oos.close();
			// output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
