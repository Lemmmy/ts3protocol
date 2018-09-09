package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class DoubleProperty extends Property<Double> {
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(getName(), Double.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		setValue(Double.parseDouble(arguments.get(getName())));
	}
}
