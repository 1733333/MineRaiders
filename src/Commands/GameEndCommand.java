package Commands;

import Events.GameEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;

public class GameEndCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p){
            if(p.isOp()){
                World w = p.getWorld();
                Bukkit.getPluginManager().callEvent(new GameEndEvent(w));
                Bukkit.broadcastMessage(ChatColor.RED + p.getName() + "使用指令停止了游戏！");
            }else {
                p.sendMessage(ChatColor.RED + "你没有使用此命令的权限！");
            }
        }
        return true;
    }
}
