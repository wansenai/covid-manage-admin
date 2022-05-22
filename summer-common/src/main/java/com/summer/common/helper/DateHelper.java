package com.summer.common.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.summer.common.support.CalendarInfo;
import com.summer.common.support.DateFormat;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * 时间工具类
 **/
public final class DateHelper {
    public static final long SECOND_TIME = 1000L;
    public static final long MINUTE_TIME = 60 * SECOND_TIME;
    public static final long HOUR_TIME = 60 * MINUTE_TIME;
    public static final long DAY_TIME = 24 * HOUR_TIME;
    /**
     * 给定 format 将 string 转换成 LocalDate
     **/
    private static final Set<DateFormat> DF = Sets.newHashSet(DateFormat.ShortNumDate, DateFormat.NumDate, DateFormat.StrikeDate);
    public static Date MIN = ofDate(0L);

    private DateHelper() {
    }

    /**
     * 给定 format 将 Date 转换成 string
     **/
    public static String format(final Date date, final DateFormat format) {
        return null == date ? format.name() : new SimpleDateFormat(format.name()).format(date);
    }

    /**
     * 给定 format 将 LocalDate 转换成 string
     **/
    public static String format(final LocalDate date, DateFormat format) {
        return null == date ? format.name() : date.format(DateTimeFormatter.ofPattern(format.name()));
    }

    /**
     * 给定 format 将 LocalDateTime 转换成 string
     **/
    public static String format(final LocalDateTime date, DateFormat format) {
        return null == date ? format.name() : date.format(DateTimeFormatter.ofPattern(format.name()));
    }

    /**
     * 给定 format 将毫秒时间戳 string
     **/
    public static String format(long time, DateFormat format) {
        return format(ofLocalDateTime(time), format);
    }

    /**
     * 给定 format 将 string 转换成 Date
     **/
    public static Date ofDate(String sDate, DateFormat format) {
        try {
            return new SimpleDateFormat(format.name()).parse(sDate);
        } catch (Exception e) {
            throw new RuntimeException("ofDate error, sDate: " + sDate + " format: " + format.name(), e);
        }
    }

    /**
     * 将秒时间戳 转换成 Date
     **/
    public static Date ofDate(long time) {
        return Date.from(Instant.ofEpochMilli(time));
    }

    public static LocalDate ofLocalDate(String sDate, DateFormat format) {
        if (!DF.contains(format)) {
            throw new RuntimeException("ofLocalDate only use to parse day can not parse time");
        }
        return LocalDate.parse(sDate, DateTimeFormatter.ofPattern(format.name()));
    }

    /**
     * 将秒时间戳 转换成 LocalDate
     **/
    public static LocalDate ofLocalDate(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 给定 format 将 string 转换成 LocalDateTime
     **/
    public static LocalDateTime ofLocalDateTime(String sDate, DateFormat format) {
        return LocalDateTime.parse(sDate, DateTimeFormatter.ofPattern(format.name()));
    }

    /**
     * 将毫秒时间戳 转换成 LocalDateTime
     **/
    public static LocalDateTime ofLocalDateTime(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    /**
     * 将毫秒时间戳 转换成 LocalDate
     **/
    public static LocalDate date2Local(long date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 将毫秒时间戳 转换成 LocalDateTime
     **/
    public static LocalDateTime time2Local(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    /**
     * 当前系统时间 Date 类型
     **/
    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    /**
     * 给定 format 将当前系统时间转换成 string
     **/
    public static String now(DateFormat format) {
        return format(now(), format);
    }

    /**
     * 当前系统时间秒
     **/
    public static long second() {
        return Instant.now().getEpochSecond();
    }

    /**
     * Date 时间秒
     **/
    public static long second(final Date date) {
        return null == date ? 0L : date.getTime() / 1000;
    }

    /**
     * LocalDate 时间秒
     **/
    public static long second(final LocalDate date) {
        return null == date ? 0L : date.atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * LocalDateTime 时间秒
     **/
    public static long second(final LocalDateTime dateTime) {
        return null == dateTime ? 0L : dateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 当前系统时间毫秒
     **/
    public static long time() {
        return System.currentTimeMillis();
    }

    public static double doubleTime() {
        return doubleTime(System.currentTimeMillis());
    }

    /**
     * 将 LocalDate 转换成毫秒
     **/
    public static long time(final Date date) {
        return null == date ? 0L : date.getTime();
    }

    /**
     * 将毫秒时间戳 转换成 yyyyMMddHHmmss.SSS
     **/
    public static double doubleTime(long time) {
        return Double.parseDouble(format(time, DateFormat.DoubleDateTime));
    }

    /**
     * 将 LocalDate 转换成毫秒
     **/
    public static long time(final LocalDate date) {
        return null == date ? 0L : date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 将 LocalDateTime 转换成毫秒
     **/
    public static long time(final LocalDateTime dateTime) {
        return null == dateTime ? 0L : dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 指定当天几点时间
     **/
    public static Date hourAt(int hour) {
        if (hour < 0 || hour > 23) {
            throw new RuntimeException("hour must in [0, 23], but actual is " + hour);
        }
        return instanceDate(hour, 0, 0);
    }

    /**
     * 指定当天几点几分时间
     **/
    public static Date minuteAt(int hour, int minute) {
        checkHourMinute(hour, minute);
        return instanceDate(hour, minute, 0);
    }

    /**
     * 指定当天几点几分几秒时间
     **/
    public static Date secondAt(int hour, int minute, int second) {
        checkHourMinute(hour, minute);
        if (second < 0 || second > 59) {
            throw new RuntimeException("second must in [0, 59], but actual is " + second);
        }
        return instanceDate(hour, minute, second);
    }

    /**
     * 当前时间距离下次某一时刻的时间差, 如果时间已经过去那么计算下一周期经过这一时刻的时间差(TimeUnit.MILLISECONDS)
     **/
    public static long timeSlot(final Date time, final Long period) {
        long now = DateHelper.time();
        long timeLong = time.getTime();
        // 给的时间大于当前时间
        if (now < timeLong) {
            // 给定时间大于了多个执行周期
            if (timeLong - now > period) {
                return (timeLong - now) % period;
            } else {
                return timeLong - now;
            }
        }
        //给定时间小于当前时间且小于多个执行周期
        if (now - timeLong >= period) {
            long slot = (now - timeLong) % period;
            return 0 == slot ? 0 : period - slot;
        } else {
            return period - (now - timeLong);
        }
    }

    /**
     * 给定毫秒时间戳得出当天的开始时间毫秒
     **/
    public static long ofDayStart(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ofDate(time));
        return instanceDate(0, 0, 0, calendar).getTime();
    }

    /**
     * 在给定的 Date 时间加 分钟
     **/
    public static Date addMinutes(final Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * 在给定的 Date 时间加 秒
     **/
    public static Date addSecond(final Date date, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }

    /**
     * 在给定的 Date 时间加 hours 小时
     **/
    public static Date addHours(final Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hours);
        return calendar.getTime();
    }

    /**
     * 在给定的 Date 时间加 days 天
     **/
    public static Date addDays(final Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static Date addYears(int years) {
        Date date = now();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    public static Date addYears(final Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    public static Date addMonths(final Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 给定时间为一周的第几天，from 1 (Monday) to 7 (Sunday)， 0 InvalidDate
     **/
    public static int dayOfWeek(final Date date) {
        return null == date ? 0 : ofLocalDate(date.getTime()).getDayOfWeek().getValue();
    }

    /**
     * 给定的 date 为周几 from 1 (Monday) to 7 (Sunday)
     **/
    public static int dayOfWeek(final LocalDate date) {
        return null == date ? 0 : date.getDayOfWeek().getValue();
    }

    /**
     * from 1 (Monday) to 7 (Sunday)
     **/
    public static int dayOfWeek() {
        return LocalDate.now().getDayOfWeek().getValue();
    }

    /**
     * 获取两个日期之前相差的天数据
     */
    public static BigDecimal daySize(final Date startDate, final Date endDate) {
        if (startDate == null || endDate == null) {
            return new BigDecimal("-1");
        }
        long timMillis = Math.abs(startDate.getTime() - endDate.getTime());
        return BigDecimal.valueOf(timMillis).divide(BigDecimal.valueOf(DAY_TIME), 6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 取当月天数
     **/
    public static int monthDays(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        calendar.roll(Calendar.DATE, -1);
        return calendar.get(Calendar.DATE);
    }

    /**
     * 获取指定时间所在月的开始时间
     **/
    public static Date firstDayOfMonth(final Date date) {
        if (null == date) {
            return DateHelper.MIN;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * 获取指定时间所在月的结束时间
     **/
    public static Date lastDayOfMonth(final Date date) {
        if (null == date) {
            return DateHelper.MIN;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, monthDays(calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1)));
        return calendar.getTime();
    }

    /**
     * 是否今天
     **/
    public static boolean isToday(final LocalDate date) {
        LocalDate now = LocalDate.now();
        return now.getYear() == date.getYear()
                && now.getMonthValue() == date.getMonthValue()
                && now.getDayOfMonth() == date.getDayOfMonth();
    }

    /**
     * 获取指定时间所在周的开始时间
     **/
    public static Date firstDayOfWeek(final Date date) {
        if (null == date) {
            return DateHelper.MIN;
        }
        Calendar calendar = newCalendar(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return calendar.getTime();
    }

    /**
     * 获取指定时间所在周的结束时间
     **/
    public static Date lastDayOfWeek(final Date date) {
        if (null == date) {
            return DateHelper.MIN;
        }
        Calendar calendar = newCalendar(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() + 6);
        return calendar.getTime();
    }

    /**
     * 日历信息
     **/
    public static <T> List<CalendarInfo<T>> calender(int year, int month, final Function<LocalDate, Optional<T>> func) {
        if (MathHelper.nvl(year) < 1111 || !MathHelper.isBetween(month, 1, 12)) {
            return Lists.newArrayList();
        }
        List<CalendarInfo<T>> cdList = Lists.newArrayList();
        int monthDays = DateHelper.monthDays(year, month);
        CalendarInfo<T> cdR;
        for (int day = 1; day < monthDays + 1; day++) {
            cdR = new CalendarInfo<>();
            cdR.day = day;
            final LocalDate date = LocalDate.of(year, month, day);
            cdR.weekDay = DateHelper.dayOfWeek(date);
            cdR.isToday = DateHelper.isToday(date);
            if (null != func) {
                Optional<T> optional = func.apply(date);
                if (optional.isPresent()) {
                    cdR.info = optional.get();
                }
                cdList.add(cdR);
            }
        }
        return cdList;
    }

    private static Date instanceDate(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        return instanceDate(hour, minute, second, calendar);
    }

    private static Date instanceDate(int hour, int minute, int second, Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static void checkHourMinute(int hour, int minute) {
        if (hour < 0 || hour > 23) {
            throw new RuntimeException("hour must in [0, 23], but actual is " + hour);
        }
        if (minute < 0 || minute > 59) {
            throw new RuntimeException("minute must in [0, 59], but actual is " + minute);
        }
    }

    private static Calendar newCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 计算距离当前时间n个月的时间戳
     */
    public static Long monthLongAgo(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now());
        calendar.add(Calendar.MONTH, -month);
        Date time = calendar.getTime();
        /**确定开始时间为当前月的1号 **/
        Date firstDayOfMonth = firstDayOfMonth(time);
        return firstDayOfMonth.getTime();
    }

    /**
     * 计算某个时间戳与当前相差月份
     */
    public static int monthBetweenNow(Long startTime) {
        LocalDate now = LocalDate.now();
        LocalDate start = ofLocalDate(startTime);
        int month = now.getMonthValue() - start.getMonthValue();
        int year = now.getYear() - start.getYear();
        return year * 12 + month;
    }

    /**
     * 比较 beginTime > endTime true
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean afterCalendar(Date beginTime, Date endTime) {
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return begin.after(end);
    }

    /**
     * 比较 beginTime >= endTime false
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean beforeCalendar(Date beginTime, Date endTime) {
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return begin.before(end);
    }
}
