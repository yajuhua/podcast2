package com.podcast.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间格式
 */
public class TimeFormat {
    /**
     * 时间格式转换
     * @param ms 毫秒 要在后面加L，不然默认是int型
     * @return
     */
    public static String change(String ms){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy H:m:s ZZZ", Locale.ENGLISH);
        //转换为long型
        String date = dateFormat.format(new Date(Long.parseLong(ms)));
        return date;
    }

    /**
     * 当前时间
     * @param
     * @return 返回以EEE, dd MMM yyyy H:m:s ZZZ为格式的时间
     */
    public static String now(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy H:m:s ZZZ", Locale.ENGLISH);
        //转换为long型
        String date = dateFormat.format(new Date(Long.parseLong(String.valueOf(System.currentTimeMillis()))));
        return date;
    }

    /**
     * 将秒数转换成 00:24:01这种时长形式的字符串
     * @param s 秒数
     * @return
     */
    public static String duration(int s){

        // 创建一个Duration对象，表示1200秒
        Duration duration = Duration.ofSeconds(s);

        // 获取总小时数、分钟数和秒数
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long remainingSeconds = duration.toSecondsPart();

        // 创建一个LocalTime对象，表示时间部分
        LocalTime time = LocalTime.of((int)hours, (int)minutes, (int)remainingSeconds);

        // 使用DateTimeFormatter将LocalTime格式化为"00:20:00"的字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);

        return formattedTime;
    }

    /**
     * 字符串时间解析时间 秒
     * @param TimeString
     * @return 秒
     */
    public static long parseTimeString(String TimeString){
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy H:m:s ZZZ", Locale.ENGLISH);
        long timestamp = 1L;

        try {
            Date date = format.parse(TimeString);
            timestamp = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp/1000;
    }


    /**
     * 根据时间秒数转换成
     * @param timestamp 秒
     * @return 今天8:25、昨天22:21、8月12日、2023年11月12日 这三种形式
     */
    public static String formatDate(long timestamp) {
        Date currentDate = new Date();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(timestamp * 1000);

        SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");
        SimpleDateFormat dayFormat = new SimpleDateFormat("M月d日");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy年M月d日");

        if (isSameDay(currentCalendar, targetCalendar)) {
            return "今天 " + timeFormat.format(targetCalendar.getTime());
        } else if (isYesterday(currentCalendar, targetCalendar)) {
            return "昨天 " + timeFormat.format(targetCalendar.getTime());
        } else if (isDayBeforeYesterday(currentCalendar, targetCalendar)) {
            return "前天 " + timeFormat.format(targetCalendar.getTime());
        } else if (isSameYear(currentCalendar, targetCalendar)) {
            return dayFormat.format(targetCalendar.getTime());
        } else {
            return yearFormat.format(targetCalendar.getTime());
        }
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isYesterday(Calendar cal1, Calendar cal2) {
        cal1.add(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(cal1, cal2);
    }

    private static boolean isDayBeforeYesterday(Calendar cal1, Calendar cal2) {
        cal1.add(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(cal1, cal2);
    }

    private static boolean isSameYear(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

}
