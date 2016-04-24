import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

	ServerSocket serversocket;
	int port;

	public Server(int port) {
		this.port = port;

	}

	@Override
	public void run() {

		// this will create a server socket.
		try {
			serversocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// accept incoming connections for ever.
		while (true) {

			try {
				Socket clientsocket = serversocket.accept();
				InputStream input = clientsocket.getInputStream();
				ObjectInputStream oinput = new ObjectInputStream(input); // input
																			// stream.
				OutputStream output = clientsocket.getOutputStream();
				ObjectOutputStream ooutput = new ObjectOutputStream(output); // output
																				// stream.

				// give a thread the input and output stream so that it can keep
				// independently listen.
				ClientListener c = new ClientListener(oinput, ooutput);
				c.start();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
