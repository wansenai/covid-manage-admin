package com.summer.common.helper;

import com.summer.common.support.IConstant;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** 分布式唯一 ID 生成器 **/
public final class SnowIdHelper {
    // 标准雪花算法ID, 18L
    private static final Worker ID_WORKER = new Worker(10);

    public static String unique(){
        return String.valueOf(ID_WORKER.nextId());
    }

    public static long nextId(){
        return ID_WORKER.nextId();
    }

    public static String uuid() {
        return EncryptHelper.md5(UUID.randomUUID().toString() + NetworkHelper.machineIP() + System.currentTimeMillis());
    }

    private SnowIdHelper() {
    }

    /**
     * Twitter_Snowflake
     * SnowFlake的结构如下(每部分用-分开): 1位标识 + 41位时间截 + 自定义位数（最大10位）+ 12位毫秒内的序列 <= 64
     * 0 -- 0000000000 0000000000 0000000000 0000000000 0 -- xxx -- 000000000000
     * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
     * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
     * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
     * 10位的数据机器位，可以部署在1024个节点， 0 ～ 1023
     * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号
     * 加起来刚好64位，为一个Long型。
     * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
     */
    public static class Worker {
        // 开始时间截，当前年份的第一天
        private static final long START = start();
        // 毫秒内序列ID所占的位数
        private static final long SEQUENCE_BITS = 12L;
        // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
        private static final long SEQUENCE_MASK = 4095L;

        // 毫秒内序列(0~4095)
        private long sequence = 0L;
        // 上次生成ID的时间截
        private long lastTimestamp = -1L;

        /** 机器ID（0～1023, 最大占10位） **/
        private final int machineId;
        /** 时间截向左移位(12 + 自定义位数) **/
        private final long timestampLeftShift;

        /** 自定义机器号位数， 范围 [0 — 10] **/
        public Worker(int bits) {
            if(bits < 0 || bits > 10) {
                throw new IllegalArgumentException("Custom bits can't be greater than 10 or less than 0");
            }
            this.machineId = machineId(bits);
            this.timestampLeftShift = SEQUENCE_BITS + bits;
        }

        // 获得下一个ID (该方法是线程安全的)
        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
            if (timestamp < lastTimestamp) {
                long time = lastTimestamp - timestamp;
                throw new RuntimeException("Clock moved backwards refusing to generate id for " + time + " milliseconds");
            }
            //如果是同一时间生成的，则进行毫秒内序列
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                //毫秒内序列溢出
                if (sequence == 0) {
                    //阻塞到下一个毫秒,获得新的时间戳
                    timestamp = tilNextMillis(lastTimestamp);
                }
            }
            //时间戳改变，毫秒内序列重置
            else {
                sequence = 0L;
            }

            //上次生成ID的时间截
            lastTimestamp = timestamp;

            //移位并通过或运算拼到一起组成64位的ID
            return ((timestamp - START) << timestampLeftShift) | (machineId << SEQUENCE_BITS) | sequence;
        }
        // 阻塞到下一个毫秒，直到获得新的时间戳
        long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }

        private int machineId(int bits) {
            if("hostname".equalsIgnoreCase(SpringHelper.confValue(IConstant.KEY_SNOW_MID_GENERATOR))) {
                return hostNameGenerator(bits);
            }
            return ipGenerator(bits);
        }
        private int hostNameGenerator(int customBits) {
            String hostName = NetworkHelper.localHostName();
            int lastIndex = hostName.lastIndexOf(".");
            if(lastIndex < 0 || (lastIndex + 1) == hostName.length()) {
                return 0;
            }
            String machineId = hostName.substring(lastIndex + 1);
            if(StringHelper.isNumeric(machineId)) {
                int mid = Integer.parseInt(machineId);
                long maxId = ~(-1L << customBits);
                if(mid > maxId) {
                    throw new RuntimeException("Machine hostname number can't be greater than " + maxId + " or less than 0");
                }
                return mid;
            }
            return 0;
        }
        private static int ipGenerator(int bits) {
            String ip = NetworkHelper.machineIP();
            if(StringHelper.isBlank(ip)) {
                return 0;
            }
            String binaryIp = Long.toBinaryString(NetworkHelper.ip2long(ip));
            int ipLength = binaryIp.length();
            return Integer.valueOf((ipLength > bits ? binaryIp.substring(ipLength - bits) : binaryIp), 2);
        }

        private static long start() {
            String nowYear = LocalDate.now().getYear() + "-01-01 00:00:00";
            return 1000 * LocalDate.parse(nowYear, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                   .atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
        }
    }
}
