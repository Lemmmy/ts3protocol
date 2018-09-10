package pw.lemmmy.ts3protocol.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandHandler {
	private Map<Class<? extends Command>, Set<CommandListener>> commandListenersQueue = new HashMap<>();
	private Map<Class<? extends Command>, Set<CommandListener>> commandListeners = new HashMap<>();
	
	public <T extends Command> void addCommandListener(Class<T> commandClass, CommandListener<T> listener) {
		if (!commandListenersQueue.containsKey(commandClass)) {
			commandListenersQueue.put(commandClass, new HashSet<>());
		}
		
		commandListenersQueue.get(commandClass).add(listener);
	}
	
	public void handleCommand(Command command) {
		commandListenersQueue.forEach((commandClass, commands) -> {
			if (!commandListeners.containsKey(commandClass)) {
				commandListeners.put(commandClass, commands);
			} else {
				commandListeners.get(commandClass).addAll(commands);
			}
		});
		commandListenersQueue.clear();
		
		if (!commandListeners.containsKey(command.getClass())) return;
		commandListeners.get(command.getClass()).forEach(l -> {
			try {
				l.handle(command);
			} catch (Exception e) {
				System.err.println("Error in command handler for " + command.getName());
				e.printStackTrace();
			}
		});
	}
}
