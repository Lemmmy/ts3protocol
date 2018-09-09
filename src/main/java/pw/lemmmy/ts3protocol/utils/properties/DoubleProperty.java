package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class DoubleProperty extends Property<Double> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(name, Double.toString(getValue()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(Double.parseDouble(arguments.get(name)));
	}
}
