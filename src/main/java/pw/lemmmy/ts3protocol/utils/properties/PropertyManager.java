package pw.lemmmy.ts3protocol.utils.properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.reflect.ConstructorUtils;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;
import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.packets.command.PacketCommand;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
@SuppressWarnings("unchecked")
public class PropertyManager {
	private Map<Class<? extends Property>, Property<?>> properties = new HashMap<>();
	private Map<Class<? extends Property>, List<ChangeListener<?>>> changeListeners = new HashMap<>();
	
	private Class<? extends CommandUpdateProperties> updateCommand;
	private List<Class<? extends CommandNotifyProperties>> notifyCommands = new ArrayList<>();
	
	private Client client;
	
	public PropertyManager(Client client,
						   Class<? extends CommandUpdateProperties> updateCommand,
						   Class<? extends CommandNotifyProperties>... notifyHandlers) {
		this.client = client;
		
		this.updateCommand = updateCommand;
		
		notifyCommands.addAll(Arrays.asList(notifyHandlers));
		notifyCommands.forEach(n -> client.commandHandler.addCommandListener(n, this::readFromCommand));
	}
	
	public <T> ChangeListener<T> addChangeListener(Class<? extends Property<T>> property, ChangeListener<T> listener) {
		if (!changeListeners.containsKey(property)) {
			changeListeners.put(property, new ArrayList<>());
		}
		
		changeListeners.get(property).add(listener);
		return listener;
	}
	
	public <T> void removeChangeListener(Class<? extends Property<T>> property, ChangeListener<T> listener) {
		if (!changeListeners.containsKey(property)) return;
		changeListeners.get(property).remove(listener);
	}
	
	public <T> T get(Class<? extends Property<T>> property) {
		if (!properties.containsKey(property)) return null;
		return (T) properties.get(property).getValue();
	}
	
	public PropertyManager add(Property... props) {
		for (Property property : props) {
			property.setManager(this);
			properties.put(property.getClass(), property);
		}
		
		return this;
	}
	
	/**
	 * Sets a property on the object. Run {@link #flush()} to synchronise the properties to the server, or if you are
	 * only setting one property, you can use {@link #setInstantly(Class, Object)}.
	 *
	 * @param property The {@link Property} to set the value of. Must be a valid property of the destination object.
	 * @param value The value to assign to the property.
	 * @param <T> The type of the property.
	 * @return This {@link PropertyManager} (for method chaining).
	 */
	public <T> PropertyManager set(Class<? extends Property<T>> property, T value) {
		if (properties.containsKey(property)) {
			((Property<T>) properties.get(property)).setValue(value);
		} else {
			try {
				Property<T> propertyInstance = (Property<T>) ConstructorUtils.invokeConstructor(property, null);
				propertyInstance.setValue(value);
				propertyInstance.setManager(this);
				properties.put(property, propertyInstance);
			} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				log.error("Error setting property {}", property.getSimpleName(), e);
			}
		}
		
		return this;
	}
	
	/**
	 * Sets a property on the object and immediately flushes it to the server.
	 *
	 * @param property The {@link Property} to set the value of. Must be a valid property of the destination object.
	 * @param value The value to assign to the property.
	 * @param <T> The type of the property.
	 * @return This {@link PropertyManager} (for method chaining).
	 *
	 * @see #set(Class, Object)
	 */
	public <T> PropertyManager setInstantly(Class<? extends Property<T>> property, T value) {
		set(property, value);
		flush();
		return this;
	}
	
	public void flush() {
		if (updateCommand == null) return;
		
		try {
			Command command = (Command) ConstructorUtils.invokeConstructor(
				updateCommand,
				properties.values().stream()
					.filter(Property::isChanged)
					.peek(p -> p.setChanged(false))
					.collect(Collectors.toList())
			);
			
			handleFlushCommand(command);
			
			client.packetHandler.send(new PacketCommand(command));
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			log.error("Error sending update command while flushing properties", e);
		}
	}
	
	protected void handleFlushCommand(Command command) {}
	
	protected boolean shouldReadCommand(Command command, Map<String, String> arguments) {
		return true;
	}
	
	public void readFromCommand(Command command) {
		command.getArgumentSets().forEach(arguments -> readFromArgumentSet(command, arguments));
	}
	
	public void readFromArgumentSet(Command command, Map<String, String> arguments) {
		if (!shouldReadCommand(command, arguments)) return;
		
		properties.values().forEach(p -> {
			try {
				boolean alreadyChanged = p.isChanged();
				p.decodeProperty(p.fromRootSet ? command.getArguments(0) : arguments);
				if (!alreadyChanged) p.setChanged(false);
			} catch (Exception e) {
				log.error("Error decoding property {} from command {}", p.getClass().getSimpleName(), command.getName(), e);
			}
		});
	}
	
	protected <T> void notifyPropertyChange(Property<T> property) {
		if (!changeListeners.containsKey(property.getClass())) return;
		
		changeListeners.get(property.getClass())
			.forEach(l -> ((ChangeListener<T>) l).onChange(property));
	}
	
	@FunctionalInterface
	public interface ChangeListener<T> {
		void onChange(Property<T> property);
	}
}
