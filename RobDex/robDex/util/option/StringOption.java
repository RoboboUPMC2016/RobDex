package robDex.util.option;

class StringOption extends Option<String>{

	public StringOption(String defaultValue, String alias, String... aliases) {
		super(defaultValue, alias, aliases);
	}

	@Override
	public void scan(String[] args) throws IndexOutOfBoundsException{
		
		String s = getFirstValue(args);
		
		if(s != null)
			setValue(s);
	}
}
