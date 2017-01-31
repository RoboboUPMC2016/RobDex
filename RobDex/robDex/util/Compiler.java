package robDex.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import robDex.exceptions.FailedCompilationException;
import robDex.util.option.OptionManager;

/**
 * This class is used to compile java files into a DEX file.
 * 
 * 
 * @author Nicolas Fedy
 *
 */

public class Compiler {
	
	private static final String errorFileName = "error.log";
	private static final String dexFileName = "out.dex";
	
	// Suppresses default constructor, ensuring non-instantiability.
	private Compiler(){}
	
	/**
	 * Starts a process and waits for its death.
	 * 
	 * @param directory directory in which the process must be launched.
	 * @param errorFileName name of the file to redirect errors. If {@code null} errors will be ignored.
	 * @param command the command with its arguments to be launched.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
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
	
	/**
	 * Start a process with {@code files} as arguments and waits for its death.
	 * 
	 * 
	 * @param dir directory in which the process must be launched.
	 * @param errorFile name of the file to redirect errors. If {@code null} errors will be ignored.
	 * @param files files' names to be added as arguments.
	 * @param command command to be launched.
	 * @param options command's additional arguments.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	private static void startCommandProcess(String dir, String errorFile, List<String> files, String command, String... options) throws FailedCompilationException{
		
		List<String> l = new ArrayList<>();
		l.add(command);
		l.addAll(Arrays.asList(options));
		
		if(files != null)
			l.addAll(files);
		
		startCommandProcess(dir, errorFile, l);
	}
	
	/**
	 * Start a process with {@code files} as arguments and waits for its death.
	 * Errors will be ignored.
	 * 
	 * 
	 * @param dir directory in which the process must be launched.
	 * @param files files' names to be added as arguments.
	 * @param command command to be launched.
	 * @param options command's additional arguments.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	private static void startCommandProcess(String dir, List<String> files, String command, String... options) throws FailedCompilationException{
		
		startCommandProcess(dir, null, files, command, options);
	}
	
	/**
	 * Compile a list of java files into class files.
	 * 
	 * @param idir java files' location.
	 * @param odir directory in which the class files will be located.
	 * @param fileNames java files' names.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	private static void compileIntoClass(String idir, String odir, List<String> fileNames) throws FailedCompilationException{
		
		String errorPath = idir + File.separator + errorFileName;
		
		startCommandProcess(idir, errorPath, fileNames, "javac", "-d", odir, "-cp", OptionManager.getJar());
	}
	
	/**
	 * Convert java 8 class files to java 7 class files.
	 * 
	 * @param idir java 8 class files' location.
	 * @param odir directory in which the java 7 class files will be located.
	 * @param fileNames class files' names.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	private static void convertLambda(String idir, String odir, List<String> fileNames) throws FailedCompilationException{
		
		String classPath = "-Dretrolambda.classpath=" + idir + File.pathSeparatorChar + OptionManager.getClassPath();
		
		String rl = OptionManager.getRetroLambda();
		
		String retroDir = new File(rl).getAbsoluteFile().getParent();
		rl = new File(rl).getName();
		
		String inOption = "-Dretrolambda.inputDir=" + idir;
		String outOption = "-Dretrolambda.outputDir=" + odir;
		
		startCommandProcess(retroDir, idir + File.separator + errorFileName, null, "java", inOption, outOption, classPath, "-jar", rl);
	}
	
	/**
	 * Extracts the content of a jar file.
	 * 
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	public static void extractJar() throws FailedCompilationException{
		
		startCommandProcess(OptionManager.getClassPath(), null, "jar", "xf", OptionManager.getJar());
	}
	
	/**
	 * Compiles a list of class files  into a DEX file.
	 * If class files are compiled using java 8, the result is undefined.
	 * 
	 * @param idir class files' location.
	 * @param odir directory in which the DEX file will be located.
	 * @param fileNames class files' names.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	private static void compileIntoDex(String idir, String odir, List<String> fileNames) throws FailedCompilationException{
		
		startCommandProcess(idir, fileNames, OptionManager.getDx(), "--dex", "--output=" + odir + File.separator + dexFileName);
	}
	
	/**
	 * Compiles a list of java files into a DEX file.
	 * 
	 * @param dir java files' location.
	 * @param files java files' names.
	 * @throws FailedCompilationException if the process' exit status is different from {@code 0}.
	 */
	public static void compile(String dir, List<File> files) throws FailedCompilationException{

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
