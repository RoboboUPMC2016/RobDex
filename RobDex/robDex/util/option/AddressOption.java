package robDex.util.option;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddressOption extends Option<InetAddress> {

	public AddressOption(InetAddress defaultValue, String alias, String... aliases) {
		super(defaultValue, alias, aliases);
	}

	@Override
	public void scan(String[] args) throws UnknownHostException {

		String s = getFirstValue(args);
		
		if(s != null)
			setValue(InetAddress.getByName(s));
	}

}
