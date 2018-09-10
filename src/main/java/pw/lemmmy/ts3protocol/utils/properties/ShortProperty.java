package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class ShortProperty extends Property<Short> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(name, Short.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(Short.parseShort(arguments.get(name)));
	}
}
