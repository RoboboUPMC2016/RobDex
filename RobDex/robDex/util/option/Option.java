package robDex.util.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class Option<T> {

	private List<String> aliases;
	private T value;
	private boolean modified;
	
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
	
	public abstract void scan(String[] args);
}
