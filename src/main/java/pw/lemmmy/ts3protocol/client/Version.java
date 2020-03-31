package pw.lemmmy.ts3protocol.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Version {
	public static final Version DEFAULT_VERSION = new Version(
		"Beta",
		"3.5.1 [Build: 1584955996]",
		"YtR6uJ0zzFwlAXoV1ikV8DVD9y7ka0WCh46wvALArdCBw9zaBLE7ese6Uu3U2Dmg4//ook5cNvupeOlzHcGPDQ==",
		"Linux"
	); // TODO: load from Versions.csv
	
	private String channel, version, hash, platform;
}
