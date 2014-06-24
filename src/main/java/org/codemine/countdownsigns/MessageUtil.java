package org.codemine.countdownsigns;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.ChatColor.*;

/**
 * Name: MessageUtil.java Created: 15 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class MessageUtil implements consoleColors {

    private static final char COLOR_CHAR='\u00A7';
    private static final char SAFE_CHAR='&';
    private static final String prefix=DARK_PURPLE + "[" + AQUA + "CDS" + DARK_PURPLE + "] ";
    private static final String debugPrefix=DARK_RED + "[DEBUG] " + YELLOW;
    private static final ChatColor infoColor=GREEN;
    private static final ChatColor errorColor=RED;
    private static final ChatColor adminColor=LIGHT_PURPLE;
    private static final String logPrefix=CON_WHITE + "[" + CON_BOLDGREEN + "CDS" + CON_WHITE + "] " + CON_RESET;
    private static final String logDebugPrefix=CON_WHITE + "[" + CON_BOLDRED + "CDS-DEBUG" + CON_WHITE + "] " + CON_RESET;
    private static final String logInfoColor=CON_BOLDYELLOW;
    private static final String logSevereColor=CON_BOLDRED;
    private static final String logWarningColor=CON_BOLDMAGENTA;
    private static final String prefixNoColor="[CDS] ";

    /**
     * Send command raw message.
     *
     * @param sender the @{@link org.bukkit.command.CommandSender} sender
     * @param message the {@link String} message
     */
    public static void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    /**
     * Send raw message. No formatting is done to the messages
     *
     * @param sender the sender
     * @param messages the messages
     */
    public static void sendRawMessage(CommandSender sender, String[] messages) {
        sender.sendMessage(messages);
    }

    /**
     * Send String list of messages.
     *
     * @param sender the sender
     * @param messages the messages
     */
    public static void sendMessage(CommandSender sender, String[] messages) {
        for(String s : messages) {
            sender.sendMessage(format(s));
        }

    }

    /**
     * Send command message.
     *
     * @param sender the @{@link org.bukkit.command.CommandSender} sender
     * @param message the {@link String} message
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    /**
     * Send command error message.
     *
     * @param sender the @{@link org.bukkit.command.CommandSender} sender
     * @param message the {@link String} message
     */
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(errorFormat(message));
    }

    /**
     * Send command admin message.
     *
     * @param sender the @{@link org.bukkit.command.CommandSender} sender
     * @param message the {@link String} message
     */
    public static void sendAdminMessage(CommandSender sender, String message) {
        sender.sendMessage(adminFormat(message));
    }

    /**
     * Format string.
     *
     * @param message the message
     * @return the string formatted with prefix, message color and converts any other color codes in message
     */
    public static String format(String message) {
        return addColor(prefix + infoColor + message);
    }

    /**
     * Format string.
     *
     * @param name the name to use in prefix
     * @param message the message
     * @return the string formatted with prefix with custom name, message color and converts any other color codes in message
     */
    public static String format(String name, String message) {
        return addColor(DARK_PURPLE + "[" + AQUA + name + DARK_PURPLE + "] " + message);
    }

    /**
     * Error format.
     *
     * @param message the message
     * @return the string
     */
    public static String errorFormat(String message) {
        return addColor(prefix + errorColor + message);
    }

    /**
     * Admin format.
     *
     * @param message the message
     * @return the string
     */
    public static String adminFormat(String message) {
        return addColor(prefix + adminColor + message);
    }

    /**
     * Log to console
     *
     * @param message the {@link Object} message
     */
    public static void log(Object message) {
        System.out.println(message);
    }

    /**
     * Log to Minecraft Logger
     *
     * @param level the {@link java.util.logging.Level}
     * @param message the {@link Object} message
     */
    public static void log(Level level, Object message) {
        Logger.getLogger("MineCraft").log(level, prefixNoColor + message.toString() + CON_RESET);
    }

    /**
     * Log - Level INFO with color formatted.
     *
     * @param message the {@link Object} message
     */
    public static void logInfoFormatted(Object message) {
        System.out.println(logPrefix + logInfoColor + removeColor(message.toString()) + CON_RESET);
    }

    /**
     * Log - Level SEVERE with color formatted.
     *
     * @param message the {@link Object} message
     */
    public static void logServereFormatted(Object message) {
        System.out.println(logPrefix + logSevereColor + removeColor(message.toString()) + CON_RESET);
    }

    /**
     * Log - Level WARNING with color formatted.
     *
     * @param message the {@link Object} message
     */
    public static void logWarningFormatted(Object message) {
        System.out.println(logPrefix + logWarningColor + removeColor(message.toString()) + CON_RESET);
    }

    /**
     * Remove color.
     *
     * @param message the message
     * @return the {@link String} that has color removed
     */
    public static String removeColor(String message) {
        return ChatColor.stripColor(message);
    }

    /**
     * Log a debug message to the console. Will only log if debug is set to true in config.yml
     *
     * @param level the level
     * @param message the message
     */
    public static void logDebug(Level level, Object message) {
        if(CDS.getInstance().isDebug()) {
            Logger.getLogger("MineCraft").log(level, logDebugPrefix + message.toString() + CON_RESET);
        }
    }

    /**
     * Display a debug message in game. Will only log if debug is set to true in config.yml
     *
     * @param sender the sender
     * @param message the message
     */
    public static void debugGameMessage(CommandSender sender, String message) {
        if(CDS.getInstance().isDebug()) {
            sender.sendMessage(addColor(debugPrefix) + addColor(message) + addColor("&r"));
        }

    }

    /**
     * Convert color. Shade from Bukkit Api
     *
     * @param s {@link String} {@link String} the text to convert color chars to correct format
     * @return the {@link String} line of text which has color formatted
     */
    public static String addColor(String s) {
        char[] b=s.toCharArray();
        for(int i=0 ; i < b.length - 1 ; i++) {
            if(b[i] == SAFE_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i]=COLOR_CHAR;
                b[i + 1]=Character.toLowerCase(b[i + 1]);
            }

        }
        return new String(b);
    }

    /**
     * Convert color. Shade from Bukkit Api
     *
     * @param s {@link String} {@link String} the text to convert color chars to correct format
     * @return the {@link String} line of text which has color formatted
     */
    public static String addAltColor(String s) {
        char[] b=s.toCharArray();
        for(int i=0 ; i < b.length - 1 ; i++) {
            if(b[i] == COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i]=SAFE_CHAR;
                b[i + 1]=Character.toLowerCase(b[i + 1]);
            }

        }
        return new String(b);
    }

    public static void clearChat(Player player) {
        for(int i=0 ; i < 100 ; i++) {
            player.sendMessage("");
        }

    }

    public static void clearChat(Player player, int lines) {
        for(int i=0 ; i < lines ; i++) {
            player.sendMessage("");
        }

    }

    public static String fullline(final ChatColor color, final ChatColor color2, final ChatColor style, char character) {
        StrBuilder sb=new StrBuilder();
        boolean t=true;
        for(int i=0 ; i < 53 ; i++) {
            if(t) {
                sb.append(style).append(color).append(character);
                t=false;
            }
            else {
                sb.append(style).append(color2).append(character);
                t=true;
            }
        }
        return sb.toString();
    }

    public static String centeredHeading(ChatColor color, ChatColor style, String heading) {
        StrBuilder sb=new StrBuilder(58);
        sb.append(" ").appendPadding(20, ' ').append(style).append("").append(color).append(heading).appendPadding(8, ' ');
        return sb.toString();
    }

}
