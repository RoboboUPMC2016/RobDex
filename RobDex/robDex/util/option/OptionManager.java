package robDex.util.option;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import robDex.util.Compiler;

/**
 * This class is used to manage the program's options.
 * 
 * @author Nicolas Fedy
 *
 */

public class OptionManager {
	
	private static List<Option<? extends Object>> options;
	private static StringOption dir, dx, jar, rlambda;
	private static IntOption port;
	private static AddressOption host;
	private static String classPath;
	
	private OptionManager(){}
	
	public static void init(String[] args) throws Exception{
		
		options = new ArrayList<>();
		addOptions();
				
		for(Option<? extends Object> o : options)
			o.scan(args);
		
		checkDx();
		checkJar();
		
		checkExistence(getRetroLambda(), ".jar");
		
		String d = getDir();
		
		new File(d).mkdirs();
		checkExistence(d);
		dir.setValue(getAbsolute(d));
		
		classPath = d + File.separator + "contentJar";
		new File(classPath).mkdir();
		checkExistence(classPath);
		classPath = getAbsolute(classPath);
		
		Compiler.extractJar();
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
	
	public static String getRetroLambda(){
		return rlambda.getValue();
	}
	
	public static InetAddress getHost(){
		return host.getValue();
	}
	
	public static String getClassPath(){
		return classPath;
	}
	
	public static void addOptions() throws Exception{
				
		port = new IntOption(5668, "-p", "--port");
		dir = new StringOption("." + File.separator + "tmp", "-d", "--directory");
		dx = new StringOption("dx","-x", "--executable");
		jar = new StringOption("", "-j", "--jar");
		rlambda = new StringOption(".", "-r", "--retroLambda");
		host = new AddressOption(InetAddress.getByName("127.0.0.1"), "-h", "--host");
		
		options.addAll(Arrays.asList(port, dir, dx, jar, rlambda, host));
	}
	
	private static void checkExistence(String fileName, String extension){
		
		if(!fileName.endsWith(extension))
			throw new IllegalArgumentException("File \"" + fileName + "\" does not match extension \"" + extension + "\".");
		
		File f = new File(fileName);
		
		if(!f.exists())
			throw new IllegalArgumentException("File \"" + fileName + "\" does not exist");
	}
	
	private static void checkExistence(String fileName){
		checkExistence(fileName, "");
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
			jar.setValue(lookForJar("retrolambda", false));
		
		if(getRetroLambda().equals(""))
			rlambda.setValue(lookForJar("retrolambda", true));
		
		checkExistence(getJar(), ".jar");
	}
	
	private static String lookForJar(String nameContent, boolean mustContain){
		
		File f = new File(".");
		
		FilenameFilter fnf = new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				boolean b = name.contains(nameContent) && mustContain || !name.contains(nameContent) && !mustContain;
				return name.length() > 4 && name.endsWith(".jar") && b;
			}
			
		};
		
		String[] r = f.list(fnf);
		
		if(r.length < 1)
			throw new IllegalArgumentException("No jar file found in current directory.");
		
		if(r.length > 1)
			throw new IllegalArgumentException("Multiple jar files found in current directory.");
		
		return r[0];
	}
	
	private static String getAbsolute(String path){
		
		return new File(path).getAbsolutePath();
	}
}
