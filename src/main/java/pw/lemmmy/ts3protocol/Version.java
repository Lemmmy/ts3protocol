package pw.lemmmy.ts3protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Version {
	public static final Version DEFAULT_VERSION = new Version(
		"Beta",
		"3.2.2 [Build: 1535010838]",
		"7uY1DWoJD2x80002ha6jSkNRXGcfHAqS9hdJz80JjafvhmxlhQKSwfsofFjEq10TI8LFbhLdv7TQxd+gPyNEAg==",
		"Linux"
	); // TODO: load from Versions.csv
	
	private String channel, version, hash, platform;
}
