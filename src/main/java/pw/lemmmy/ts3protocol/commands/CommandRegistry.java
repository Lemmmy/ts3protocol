package pw.lemmmy.ts3protocol.commands;

import pw.lemmmy.ts3protocol.commands.channels.*;
import pw.lemmmy.ts3protocol.commands.clients.*;
import pw.lemmmy.ts3protocol.commands.errors.CommandError;
import pw.lemmmy.ts3protocol.commands.handshake.*;
import pw.lemmmy.ts3protocol.commands.messages.CommandNotifyTextMessage;
import pw.lemmmy.ts3protocol.commands.messages.CommandSendTextMessage;
import pw.lemmmy.ts3protocol.commands.server.CommandNotifyServerEdited;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CommandRegistry {
	private static final Map<String, Supplier<Command>> commands = new HashMap<>();
	
	static {
		// handshake
		commands.put("clientinitiv", CommandClientInitIV::new);
		commands.put("initivexpand2", CommandInitIVExpand2::new);
		commands.put("clientek", CommandClientEK::new);
		commands.put("clientinit", CommandClientInit::new);
		commands.put("initserver", CommandInitServer::new);
		
		// error
		commands.put("error", CommandError::new);
		
		// clients
		commands.put("clientmove", CommandClientMove::new);
		commands.put("clientupdate", CommandClientUpdate::new);
		commands.put("notifycliententerview", CommandNotifyClientEnterView::new);
		commands.put("notifyclientleftview", CommandNotifyClientLeftView::new);
		commands.put("notifyclientupdated", CommandNotifyClientUpdated::new);
		commands.put("notifyclientmoved", CommandNotifyClientMoved::new);
		
		// channels
		commands.put("channeledit", CommandChannelEdit::new);
		commands.put("channellist", CommandChannelList::new);
		commands.put("channellistfinished", CommandChannelListFinished::new);
		commands.put("channelsubscribeall", CommandChannelSubscribeAll::new);
		commands.put("notifychannelcreated", CommandNotifyChannelCreated::new);
		commands.put("notifychannelchanged", CommandNotifyChannelChanged::new);
		commands.put("notifychanneledited", CommandNotifyChannelEdited::new);
		commands.put("notifychanneldeleted", CommandNotifyChannelDeleted::new);
		commands.put("notifychanneldescriptionchanged", CommandNotifyChannelDescriptionChanged::new);
		
		// messages
		commands.put("sendtextmessage", CommandSendTextMessage::new);
		commands.put("notifytextmessage", CommandNotifyTextMessage::new);
		
		// server
		commands.put("notifyserveredited", CommandNotifyServerEdited::new);
	}
	
	public static Optional<Command> getCommand(String name) {
		if (!commands.containsKey(name)) return Optional.empty();
		
		return Optional.of(commands.get(name).get());
	}
}
