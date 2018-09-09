package pw.lemmmy.ts3protocol.utils.properties;

import lombok.Getter;
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
		notifyCommands.forEach(n -> client.getCommandHandler().addCommandListener(n, this::readFromCommand));
	}
	
	public <T> void addChangeListener(Class<? extends Property<T>> property, ChangeListener<T> listener) {
		if (!changeListeners.containsKey(property)) {
			changeListeners.put(property, new ArrayList<>());
		}
		
		changeListeners.get(property).add(listener);
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
				p.decodeProperty(arguments);
			} catch (Exception err) {
				System.err.println("Error decoding property " + p.getClass().getSimpleName() + " from command " + command.getName());
				err.printStackTrace();
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
