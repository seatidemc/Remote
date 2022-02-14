package top.seatide.remote.Utils;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class LogUtil {
    public static Logger logger;
    public final static String richPrefix = "[" + ChatColor.YELLOW + "SEAT" + ChatColor.AQUA + "i" + ChatColor.YELLOW + "DE" + ChatColor.RESET + "] ";
    public final static String richERROR = "[" + ChatColor.RED + "ERROR" + ChatColor.RESET + "] ";
    public final static String richSUCCESS = "[" + ChatColor.GREEN + "SUCCESS" + ChatColor.RESET + "] ";
    public final static String richINFO = "[" + ChatColor.AQUA + "INFO" + ChatColor.RESET + "] ";
    public final static String richWarning = "[" + ChatColor.YELLOW + "WARN" + ChatColor.RESET + "] ";
    public final static String prefix = "[SEATiDE] ";
    public final static String ERROR = "[ERROR] ";
    public final static String SUCCESS = "[SUCCESS] ";
    public final static String INFO = "[INFO] ";
    public final static String WARNING = "[WARN] ";

    public static void init() {
        try {
            logger = Bukkit.getLogger();
        } catch (NullPointerException e) {
            logger = Logger.getLogger("");
        }
    }

    public static String translate(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void send(CommandSender p, String msg) {
        p.sendMessage(translate(getPrefixForSender(p, LogLevel.SEATIDE)
                + (p instanceof Player ? msg : msg.replaceAll("&(\\d|k|m|n|o|l|a|b|c|d|e|f)", ""))));
    }

    public static void send(CommandSender p, LogLevel prefix, String msg) {
        p.sendMessage(translate(getPrefixForSender(p, LogLevel.SEATIDE) + getPrefixForSender(p, prefix)
                + (p instanceof Player ? msg : msg.replaceAll("&(\\d|k|m|n|o|l|a|b|c|d|e|f)", ""))));
    }

    public static String getPrefixForSender(CommandSender p, LogLevel prefix) {
        if (p instanceof Player) {
            switch (prefix) {
                case SEATIDE: return richPrefix;
                case INFO: return richINFO;
                case ERROR: return richERROR;
                case SUCCESS: return richSUCCESS;
                case WARNING: return richWarning;
            }
        } else {
            switch (prefix) {
                case SEATIDE: return LogUtil.prefix;
                case INFO: return INFO;
                case ERROR: return ERROR;
                case SUCCESS: return SUCCESS;
                case WARNING: return WARNING;
            }
        };
        return "";
    }

    public static void log(String msg) {
        logger.info(translate(msg));
    }

    public static void info(String msg) {
        log(prefix + INFO + msg);
    }

    public static void error(String msg) {
        log(prefix + ERROR + msg);
    }

    public static void success(String msg) {
        log(prefix + SUCCESS + msg);
    }

    public static void warn(String msg) {
        log(prefix + WARNING + msg);
    }
}