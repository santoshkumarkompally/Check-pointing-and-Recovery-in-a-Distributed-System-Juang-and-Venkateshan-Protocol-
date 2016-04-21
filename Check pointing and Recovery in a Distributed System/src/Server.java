import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server extends Thread {
	ServerSocket serversocket;
	Socket clientsocket;
	int nodeid;
	HashMap<Integer, NodeInfo> nodeinfo;
	maintainRequestMsgs nodeDetails;
	static int rcvRequests;

	public Server(int nodeid, HashMap<Integer, NodeInfo> nodeinfo) {
		this.nodeid = nodeid;
		this.nodeinfo = nodeinfo;

	}

	public void run() {

		try {

			serversocket = new ServerSocket(nodeinfo.get(nodeid).port);
			// System.out.println("in try of server : " +
			// nodeinfo.get(nodeid).port);

			// listenSocket
			while (true) {
				// System.out.println("BEFORE ACCEPT in server for : " +
				// nodeid);
				clientsocket = serversocket.accept();
				// System.out.println("AFTER ACCEPT in server for : " + nodeid);
				InputStream input = clientsocket.getInputStream();
				ObjectInputStream oinput = new ObjectInputStream(input);

				try {
					System.out.println("In server:  getting nodedetails");
					nodeDetails = (maintainRequestMsgs) oinput.readObject();
					rcvRequests++;
					System.out.println("THE VALUE value is : " + nodeDetails.node.nodeid);

					// check if the node that received message is passive or
					// active and update them
					if ((REB.passive.get(nodeDetails.node.nodeid) == true)
							&& (REB.numReq[nodeDetails.node.nodeid] <= REB.maxNumber)) {
						REB.active.put(nodeDetails.node.nodeid, true);
						REB.passive.put(nodeDetails.node.nodeid, false);
					} else if ((REB.passive.get(nodeDetails.node.nodeid) == true)
							&& (REB.numReq[nodeDetails.node.nodeid] > REB.maxNumber)) {
						REB.active.put(nodeDetails.node.nodeid, false);
						REB.passive.put(nodeDetails.node.nodeid, true);
					}

					else if ((REB.active.get(nodeDetails.node.nodeid) == true)
							&& (REB.numReq[nodeDetails.node.nodeid] <= REB.maxNumber)) {
						REB.active.put(nodeDetails.node.nodeid, true);
						REB.passive.put(nodeDetails.node.nodeid, false);

					} else if ((REB.active.get(nodeDetails.node.nodeid) == true)
							&& (REB.numReq[nodeDetails.node.nodeid] > REB.maxNumber)) {
						REB.active.put(nodeDetails.node.nodeid, false);
						REB.passive.put(nodeDetails.node.nodeid, true);

					}

					// check for the current node conditions and update the
					// status of active and passive
					if (rcvRequests < nodeDetails.totRequests && REB.numReq[nodeid] <= REB.maxNumber) {
						REB.active.put(nodeid, true);
						REB.passive.put(nodeid, false);
					}

					else if (rcvRequests <= nodeDetails.totRequests && REB.numReq[nodeid] > REB.maxNumber) {
						REB.active.put(nodeid, false);
						REB.passive.put(nodeid, true);
					} else if (rcvRequests == nodeDetails.totRequests && REB.numReq[nodeid] <= REB.maxNumber) {
						REB.active.put(nodeid, true);
						REB.passive.put(nodeid, false);
						rcvRequests = 0;

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
}
