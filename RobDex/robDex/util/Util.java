package robDex.util;

import java.io.File;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Util {
	
	private static final long FILE_SIZE_MAX = 1000000; //1MB

	private Util(){}
	
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) 
	        for (File f : contents) 
	            deleteDir(f);  
	    
	    file.delete();
	}
	
	public static String filterFileName(String path){
		
		int n = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		
		return path.substring(n + 1);
	}
	
	public static String getclientIdentifier(Socket client){
		
		return Integer.toHexString(System.identityHashCode(client));
	}
	
	public static boolean isValid(String fileName, long size){
		
		return size < FILE_SIZE_MAX && isNameValid(fileName);
	}
	
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
	
	public static List<String> getNames(List<File> files){
		
		List<String> names = new LinkedList<String>();
		
		for(File f : files){
			names.add(f.getName());
		}
		
		return names;
	}
	
	public static List<String> getClassFileNames(List<String> javaFileNames){
		
		List<String> names = new LinkedList<String>();

		for(String name : javaFileNames){
			
			String className = name.substring(0, name.length() - ".java".length());
			names.add(className + ".class");
		}
		
		return names;
	}
}
