package pw.lemmmy.ts3protocol.commands;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Command {
	@Getter protected List<Map<String, String>> argumentSets = new ArrayList<>();
	
	// for easy building
	protected Map<String, String> arguments = new HashMap<>();
	
	public Command() {}
	
	public abstract String getName();
	
	public Map<String, String> getArguments() {
		return argumentSets.get(0);
	}
	
	public Map<String, String> getArguments(int index) {
		return argumentSets.get(index);
	}
	
	public void populateArguments() {}
	
	protected void beginNewArgumentSet() {
		argumentSets.add(arguments);
		arguments = new HashMap<>();
	}
	
	public String encode() {
		argumentSets.clear();
		arguments = new HashMap<>();
		populateArguments();
		argumentSets.add(arguments);
		
		return getName() + " " + argumentSets.stream()
			.map(this::encodeArgumentSet)
			.collect(Collectors.joining("|"));
	}
	
	private String encodeArgumentSet(Map<String, String> args) {
		return args.entrySet().stream()
			.map(e -> e.getKey() + "=" + encodeValue(e.getValue()))
			.collect(Collectors.joining(" "));
	}
	
	public void decode(String data) {
		argumentSets.clear();
		
		String[] sets = data.split("\\|");
		
		for (String set : sets) {
			String[] args = set.split(" ");
			arguments = new HashMap<>();
			
			for (String arg : args) {
				String[] parts = arg.split("=", 2);
				
				if (parts.length > 1) {
					arguments.put(parts[0], decodeValue(parts[1]));
				}
			}
			
			argumentSets.add(arguments);
		}
	}
	
	public static String encodeValue(String value) {
		return value == null ? "" : value
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
		return value == null ? "" : value
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
