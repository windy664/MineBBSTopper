package com.mythmc.tools.utils;

import com.mythmc.MineBBSTopper;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.tools.Debugger;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    private static final long SECONDS_THRESHOLD = 10000000000L;
    public static String convertTimestamp(long Timestamp) {
        boolean isMillisecond = Timestamp > SECONDS_THRESHOLD;
        long adjustedTimestamp = isMillisecond ? Timestamp : Timestamp * 1000;

        Date date = new Date(adjustedTimestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return formatter.format(date);
    }
    public static long convertDataTime(String dateTimeStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date = formatter.parse(dateTimeStr);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static boolean isCurrentHourWithinSpan() {
        String[] parts = ConfigFile.claimSpan.split("-");
        if (parts.length != 2) {
            MineBBSTopper.instance.getServer().getPluginManager().disablePlugins();
            Debugger.logger("§c请重新配置时间跨度，格式为：hh-hh 例如:8-22");
        }

        int startHour = Integer.parseInt(parts[0]);
        int endHour = Integer.parseInt(parts[1]);

        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();

        return currentHour >= startHour && currentHour <= endHour;
    }

}
