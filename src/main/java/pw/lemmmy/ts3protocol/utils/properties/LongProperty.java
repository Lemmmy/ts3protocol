package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class LongProperty extends Property<Long> {
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(getName(), Long.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		setValue(Long.parseLong(arguments.get(getName())));
	}
}
