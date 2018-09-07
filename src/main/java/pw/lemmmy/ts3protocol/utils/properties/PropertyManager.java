package pw.lemmmy.ts3protocol.utils.properties;

import lombok.Getter;
import org.apache.commons.lang.reflect.ConstructorUtils;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;
import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.packets.command.PacketCommand;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("unchecked")
public class PropertyManager {
	private Map<Class<? extends Property>, Property<?>> properties = new HashMap<>();
	private Map<Class<? extends Property>, List<ChangeListener<?>>> changeListeners = new HashMap<>();
	
	private Class<? extends CommandUpdateProperties> updateCommand;
	private Class<? extends CommandNotifyProperties> notifyCommand;
	
	private Client client;
	
	public PropertyManager(Client client, Class<? extends CommandUpdateProperties> updateCommand, Class<? extends CommandNotifyProperties> notifyCommand) {
		this.updateCommand = updateCommand;
		this.notifyCommand = notifyCommand;
		
		this.client = client;
		
		addCommandHandlers(client);
	}
	
	private void addCommandHandlers(Client client) {
		client.getCommandHandler().addCommandListener(notifyCommand, this::readFromCommand);
	}
	
	public <T> void addChangeListener(Class<? extends Property<T>> property, ChangeListener<T> listener) {
		if (!changeListeners.containsKey(property)) {
			changeListeners.put(property, new ArrayList<>());
		}
		
		changeListeners.get(property).add(listener);
	}
	
	public <T, C> T get(Class<? extends Property<T>> property) {
		return (T) properties.get(property).getValue();
	}
	
	public void add(Property property) {
		property.setManager(this);
		properties.put(property.getClass(), property);
	}
	
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
				e.printStackTrace();
			}
		}
		
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
			
			client.getPacketHandler().send(new PacketCommand(command));
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void readFromCommand(Command command) {
		properties.values().forEach(p -> p.decodeProperty(command.getArguments()));
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
