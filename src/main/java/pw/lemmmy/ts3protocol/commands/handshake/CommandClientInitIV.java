package pw.lemmmy.ts3protocol.commands.handshake;

import lombok.AllArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.crypto.ASN;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;

@AllArgsConstructor
public class CommandClientInitIV extends Command {
	private byte[] alpha;
	private KeyPair keyPair;
	private InetAddress host;
	
	public CommandClientInitIV() {}
	
	@Override
	public String getName() {
		return "clientinitiv";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("alpha", Base64.toBase64String(alpha));
		try {
			arguments.put("omega", ASN.encodeBase64ASN(keyPair));
		} catch (IOException e) {
			e.printStackTrace();
		}
		arguments.put("ot", "1");
		arguments.put("ip", host.getHostAddress());
	}
}
