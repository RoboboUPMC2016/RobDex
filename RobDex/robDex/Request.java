package robDex;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import robDex.Exceptions.FailedCompilationException;
import robDex.Exceptions.IllegalFileException;

/**
 * <p>
 * This class is used to receive the client's java sources, compile them into a .dx file, then send it back to them.
 * </p>
 * <p>
 * During the transfer, the server will check if the files are appropriate (name and size).<br />
 * The server will send to the client:<br />
 * 	{@code 0} if the file is valid,<br />
 * 	{@code -1} otherwise. In this case, the server will immediately close the connection.
 * </p>
 * <p>
 * When compiling is done, the server will send an answer to the client specifying if the compilation was successful:<br />
 * 	{@code 0} if the compilation is successful,<br />
 * 	{@code -1} otherwise.
 * 
 * </p>
 * <p>
 * The operations are as follows :<br />
 * <ol>
 * <li>A handshaking sets place to ensure trust with the client.</li>
 * <li>The server reads the number of files to be received.</li>
 * <li>Files are then received ; each file is read this way :
 * 		<ol>
 * 		<li>The name of the file is received.</li>
 * 		<li>The size of the file is received.</li>
 * 		<li>An answer is given to the client based on the validity of the file</li>
 * 		<li>The content of the file is received.</li>
 * 		</ol></li>
 * <li>The received files are compiled.</li>
 * <li>The server will send an answer to the client, specifying if the compilation was successful.</li>
 * <li>The server will send the size of the file to be sent.</li>
 * <li>The server will send the aforementioned file to the user.</li>
 * <li>All files created during the operation are then destroyed.</li>
 * </ol>
 * 
 * @author Nicolas Fedy
 * 
 */

public class Request extends Thread{
	
	private static final int BUF_SIZE = 4096, ERROR_TAG = -1, SUCCESS_TAG = 0;
	private static final String OUTPUT_FILE = "out.dex", ERROR_FILE = "error.log";
	private static final String ERROR_MSG = "An error unrelated to the java compilation occured.";
	private String directory;
	private Socket client;
	private ArrayList<File> files;
	
	public Request(Socket client){
		
		this.client = client;
		this.directory = Util.directory + '/' + Util.getclientIdentifier(client);		
	}
	
	public void run(){
		
		DataInputStream dis = null;
		DataOutputStream dos = null;
		
		try {
			
			dis = new DataInputStream(new BufferedInputStream(client.getInputStream()));
	        dos = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
			
			if(handshaking()){
				
				receiveFiles(dis, dos);
				
				try {
					Util.compile(directory, files);
					sendDexFile(dos);
				} catch (FailedCompilationException e) {
					sendErrorFile(dos);
				}	
			}
		} catch (IllegalFileException e) {
			//client's mistake
		} catch (IOException e) {}
		
		finally{
			
			try {
				
				dis.close();
				dos.close();
				client.close();
				Util.deleteDir(new File(directory));
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
			
			catch(NullPointerException e){}
		}
	}
	
	private boolean handshaking(){
		
		return true;
	}
	
	private void receiveFiles(DataInputStream dis, DataOutputStream dos) throws IOException, IllegalFileException{
	
		//number of files
		int filesCount = dis.readInt();
		 
		files = new ArrayList<File>(filesCount);
        
        int n = 0;
        byte[] buf = new byte[BUF_SIZE];
        
        //creates repertories to store client files
        new File(directory).mkdirs();

        for(int i = 0; i < filesCount; i++){
            
            String fileName = Util.filterFileName(dis.readUTF());
            long fileSize = dis.readLong();
            
            if(Util.isValid(fileName, fileSize)){
            	dos.writeInt(SUCCESS_TAG);
            	dos.flush();
            	
            	File file = new File(directory + '/' + fileName);
                
                files.add(file);
                
                FileOutputStream fos = new FileOutputStream(file);
                
                while (fileSize > 0 && (n = dis.read(buf, 0, (int)Math.min(buf.length, fileSize))) != -1){
                	
                	fos.write(buf, 0, n);
                	fileSize -= n;
                }
                
                fos.close();
            }            
            
            else{
            	
            	dos.writeInt(ERROR_TAG);
            	throw new IllegalFileException();
            }
        }
	}
	
	private void sendDexFile(DataOutputStream dos) throws IOException{
		
		dos.writeInt(SUCCESS_TAG);
		sendFile(dos, new File(directory + '/' + OUTPUT_FILE));
	}
	
	/**
	 * Sends the error file to the client. If the error log file is empty, a default message will be sent.
	 * 
	 * @param dos the socket's output stream
	 * @throws IOException if an IO issue appears
	 */
	private void sendErrorFile(DataOutputStream dos) throws IOException{
		
		dos.writeInt(ERROR_TAG);
		
		File f = new File(directory + '/' + ERROR_FILE);
		
		if(f.exists() && f.length() > 0)
			sendFile(dos, f);
		
		else{
			
			dos.writeLong(ERROR_MSG.length());
			dos.flush();
			dos.write(ERROR_MSG.getBytes());
			dos.flush();
		}
	}
	
	private void sendFile(DataOutputStream dos, File f) throws IOException{
		
		FileInputStream fis = new FileInputStream(f);

        //sends file size
        dos.writeLong(f.length());
        dos.flush();
        
        int n;
        byte[] buf = new byte[BUF_SIZE];

        while((n = fis.read(buf)) != -1){

            dos.write(buf, 0, n);
            dos.flush();
        }
        
        fis.close();
	}
	
}
