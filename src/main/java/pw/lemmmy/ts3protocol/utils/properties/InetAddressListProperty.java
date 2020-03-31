package pw.lemmmy.ts3protocol.utils.properties;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class InetAddressListProperty extends Property<List<InetAddress>> {
	protected String name;
	
	@Override
	public void encodeProperty(Map<String, String> arguments) {
		if (getValue() == null || getValue().isEmpty()) return;
		
		arguments.put(name, getValue().stream()
			.map(InetAddress::getHostAddress)
			.collect(Collectors.joining(",")));
	}
	
	@Override
	public void decodeProperty(Map<String, String> arguments) {
		if (!arguments.containsKey(name)) return;
		
		try {
			String[] parts = arguments.get(name).split(",\\s*");
			List<InetAddress> list = new ArrayList<>();
			
			for (String part : parts) {
				list.add(InetAddress.getByName(part));
			}
			
			setValue(list);
		} catch (UnknownHostException e) {
			log.error("Error parsing InetAddress from {} property", name, e);
		}
	}
}
