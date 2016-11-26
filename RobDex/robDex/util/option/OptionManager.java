package robDex.util.option;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OptionManager {
	
	private static List<Option<? extends Object>> options;
	private static StringOption dir, dx, jar;
	private static IntOption port;
	
	private OptionManager(){}
	
	public static void init(String[] args){
		
		options = new ArrayList<>();
		addOptions();
				
		for(Option<? extends Object> o : options)
			o.scan(args);
		
		checkDx();
		checkJar();
		
		new File(getDir()).mkdirs();
		checkExistence(getDir(), "");
	}
	
	public static String getDir(){
		return dir.getValue();
	}
	
	public static String getDx(){
		return dx.getValue();
	}
	
	public static String getJar(){
		return jar.getValue();
	}
	
	public static int getPort(){
		return port.getValue();
	}
	
	public static void addOptions(){
		
		port = new IntOption(5668, "-p", "--port");
		dir = new StringOption("." + File.separator +"tmp", "-d", "--directory");
		dx = new StringOption("dx","-x", "--executable");
		jar = new StringOption("", "-j", "--jar");
		
		options.add(port);
		options.add(dir);
		options.add(dx);
		options.add(jar);
	}
	
	private static void checkExistence(String fileName, String extension){
		
		if(!fileName.endsWith(extension))
			throw new IllegalArgumentException("File \"" + fileName + "\" does not match extension \"" + extension + "\".");
		
		File f = new File(fileName);
		
		if(!f.exists())
			throw new IllegalArgumentException("File \"" + fileName + "\" does not exist");
	}
	
	public static void checkDx() throws IllegalArgumentException{
				
		if(dx.modified())
			checkExistence(getDx(), isWindows() ? ".bat" : "");
		
		else
			checkDxCommand();
	}
	
	private static void checkDxCommand() throws IllegalArgumentException{
		
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			
			String command = isWindows() ? "where" : "which";
			
			proc = rt.exec(command + " dx");
			proc.waitFor();
			int exitVal = proc.exitValue();
			
			if(exitVal != 0)
				throw new IllegalArgumentException("dx command unreachable.");
			
		} catch (IOException | InterruptedException e) {

			throw new IllegalArgumentException("dx command unreachable", e);
		}
	}
	
	private static boolean isWindows(){
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	private static void checkJar(){
		
		if(getJar().equals(""))
			jar.setValue(lookForJar());
		
		checkExistence(getJar(), ".jar");
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
			throw new IllegalArgumentException("No jar file found in current directory.");
		
		if(r.length > 1)
			throw new IllegalArgumentException("Multiple jar files found in current directory.");
		
		return r[0];
	}
}
