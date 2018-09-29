package pw.lemmmy.ts3protocol.utils.properties;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Slf4j
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
			log.error("Error parsing URL from {} property", name, e);
		}
	}
}
