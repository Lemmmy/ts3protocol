package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Map;

public abstract class EnumProperty<E extends Enum> extends Property<E> {
	protected Class<E> enumClass;
	
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		arguments.put(name, Integer.toString(getValue().ordinal()));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(parseEnumProperty(enumClass, arguments, name));
	}
	
	public static <E> E parseEnumProperty(Class<E> enumClass, int value) {
		return enumClass.getEnumConstants()[value];
	}
	
	public static <E> E parseEnumProperty(Class<E> enumClass, Map<String, String> arguments, String name) {
		return parseEnumProperty(enumClass, Integer.parseInt(arguments.get(name)));
	}
}
