package robDex.util.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to manage the program's options for better use and readability.
 * 
 * 
 * @author Nicolas Fedy
 *
 * @param <T> type of the option
 */

abstract class Option<T> {

	/**
	 * names the user can use to refer to this option.
	 */
	private List<String> aliases;
	
	/**
	 * Option's value.
	 */
	private T value;
	
	/**
	 * {@code true} if the value has been modified by the user, {@code false} otherwise.
	 */
	private boolean modified;
	
	/**
	 * Creates an option.
	 * 
	 * @param defaultValue default value of this option.
	 * @param alias alias for this option.
	 * @param aliases additional aliases for this option.
	 */
	protected Option(T defaultValue, String alias, String... aliases){
		
		this.aliases = new ArrayList<>(aliases.length + 1);
		this.aliases.add(alias);
		this.aliases.addAll(Arrays.asList(aliases));
		this.value = defaultValue;
	}
	
	public void setValue(T value){
		this.value = value;
		this.modified = true;
	}
	
	public T getValue(){
		return value;
	}
	
	public boolean modified(){
		return modified;
	}
	
	/**
	 * Returns the value of the first alias belonging to this option.
	 * @param args the program's arguments
	 * @return the value of the first alias belonging to this option
	 * @throws IndexOutOfBoundsException if the alias doesn't have a value not beginning with '-' 
	 */
	protected String getFirstValue(String[] args) throws IndexOutOfBoundsException{
		
		for(int i = 0; i < args.length; i++)
			if(aliases.contains(args[i])){
				
				if(args[i + 1].charAt(0) == '-')
					throw new IndexOutOfBoundsException();
				
				return args[i + 1];
			}
		return null;
	}
	
	/**
	 * Scan {@code args} to look for any matching alias to this option, and sets its value accordingly.
	 * 
	 * @param args array to be scanned
	 * @throws Exception if the scanned option's value is invalid.
	 */
	public abstract void scan(String[] args) throws Exception;
}
