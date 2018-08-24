package pw.lemmmy.ts3protocol.commands;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class Command {
	public abstract String getName();
	
	public String encode() {
		return getName() + " " + getArguments().entrySet().stream().map(e -> e.getKey() + "=" + encodeValue(e
			.getValue())).collect(Collectors.joining(" "));
	}
	
	public abstract Map<String, String> getArguments();
	
	public static String encodeValue(String value) {
		return value
			.replaceAll("\\\\", "\\\\\\\\")
			.replaceAll("\t", "\\\\\\t")
			.replaceAll("\r", "\\\\\\r")
			.replaceAll("\n", "\\\\\\n")
			.replaceAll("\\|", "\\\\\\p")
			.replaceAll(" ", "\\\\\\s")
			.replaceAll("/", "\\\\\\/");
	}
}
