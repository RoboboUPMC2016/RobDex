package robDex;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * TODO:
 * - add verbose mode
 * - add bash path to possible arguments
 */

public class Server {
	
	public static int poolSize = 50;

	public static void main(String[] args){
		
		ServerSocket serverSocket = null;
		ExecutorService pool = Executors.newFixedThreadPool(poolSize); 
		
		try{
			Util.init(args);
			
		}

		catch(RuntimeException e){
			
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		try {
            serverSocket = new ServerSocket(Util.port);
            Socket client = null;
    		
            System.out.println("Server launched on port " + Util.port + ". Waiting for connections.\n");
            
    		while(true){

    			client = serverSocket.accept();
    			
    			//TODO check source
    			pool.submit(new TimedRequest(client));
    		}
        } 
		
		catch (IOException ex) {
            System.out.println("Can't set server on port " + Util.port + ".");
        }
		
		finally{
			
			try {
				
				if(serverSocket != null)
					serverSocket.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
