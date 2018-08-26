package pw.lemmmy.ts3protocol.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CommandRegistry {
	private static final Map<String, Supplier<Command>> commands = new HashMap<>();
	
	static {
		commands.put("clientinitiv", CommandClientInitIV::new);
	}
}
