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
	
	protected boolean fromRootSet;
	
	@Setter private PropertyManager manager;
	
	public Property<T> setValue(T value) {
		this.value = value;
		markChanged();
		return this;
	}
	
	/**
	 * For bean properties. Calling {@link #setValue} already marks a property as changed.
	 */
	public void markChanged() {
		changed = true;
		manager.notifyPropertyChange(this);
	}
	
	public abstract void encodeProperty(Map<String, String> arguments);
	
	public abstract void decodeProperty(Map<String, String> arguments);
}
