import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class REB {

	Thread server;
	Thread client;
	static int noNodes, noFailEvents, maxNumber, maxPerActive, minSendDelay;
	static HashMap<Integer, NodeInfo> nodeInfo = new HashMap<>();
	static ArrayList<Integer> neighbors = new ArrayList<Integer>();
	static ArrayList<FailuresCheckpoints> failedCheckPoints = new ArrayList<FailuresCheckpoints>();
	static int nodeid;
	static boolean active;
	static int numberOfCheckPointsTaken;
	static Random rndIndex = new Random();
	static Random rndSubset = new Random();
	static Random rndNodes = new Random();
	static REB reb;
	static ArrayList<Client> clients;
	static int numReq;
	static ArrayList<CheckPoint> checkPoints;
	// To store all the checkpoints.
	static int[] clockVector;

	public static void main(String[] args) throws InterruptedException {

		numberOfCheckPointsTaken = 0;
		checkPoints = new ArrayList<>();
		clients = new ArrayList<Client>();
		int node_id, port;
		String hostname;
		// Input the values from command line
		noNodes = Integer.parseInt(args[0]);
		int[] sentValues = new int[noNodes];
		int[] recievedValues = new int[noNodes];
		clockVector = new int[noNodes];
		for (int i = 0; i < noNodes; i++) {
			sentValues[i] = 0;
			recievedValues[i] = 0;
			clockVector[i] = 0;
		}
		// first checkpoint at starting before REB starts.
		CheckPoint firstCheckpoint = new CheckPoint(sentValues, recievedValues, clockVector);
		checkPoints.add(firstCheckpoint);

		noFailEvents = Integer.parseInt(args[1]);
		maxNumber = Integer.parseInt(args[2]);
		maxPerActive = Integer.parseInt(args[3]);
		nodeid = Integer.parseInt(args[7]);
		minSendDelay = Integer.parseInt(args[8]);

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
			nodeInfo.put(node_id, new NodeInfo(node_id, hostname, port, false, clockVector));

		}
		// Storing the neighbors information
		String[] q = args[5].split(" ");
		for (int i = 0; i < q.length; i++) {
			neighbors.add(Integer.parseInt(q[i]));
		}

		String[] fullFailEvents = new String[noFailEvents];
		fullFailEvents = args[6].split("#");

		for (int i = 0; i < noFailEvents; i++) {
			String[] FailNodeSplit = new String[2];
			FailNodeSplit = fullFailEvents[i].split(" ");
			int failnode = Integer.parseInt(FailNodeSplit[0]);
			int checkpoint = Integer.parseInt(FailNodeSplit[1]);
			failedCheckPoints.add(new FailuresCheckpoints(i, failnode, checkpoint));
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
			active = true;
		} else {
			active = false;
		}

		// Creating Client Objects but why till numberOfNodes. Shouldn't it be
		// number of neighbors.
		for (int i = 0; i < neighbors.size(); i++) {
			// System.out.println("In creating objects");
			Client c = new Client(nodeInfo.get(neighbors.get(i)).hostname, nodeInfo.get(neighbors.get(i)).port);
			clients.add(c);
		}

		// if node is active initially, it sends messages to random subset of
		// neighbors.
		if (active) {
			System.out.println("This is selected as active node initially: " + nodeid);
			sndMsg();

		}

	}

	public static synchronized void sndMsg() {

		// select a neighbor randomly and then send messages from them to
		// others.
		CheckPoint ckpt = checkPoints.get(checkPoints.size() - 1);
		// matrixVector)
		int count = 0;
		int choose;

		// iterating through all the neighbors.
		for (int i = 0; i < neighbors.size(); i++) {

			choose = rndSubset.nextInt(2);

			// we will send a message to this neighbor.
			if (choose == 1 && numReq < maxNumber) {
				// get client object and then write to the client object.
				nodeInfo.get(neighbors.get(i)).clockVector[nodeid]++;
				clients.get(i).write(nodeid, nodeInfo.get(neighbors.get(i)));
				ckpt.sentValues[neighbors.get(i)]++;
				ckpt.matrixVector = nodeInfo.get(neighbors.get(i)).clockVector;
				checkPoints.add(ckpt);

				System.out.println("sending message from: " + nodeid + "to: " + neighbors.get(i));
				numReq++;
				count++; // to check at the end if we are able to send to anyone
							// or not.

				try {
					Thread.sleep(minSendDelay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		// send to the first neighbor.
		if (count == 0 && numReq < maxNumber) {

			clients.get(neighbors.get(0)).write(nodeid, nodeInfo.get(neighbors.get(0)));
			System.out.println("sending message from: " + nodeid + "to: " + neighbors.get(0));
			numReq++;
		}
		active = false;
	}

	// Method for calling server
	void serverCall(int nodeid, HashMap<Integer, NodeInfo> nodeInfo) {
		server = new Thread(new Server(nodeInfo.get(nodeid).port));
		server.start();
	}

	/**
	 * This method is called when a process is in passive state and receives a
	 * message from another process.
	 */
	public static synchronized void actionNeeded(NodeInfo node) {

		CheckPoint ckpt = checkPoints.get(checkPoints.size() - 1);
		// updating the clock vector.
		for (int i = 0; i < noNodes; i++) {

			if (clockVector[i] <= node.clockVector[i]) {

				clockVector[i] = node.clockVector[i];
			}

		}
		clockVector[node.nodeid]++;

		ckpt.recievedValues[node.nodeid]++;
		ckpt.matrixVector = clockVector;
		// create a checkpoint.

		// System.out.println(numReq);
		if (numReq <= maxNumber) {

			if (!active) {
				active = true;
			}
			sndMsg();
		} else if (numReq > maxNumber) {
			active = false;
		}
	}

	/**
	 * This method will create check point Check point is issued on every send
	 * and receive of a message. This method will update the send and receive
	 * message lists accordingly.
	 */
	public static synchronized void createCheckPoint() {

	}

}
