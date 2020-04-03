package pw.lemmmy.ts3protocol.declarations;

import pw.lemmmy.ts3protocol.commands.Command;

import java.util.Map;
import java.util.Optional;

/** Reasons for ChannelEdited, ChannelMoved, ClientEnterView, ClientLeftView, ClientMoved and ServerEdited **/
public enum TS3Reason {
    NONE,
    MOVED,
    SUBSCRIPTION,
    LOST_CONNECTION,
    KICK_CHANNEL,
    KICK_SERVER,
    KICK_SERVER_BAN,
    SERVER_STOPPED,
    CLIENT_DISCONNECT,
    CHANNEL_UPDATE,
    CHANNEL_EDIT,
    CLIENT_DISCONNECT_SERVER_SHUTDOWN;
    
    public static Optional<TS3Reason> getReasonFromCommand(Command command) {
        Map<String, String> args = command.getArguments();
        
        if (!args.containsKey("reason") && !args.containsKey("reasonid"))
            return Optional.empty();
        
        int reason = Integer.parseInt(args.containsKey("reason") ? args.get("reason") : args.get("reasonid"));
        if (reason < 0 || reason >= values().length) return Optional.empty();
        
        return Optional.of(values()[reason]);
    }
}
