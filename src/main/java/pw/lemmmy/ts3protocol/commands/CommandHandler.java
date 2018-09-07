package pw.lemmmy.ts3protocol.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandHandler {
	private Map<Class<? extends Command>, Set<CommandListener>> commandListeners = new HashMap<>();
	
	public <T extends Command> void addCommandListener(Class<T> commandClass, CommandListener<T> listener) {
		if (!commandListeners.containsKey(commandClass)) {
			commandListeners.put(commandClass, new HashSet<>());
		}
		
		commandListeners.get(commandClass).add(listener);
	}
	
	public void handleCommand(Command command) {
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
