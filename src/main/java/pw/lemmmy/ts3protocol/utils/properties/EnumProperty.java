package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class EnumProperty<E extends Enum> extends Property<E> {
	public abstract Class<E> getEnumClass();
	
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(getName(), Integer.toString(getValue().ordinal()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		setValue(getEnumClass().getEnumConstants()[Integer.parseInt(arguments.get(getName()))]);
	}
}
