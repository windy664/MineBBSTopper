package com.mythmc;

import com.mythmc.commands.CommandsManager;
import com.mythmc.commands.command.MainCommands;

import com.mythmc.events.listener.GUIListener;
import com.mythmc.impl.cache.TargetManager;
import com.mythmc.impl.cache.target.GlobalInfo;
import com.mythmc.impl.database.DatabaseManager;
import com.mythmc.events.EventsManager;
import com.mythmc.externs.HookManager;
import com.mythmc.externs.hook.placeholders.PlaceholderCache;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.file.FileManager;
import com.mythmc.commands.gui.MainGUI;
import com.mythmc.commands.gui.RewardGUI;
import com.mythmc.events.listener.bungee.PluginMsgListener;
import com.mythmc.tools.remote.WebPreChecker;
import com.mythmc.tools.local.AutoUpdater;
import com.mythmc.tools.remote.Metrics;
import com.mythmc.tools.remote.UpdateChecker;

import com.mythmc.tools.utils.RefreshUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineBBSTopper extends JavaPlugin {
    public static MineBBSTopper instance;
    private static final String version = Bukkit.getVersion();
    public static final boolean isLowVersion = version.contains("1.7") || version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12");
    public static boolean bungeecord;
    public static final ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        instance = this;
        this.pluginBanner();
        (new AutoUpdater(this)).start();
        (new FileManager(this)).load();
        (new DatabaseManager(this)).load();
        (new PluginMsgListener(this)).load();
        (new EventsManager(this)).load();
        (new CommandsManager(this)).load();
        (new HookManager(this)).load();
        (new WebPreChecker(this)).start();
        new Metrics(this, 22565).addCustomChart(new Metrics.SingleLineChart("topper_count", () -> TargetManager.getGlobalInfo().getAmount()));

        logger("§b信息 §8| §3插件加载完成，总耗时: §a" + (System.currentTimeMillis() - startTime) + "ms");
        logger("§b信息 §8| §3您正在使用版本: §av" + getDescription().getVersion() + " by 404Yuner");
        logger("§b信息 §8| §3获取支持请前往QQ群: §a301662621 §7§n（请发送尽可能完整的问题描述以及报错日志）");
        (new UpdateChecker(this)).checkForUpdates();
        (new RefreshUtil(this)).scheduleDailyTask();
    }

    @Override
    public void onDisable() {
        logger("§c卸载 §8| §6插件正在卸载中...");
        instance = null;

        (new DatabaseManager(this)).close();
        this.getServer().getScheduler().cancelTasks(this);
        logger("§c卸载 §8| §6已关闭所有正在运行的任务");
        this.cacheClear();
        logger("§c卸载 §8| §6插件的残余内容已清理完毕");
        logger("§c卸载 §8| §6插件卸载完毕，感谢您的使用。QQ交流群: 301662621");
    }

    public void onReload() {
        logger("§a重载 §8| §6插件正在重载，若需更改数据库配置请重启。");
        reloadConfig();
        (new FileManager(this)).load();
        (new MainGUI()).initializeSlotCommands();
        (new RewardGUI()).initializeSlotData();
    //    (new DatabaseManager(this)).load(); 方法不稳定，取消重载数据库
        (new WebPreChecker(this)).start();
    }
    private void pluginBanner() {
        console.sendMessage("\n" +
                "      ___       _  _   _   __ ___ _   _   _   _  _  \n" +
                " |\\/|  |  |\\ | |_ |_) |_) (_   | / \\ |_) |_) |_ |_) \n" +
                " |  | _|_ | \\| |_ |_) |_) __)  | \\_/ |   |   |_ | \\ \n" +
                "                                                    ");
    }

    public void logger(String message) {
        console.sendMessage("§7[§bMineBBSTopper§7] " + message);
    }

    public void cacheClear() {
        HookManager.clear();
        PlaceholderCache.clear();
        GUIListener.lastClickTimes.clear();
        MainCommands.cooldownMap.clear();
        ConfigFile.normalRewardCommands.clear();
        ConfigFile.offdayRewardCommands.clear();
        ConfigFile.offdays.clear();
        RewardGUI.slotData.clear();
        MainGUI.slotCommands.clear();
    }
}
