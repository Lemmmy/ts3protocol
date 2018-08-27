package pw.lemmmy.ts3protocol.commands;

import lombok.AllArgsConstructor;
import org.bouncycastle.util.encoders.Base64;

@AllArgsConstructor
public class CommandClientEK extends Command {
	private byte[] ek;
	private byte[] proof;
	
	public CommandClientEK() {}
	
	@Override
	public String getName() {
		return "clientek";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("ek", Base64.toBase64String(ek));
		arguments.put("proof", Base64.toBase64String(proof));
	}
}
