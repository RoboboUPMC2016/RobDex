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
	
	private static void compileIntoClass(String idir, String odir, List<String> fileNames) throws FailedCompilationException{
		
		String errorPath = idir + File.separator + errorFileName;
		
		startCommandProcess(idir, errorPath, fileNames, "javac", "-d", odir, "-cp", OptionManager.getJar());
	}
	
	private static void convertLambda(String idir, String odir, List<String> fileNames) throws FailedCompilationException{
		
		String classPath = "-Dretrolambda.classpath=" + idir + File.pathSeparatorChar + OptionManager.getClassPath();
		
		String rl = OptionManager.getRetroLambda();
		
		String retroDir = new File(rl).getAbsoluteFile().getParent();
		rl = new File(rl).getName();
		
		String inOption = "-Dretrolambda.inputDir=" + idir;
		String outOption = "-Dretrolambda.outputDir=" + odir;
		
		startCommandProcess(retroDir, idir + File.separator + errorFileName, null, "java", inOption, outOption, classPath, "-jar", rl);
	}
	
	public static void extractJar() throws FailedCompilationException{
		
		startCommandProcess(OptionManager.getClassPath(), null, "jar", "xf", OptionManager.getJar());
	}
	
	private static void compileIntoDex(String idir, String odir, List<String> fileNames) throws FailedCompilationException, IOException{
		
		startCommandProcess(idir, fileNames, OptionManager.getDx(), "--dex", "--output=" + odir + File.separator + dexFileName);
	}
	
	public static void compile(String dir, List<File> files) throws FailedCompilationException, IOException{

		String javacdir = dir + File.separator + "javacdir";
		File javacDir = new File(javacdir);
		javacDir.mkdirs();
		
		String java7Dir = dir + File.separator + "java7dir";
		new File(java7Dir).mkdirs();
		
		compileIntoClass(dir, javacdir, Util.getNames(files));
		
		List<String> classFileNames = Util.getNames(Arrays.asList(javacDir.listFiles()));
		classFileNames.removeIf(n -> !n.endsWith(".class"));
		
		convertLambda(javacdir, java7Dir, classFileNames);
		
		compileIntoDex(java7Dir, dir, classFileNames);
	}
}
