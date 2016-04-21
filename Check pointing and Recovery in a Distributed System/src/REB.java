import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class REB {

	Thread server;
	Thread client;
	static int noNodes, noFailEvents, maxNumber, maxPerActive, minSendDelay;
	static HashMap<Integer, NodeInfo> nodeInfo = new HashMap<>();
	static ArrayList<Integer> neighbors = new ArrayList<Integer>();
	static ArrayList<FailuresCheckpoints> checkpoints = new ArrayList<FailuresCheckpoints>();
	static int nodeid;
	static HashMap<Integer, Boolean> active = new HashMap<Integer, Boolean>();
	static HashMap<Integer, Boolean> passive = new HashMap<Integer, Boolean>();

	static Random rndIndex = new Random();
	static Random rndSubset = new Random();
	static Random rndNodes = new Random();
	static REB reb;
	static ArrayList<Client> clients;
	static int numReq[];

	public static void main(String[] args) {
		reb = new REB();
		clients = new ArrayList<Client>();
		int node_id, port;
		String hostname;
		// Input the values from command line
		noNodes = Integer.parseInt(args[0]);
		numReq = new int[noNodes];
		noFailEvents = Integer.parseInt(args[1]);
		maxNumber = Integer.parseInt(args[2]);
		maxPerActive = Integer.parseInt(args[3]);
		nodeid = Integer.parseInt(args[7]);
		minSendDelay = Integer.parseInt(args[8]);
		// System.out.println("Num Nodes : " + noNodes + " Num Fail Events : " +
		// noFailEvents + " MAX number : "
		// + maxNumber + " MAX per Active : " + maxPerActive);
		// ;

		String str = args[4];
		String[] val = new String[noNodes];
		val = str.split("#");

		// Storing all the nodes information
		for (int i = 0; i < noNodes; i++) {
			String[] det = new String[3];
			det = val[i].split(" ");
			node_id = Integer.parseInt(det[0]);
			hostname = det[1];
			port = Integer.parseInt(det[2]);
			nodeInfo.put(node_id, new NodeInfo(nodeid, hostname, port));

			// System.out.println("NodeID : " + node_id + " hostname : " +
			// hostname + " port : " + port);

		}
		// Storing the neighbors information
		String[] q = args[5].split(" ");
		for (int i = 0; i < q.length; i++) {
			neighbors.add(Integer.parseInt(q[i]));
			// System.out.println("Node is : " + nodeid + " Node neighbours : "
			// + q[i]);
		}

		// Storing the sequence of failure events and checkpoints

		String[] fullFailEvents = new String[noFailEvents];
		fullFailEvents = args[6].split("#");
		// System.out.println("len of fail : " + fullFailEvents.length);
		// System.out.println("element 1 : " + fullFailEvents[0]);
		// System.out.println("element 2 : " + fullFailEvents[1]);
		for (int i = 0; i < noFailEvents; i++) {
			String[] FailNodeSplit = new String[2];
			FailNodeSplit = fullFailEvents[i].split(" ");
			int failnode = Integer.parseInt(FailNodeSplit[0]);
			int checkpoint = Integer.parseInt(FailNodeSplit[1]);
			checkpoints.add(new FailuresCheckpoints(i, failnode, checkpoint));
			// System.out.println("Failnode : " + failnode + " Checkpoints : " +
			// checkpoint);
		}

		// Put the server to listening mode
		reb.serverCall(nodeid, nodeInfo);

		// Active and Passive Nodes - atleast one node needs to be active in
		// order to send messages
		int activeNode = rndNodes.nextInt(2);
		// activeNode returns either 0 or 1. 0 denotes the node to be passive
		// and 1 denotes the nodes to be active and node 0 is set to be active
		// in order to avoid the situation of not getting any node to be active
		if (activeNode == 1 || nodeid == 0) {
			active.put(nodeid, true);
			passive.put(nodeid, false);
		} else {
			active.put(nodeid, false);
			passive.put(nodeid, true);
		}

		// Creating Client Objects
		for (int i = 0; i < noNodes; i++) {
			// System.out.println("In creating objects");
			Client c = new Client(nodeInfo.get(i).hostname, nodeInfo.get(i).port);
			clients.add(c);
		}

		// if node is active initially, it sends messages to random subset of
		// neighbors
		while (active.get(nodeid) == true) {
			System.out.println("In while active for nodeid : " + nodeid);
			reb.sndMsg();
			// After sending messages to the subset of its neighbors, the node
			// becomes passive
			passive.put(nodeid, true);
			active.put(nodeid, false);

		}

		System.out.println("The node id is : " + nodeid + " and messages sent : " + numReq[nodeid]);

	}

	void sndMsg() {
		ArrayList<Integer> toSendReq = new ArrayList<Integer>();
		// Send requests to subset of neighbors. Generate random subsets and
		// random index values to retrieve random neighbors

		// we need subset to be greater than 0
		int subset = rndSubset.nextInt(maxPerActive) + 1;

		for (int i = 0; i < subset; i++) {
			int index = rndIndex.nextInt(neighbors.size());
			toSendReq.add(neighbors.get(index));
		}

		// Send Requests to the randomly generated neighbors
		for (int i = 0; i < toSendReq.size(); i++) {
			numReq[nodeid]++;
			// Make client requests
			clients.get(toSendReq.get(i)).write(nodeid, nodeInfo.get(toSendReq.get(i)), toSendReq.size());
			// reb.clientCall(nodeid, nodeInfo.get(toSendReq.get(i)),
			// toSendReq.size());
			// After making one client request, the node has to wait for
			// some time until it can send request to another client
			try {
				WaitTillNextRequest.sleep(minSendDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Method for calling server
	void serverCall(int nodeid, HashMap<Integer, NodeInfo> nodeInfo) {
		server = new Thread(new Server(nodeid, nodeInfo));
		server.start();
	}

}
