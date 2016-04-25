package Models;

public class Message {
	public int fromNodeID;
	public int sentMessagesCount;
	public NodeInfo nodeInfo;

	public Message(int nodeId, int sentMessagesCount, NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
		this.fromNodeID = nodeId;
		this.sentMessagesCount = sentMessagesCount;
	}

	public boolean isRollbackMessage() {
		return sentMessagesCount == -1;
	}

	public boolean recoveryInitiator() {
		return sentMessagesCount == -2;
	}
}
