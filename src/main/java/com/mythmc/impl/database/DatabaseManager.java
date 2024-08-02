package com.mythmc.impl.database;

import com.mythmc.MineBBSTopper;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.api.DbManager;
import com.mythmc.impl.database.mysql.MySQLConnection;
import com.mythmc.impl.database.mysql.MySQLManager;
import com.mythmc.impl.database.sqlite.SQLiteManager;
import com.mythmc.impl.database.yaml.YamlManager;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final MineBBSTopper plugin;


    public DatabaseManager(MineBBSTopper plugin) {
        this.plugin = plugin;
    }
    private static volatile DbManager dbManager = null;
    public static String dbPath = null;

    public  void initialize(String type) {
        if (dbManager == null) {
            synchronized (DatabaseManager.class) {
                if (dbManager == null) {
                    switch (type) {
                        case "mysql":
                            dbManager = new MySQLManager();
                            break;
                        case "sqlite":
                            dbManager = new SQLiteManager();
                            break;
                        case "yaml":
                            dbManager = new YamlManager();
                            break;
                        default:
                            plugin.logger("§c警告 §7| §c无法正确加载名为 §f" + ConfigFile.DataType + " §c的数据库");
                            plugin.getPluginLoader().disablePlugin(plugin);
                    }
                }
            }
        }
    }

    public void load() {
        plugin.logger("§6数据 §8| §a当前选择的数据库为: " + ConfigFile.DataType);
        String type = ConfigFile.DataType.toLowerCase();
        initialize(type);

        switch (type) {
            case "mysql":
                // mysql处理
                plugin.logger("§6数据 §8| §a正在尝试访问数据库");
                MySQLConnection.dataSource = new HikariDataSource(MySQLConnection.getHikariConfig());
                dbManager.createTable();
                break;
            case "sqlite":
                // sqlite处理
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) dataFolder.mkdirs();
                dbPath = dataFolder.getAbsolutePath() + File.separator + "database.db";
                // 检查数据库文件是否存在，如果不存在则重新创建数据库连接
                if (!new File(dbPath).exists()) plugin.logger("§6数据 §8| §e数据库文件不存在，重建数据库连接");
                dbManager.createTable();
                plugin.logger("§6数据 §8| §a成功创建数据库连接");
                break;
            case "yaml":
                // yaml处理
                dbManager.createTable();
                break;
            default:

        }
        writeOnlinePlayerRewardData();
    }
    private void writeOnlinePlayerRewardData() {
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !dbManager.playerRewardExists(name))
                .forEach(name -> dbManager.insertRewardData(name));
    }
    public void close() {
        String type = ConfigFile.DataType.toLowerCase();
        switch (type) {
            case "mysql":
                MySQLConnection.close();
                break;
            case "sqlite":
                SQLiteManager.close();
                break;
            case "yaml":

                break;
        }
        plugin.logger("§c卸载 §8| §6数据库连接已关闭: " + ConfigFile.DataType);
    }

    public static DbManager getDbManager() {
        return dbManager;
    }
}