package pw.lemmmy.ts3protocol.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CommandRegistry {
	private static final Map<String, Supplier<Command>> commands = new HashMap<>();
	
	static {
		commands.put("clientinitiv", CommandClientInitIV::new);
		commands.put("initivexpand2", CommandInitIVExpand2::new);
	}
	
	public static Optional<Command> getCommand(String name) {
		if (!commands.containsKey(name)) return Optional.empty();
		
		return Optional.of(commands.get(name).get());
	}
}
