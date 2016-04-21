import java.io.Serializable;

public class maintainRequestMsgs implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int totRequests;
	NodeInfo node;

	public maintainRequestMsgs(NodeInfo node, int totRequests) {
		// TODO Auto-generated constructor stub
		this.node = node;
		this.totRequests = totRequests;

	}
}
