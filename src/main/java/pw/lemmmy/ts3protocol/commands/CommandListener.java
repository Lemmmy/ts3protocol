package pw.lemmmy.ts3protocol.commands;

@FunctionalInterface
public interface CommandListener<T extends Command> {
	void handle(T command) throws Exception;
}
