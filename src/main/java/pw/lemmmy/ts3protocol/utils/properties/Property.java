package pw.lemmmy.ts3protocol.utils.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Accessors(chain=true)
public abstract class Property<T> {
	private T value;
	@Setter private boolean changed;
	
	@Setter private PropertyManager manager;
	
	public Property<T> setValue(T value) {
		this.value = value;
		changed = true;
		manager.notifyPropertyChange(this);
		return this;
	}
	
	public abstract void encodeProperty(Map<String, String> arguments);
	
	public abstract void decodeProperty(Map<String, String> arguments);
}
