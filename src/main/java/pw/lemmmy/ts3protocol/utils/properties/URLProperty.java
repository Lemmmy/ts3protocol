package pw.lemmmy.ts3protocol.utils.properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public abstract class URLProperty extends Property<URL> {
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
			setValue(new URL(arguments.get(getName())));
		} catch (MalformedURLException e) {
			System.err.println("Error parsing URL from " + getName() + " property");
			e.printStackTrace();
		}
	}
}
