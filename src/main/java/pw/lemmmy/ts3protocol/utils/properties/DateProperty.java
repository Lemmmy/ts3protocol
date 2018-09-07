package pw.lemmmy.ts3protocol.utils.properties;

import java.util.Date;
import java.util.Map;

public abstract class DateProperty extends Property<Date> {
	public abstract String getName();
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		if (getValue() == null) return;
		arguments.put(getName(), Long.toString(getValue().getTime() / 1000L));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(getName())) return;
		setValue(new Date(Long.parseLong(arguments.get(getName())) * 1000L));
	}
}
