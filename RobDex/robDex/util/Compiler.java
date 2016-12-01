package robDex.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import robDex.exceptions.FailedCompilationException;
import robDex.util.option.OptionManager;

public class Compiler {
	
	private static final String errorFileName = "error.log";
	private static final String dexFileName = "out.dex";
	
	// Suppresses default constructor, ensuring non-instantiability.
	private Compiler(){}
	
	private static void startCommandProcess(String directory, String errorFileName, List<String> command) throws FailedCompilationException{
		
		ProcessBuilder pb = new ProcessBuilder(command);
		
		pb.directory(new File(directory));
		
		if(errorFileName != null)
			pb.redirectError(new File(errorFileName));
		
		try {
			Process p = pb.start();
			
			int exitStatus = p.waitFor();
									
			if(exitStatus != 0)
				throw new FailedCompilationException(exitStatus);
						
		} catch (IOException | InterruptedException e) {
			throw new FailedCompilationException(e);
		}
	}
	
	private static void startCommandProcess(String dir, String errorFile, List<String> files, String command, String... options) throws FailedCompilationException{
		
		List<String> l = new ArrayList<>();
		l.add(command);
		l.addAll(Arrays.asList(options));
		
		if(files != null)
			l.addAll(files);
		
		startCommandProcess(dir, errorFile, l);
	}
	
	private static void startCommandProcess(String dir, List<String> files, String command, String... options) throws FailedCompilationException{
		
		startCommandProcess(dir, null, files, command, options);
	}
	
	private static void compileIntoClass(String dir, List<String> fileNames) throws FailedCompilationException{
		
		startCommandProcess(dir, dir + File.separator + errorFileName, fileNames, "javac", "-cp", OptionManager.getJar());
	}
	
	private static void convertLambda(String dir, List<String> fileNames) throws FailedCompilationException{
				
		String classPath = "-Dretrolambda.classpath=" + dir + File.pathSeparatorChar + OptionManager.getClassPath();
		
		String rl = OptionManager.getRetroLambda();
		
		String retroDir = new File(rl).getAbsoluteFile().getParent();
		rl = new File(rl).getName();
		
		startCommandProcess(retroDir, dir + File.separator + errorFileName, null, "java", "-Dretrolambda.inputDir=" + dir, classPath, "-jar", rl);
	}
	
	public static void extractJar() throws FailedCompilationException{
		
		startCommandProcess(OptionManager.getClassPath(), null, "jar", "xf", OptionManager.getJar());
	}
	
	private static void compileIntoDex(String dir, List<String> fileNames) throws FailedCompilationException, IOException{
		
		startCommandProcess(dir, fileNames, OptionManager.getDx(), "--dex", "--output=" + dexFileName);
	}
	
	public static void compile(String dir, List<File> files) throws FailedCompilationException, IOException{

		List<String> javaFileNames = Util.getNames(files);
		
		compileIntoClass(dir, javaFileNames);
		
		List<String> classFileNames = Util.getClassFileNames(javaFileNames);
		
		convertLambda(dir, classFileNames);
		
		compileIntoDex(dir, classFileNames);
	}
}
