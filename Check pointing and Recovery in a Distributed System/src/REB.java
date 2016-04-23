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
	static boolean active;
	static boolean passive;

	static Random rndIndex = new Random();
	static Random rndSubset = new Random();
	static Random rndNodes = new Random();
	static REB reb;
	static ArrayList<Client> clients;
	static int numReq;

	public static void main(String[] args) throws InterruptedException {
		reb = new REB();
		clients = new ArrayList<Client>();
		int node_id, port;
		String hostname;
		// Input the values from command line
		noNodes = Integer.parseInt(args[0]);

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
			nodeInfo.put(node_id, new NodeInfo(node_id, hostname, port));

			// System.out.println("NodeID : " + node_id + " hostname : " +
			// hostname + " port : " + port);

		}
		// Storing the neighbors information
		String[] q = args[5].split(" ");
		for (int i = 0; i < q.length; i++) {
			neighbors.add(Integer.parseInt(q[i]));
			// System.out.println("Node is : " + nodeid + " Node neighbours : " + q[i]);
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
		Thread.sleep(100);
		// Active and Passive Nodes - atleast one node needs to be active in
		// order to send messages
		int activeNode = rndNodes.nextInt(2);
		// activeNode returns either 0 or 1. 0 denotes the node to be passive
		// and 1 denotes the nodes to be active and node 0 is set to be active
		// in order to avoid the situation of not getting any node to be active
		if (activeNode == 1 || nodeid == 0) {
			active = true;
			passive = false;
		} else {
			active = false;
			passive = true;
		}

		// Creating Client Objects but why till numberOfNodes. Shouldn't it be number of neighbors.
		for (int i = 0; i < neighbors.size(); i++) {
			// System.out.println("In creating objects");
			Client c = new Client(nodeInfo.get(neighbors.get(i)).hostname, nodeInfo.get(neighbors.get(i)).port);
			clients.add(c);
		}

		// if node is active initially, it sends messages to random subset of
		// neighbors.
		if (active == true) {
			System.out.println("This is selected as active node initially: " + nodeid);
			sndMsg();

		}


	}

	
	public static synchronized void sndMsg(){
		
		// select a neighbor randomly and then send messages from them to others.
		
		int count=0;
		int choose;
		// iterating through all the neighbors.
		for(int i=0;i<neighbors.size();i++){
			
			choose= rndSubset.nextInt(2);
			
			// we will send a message to this neighbor.
			if(choose ==1 && numReq<maxNumber){
				// get client object and then write to the client object
				clients.get(i).write(nodeid, nodeInfo.get(neighbors.get(i)));
				System.out.println("sending message from: "+ nodeid + "to: "+ neighbors.get(i));
				numReq++;
				count++; // to check at the end if we are able to send to anyone or not.
			
				try {
					Thread.sleep(minSendDelay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		// send to the first neighbor.
		if(count==0 && numReq<maxNumber){
			
			clients.get(neighbors.get(0)).write(nodeid, nodeInfo.get(neighbors.get(0)));
			System.out.println("sending message from: "+ nodeid + "to: "+ neighbors.get(0));
			numReq++;
		}
	passive=true;
	active=false;
	}
	
	// Method for calling server
	void serverCall(int nodeid, HashMap<Integer, NodeInfo> nodeInfo) {
		server = new Thread(new Server(nodeid, nodeInfo));
		server.start();
	}

	public static synchronized void actionNeeded(){
		
		System.out.println(numReq);
		if (REB.numReq <= REB.maxNumber && REB.passive == true) {
			REB.active = true;
			REB.passive = false;

			sndMsg();
		}

		else if (REB.numReq > REB.maxNumber) {
			REB.active = false;
			REB.passive = true;
		} else if (REB.numReq <= REB.maxNumber && REB.active == true) {
			REB.active = true;
			REB.passive = false;
			sndMsg();
		}
		
	}	
	
	
}
