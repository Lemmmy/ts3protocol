package pw.lemmmy.ts3protocol.commands.handshake;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import pw.lemmmy.ts3protocol.Version;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

@Getter
@Setter
@Accessors(chain = true)
public class CommandClientInit extends Command {
	private String
		nickname, phoneticNickname,
		defaultChannel, defaultChannelPassword,
		serverPassword, defaultToken,
		hardwareID;
	
	private Version version;
	private long keyOffset;
	
	public CommandClientInit() {}
	
	@Override
	public String getName() {
		return "clientinit";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("client_nickname", nickname);
		arguments.put("client_version", version.getVersion());
		arguments.put("client_version_sign", version.getHash());
		arguments.put("client_platform", version.getPlatform());
		arguments.put("client_input_hardware", "1");
		arguments.put("client_output_hardware", "1");
		arguments.put("client_default_channel", defaultChannel);
		arguments.put("client_default_channel_password", CryptoUtils.hashTeamspeakPassword(defaultChannelPassword));
		arguments.put("client_server_password", CryptoUtils.hashTeamspeakPassword(serverPassword));
		arguments.put("client_meta_data", "");
		arguments.put("client_key_offset", Long.toString(keyOffset));
		arguments.put("client_nickname_phonetic", phoneticNickname);
		arguments.put("client_default_token", defaultToken);
		arguments.put("hardwareID", hardwareID);
	}
}
