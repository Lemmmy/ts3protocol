package pw.lemmmy.ts3protocol.utils.properties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public abstract class InetAddressProperty extends Property<InetAddress> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		if (getValue() == null) return;
		arguments.put(name, getValue().toString());
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		
		try {
			setValue(InetAddress.getByName(arguments.get(name)));
		} catch (UnknownHostException e) {
			System.err.println("Error parsing InetAddress from " + name + " property");
			e.printStackTrace();
		}
	}
}
