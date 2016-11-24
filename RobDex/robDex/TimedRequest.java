package robDex;

import java.io.IOException;
import java.net.Socket;

public class TimedRequest extends Thread{
	
	private static final long defaultDelay = 5*60*1000;
	
	private long delay;
	private Request request;
	
	public TimedRequest(Socket client){
		this(client, defaultDelay);
	}

	public TimedRequest(Socket client, long delay) {
		this.request = new Request(client);
		this.delay = delay;
	}

	@Override
	public void run(){
								
		try {

			request.start();
			request.join(delay);
			
		} catch (InterruptedException e) {}
		
		finally{
			try {
				if(request.isAlive())
					request.closeConnection();
			} 
			catch (IOException e) {	}
		}
	}
}
