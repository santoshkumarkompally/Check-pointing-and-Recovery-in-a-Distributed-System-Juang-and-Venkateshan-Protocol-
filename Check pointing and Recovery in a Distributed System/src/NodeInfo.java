import java.io.Serializable;

public class NodeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int nodeid, port;
	String hostname;

	NodeInfo(int nodeid, String hostname, int port) {
		this.nodeid = nodeid;
		this.hostname = hostname;
		this.port = port;

	}
}
