package com.mythmc.tools.utils;

import com.cryptomorin.xseries.XSound;
import com.mythmc.MineBBSTopper;
import com.mythmc.externs.HookManager;
import com.mythmc.externs.hook.placeholders.PlaceholderHook;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.file.statics.GUIFile;
import com.mythmc.file.statics.LangFile;
import com.mythmc.commands.gui.MainGUI;
import com.mythmc.commands.gui.RewardGUI;
import com.mythmc.externs.hook.economy.VaultHook;
import com.mythmc.externs.hook.economy.PlayerPointsHook;
import com.mythmc.events.listener.bungee.PluginMsgListener;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Random;

public class CommandUtil {

    public static void executeCommands(Player player, List<String> commandList) {
        int delaySeconds = 0; // 初始化延迟时间为0秒

        for (String action : commandList) {
            if (action.startsWith("[delay]")) {
                // 如果指令是延迟指令，则解析出延迟时间并设置
                delaySeconds = Integer.parseInt(action.split("]")[1]);
            } else {
                // 如果不是延迟指令，则添加到延迟执行列表中
                Bukkit.getScheduler().runTaskLater(MineBBSTopper.instance, () -> executeCommand(player, action), delaySeconds * 20L); // 将延迟时间转换为tick数
            }
        }
    }

    private static void executeCommand(Player player, String action) {
        if (action.startsWith("[")) {
            String[] parts = action.split("]", 2);
            String type = parts[0].substring(1).toLowerCase();
            String commandContent = ColorUtil.colorize(parts[1].replace("{player}", player.getName()).replace("%player%", player.getName()));

            commandContent = PAPIUtil.set(player, commandContent);

            String commandToExecute;
            // commandParts[0] = 动作组  commandParts[1] = 条件
            String[] commandParts = commandContent.split("@condition:", 2);
            // 如果找到condition
            if (commandParts.length > 1) {
                // 处理条件里的空格
                String condition = commandParts[1];
              //  player.sendMessage("条件" + condition);


                // 处理条件
                if (!ConditionUtil.checkCondition(condition)) {
                    return;
                } else {
                    // 返回满足条件的动作组
                    commandToExecute = commandParts[0];
                  //  player.sendMessage("动作" + commandToExecute);
                }

            } else {
                // 如果找不到condition直接返回原始的内容
                commandToExecute = commandContent;
            }
//            if (player.isOp())
//                player.sendMessage("§d[指令调试-管理员可见] §a将要执行的操作 §f[" + type + "]" + commandToExecute);


            switch (type) {
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
                    break;
                case "player":
                    player.performCommand(commandToExecute);
                    break;
                case "tell":
                    player.sendMessage(commandToExecute.replace("%serverUrl%", ConfigFile.url).replace("%techUrl%", ConfigFile.techUrl));
                    break;
                case "sound":
                    XSound.matchXSound(commandToExecute).get().play(player);
//                    player.playSound(player.getLocation(), Sound.valueOf(commandContent), 1.0f, 1.0f);
                    break;
                case "broadcast":
                    PluginMsgListener.sendAlertToBungeeCord(commandToExecute);
                    break;
                case "give-money":
                    VaultHook.giveMoney(player, String.valueOf(getRandomNumberInRange(commandToExecute)));
                    break;
                case "give-points":
                    PlayerPointsHook.givePoints(player, String.valueOf(getRandomNumberInRange(commandToExecute)));
                    break;
                case "close":
                    player.closeInventory();
                    break;
                case "open":
                    if (commandToExecute.equalsIgnoreCase("main")) {
                        MainGUI.openMenu(player);
                    } else {
                        if (GUIFile.rewardMenuEnable) {
                            RewardGUI.openMenu(player);
                        } else {
                            player.sendMessage(LangFile.prefix + LangFile.rewardUntenable);
                        }
                    }
                    break;
                case "title":
                    String[] args = commandToExecute.split(" ");
                    String title = args[0];
                    String subTitle = args.length > 1 ? args[1] : " ";
                    int fadeIn = args.length > 3 ? Integer.parseInt(args[2]) : 10;
                    int stay = args.length > 4 ? Integer.parseInt(args[3]) : 60;
                    int fadeOut = args.length > 5 ? Integer.parseInt(args[4]) : 10;
                    Player p = player.getPlayer();
                    p.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
                    break;
                case "bossbar":
                    String[] arg = commandToExecute.split(" ");
                    String message = arg[0];
                    String colorName = arg[1].toUpperCase();
                    int time = arg.length > 2 ? Integer.parseInt(arg[2]) : 15;

                    try {
                        BarColor color = BarColor.valueOf(colorName);
                        BossBar bossBar = Bukkit.createBossBar(message, color, BarStyle.SOLID);
                        bossBar.setProgress(1.0);
                        bossBar.addPlayer(player);

                        // 逐渐减少进度
                        for (int i = 0; i <= time; i++) {
                            final double progress = 1.0 - ((double) i / time);
                            Bukkit.getScheduler().runTaskLater(MineBBSTopper.instance, () -> bossBar.setProgress(progress), 20L * i);
                        }

                        // 在指定时间后移除 BossBar
                        Bukkit.getScheduler().runTaskLater(MineBBSTopper.instance, () -> bossBar.removePlayer(player), 20L * time);

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        player.sendMessage(LangFile.prefix + "§c错误: 无效的BOSSBAR颜色。请联系管理员使用正确的颜色名称。");
                    }
                    break;
                case "connect":
                    try {
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(byteArray);
                        out.writeUTF("Connect");
                        out.writeUTF(commandToExecute);
                        player.sendPluginMessage(MineBBSTopper.instance, "BungeeCord", byteArray.toByteArray());
                    } catch (Exception ex) {
                        player.sendMessage(LangFile.prefix + "§c在连接子服时发生错误，请联系管理员查看报错。");
                        ex.printStackTrace();
                    }
                    break;
                case "op":
                    boolean isOp = player.isOp();
                    try {
                        if (!isOp) {
                            player.setOp(true);
                        }
                        player.performCommand(commandToExecute);
                    } finally {
                        if (!isOp) {
                            player.setOp(false);
                        }
                    }
                    break;
                case "[null]":
                    break;
                default:
                    player.performCommand(commandToExecute);
                    break;
            }
        } else {
            player.performCommand(action);
        }
    }

    public static void broadcast(String meg) {
        String message = ColorUtil.colorize(meg);
        message = message.replace("\\n", "\n"); // 将字符串中的 \n 转换为换行符号

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            for (String line : message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }

    public static int getRandomNumberInRange(String origin) {
        try {
            if (!origin.contains("-")) {
                return (int) Math.round(Double.parseDouble(origin));
            } else {
                String[] range = origin.split("-");
                if (range.length != 2) {
                    throw new IllegalArgumentException("未知格式 用符号-分割");
                }

                int min = (int) Math.round(Double.parseDouble(range[0]));
                int max = (int) Math.round(Double.parseDouble(range[1]));

                if (min >= max) {
                    throw new IllegalArgumentException("左边不能大于右边");
                }

                Random r = new Random();
                return r.nextInt((max - min) + 1) + min;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("错误数字格式: " + origin, e);
        }
    }
}
