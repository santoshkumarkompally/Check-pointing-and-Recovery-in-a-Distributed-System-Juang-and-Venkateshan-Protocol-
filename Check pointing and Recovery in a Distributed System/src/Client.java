import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import Models.Message;

public class Client {

	Socket socket;
	OutputStream output;
	ObjectOutputStream oos;

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
					output = socket.getOutputStream();
					oos = new ObjectOutputStream(output);
					connected = true;
				} catch (IOException ex) {

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void write(int requestingNode, Message msg) {

		try {

			oos.writeObject(msg);
			// System.out.println("coming here:" + node);
			// oos.write(requestingNode);
			// not closing the client socket as the connection needs to be up
			// always. We need to close the output stream.
			// oos.close();
			// output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
