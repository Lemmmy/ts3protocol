package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class IntProperty extends Property<Integer> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(name, Integer.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(Integer.parseInt(arguments.get(name)));
	}
}
