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
		setValue(parseEnumProperty(getEnumClass(), arguments, getName()));
	}
	
	public static <E> E parseEnumProperty(Class<E> enumClass, int value) {
		return enumClass.getEnumConstants()[value];
	}
	
	public static <E> E parseEnumProperty(Class<E> enumClass, Map<String, String> arguments, String name) {
		return parseEnumProperty(enumClass, Integer.parseInt(arguments.get(name)));
	}
}
