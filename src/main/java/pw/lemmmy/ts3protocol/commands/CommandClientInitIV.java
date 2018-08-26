package pw.lemmmy.ts3protocol.commands;

import lombok.AllArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

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
			arguments.put("omega", Base64.toBase64String(CryptoUtils.toDERASN1(keyPair).getEncoded()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		arguments.put("ot", "1");
		arguments.put("ip", host.getHostAddress());
	}
}
