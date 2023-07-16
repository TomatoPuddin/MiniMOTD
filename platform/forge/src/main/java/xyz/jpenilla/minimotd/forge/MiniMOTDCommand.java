package xyz.jpenilla.minimotd.forge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import xyz.jpenilla.minimotd.common.CommandHandler;
import xyz.jpenilla.minimotd.forge.util.ComponentConverter;

public class MiniMOTDCommand extends CommandBase {
  final CommandHandler handler;

  public MiniMOTDCommand(CommandHandler handler) {
    this.handler = handler;
  }

  @Override
  public String getName() {
    return "minimotd";
  }

  @Override
  public String getUsage(ICommandSender sender) {
    return null;
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
    CommandHandler.Audience audience = (component, success) -> sender.sendMessage(ComponentConverter.toNative(component));

    if (args.length == 1) {
      if (args[0].equals("reload")) {
        reload(server, audience);
        return;
      } else if (args[0].equals("about")) {
        about(server, audience);
        return;
      }
    }

    help(server, audience);
  }


  void reload(MinecraftServer server, CommandHandler.Audience audience) {
    handler.reload(audience);
  }

  void about(MinecraftServer server, CommandHandler.Audience audience) {
    handler.about(audience);
  }

  void help(MinecraftServer server, CommandHandler.Audience audience) {
    handler.help(audience);
  }
}
