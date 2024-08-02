package com.mythmc.commands.command;

import com.mythmc.MineBBSTopper;
import com.mythmc.api.event.PlayerTopperRewardClaimEvent;
import com.mythmc.commands.command.sub.*;
import com.mythmc.commands.gui.RewardGUI;
import com.mythmc.externs.HookManager;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.file.statics.LangFile;
import com.mythmc.commands.gui.MainGUI;
import com.mythmc.externs.hook.placeholders.PlaceholderHook;
import com.mythmc.impl.cache.TargetManager;
import com.mythmc.impl.cache.target.GlobalInfo;
import com.mythmc.impl.cache.target.PlayerInfo;
import com.mythmc.tools.remote.HtmlParser;
import com.mythmc.tools.utils.CommandUtil;
import com.mythmc.tools.Debugger;
import com.mythmc.tools.utils.MessageUtil;
import com.mythmc.tools.utils.OffdayUtil;
import com.mythmc.tools.utils.TimeUtil;
import eu.decentsoftware.holograms.api.commands.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

// 定义一个主类 MainCommands，实现 CommandExecutor 和 TabCompleter 接口
public class MainCommands implements CommandExecutor, TabCompleter {

    // 定义一个插件实例，MineBBSTopper 类型
    private final MineBBSTopper plugin;

    // 定义一个静态的 HashMap，用于存储玩家的冷却时间
    public static final Map<String, Long> cooldownMap = new HashMap<>();

    // 构造函数，初始化 plugin 变量
    public MainCommands(MineBBSTopper plugin) {
        this.plugin = plugin;
    }

    // 处理命令执行的逻辑
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    return Reload.handle(sender, plugin);
                case "open":
                    return Open.handle(player, args);
                case "test":
                    return Test.handle(player, args);
                case "url":
                    return URL.handle(sender);
                case "claim":
                    return Claim.handle(player, plugin);
                default:
                    sender.sendMessage("无效的子命令！");
                    return false;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("test");
            completions.add("open");
            completions.add("claim");
        }
        if (args.length == 2 && args[0].equals("test")) {
            completions.add("normal");
            completions.add("offday");
        }
        if (args.length == 2 && args[0].equals("open")) {
            completions.add("main");
            completions.add("reward");
        }
        return completions;
    }
}
