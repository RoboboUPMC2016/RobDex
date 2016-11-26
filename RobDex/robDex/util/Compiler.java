package robDex.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import robDex.Exceptions.FailedCompilationException;
import robDex.util.option.OptionManager;

public class Compiler {
	
	private static final String errorFileName = "error.log";
	private static final String dexFileName = "out.dex";
	
	// Suppresses default constructor, ensuring non-instantiability.
	private Compiler(){}
	
	private static void startCompilerProcess(String directory, String errorFileName, List<String> command) throws FailedCompilationException{
		
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
	
	private static void startCompilerProcess(String dir, String errorFile, List<String> files, String command, String... options) throws FailedCompilationException{
		
		List<String> l = new ArrayList<>();
		l.add(command);
		l.addAll(Arrays.asList(options));
		l.addAll(files);
		
		startCompilerProcess(dir, errorFile, l);
	}
	
	private static void compileIntoClass(String directory, List<String> fileNames) throws FailedCompilationException{
		
		startCompilerProcess(directory, errorFileName, fileNames, "javac", "-cp", OptionManager.getJar(), "-source", "1.7", "-target", "1.7");
	}
	
	private static void compileIntoDex(String directory, List<String> fileNames) throws FailedCompilationException, IOException{
		
		startCompilerProcess(directory, null, fileNames, OptionManager.getDx(), "--dex", "--output=" + dexFileName);
	}
	
	public static void compile(String directory, List<File> files) throws FailedCompilationException, IOException{

		List<String> javaFileNames = Util.getNames(files);
		
		compileIntoClass(directory, javaFileNames);
		
		List<String> classFileNames = Util.getClassFileNames(javaFileNames);
		
		compileIntoDex(directory, classFileNames);
	}
}
