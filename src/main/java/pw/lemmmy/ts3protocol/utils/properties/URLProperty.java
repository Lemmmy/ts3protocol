package pw.lemmmy.ts3protocol.utils.properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public abstract class URLProperty extends Property<URL> {
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
			setValue(new URL(arguments.get(name)));
		} catch (MalformedURLException e) {
			System.err.println("Error parsing URL from " + name + " property");
			e.printStackTrace();
		}
	}
}
