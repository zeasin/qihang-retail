package cn.qihangerp.utils;

import java.lang.management.ManagementFactory;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 *
 * @author qihang
 */
public class DateUtils {

    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    private static final String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前日期时间
     */
    public static LocalDateTime getNowDate() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期时间字符串
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DEFAULT_FORMATTER);
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, LocalDateTime.now());
    }

    public static final String dateTime(final LocalDateTime date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final LocalDateTime date) {
        return DateTimeFormatter.ofPattern(format).format(date);
    }

    public static final LocalDateTime dateTime(final String format, final String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 把符合日期格式的字符串转换为日期类型
     */
    public static LocalDateTime stringtoDate(String dateStr, String format) {
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime stringtoDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr, DEFAULT_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 时间字符串转时间戳
     */
    public static final Long dateTimeStrToTimeStamp(String format, final String time) {
        try {
            if (format == null || format.isEmpty()) {
                format = YYYY_MM_DD_HH_MM_SS;
            }
            LocalDateTime ldt = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(format));
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTimePath() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 字符串转日期
     */
    public static LocalDateTime parseDate(Object str) {
        if (str == null) {
            return null;
        }
        String strVal = str.toString();
        for (String pattern : parsePatterns) {
            try {
                return LocalDateTime.parse(strVal, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                // 尝试下一个格式
            }
            try {
                return LocalDate.parse(strVal, DateTimeFormatter.ofPattern(pattern)).atStartOfDay();
            } catch (Exception e) {
                // 尝试下一个格式
            }
        }
        return null;
    }

    /**
     * 获取服务器启动时间
     */
    public static LocalDateTime getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 计算相差天数
     */
    public static int differentDays(LocalDateTime date1, LocalDateTime date2) {
        return (int) Math.abs(Duration.between(date1, date2).toDays());
    }

    /**
     * 计算时间差
     *
     * @param endTime   结束时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(LocalDateTime endTime, LocalDateTime startTime) {
        Duration duration = Duration.between(startTime, endTime);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        return days + "天" + hours + "小时" + minutes + "分钟";
    }

    /**
     * 日期转字符串 yyyy-MM-dd HH:mm:ss
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * 日期转字符串 指定格式
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
