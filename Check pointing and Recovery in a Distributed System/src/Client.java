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
					// System.out.println("Connected to client at port: " +
					// port);
					connected = true;
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
			output = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(output);
			oos.writeObject(node);
			oos.write(requestingNode);
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
