package robDex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import robDex.Exceptions.BadRequestException;
import robDex.Exceptions.FailedCompilationException;

/**
 * <p>
 * This class is used to receive the client's java sources, compile them into a .dx file, then send it back to them.
 * </p>
 * <p>
 * During the transfer, the server will check at mutiple times if the request is appropriate.<br />
 * The server will send to the client:<br />
 * 	{@code 0} if the request is valid,<br />
 * 	{@code -1} otherwise. In this case, the server will immediately close the connection.
 * </p>
 * <p>
 * When compiling is done, the server will send an answer to the client specifying if the compilation was successful:<br />
 * 	{@code 0} if the compilation is successful,<br />
 * 	{@code -1} otherwise.
 *
 * </p>
 * 
 * <p>
 * Except for the files content, all requests must end with a line feed ('\n') or a carriage return ('\r') to be read.<br/>
 * All answers given by the server end with a line feed.
 * </p>
 * <p>
 * The operations are as follows :<br />
 * <ol>
 * <li>A handshaking sets place to ensure trust with the client.</li>
 * <li>The server reads the number of files to be received.</li>
 * <li>An answer is given to the client based on the validity of the request.</li>
 * <li>Files are then received ; each file is read this way :
 * 		<ol>
 * 		<li>The name of the file is received.</li>
 * 		<li>The size of the file is received.</li>
 * 		<li>An answer is given to the client based on the validity of the file.</li>
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
	
	private static final int BUF_SIZE = 4096, ERROR_TAG = -1, SUCCESS_TAG = 0, MAX_FILES = 100;
	private static final String OUTPUT_FILE = "out.dex", ERROR_FILE = "error.log";
	private static final String ERROR_MSG = "An error unrelated to the java compilation occured.";
	private String directory;
	protected final Socket client;
	private ArrayList<File> files;
	
	public Request(Socket client){
		
		this.client = client;
		this.directory = Util.directory + File.separator + Util.getclientIdentifier(client);		
	}
	
	@Override
	public void run(){
		
		DataBufferedReader in = null;
		DataPrintWriter out = null;
		
		try {
			
			in = new DataBufferedReader(client.getInputStream());
			out = new DataPrintWriter(client.getOutputStream());
			
			if(handshaking()){
				
				receiveFiles(in, out);
								
				try {
					
					Util.compile(directory, files);
					sendDexFile(out);
				} catch (FailedCompilationException e) {
					sendErrorFile(out);
				}	
			}
		} catch (IOException e) {
			//TODO printstacktrace on verbose
		} catch (BadRequestException e) {
			//TODO printstacktrace on verbose
		}
		
		finally{
			
			try {
				
				in.close();
				out.close();
				client.close();
				Util.deleteDir(new File(directory));
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
			
			catch(Exception e){
				//TODO add verbose
			}
		}
	}
	
	public void closeConnection() throws IOException{
		client.close();
	}
	
	private boolean handshaking(){
		
		return true;
	}
	
	private void receiveFiles(DataBufferedReader in, PrintWriter out) throws IOException, BadRequestException{
				
    	int filesCount;
    	
    	filesCount = in.readInt();
    	
    	try{
    		
	    	if(filesCount > MAX_FILES)
	    		throw new BadRequestException();
			 
	    	out.println(SUCCESS_TAG);
	    	out.flush();
	    	
			files = new ArrayList<File>(filesCount);
	        
	        int n = 0;
	        char[] buf = new char[BUF_SIZE];
	        
	        //creates repertories to store client files
	        new File(directory).mkdirs();
	
	        for(int i = 0; i < filesCount; i++){
	                    	
	            String fileName = Util.filterFileName(in.readLine());
	                        
	            long fileSize = in.readLong();
	            	            	
            	if(!Util.isValid(fileName, fileSize))
            		throw new BadRequestException();
            	
            	out.println(SUCCESS_TAG);
            	out.flush();

            	File file = new File(directory + File.separator + fileName);
                
                files.add(file);
                
                PrintWriter pw = new PrintWriter(file);
                
                String s;
                                                
                while (fileSize > 0 && (n = in.read(buf)) != -1){
                	
                	s = new String(buf, 0, n);
                	pw.print(s);
                	
                	fileSize -= n;
                }
                
                pw.close();    
	                      
	        }
	        
    	} catch (BadRequestException e){
    		
    		out.println(ERROR_TAG);
        	out.flush();
        	throw e;
    	}
	}
	
	public void sendDexFile(DataPrintWriter out) throws IOException{
		
		out.println(SUCCESS_TAG);
		sendFile(out, new File(directory + File.separator + OUTPUT_FILE));
	}
	
	/**
	 * Sends the error file to the client. If the error log file is empty, a default message will be sent.
	 * 
	 * @param out the socket's output stream
	 * @throws IOException if an IO issue appears
	 */
	private void sendErrorFile(DataPrintWriter out) throws IOException{
		
		out.println(ERROR_TAG);
		
		File f = new File(directory + File.separator + ERROR_FILE);
		
		if(f.exists() && f.length() > 0)
			sendFile(out, f);
		
		else{
			
			out.println(ERROR_MSG.length());
			out.flush();
			out.println(ERROR_MSG);
			out.flush();
		}
	}
	
	private void sendFile(DataPrintWriter out, File f) throws IOException{
		
		FileInputStream fis = new FileInputStream(f);

        //sends file size
        out.println(f.length());
        out.flush();
        
        byte[] buf = new byte[BUF_SIZE];
        String s;
        int n;

        while((n = fis.read(buf)) != -1){

        	s = new String(buf, 0, n, "UTF-8");
        	
            //out.print(s);
            out.write(buf, 0, n);
            out.flush();
        }
        
        fis.close();
	}
}
