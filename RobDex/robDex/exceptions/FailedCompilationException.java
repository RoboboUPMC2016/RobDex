package robDex.exceptions;

public class FailedCompilationException extends Exception{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Exit status of the process running the failed compilation
	 */
	private int status;
	
	public FailedCompilationException(){}
	
	public FailedCompilationException(int status){
		this.status = status;
	}
	
	public FailedCompilationException(String message){
		super(message);
	}
	
	public FailedCompilationException(Throwable cause){
		super(cause);
	}
	
	public FailedCompilationException(String message, Throwable cause){
		super(cause);
	}	
	
	public String toString(){
		return super.toString() + "\nerror : " + status;
	}
}
