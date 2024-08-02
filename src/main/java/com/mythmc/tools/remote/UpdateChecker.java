package com.mythmc.tools.remote;

import com.mythmc.MineBBSTopper;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.file.statics.LangFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private final MineBBSTopper plugin;
    private static final String API_URL = "https://api.minebbs.com/api/openapi/v1/resources/8762";

    public UpdateChecker(MineBBSTopper plugin) {
        this.plugin = plugin;
    }
    public void checkForUpdates() {
        checkForUpdates(null);
    }

    public void checkForUpdates(Player p) {
        if (ConfigFile.UpdateCheck) {
            // 异步检查更新
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    JSONObject json  = getJsonObject().getJSONObject("data");
                    String updateUrl = json.getString("view_url");
                    String updateMessage = createUpdateMessage(plugin.getDescription().getVersion(),
                            json.getString("version").substring(0, 3),updateUrl);

                    if (p == null) {
                        plugin.logger("§5更新 §8| " + updateMessage);
                    } else {
                        p.sendMessage(LangFile.prefix + updateMessage + " §a感谢您的使用！");
                    }
                } catch (Exception e) {
                    plugin.logger("§5更新 §8| §c顶贴插件检查更新时出现错误!");
                }
            });
        }
    }

    private String createUpdateMessage(String pluginVersion, String latestVersion, String updateUrl) {
        if (latestVersion != null) {
            return Double.parseDouble(pluginVersion) < Double.parseDouble(latestVersion) ?
                    "§d顶贴插件发现新版本! 当前版本: v" + pluginVersion + ", 最新版本: v" + latestVersion + "\n§6下载地址为: §a§n" + updateUrl:
                    "§a您已使用最新版本: v" + pluginVersion;
        }
        return "§c顶贴插件检查更新时出现错误!";
    }

    private JSONObject getJsonObject() throws IOException {
        // 创建链接
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 读取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();

        // 解析JSON数据
        return new JSONObject(response.toString());
    }



}
