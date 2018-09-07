package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class IntProperty extends Property<Integer> {
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(getName(), Integer.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		setValue(Integer.parseInt(arguments.get(getName())));
	}
}
