package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class BooleanProperty extends Property<Boolean> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		if (getValue() == null) return;
		arguments.put(name, Integer.toString(getValue() ? 1 : 0));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(Integer.parseInt(arguments.get(name)) == 1);
	}
}
