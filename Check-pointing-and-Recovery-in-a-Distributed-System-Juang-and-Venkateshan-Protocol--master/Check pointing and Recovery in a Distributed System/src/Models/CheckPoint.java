package Models;

public class CheckPoint {

	public int[] sentValues;
	public int[] recievedValues;
	public int[] clockVector;

	// Value to be set to 0 at initialization and each time rollback occurs.
	public int indexSinceLastRollback = 0;

	public CheckPoint(int[] sentValues, int[] recievedValues, int[] matrixVector, int lastRollbackIndex) {
		super();
		this.sentValues = sentValues;
		this.recievedValues = recievedValues;
		this.clockVector = matrixVector;
		this.indexSinceLastRollback = lastRollbackIndex;
	}

}
