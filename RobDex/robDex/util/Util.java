package robDex.util;

import java.io.File;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * This class is used as a set of static functions to be used by any method.
 * 
 * @author Nicolas Fedy
 */

public class Util {
	
	/**
	 * Maximum size of a java file to be received by the server.
	 */
	private static final long FILE_SIZE_MAX = 1000000; //1MB

	//Prevents instantiation
	private Util(){}
	
	/**
	 * Deletes a directory and its content, recursively.
	 * 
	 * @param file The directory to be deleted.
	 */
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) 
	        for (File f : contents) 
	            deleteDir(f);  
	    
	    file.delete();
	}
	
	/**
	 * Returns the name of the file denoted by pathname given as parameter.
	 * 
	 * @param path The pathname from which the name of the file is to be extracted.
	 * @return The name of the file
	 */
	public static String filterFileName(String path){
		
		return new File(path).getName();
	}
	
	/**
	 * Returns a client identifier to differentiate between clients content. 
	 * 
	 * @param client The socket representing the client
	 * @return an Identifier for the client.
	 */
	public static String getclientIdentifier(Socket client){
		
		return Integer.toHexString(System.identityHashCode(client));
	}
	
	/**
	 * Checks if the name and size of the file are valid.
	 * 
	 * @param fileName The name of the file
	 * @param size The size of the file
	 * @return {@code true} if the name and size are both valid, {@code false} otherwise.
	 */
	public static boolean isValid(String fileName, long size){
		
		return size < FILE_SIZE_MAX && isNameValid(fileName);
	}
	
	/**
	 * Checks if the name of the file is valid.
	 * 
	 * @param fileName The name of the file.
	 * @return {@code true} if the name is valid, {@code false} otherwise.
	 */
	public static boolean isNameValid(String fileName){
		
		// =".java".length()
		int n = 5;
				
		if(fileName.length() <= 5 || !fileName.endsWith(".java"))
			return false;
		
		boolean start = true;
        boolean validIdentifier = true;
        
        String className = fileName.substring(0, fileName.length() - n);
        
        
        for(char c : className.toCharArray()) {

        	if(start) {
                validIdentifier = validIdentifier && Character.isJavaIdentifierStart(c);
                start = false;
            } 
        	
        	else 
                validIdentifier = validIdentifier && Character.isJavaIdentifierPart(c);
        }
        
        return validIdentifier;
	}
	
	/**
	 * Returns a list containing the names of a list of files
	 * @param files The list of files from which the names are to be extracted.
	 * @return The list containing the names of the files
	 */
	public static List<String> getNames(List<File> files){
		
		List<String> names = new LinkedList<String>();
		
		for(File f : files){
			names.add(f.getName());
		}
		
		return names;
	}
}
