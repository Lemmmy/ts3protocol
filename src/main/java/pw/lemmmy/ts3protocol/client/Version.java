package pw.lemmmy.ts3protocol.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Version {
	public static final Version DEFAULT_VERSION = new Version(
		"Beta",
		"3.5.2 [Build: 1585741147]",
		"qILhBdVwjtE8enMjQL02b9i80CRybJgolFQWfQgLPjWLvgbNMeqAAFS+X7HB7FuF5VKq82biDYK2cuQJjTAbCw==",
		"Linux"
	); // TODO: load from Versions.csv
	
	private String channel, version, hash, platform;
}
