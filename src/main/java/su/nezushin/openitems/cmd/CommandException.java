package su.nezushin.openitems.cmd;

import org.bukkit.command.CommandSender;
import su.nezushin.openitems.utils.Message;

public class CommandException extends RuntimeException {

    private Message.ChatMessageSender sender;

    public CommandException(Message.ChatMessageSender sender) {
        this.sender = sender;
    }

    public void send(CommandSender commandSender) {
        this.sender.send(commandSender);
    }

}
