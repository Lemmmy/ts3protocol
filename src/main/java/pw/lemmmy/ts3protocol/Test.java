package pw.lemmmy.ts3protocol;

import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.client.Identity;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class Test {
	public static void main(String[] args) {
		try (FileInputStream fis = new FileInputStream("test.properties")) {
			Properties properties = new Properties();
			properties.load(fis);
			
			String host = properties.getProperty("host", "127.0.0.1");
			
			new Client(new Identity(), InetAddress.getByName(host)).run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
