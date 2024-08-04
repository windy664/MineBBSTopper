package com.mythmc.tools.remote.core;

import com.mythmc.MineBBSTopper;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.tools.Debugger;
import com.mythmc.tools.remote.core.parser.ParserManager;
import com.mythmc.tools.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerHtmlParser implements ParserManager {


    // 异步获取时间元素的方法
    public void fetchTimeElementsAsync(TimeElementResultHandler handler) {
        // 使用 Bukkit 的异步任务调度器执行任务
        Bukkit.getScheduler().runTaskAsynchronously(MineBBSTopper.instance, () -> {
            List<String> list = new ArrayList<>(); // 用于存储从网页上提取的时间数据
            try {
                // 使用 Jsoup 连接到指定的 URL
                Connection connection = Jsoup.connect(ConfigFile.url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // 设置用户代理
                        .timeout(10000); // 设置超时时间为10秒
                if (ConfigFile.isProxyMode) connection.proxy(ConfigFile.proxyIP,ConfigFile.proxyPort); // 使用代理
                Document doc = connection.get(); // 获取网页内容
                Elements timeElements = doc.select("time.u-dt"); // 选择网页上所有具有 'time.u-dt' 类的元素

                if (!timeElements.isEmpty()) { // 如果找到时间元素
                    int count = 0;
                    int size = timeElements.size();
                    for (int i = 0; i < size && count < 2; i++) {
                        Element timeElement = timeElements.get(i); // 获取时间元素
                        String datetime = timeElement.attr("data-time"); // 提取 'data-time' 属性的值
                        list.add(datetime); // 添加到列表中
                        count++;
                    }
                    // 处理结果，转换为时间戳并进行比较
                    long timeStamp = Long.parseLong(list.get(1)); // 从列表中获取第二个时间戳 -> 这个即为记录的顶贴时间
                    // 将时间戳转换为格式化时间字符串，输出提取到的时间到控制台
                    Debugger.logger("§a从宣传贴上提取到的顶贴时间为 §e" + TimeUtil.convertTimestamp(timeStamp));
                    long currentTimestamp = System.currentTimeMillis() / 1000; // 获取当前时间戳（秒）

                    long compare = currentTimestamp - timeStamp; // 计算时间差

                    // 检查时间差是否大于配置文件中的阈值（以分钟为单位）
                    boolean isMoreThanTenMinutes = compare / 60 <= ConfigFile.claimTime;
                    // 调用回调方法处理结果，返回布尔值的字符串格式
                    handler.handleResult(String.valueOf(isMoreThanTenMinutes));
                } else {
                    // 没有找到时间元素，调用回调方法
                    handler.handleResult("error");
                    Debugger.logger("§c没有找到对应元素，返回为空，请检查宣传帖子状态!!!");
                }
            } catch (IOException e) {
                // 捕获并处理 IO 异常
                Debugger.logger("§c获取异常，请寻求插件开发者帮助");
                e.printStackTrace(); // 打印异常堆栈
                // 返回异常表示
                handler.handleResult("error");
            }
        });
    }

}