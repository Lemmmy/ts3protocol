package pw.lemmmy.ts3protocol.commands;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class Command {
	Map<String, String> arguments = new HashMap<>();
	
	public Command() {}
	
	public abstract String getName();
	
	public abstract void populateArguments();
	
	public String encode() {
		arguments.clear();
		populateArguments();
		
		return getName() + " " + arguments.entrySet().stream()
			.map(e -> e.getKey() + "=" + encodeValue(e.getValue()))
			.collect(Collectors.joining(" "));
	}
	
	public void decode(String[] args) {
		arguments.clear();
		
		for (int i = 1; i < args.length; i++) { // start at 1 to skip the command name itself
			String arg = args[i];
			String[] parts = arg.split("=", 2);
			
			arguments.put(parts[0], decodeValue(parts[1]));
		}
	}
	
	public static String encodeValue(String value) {
		return value
			.replace("\u000b", "\\v")
			.replace("\u000c", "\\f")
			.replace("\\", "\\\\")
			.replace("\t", "\\t")
			.replace("\r", "\\r")
			.replace("\n", "\\n")
			.replace("|", "\\p")
			.replace(" ", "\\s")
			.replace("/", "\\/");
	}
	
	public static String decodeValue(String value) {
		return value
			.replace("\\v", "\u000b")
			.replace("\\f", "\u000c")
			.replace("\\t", "\t")
			.replace("\\r", "\r")
			.replace("\\n", "\n")
			.replace("\\p", "|")
			.replace("\\s", " ")
			.replace("\\/", "/")
			.replace("\\\\", "\\");
	}
}
