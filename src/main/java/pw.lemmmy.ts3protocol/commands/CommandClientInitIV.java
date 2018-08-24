package pw.lemmmy.ts3protocol.commands;

import lombok.AllArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class CommandClientInitIV extends Command {
	private byte[] alpha;
	private KeyPair keyPair;
	private InetAddress host;
	
	@Override
	public String getName() {
		return "clientinitiv";
	}
	
	@Override
	public Map<String, String> getArguments() {
		return new HashMap<String, String>() {{
			put("alpha", Base64.toBase64String(alpha));
			try {
				put("omega", Base64.toBase64String(CryptoUtils.toTomcrypt(keyPair).getEncoded()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			put("ot", "1");
			put("ip", host.getHostAddress());
		}};
	}
}
