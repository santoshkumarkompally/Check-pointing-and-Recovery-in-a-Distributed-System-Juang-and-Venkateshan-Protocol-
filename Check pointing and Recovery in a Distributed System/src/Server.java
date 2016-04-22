import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server extends Thread {
	ServerSocket serversocket;
	Socket clientsocket;
	int nodeid;
	HashMap<Integer, NodeInfo> nodeinfo;
	NodeInfo node;

	public Server(int nodeid, HashMap<Integer, NodeInfo> nodeinfo) {
		this.nodeid = nodeid;
		this.nodeinfo = nodeinfo;

	}

	public void run() {

		try {

			serversocket = new ServerSocket(nodeinfo.get(nodeid).port);

			// listenSocket
			while (true) {
				// System.out.println("BEFORE ACCEPT in server for : " +
				// nodeid);
				clientsocket = serversocket.accept();
				System.out.println("AFTER ACCEPT in server for : " + nodeid);
				InputStream input = clientsocket.getInputStream();
				ObjectInputStream oinput = new ObjectInputStream(input);
				System.out.println("after servers objectinput stream");

				try {
					System.out.println("in server try");
					node = (NodeInfo) oinput.readObject();
					System.out.println("In server for nodeid : " + node.nodeid);
					// int requestingNode = (int) oinput.readObject();
					// System.out.println(requestingNode + " sent message to : "
					// + node.nodeid);
					// check for the receiving node conditions and update the
					// status of active and passive
					if (REB.numReq <= REB.maxNumber && REB.passive == true) {
						REB.active = true;
						REB.passive = false;

						sndMsg(node);
					}

					else if (REB.numReq > REB.maxNumber) {
						REB.active = false;
						REB.passive = true;
					} else if (REB.numReq <= REB.maxNumber && REB.active == true) {
						REB.active = true;
						REB.passive = false;
						sndMsg(node);
					}

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		catch (IOException e) {
			System.out.println("Read Failed");
			e.printStackTrace();
		}

	}

	public void sndMsg(NodeInfo node) {
		ArrayList<Integer> toSendReq = new ArrayList<Integer>();
		// Send requests to subset of neighbors. Generate random subsets and
		// random index values to retrieve random neighbors

		// we need subset to be greater than 0
		int subset = 0;
		while (subset <= REB.maxPerActive) {
			subset = REB.rndSubset.nextInt(REB.neighbors.size()) + 1;
			if (subset <= REB.maxPerActive) {
				for (int i = 0; i < subset; i++) {
					int index = REB.rndIndex.nextInt(REB.neighbors.size());
					toSendReq.add(REB.neighbors.get(index));
					// System.out.println("Node id " + nodeid + " to send msg to
					// : " + REB.neighbors.get(index));
				}
				break;
			} else {
				subset = 0;
			}
		}

		// Send Requests to the randomly generated neighbors
		for (int i = 0; i < toSendReq.size(); i++) {
			REB.numReq++;
			System.out.println("The number of requests for nodeid : " + nodeid + "is : " + REB.numReq
					+ " and neighbor is : " + toSendReq.get(i));
			// Make client requests
			REB.clients.get(toSendReq.get(i)).write(node.nodeid, REB.nodeInfo.get(toSendReq.get(i)));
			System.out.println("Node id " + node.nodeid + " to send msg to : " + REB.nodeInfo.get(toSendReq.get(i)));
			// reb.clientCall(nodeid, nodeInfo.get(toSendReq.get(i)),
			// toSendReq.size());
			// After making one client request, the node has to wait for
			// some time until it can send request to another client
			try {
				WaitTillNextRequest.sleep(REB.minSendDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// After sending messages to the subset of its neighbors, the node
		// becomes passive
		REB.passive = true;
		REB.active = false;

	}

}
