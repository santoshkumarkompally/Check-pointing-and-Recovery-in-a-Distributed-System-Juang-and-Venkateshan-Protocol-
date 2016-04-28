import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Models.CheckPoint;
import Models.Message;
import Models.NodeInfo;

public class REB {

	static Thread server;
	static Thread client;
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
	static int failuresSimulatedTillNow;
	static boolean inRecovery;
	static int numberOfIterations;
	// Checkpoints specific to this particular node - optimize - save only this
	// information for each node after parsing the config file.
	static List<Integer> myFailedCheckpoints;
	static int[] count;
	static int counter;
	static int iteration;

	public static void main(String[] args) throws InterruptedException {
		iteration = 0;
		counter = 0;
		inRecovery = false;
		numberOfCheckPointsTaken = 0;
		checkPoints = new ArrayList<>();
		clients = new ArrayList<Client>();
		myFailedCheckpoints = new ArrayList<>();
		failuresSimulatedTillNow = 0;
		numberOfIterations = 0;

		int node_id, port;
		String hostname;
		// Input the values from command line
		noNodes = Integer.parseInt(args[0]);
		count = new int[noNodes];

		// init the count array to 0.

		resetCounter();
		// first checkpoint at starting before REB starts.

		checkPoints.add(createFirstCheckPoint());

		noFailEvents = Integer.parseInt(args[1]);
		maxNumber = Integer.parseInt(args[2]);
		maxPerActive = Integer.parseInt(args[3]);
		nodeid = Integer.parseInt(args[7]);
		minSendDelay = Integer.parseInt(args[8]);
		// System.out.println("node id is:" + nodeid);
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

			// TODO : Test if clockVector is null - since its initialization is
			// moved to createFirstCheckPoint()
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

			// Saving personal fail-checkpoints
			if (failnode == nodeid) {
				myFailedCheckpoints.add(checkpoint);
			}
			// System.out.println("Failnode : " + failnode + " Checkpoints : " +
			// checkpoint);
		}

		// Put the server to listening mode
		serverCall(nodeid, nodeInfo);
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
			// System.out.println("This is selected as active node initially: "
			// + nodeid);
			sndMsg();

		}

	}

	public static synchronized void sndMsg() {

		// select a neighbor randomly and then send messages from them to
		// others.
		CheckPoint ckpt;
		if (checkPoints.size() < 1) {
			ckpt = createFirstCheckPoint();
		} else {
			ckpt = checkPoints.get(checkPoints.size() - 1);
		}
		int count = 0;
		int choose;

		// iterating through all the neighbors.
		for (int i = 0; i < neighbors.size(); i++) {

			choose = rndSubset.nextInt(2);

			// we will send a message to this neighbor.
			if (choose == 1 && numReq < maxNumber) {

				nodeInfo.get(neighbors.get(i)).clockVector[nodeid]++;
				// Setting noOfSentMessages to -1 to indicate it is not a
				// rollback message
				Message msg = new Message(nodeid, -1, nodeInfo.get(neighbors.get(i)));
				// get client object and then write to the client object.

				clients.get(i).write(nodeid, msg);
				ckpt.sentValues[neighbors.get(i)]++;
				ckpt.clockVector = nodeInfo.get(neighbors.get(i)).clockVector;
				ckpt.indexSinceLastRollback++;
				checkPoints.add(ckpt);

				// Process fail simulation
				if (hasProcessFailed(ckpt)) {
					handleProcessFailed();
				}

				// System.out.println("sending message from: " + nodeid + "to: "
				// + neighbors.get(i));
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
			// Setting noOfSentMessages to -1 indicates it is not a rollback
			// message
			Message msg = new Message(nodeid, -1, nodeInfo.get(neighbors.get(0)));
			clients.get(neighbors.get(0)).write(nodeid, msg);
			// System.out.println("sending message from: " + nodeid + "to: " +
			// neighbors.get(0));
			numReq++;
		}
		active = false;
	}

	// Method for calling server
	static void serverCall(int nodeid, HashMap<Integer, NodeInfo> nodeInfo) {
		// System.out.println("value coming here is:" +
		// nodeInfo.get(nodeid).port);
		server = new Thread(new Server(nodeInfo.get(nodeid).port));
		server.start();
	}

	static void rollBackIfNeeded() {

		// System.out.println("Roll back if needed: " + nodeid);

		for (int i = 0; i < noNodes; i++) {
			while (checkPoints.get(checkPoints.size() - 1).recievedValues[i] > count[i] && count[i] >= 0) {
				checkPoints.remove(checkPoints.size() - 1);
			}
		}

		sendRollbackMessage();

	}

	/**
	 * This method is called when a process is in passive state and receives a
	 * message from another process.
	 */
	public static synchronized void actionNeeded(Message msg) {

		// this is to start the recovery initiation.
		// after this no one will able to send message
		if (msg.recoveryInitiator()) {

			if (inRecovery == false) {

				inRecovery = true;
				floodRecoveryInitiation();
				sendRollbackMessage();

			}
			return;
		}

		if (msg.isRollbackMessage()) {
			inRecovery = true;
			count[msg.fromNodeID] = msg.sentMessagesCount;

			for (int i = 0; i < noNodes; i++) {

				if (count[i] != -1)
					counter++;
			}

			if (counter == neighbors.size()) {
				// check consistency and broad cast to neighbors.
				iteration++;

				if (iteration < noNodes - 1) {

					rollBackIfNeeded();
				} else {
					System.out.println(
							"We are able to do simulate iteration:" + failuresSimulatedTillNow + " at node: " + nodeid);
					// increase the simulation failure value so that the next
					// process can simulate failure.
					failuresSimulatedTillNow++;
					// iteration is set to 0 since reb is complete.
					iteration = 0;
					// restart the REB Protocol.
					sndMsg();

				}
				// reset all the received from neighbors.
				resetCounter();

			}

		}

		// -----------------------------

		// If it is a rollback message, take appropriate action
		// if (msg.isRollbackMessage()) {
		// rollback(msg);
		// return;
		// }
		NodeInfo node = msg.nodeInfo;

		CheckPoint ckpt = checkPoints.get(checkPoints.size() - 1);
		// updating the clock vector.
		for (int i = 0; i < noNodes; i++) {
			if (clockVector[i] <= node.clockVector[i]) {
				clockVector[i] = node.clockVector[i];
			}
		}

		// TODO : Do we need to update clockVector this way? Isn't it already
		// updated?
		// clockVector[node.nodeid]++;

		ckpt.recievedValues[node.nodeid]++;
		ckpt.clockVector = clockVector;
		ckpt.indexSinceLastRollback++;
		checkPoints.add(ckpt);
		if (hasProcessFailed(ckpt)) {
			inRecovery = true; // this will mean we are initiating/participating
								// in the recovery.
			// flood the system to initiate the recovery so that they will stop
			// sending the application messaged.
			floodRecoveryInitiation();
			// -----------------------------------------------------------------
			handleProcessFailed();

			return;
		}

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
	 * send a message to all the other processes about the failure.
	 */
	static void floodRecoveryInitiation() {

		for (int i = 0; i < neighbors.size(); i++) {
			// NodeInfo node = nodeInfo.get(neighbors.get(i));
			Message msg = new Message(nodeid, -2, nodeInfo.get(neighbors.get(i)));
			clients.get(i).write(nodeid, msg);
		}

	}

	/**
	 * This method will create check point Check point is issued on every send
	 * and receive of a message. This method will update the send and receive
	 * message lists accordingly.
	 */
	public static synchronized CheckPoint createFirstCheckPoint() {
		int[] sentValues = new int[noNodes];
		int[] recievedValues = new int[noNodes];
		clockVector = new int[noNodes];
		for (int i = 0; i < noNodes; i++) {
			sentValues[i] = 0;
			recievedValues[i] = 0;
			clockVector[i] = 0;
		}
		return new CheckPoint(sentValues, recievedValues, clockVector, 0);
	}

	// for simulating failures.
	public static boolean hasProcessFailed(CheckPoint ckpt) {

		// We have to check if this is out turn to fail or not first.

		if (failedCheckPoints.get(failuresSimulatedTillNow).nodeid == nodeid) {

			return (ckpt.indexSinceLastRollback == failedCheckPoints.get(failuresSimulatedTillNow).numCheckpoints);
			// return ckpt.indexSinceLastRollback == myFailedCheckpoints.get(0);
		}
		return false;

	}

	// Called when a process fails
	public static synchronized void handleProcessFailed() {
		// if a process fails it should execute random backing of checkpoints
		// and assume it to be the stable
		// storage and start from there.
		// We need to change the below line to back off randomly.
		// System.out.println("process init the roll back is: " + nodeid);
		checkPoints.remove(checkPoints.size() - 1);
		// update index
		checkPoints.get(checkPoints.size() - 1).indexSinceLastRollback = 0;
		sendRollbackMessage();
	}

	private static void sendRollbackMessage() {
		CheckPoint lastKnownCheckPoint = checkPoints.get(checkPoints.size() - 1);
		for (int i = 0; i < neighbors.size(); i++) {
			NodeInfo node = nodeInfo.get(neighbors.get(i));
			Message msg = new Message(nodeid, lastKnownCheckPoint.sentValues[neighbors.get(i)], node);
			clients.get(i).write(nodeid, msg);
		}
	}

	// TODO : Probably FAULTY functioning of checkPoint.indexSinceLastRollback.
	// Check once.

	static void resetCounter() {

		counter = 0;
		for (int i = 0; i < noNodes; i++) {

			count[i] = -1;
		}
	}

}
