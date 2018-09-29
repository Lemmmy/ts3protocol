package pw.lemmmy.ts3protocol.commands.handshake;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.crypto.ASN;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;

@AllArgsConstructor
@Slf4j
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
			log.error("Error encoding ASN for initiv keypair", e);
		}
		arguments.put("ot", "1");
		arguments.put("ip", host.getHostAddress());
	}
}
