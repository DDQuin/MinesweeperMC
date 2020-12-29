package me.ddquin.minesweeper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinesweeperCommands implements CommandExecutor {

    private MinesweeperPlugin main;

    public MinesweeperCommands(MinesweeperPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!(s instanceof Player)) {
            s.sendMessage(ChatColor.RED + "Only players can use this!");
            return false;
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("play")) {
                main.playGame((Player) s, args);
            } else if (args[0].equalsIgnoreCase("quit")) {
                main.quitGame((Player) s);
            } else if (args[0].equalsIgnoreCase("help")) {
                main.sendHelp((Player) s);
            } else {
                s.sendMessage(ChatColor.RED + "Type /ms help to get more info");
            }
        } else {
            s.sendMessage(ChatColor.RED + "Type /ms help to get more info");
        }
        return false;
    }
}
