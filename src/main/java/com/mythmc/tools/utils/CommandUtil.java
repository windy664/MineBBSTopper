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
            // 分割原始内容
            String[] parts = action.split("]", 2);
            // 找出动作类型
            String type = parts[0].substring(1).toLowerCase();
            // 替换内置占位符
            String commandContent = ColorUtil.colorize(parts[1].replace("{player}", player.getName()).replace("%player%", player.getName()));
            // 使用 PAPI 解析变量
            commandContent = PAPIUtil.set(player, commandContent);
            // 初始化命令内容
            String commandToExecute;
            // 分割条件与命令内容
            String[] commandParts = commandContent.split("@condition:", 2);
            // 如果找到condition
            if (commandParts.length > 1) {
                // 处理条件里的空格
                String condition = commandParts[1];
                // 处理条件
                if (!ConditionUtil.checkCondition(condition)) {
                    return;
                } else {
                    // 返回满足条件的动作组
                    commandToExecute = commandParts[0];
                }

            } else {
                // 如果找不到condition直接返回原始的内容
                commandToExecute = commandContent;
            }


            switch (type) {
                // 控制台执行指令
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
                    break;
                // 玩家执行指令
                case "player":
                    player.performCommand(commandToExecute);
                    break;
                // 向玩家发送消息
                case "tell":
                    player.sendMessage(commandToExecute.replace("%serverUrl%", ConfigFile.url).replace("%techUrl%", ConfigFile.techUrl));
                    break;
                // 使用 XSound 向玩家发送音效
                case "sound":
                    XSound.matchXSound(commandToExecute).get().play(player);
                    break;
                // 全服播报，BC可子服接受信息
                case "broadcast":
                    PluginMsgListener.sendAlertToBungeeCord(commandToExecute);
                    break;
                // 给予游戏币，支持随机值
                case "give-money":
                    VaultHook.giveMoney(player, String.valueOf(getRandomNumberInRange(commandToExecute)));
                    break;
                // 给予点券，支持随机值
                case "give-points":
                    PlayerPointsHook.givePoints(player, String.valueOf(getRandomNumberInRange(commandToExecute)));
                    break;
                // 关闭玩家当前的菜单界面
                case "close":
                    player.closeInventory();
                    break;
                // 为玩家打开一个菜单界面 可选 main/reward
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
                // 向玩家发送标题，最少得有一个参数
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
                // 向玩家发送一个进度条 BOSSBAR
                case "bossbar":
                    // 解析字符串
                    String[] arg = commandToExecute.split(" ");
                    String message = arg[0];
                    String colorName = arg[1].toUpperCase();
                    int time = arg.length > 2 ? Integer.parseInt(arg[2]) : 15;

                    try {
                        // 创建 BOSSBAR 基础
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
                // 让玩家连接到子服
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
                // 把玩家设置为OP执行指令完成后撤销OP（不建议使用）
                case "op":
                    // 获取玩家OP状态
                    boolean isOp = player.isOp();
                    try {
                        if (!isOp) {
                            // 如果不是OP，则设为OP
                            player.setOp(true);
                        }
                        // 执行指令
                        player.performCommand(commandToExecute);
                    } finally { // 执行完毕后
                        // 判断原始OP状态
                        if (!isOp) {
                            // 原本不是OP，就卸掉他的OP
                            player.setOp(false);
                        }
                    }
                    break;
                // 不执行任何操作
                case "[null]":
                    break;
                // 没找到指定类型动作，则直接以玩家身份执行命名
                default:
                    player.performCommand(commandToExecute);
                    break;
            }
        } else {
            // 不含动作，则直接以玩家身份执行命名
            player.performCommand(action);
        }
    }

    public static void broadcast(String meg) {
        // 格式化颜色
        String message = ColorUtil.colorize(meg);
        // 分割段落
        message = message.replace("\\n", "\n"); // 将字符串中的 \n 转换为换行符号
        // 遍历在线玩家
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            for (String line : message.split("\n")) {
                // 发送消息
                player.sendMessage(line);
            }
        }
    }

    public static int getRandomNumberInRange(String origin) {
        try {
            // 预判断时间格式是否正确
            if (!origin.contains("-")) {
                // 强制转换double为int类型
                return (int) Math.round(Double.parseDouble(origin));
            } else {
                // 开始分割时间
                String[] range = origin.split("-");
                if (range.length != 2) {
                    // 不存在符号则抛出错误
                    throw new IllegalArgumentException("未知格式 用符号-分割");
                }
                // 转换
                int min = (int) Math.round(Double.parseDouble(range[0]));
                int max = (int) Math.round(Double.parseDouble(range[1]));
                // 比大小
                if (min >= max) {
                    throw new IllegalArgumentException("左边不能大于右边");
                }
                // 随机值对象
                Random r = new Random();
                // 因为是从0开始，所以得+1
                return r.nextInt((max - min) + 1) + min;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("错误数字格式: " + origin, e);
        }
    }
}
