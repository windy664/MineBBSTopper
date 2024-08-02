package com.mythmc.tools.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mythmc.externs.hook.placeholders.PlaceholderHook;
import org.bukkit.entity.Player;

public class ConditionUtil {

    public static boolean checkCondition(String condition) {
        // 解析条件字符串
        Pattern pattern = Pattern.compile("(.+?)([<>!=]+)(.+)");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String leftOperand = matcher.group(1).trim();
            String operator = matcher.group(2).trim();
            String rightOperand = matcher.group(3).trim();

            try {
                // 尝试将操作数转换为数字进行比较
                double leftValue = Double.parseDouble(leftOperand);
                double rightValue = Double.parseDouble(rightOperand);

                switch (operator) {
                    case "==":
                        return leftValue == rightValue;
                    case "!=":
                        return leftValue != rightValue;
                    case ">":
                        return leftValue > rightValue;
                    case "<":
                        return leftValue < rightValue;
                    case ">=":
                        return leftValue >= rightValue;
                    case "<=":
                        return leftValue <= rightValue;
                    default:
                        return false;
                }
            } catch (NumberFormatException e) {
                // 如果无法转换为数字，则进行字符串比较
                switch (operator) {
                    case "==":
                        return leftOperand.equals(rightOperand);
                    case "!=":
                        return !leftOperand.equals(rightOperand);
                    default:
                        return false;
                }
            }
        }

        // 如果条件不匹配，返回 false
        return false;
    }
}
