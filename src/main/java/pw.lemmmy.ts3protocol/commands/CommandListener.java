package pw.lemmmy.ts3protocol.commands;

@FunctionalInterface
public interface CommandListener {
	void handle(Command command);
}
