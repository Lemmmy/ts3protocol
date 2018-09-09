package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class LongProperty extends Property<Long> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(name, Long.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(Long.parseLong(arguments.get(name)));
	}
}
