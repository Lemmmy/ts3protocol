package pw.lemmmy.ts3protocol.utils.properties;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

public abstract class InetAddressProperty extends Property<InetAddress> {
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		if (getValue() == null) return;
		arguments.put(getName(), getValue().toString());
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		
		try {
			setValue(InetAddress.getByName(arguments.get(getName())));
		} catch (UnknownHostException e) {
			System.err.println("Error parsing InetAddress from " + getName() + " property");
			e.printStackTrace();
		}
	}
}
