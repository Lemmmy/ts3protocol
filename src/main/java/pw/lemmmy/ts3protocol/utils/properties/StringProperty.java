package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class StringProperty extends Property<String> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(name, getValue());
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(arguments.get(name));
	}
}
