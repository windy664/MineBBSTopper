package com.mythmc.tools.remote;

import com.mythmc.MineBBSTopper;
import com.mythmc.file.statics.ConfigFile;
import org.bukkit.Bukkit;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class WebPreChecker {
    private final MineBBSTopper plugin;

    public WebPreChecker(MineBBSTopper plugin) {
        this.plugin = plugin;
    }
    public void start() {
        try {
            String[] split = ConfigFile.reconnect.split(":");
            if (split.length != 2)
                throw new IllegalArgumentException("§2检查 §8| §c配置格式错误，应该为 '最大重连次数:重连间隔秒数'");

            final int maxRetries = Integer.parseInt(split[0]);
            int reconnectInterval = Integer.parseInt(split[1]) * 20;
            preCheckWithRetries(maxRetries, 0, reconnectInterval);
        } catch (NumberFormatException e) {
            plugin.logger("§2检查 §8| §c配置的整数格式不正确，请检查是否为 '最大重连次数:重连间隔秒数'");
        } catch (IllegalArgumentException e) {
            plugin.logger("§2检查 §8| §c配置的格式不正确，请检查是否为 '最大重连次数:重连间隔秒数'");
        } catch (Exception e) {
            plugin.logger("§2检查 §8| §c预检查时发生未知异常");
            e.printStackTrace();
        }
    }

    private void preCheckWithRetries(int maxRetries, int currentRetry, int reconnectInterval) {
        Bukkit.getScheduler().runTaskAsynchronously(MineBBSTopper.instance, () -> {
            String url = ConfigFile.url;

            try {
                Connection.Response response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .execute();

                int statusCode = response.statusCode();

                if (statusCode == 200) {
                    plugin.logger("§2检查 §8| §a网址预检查成功，成功加载 MineBBSTopper，感谢您的使用!");
                    HtmlParser.fetchTimeElementsAsync(ConfigFile.url, is -> {
                    });
                } else {
                    plugin.logger("§2检查 §8| §c网址预检查失败，尝试重新连接，网址状态码: " + statusCode);
                    if (currentRetry < maxRetries - 1) {
                        // 再次尝试
                        plugin.getServer().getScheduler().runTaskLater(MineBBSTopper.instance, () ->
                                preCheckWithRetries(maxRetries, currentRetry + 1, reconnectInterval), reconnectInterval); // 延时1秒后重试
                    } else {
                        plugin.logger("§2警告 §8| §c达到最大重试次数，插件已禁用！");
                        // 在这里禁用插件
                        plugin.getServer().getPluginManager().disablePlugin(plugin);
                    }
                }
            } catch (IOException e) {
                plugin.logger("§2检查 §8| §c网址预检查失败，尝试重新连接，请检查网址是否无误或帖子状态是否正常！");
                e.getCause();
                if (currentRetry < maxRetries - 1) {
                    plugin.getServer().getScheduler().runTaskLater(MineBBSTopper.instance, () ->
                            preCheckWithRetries(maxRetries, currentRetry + 1, reconnectInterval), reconnectInterval); // 延时1秒后重试
                } else {
                    plugin.logger("§2警告 §8| §c达到最大重试次数，插件已禁用！");
                    // 在这里禁用插件
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                }
            }
        });
    }
}
