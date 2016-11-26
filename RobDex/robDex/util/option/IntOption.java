package robDex.util.option;

class IntOption extends Option<Integer> {

	public IntOption(int defaultValue, String alias, String... aliases) {
		super(defaultValue, alias, aliases);
	}

	@Override
	public void scan(String[] args) throws IndexOutOfBoundsException, NumberFormatException{

		String s = getFirstValue(args);
		
		if(s != null)
			setValue(Integer.parseInt(s));
	}

}
