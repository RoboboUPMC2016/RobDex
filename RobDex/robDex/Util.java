package robDex;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import robDex.Exceptions.FailedCompilationException;

public class Util {

	private static final String PORT_OPTION = "-p";
	private static final String DIR_OPTION = "-d";
	private static final String JAR_OPTION = "-j";
	private static final String DX_OPTION = "-x";
	private static final long FILE_SIZE_MAX = 1000000; //1MB
	
	static String directory = "." + File.separator + "tmp";
	static String dxPath = "dx";
	static String jarPath;

	static String errorFileName = "error.log";
	static String dexFileName = "out.dex";
	static int port = 5668;
	
	public static void init(String[] args){
		
		List<String> a = Arrays.asList(args);
		
		updatePort(a);
		
		if(!a.contains(DX_OPTION))
			checkDxCommand();
		
		else{
			
			dxPath = update(a, DX_OPTION, dxPath);
			checkExistence(dxPath, "dx");
		}
		
		if(!a.contains(JAR_OPTION))
			jarPath = lookForJar();
		
		else{
			jarPath = update(a, JAR_OPTION, jarPath);
			checkExistence(jarPath, ".jar");
		}
		
		directory = update(a, DIR_OPTION, directory);

		new File(directory).mkdirs();
		checkExistence(directory, "");
	}
	
	private static String lookForJar(){
		
		File f = new File(".");
		
		FilenameFilter fnf = new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				return name.length() > 4 && name.endsWith(".jar");
			}
			
		};
		
		String[] r = f.list(fnf);
		
		if(r.length < 1)
			throw new RuntimeException("No jar file found in current directory.");
		
		if(r.length > 1)
			throw new RuntimeException("Multiple jar files found in current directory.");
		
		return r[0];
	}
	
	private static void checkDxCommand(){
		
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			
			proc = rt.exec("which dx");
			proc.waitFor();
			int exitVal = proc.exitValue();
			
			if(exitVal != 0)
				throw new RuntimeException("dx command unreachable.");
			
		} catch (IOException | InterruptedException e) {

			throw new RuntimeException(e);
		}
	}
	
	private static void checkExistence(String fileName, String extension){
		
		if(!fileName.endsWith(extension))
			throw new RuntimeException("File " + fileName + " does not match \"" + extension + "\".");
		
		File f = new File(fileName);
		
		if(!f.exists())
			throw new RuntimeException(fileName + " does not exist");
	}
	
	private static void updatePort(List<String> args){
		
		int index = args.indexOf(PORT_OPTION);
		
		if(index != -1)
			try{
				
				String portString = args.get(index + 1);
				
				if(portString.charAt(0) == '-')
					throw new IndexOutOfBoundsException();
				
				port = Integer.parseInt(portString);
			}
			
			catch(IndexOutOfBoundsException e){
				
				System.err.println("Option " + PORT_OPTION + " lacking argument. Option ignored.");
			}
		
			catch(NumberFormatException e){
				
				System.err.println("Argument of option " + PORT_OPTION + " \" " + args.get(index + 1) + " \" is not an integer. Option ignored.");
			}
	}
	
	private static String update(List<String> args, String option, String defaultValue){
		
		int index = args.indexOf(option);

		if(index != -1)
			try{
				
				String value = args.get(index + 1);
				
				if(value.charAt(0) == '-')
					throw new IndexOutOfBoundsException();
				
				return value;
			}
			
			catch(IndexOutOfBoundsException e){
				
				System.err.println("Option " + option + " lacking argument. Option ignored.");
			}
		
		return defaultValue;
	}
	
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
	
	private static List<String> getNames(List<File> files){
		
		List<String> names = new LinkedList<String>();
		
		for(File f : files){
			names.add(f.getName());
		}
		
		return names;
	}
	
	private static List<String> getClassFileNames(List<String> javaFileNames){
		
		List<String> names = new LinkedList<String>();

		for(String name : javaFileNames){
			
			String className = name.substring(0, name.length() - ".java".length());
			names.add(className + ".class");
		}
		
		return names;
	}
	
	private static void startCompilerProcess(List<String> command, String directory, String errorFileName) throws FailedCompilationException{
		
		ProcessBuilder pb = new ProcessBuilder(command);
		
		pb.directory(new File(directory));
		
		if(errorFileName != null){
			
			File errorFile = new File(directory + File.separator + errorFileName);
			pb.redirectError(errorFile);
		}
		
		try {
			Process p = pb.start();
			
			int exitStatus = p.waitFor();
						
			if(exitStatus != 0)
				throw new FailedCompilationException(exitStatus);
						
		} catch (IOException | InterruptedException e) {
			throw new FailedCompilationException(e);
		}
	}
	
	private static void startCompilerProcess(List<String> command, String directory) throws FailedCompilationException{
		
		startCompilerProcess(command, directory, null);
	}
	
	private static void compileIntoClass(String directory, List<String> fileNames) throws FailedCompilationException{
		
		List<String> command = new LinkedList<String>();
		
		command.add("javac");
		
		command.add("-cp");
		command.add(jarPath);
		
		command.addAll(fileNames);
		
		startCompilerProcess(command, directory, errorFileName);
	}
	
	private static void compileIntoDex(String directory, List<String> fileNames) throws FailedCompilationException, IOException{
		
		List<String> command = new LinkedList<String>();

		command.add(dxPath);
		command.add("--dex");
		command.add("--output=" + dexFileName);
		command.addAll(fileNames);
				
		startCompilerProcess(command, directory);
	}
	
	public static void compile(String directory, List<File> files) throws FailedCompilationException, IOException{
		
		List<String> javaFileNames = getNames(files);
		
		compileIntoClass(directory, javaFileNames);
		
		List<String> classFileNames = getClassFileNames(javaFileNames);
		
		compileIntoDex(directory, classFileNames);
	}
}
