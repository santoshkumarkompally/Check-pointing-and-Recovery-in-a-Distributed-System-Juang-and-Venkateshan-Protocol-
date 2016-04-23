import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientListener extends Thread{
	
	ObjectInputStream oinput;
	ObjectOutputStream ooutput;
	
	
	public ClientListener(ObjectInputStream oinput,ObjectOutputStream ooutput) {
		this.oinput=oinput;
		this.ooutput=ooutput;
	
		
	}
	
	@Override
	public void run() {
		
		NodeInfo node;
		// which ever socket we are having we are going to read through this socket.
		while(true){
			
			try {
				node = (NodeInfo)oinput.readObject();
				System.out.println(node + " is coming at: "+ Server.nodeid);
				REB.actionNeeded();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}

}
