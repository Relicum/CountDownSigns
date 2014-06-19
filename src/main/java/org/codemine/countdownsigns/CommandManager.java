package org.codemine.countdownsigns;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.codemine.jchatter.JChat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Name: CommandManager.java Created: 19 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class CommandManager implements TabExecutor {

    private final CDS plugin;
    private JChat jChat;
    public CommandManager(CDS pl){
        plugin = pl;
    }
    private final List<String> help = ImmutableList.of("setup","toggle");
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can not run commands from the console");
            return true;
        }
        Player player = (Player)sender;
        if (cmd.getName().equalsIgnoreCase("cdspause") && (player.hasPermission("countdown.sign.pause") || player.isOp())) {
            if (plugin.getTask() != null && plugin.getTask().isRunning()) {
                plugin.getTask().cancelTheTask();
                MessageUtil.sendMessage(player,"The countdown sign has been toggle &6OFF");
                return true;
            } else {
                if (plugin.getConfig().contains("sign.signtimer")) {
                    SignTimer signTimer = (SignTimer)plugin.getConfig().get("sign.signtimer");
                    if (signTimer.getTimeLeft() == 0) {
                        MessageUtil.sendErrorMessage(player,"This count down has already ended");
                        return true;
                    } else {
                        plugin.startTask();
                        MessageUtil.sendMessage(player,"The countdown sign has been toggle &6ON");
                        return true;
                    }
                }
                MessageUtil.sendErrorMessage(player,"Unknown error unable to toggle the countdown on");
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("cdsstatus") && (player.hasPermission("countdown.sign.status") || player.isOp())) {
            if (plugin.getTask() != null && plugin.getTask().isRunning()) {
                MessageUtil.sendMessage(player,"The countdown sign is currently &6RUNNING");
                return true;
            } else {
                if (plugin.getConfig().contains("sign.signtimer")) {
                    SignTimer signTimer = (SignTimer)plugin.getConfig().get("sign.signtimer");
                    if (signTimer.getTimeLeft() == 0) {
                        MessageUtil.sendMessage(player,"This countdown sign is not running and has finished its countdown");
                        return true;
                    } else {
                        MessageUtil.sendMessage(player,"The countdown sign is currently &6PAUSED");
                        return true;
                    }
                }
                MessageUtil.sendErrorMessage(player,"Unknown error unable to check the countdown status");
                return true;
            }

        }
        if (cmd.getName().equalsIgnoreCase("cdshelp") && (player.hasPermission("countdown.sign.status") || player.isOp())) {
            if (args == null || args.length == 0) {
                showSetUpHelp(player);
                player.sendMessage("");
                return true;
            }
            if (args[0].equalsIgnoreCase("setup")) {
                MessageUtil.clearChat(player);
                MessageUtil.sendRawMessage(player,MessageUtil.fullline(ChatColor.GREEN,ChatColor.BLUE,ChatColor.ITALIC,'-'));
                MessageUtil.sendRawMessage(player,MessageUtil.centeredHeading(ChatColor.DARK_RED,ChatColor.ITALIC,"How to setup the Sign"));
                player.sendMessage("");
                MessageUtil.sendAdminMessage(player,"&aThe valid format requires a minimum of 1 time frame");
                MessageUtil.sendAdminMessage(player,"&6Formats: &91&f:&5W &93&f:&4D &97&f:&4H &945&f:&4N");
                MessageUtil.sendAdminMessage(player,"&aSo for 1 day you would add &b1&f:&bD &a or &b1&f:&bd");
                MessageUtil.sendAdminMessage(player,"&aMore than one period use single space to separate them");
                MessageUtil.sendAdminMessage(player,"&6Example: &92&f:&4D&f &94&f:&4H&f &930&f:&4M &a add the format on 2nd line of sign");
                player.sendMessage("");
                MessageUtil.sendRawMessage(player,MessageUtil.fullline(ChatColor.BLUE,ChatColor.GREEN,ChatColor.ITALIC,'-'));
                return true;
            }
            if (args[0].equalsIgnoreCase("toggle")) {
                MessageUtil.sendMessage(player,"Toggle selected");
                return true;
            }
            return true;
        }
        return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args){
        if (cmd.getName().equalsIgnoreCase("cdshelp") && args.length == 1) {
            return StringUtil.copyPartialMatches(args[0],help,new ArrayList<String>(help.size()));
        }
        return ImmutableList.of();
    }
    public JChat getjChat(){
        jChat = new JChat("[");
        jChat.color(ChatColor.DARK_PURPLE)
          .then("CDS")
          .color(ChatColor.AQUA)
          .then("]")
          .color(ChatColor.DARK_PURPLE)
          .then(" ");
        return jChat;

    }
    public void showSetUpHelp(Player player){
        getjChat()
          .then("Need help? ")
          .color(ChatColor.GREEN)
          .then("SETUP")
          .color(ChatColor.BLUE)
          .style(ChatColor.BOLD,ChatColor.UNDERLINE)
          .itemTooltip("Setup Guide",Arrays.asList("&cIf you need help setting up a sign"," ","&bThen just click the link"))
          .command("/cdshelp setup")
          .then(" ")
          .color(ChatColor.GREEN)
          .then("TOGGLE")
          .color(ChatColor.RED)
          .style(ChatColor.BOLD,ChatColor.UNDERLINE)
          .itemTooltip("Toggle Guide",Arrays.asList("&cIf you need help starting and stopping the countdown"," ","&bThen just click the link"))
          .command("/cdshelp toggle")
          .send(player);
    }

}
