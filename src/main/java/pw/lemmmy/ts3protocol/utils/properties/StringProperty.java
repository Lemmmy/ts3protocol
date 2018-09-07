package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class StringProperty extends Property<String> {
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(getName(), getValue());
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		setValue(arguments.get(getName()));
	}
}
