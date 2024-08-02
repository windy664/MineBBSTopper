package com.mythmc.externs.hook.placeholders;

import com.mythmc.impl.cache.TargetManager;
import com.mythmc.impl.cache.target.GlobalInfo;
import com.mythmc.impl.cache.target.PlayerInfo;
import com.mythmc.impl.database.DatabaseManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderHook extends PlaceholderExpansion {

    // 默认构造函数
    public PlaceholderHook() {
    }

    // 替换列表中的占位符
    public static List<String> listReplace(Player player, List<String> content) {
        return PlaceholderAPI.setPlaceholders(player, content);
    }

    // 替换字符串中的占位符
    public static String stringReplace(Player player, String content) {
        return PlaceholderAPI.setPlaceholders(player, content);
    }

    @Override
    public String getIdentifier() {
        return "minebbstopper";
    }

    @Override
    public String getAuthor() {
        return "404Yuner";
    }

    @Override
    public String getVersion() {
        return "1.6";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        // 分割字符串
        String[] parts = params.split("_");
        // 获取对象
        PlayerInfo playerInfo = TargetManager.getPlayerInfo(player);
        GlobalInfo globalInfo = TargetManager.getGlobalInfo();
        // 根据参数长度
        switch (parts.length) {
            case 1:
                return handleSingleParam(parts[0], playerInfo, globalInfo);
            case 2:
                return handleDoubleParam(parts, playerInfo, globalInfo);
            default:
                return "null";
        }
    }

    private String handleSingleParam(String param, PlayerInfo playerInfo, GlobalInfo globalInfo) {
        switch (param) {
            case "count":
                return String.valueOf(playerInfo.getAmount());
            case "lasttime":
                return String.valueOf(playerInfo.getLastTimeStr());
            case "cantop":
                return String.valueOf(globalInfo.getCooldownTimestamp() <= System.currentTimeMillis() / 1000);
            case "all":
                return String.valueOf(globalInfo.getAmount());
            case "todaycount":
                return String.valueOf(playerInfo.getTodayAmount());
            default:
                return "null";
        }
    }

    private String handleDoubleParam(String[] parts, PlayerInfo playerInfo, GlobalInfo globalInfo) {
        String key = parts[0];
        String value = parts[1];

        switch (key) {
            case "count":
                return String.valueOf(DatabaseManager.getDbManager().countPlayerRecords(value));
            case "cooldown":
                return handleCooldown(value, globalInfo);
            case "reward":
                return getRewardData(playerInfo, value);
            case "rank":
                return getRank(value);
            default:
                return "null";
        }
    }

    private String handleCooldown(String type, GlobalInfo globalInfo) {
        switch (type.toLowerCase()) {
            case "long":
                return String.valueOf(globalInfo.getCooldownTimestamp());
            case "iso":
                return globalInfo.getCooldownTimeStr();
            default:
                return "null";
        }
    }



    private String getRewardData(PlayerInfo playerInfo, String parts) {
        try {
            String[] rewardData = playerInfo.getRewardData();
            int index = Integer.parseInt(parts);
            if (index >= 0 && index < rewardData.length) {
                if ("1".equals(rewardData[index - 1])) {
                    return "true";
                }
            }
            return "false";
        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        }
    }
    private String getRank(String parts) {
        try {
            int position = Integer.parseInt(parts);
            List<String> cachedResult = PlaceholderCache.getCache();

            // 确保 cachedResult 不为空
            if (cachedResult == null) {
                return "---";
            }

            // 确保 position 在缓存列表的有效范围内
            return (position > 0 && position <= cachedResult.size()) ? cachedResult.get(position - 1) : "---";
        } catch (Exception e) {
            // 捕捉其他异常
            e.printStackTrace();
            return "---";
        }
    }

}
