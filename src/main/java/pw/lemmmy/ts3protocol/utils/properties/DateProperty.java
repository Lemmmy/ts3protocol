package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Date;
import java.util.Map;

public abstract class DateProperty extends Property<Date> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		if (getValue() == null) return;
		arguments.put(name, Long.toString(getValue().getTime() / 1000L));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		setValue(new Date(Long.parseLong(arguments.get(name)) * 1000L));
	}
}
