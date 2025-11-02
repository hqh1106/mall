package com.hqh.mall;

/**雪花算法
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位标志，默认0
 * 41位时间戳(毫秒级)，存储的是时间戳差值（当前时间戳-开始时间戳），开始时间戳一般是我们的id生成器开始使用的时间，由程序指定
 * 41位的时间戳，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * 10位数据及其位置，可以部署1024个节点，包含5位datacenterId和5位workerId
 * 12为序列，毫秒内的计数，12位支持每个节点每个毫秒产生4096个
 * 一共64位，long
 */
public class SnowFlakeIdWorker {
    /**
     * 基础时间，用于计算差值
     */
    private final long baseEpoch = 1762097531000L;
    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;
    /**
     * 数据中心id所占的位数
     */
    private final long datacenterIdBits = 5L;
    /**
     * 支持最大的机器id 31
     */

    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /**
     * 支持最大的数据中心id 31
     */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /**
     * 序列id所占的位数
     */
    private final long sequenceBits = 12L;
    /**
     * workeId左移位数
     */
    private final long workerIdShift = sequenceBits;
    /**
     * datacenterId左移位数
     */
    private final long datacenterIdShift = sequenceBits+workerIdBits;
    /**
     * 时间戳左移位数
     */
    private final long timestampleftShift = sequenceBits + workerIdBits +datacenterIdBits;

    /**
     * 生成序列的掩码，这里为4095，对应二进制 1111 1111 1111
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    /**
     * 工作机器id 0-31
     */
    private long workerId;
    /**
     * 数据中心id 0-31
     */
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    public SnowFlakeIdWorker(long workerId, long datacenterId){
        if (workerId> maxWorkerId || workerId<0){
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    public synchronized long nextId(){
        long timestamp = System.currentTimeMillis();
        //出现时钟回退，抛异常
        if (timestamp < lastTimestamp){
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (timestamp == lastTimestamp){
            sequence =(sequence + 1) & sequenceMask;
            if (sequence==0){
                timestamp = tilNextMillis(lastTimestamp);
            }
        }else {
            //时间戳改变，序列归0
            sequence=0L;
        }
        lastTimestamp = timestamp;
        long relativeTimestamp = timestamp - baseEpoch;
        return (relativeTimestamp << timestampleftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;

    }

    /**
     * 阻塞到下一毫秒
     * @param lastTimestamp 上次生成时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        SnowFlakeIdWorker idWorker = new SnowFlakeIdWorker(1,1);
        for (int i = 0; i < 10; i++) {
            long l = idWorker.nextId();
            System.out.println(l);
        }
    }
}
