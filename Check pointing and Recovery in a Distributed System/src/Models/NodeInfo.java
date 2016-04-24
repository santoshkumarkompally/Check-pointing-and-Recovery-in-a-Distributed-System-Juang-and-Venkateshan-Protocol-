package Models;

import java.io.Serializable;

public class NodeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int nodeid, port;
	public String hostname;
	public boolean isFailed;
	public int[] clockVector;

	public NodeInfo(int nodeid, String hostname, int port, boolean isFailed, int[] timestamp) {
		this.nodeid = nodeid;
		this.hostname = hostname;
		this.port = port;
		this.isFailed = isFailed;
		this.clockVector = timestamp;
	}

	@Override
	public String toString() {
		return "NodeInfo [nodeid=" + nodeid + ", port=" + port + ", hostname=" + hostname + "]";
	}
}
