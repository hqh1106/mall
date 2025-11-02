package com.hqh.leafcore.segment;

import com.hqh.leafcore.IDGen;
import com.hqh.leafcore.common.Result;
import com.hqh.leafcore.common.Status;
import com.hqh.leafcore.segment.dao.IDAllocDao;
import com.hqh.leafcore.segment.model.LeafAlloc;
import com.hqh.leafcore.segment.model.Segment;
import com.hqh.leafcore.segment.model.SegmentBuffer;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SegmentIDGenImpl implements IDGen {
    private static final Logger logger = LoggerFactory.getLogger(SegmentIDGenImpl.class);
    //IDCache 初始化失败异常码
    private static final long EXCEPTION_ID_IDCACHE_INIT_FALSE = -1;
    //Key 不存在异常码
    private static final long EXCEPTION_ID_KEY_NOT_EXISTS = -1;
    //两个Segment均未从DB中装载异常码
    private static final long EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = -1;
    //最大步长
    private static final int MAX_STEP = 1000000;
    //一个Segment维持时间
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;

    private ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new UpdateThreadFactory());
    private volatile boolean initOK = false;
    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<String, SegmentBuffer>();
    private IDAllocDao dao;


    /**
     * 自定义线程工厂，设置线程名便于查错
     */
    public static class UpdateThreadFactory implements ThreadFactory {
        //optim: 使用原子操作类，比Synchronized更直观
        private static AtomicInteger threadInitNumber = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(
                    r,
                    "Thread-Segment-Update-" + threadInitNumber.incrementAndGet()
            );
        }
    }

    @Override
    public boolean init() {
        logger.info("IDCache init...");
        //初始化本地缓存后才能进行访问
        updateCacheFromDb();
        initOK = true;
        updateCacheFromDbAtEveryMinute();
        return initOK;
    }

    //单线程延时任务，固定间隔时间刷新
    private void updateCacheFromDbAtEveryMinute() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("check-idCache-thread");
                return thread;
            }
        });
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateCacheFromDb();
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void updateCacheFromDb() {
        logger.info("update cache from db");
        StopWatch sw = new Slf4JStopWatch();
        try {
            List<String> tags = dao.getAllTags();
            if (tags == null || tags.isEmpty()) {
                return;
            }
            List<String> cacheTags = new ArrayList<>(cache.keySet());
            Set<String> insertTagsSet = new HashSet<>(tags);
            Set<String> removeTagsSet = new HashSet<>(cacheTags);
            //db中新添加的tag进行缓存，缓存中已存在的不重复添加，从插入列表中移除
            for (int i = 0; i < cacheTags.size(); i++) {
                String tag = cacheTags.get(i);
                if (insertTagsSet.contains(tag)) {
                    insertTagsSet.remove(tag);
                }
            }
            //将新tag进行缓存
            for (String tag : insertTagsSet) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
                logger.info("Add tag {} from db to IdCache, SegmentBuffer {}", tag, buffer);
            }
            //获取db中被移除的tags
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if (removeTagsSet.contains(tag)) {
                    removeTagsSet.remove(tag);
                }
            }
            //缓存中移除tag
            for (String tag : removeTagsSet) {
                cache.remove(tag);
                logger.info("Remove tag {} from IdCache", tag);
            }
        } catch (Exception e) {
            logger.warn("update cache from db exception", e);
        } finally {
            sw.stop("updateCacheFromDb");
        }
    }


    @Override
    public Result get(final String key) {
        //Cache未初始化成功，直接返回
        if (!initOK) {
            return new Result(EXCEPTION_ID_IDCACHE_INIT_FALSE, Status.EXCEPTION);
        }
        if (cache.containsKey(key)) {
            SegmentBuffer buffer = cache.get(key);
            //optim: 双重检查锁，防止多线程下，重复初始化
            if (!buffer.isInitOk()) {
                synchronized (buffer) {
                    if (!buffer.isInitOk()) {
                        try {
                            updateSegmentFromDb(key, buffer.getCurrent());
                            logger.info("Init buffer. Update leafkey {} {} from db", key, buffer.getCurrent());
                            buffer.setInitOk(true);
                        } catch (Exception e) {
                            logger.warn("Init buffer {} exception", buffer.getCurrent(), e);
                        }
                    }
                }
            }
            return getIdFromSegmentBuffer(cache.get(key));
        }
        return new Result(EXCEPTION_ID_KEY_NOT_EXISTS, Status.EXCEPTION);
    }


    private void updateSegmentFromDb(String key, Segment segment) {
        StopWatch sw = new Slf4JStopWatch();
        SegmentBuffer buffer = segment.getBuffer();
        LeafAlloc leafAlloc;
        //首次初始化：从数据库获取号段，设置步长
        if (!buffer.isInitOk()) {
            leafAlloc = dao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setStep(leafAlloc.getStep());
            buffer.setMinStep(leafAlloc.getStep());
            //首次更新，记录更新时间戳
        } else if (buffer.getUpdateTimestamp() == 0) {
            leafAlloc = dao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(leafAlloc.getStep());
            buffer.setMinStep(leafAlloc.getStep());
        } else {
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION) {
                if (nextStep << 1 > MAX_STEP) {
                    //segment扩容后超过max_step,do nothing
                } else {
                    nextStep = nextStep << 1;
                }
            } else if (duration < SEGMENT_DURATION << 1) {
                //更新时间小于指定时间的两倍，do nothing
            } else {
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
            logger.info("leafKey[{}], step[{}], duration[{}mins], nextStep[{}]", key, buffer.getStep(), String.format("%.2f", ((double) duration / (1000 * 60))), nextStep);
            LeafAlloc tmp = new LeafAlloc();
            tmp.setKey(key);
            tmp.setStep(nextStep);
            leafAlloc = dao.updateMaxIdByCustomStepAndGetLeafAlloc(tmp);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(leafAlloc.getStep());
        }
        long value = leafAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value);
        segment.setMax(leafAlloc.getMaxId());
        segment.setStep(buffer.getStep());
        sw.stop("updateSegmentFromDb", key + " " + segment);
    }

    private Result getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep())
                && buffer.getThreadRunning().compareAndSet(false,true)){
                    //满足条件，进行下一号段预加载
                    service.execute(new Runnable() {
                        @Override
                        public void run() {
                            Segment next = buffer.getSegments()[buffer.nextPos()];
                            boolean updateOk = false;
                            try{
                                updateSegmentFromDb(buffer.getKey(),next);
                                updateOk =true;
                                logger.info("update segment {} from db {}", buffer.getKey(), next);
                            }catch (Exception e) {
                                logger.warn(buffer.getKey() + " updateSegmentFromDb exception", e);
                            } finally {
                                if (updateOk){
                                    buffer.wLock().lock();
                                    buffer.setNextReady(true);
                                    buffer.getThreadRunning().set(false);
                                    buffer.rLock().unlock();
                                }else {
                                    buffer.getThreadRunning().set(false);
                                }
                            }
                        }
                    });
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()){
                    return new Result(value,Status.SUCCESS);
                }
            } finally {
                buffer.rLock().unlock();
            }
            //执行到此处表示，表示当前号段用尽
            waitAndSleep(buffer);
            buffer.wLock().lock();
            try{
                //再次请求，等待期间可能完成号段切换
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()){
                    return new Result(value,Status.SUCCESS);
                }
                //号段切换
                if (buffer.isNextReady()){
                    buffer.switchPos();
                    buffer.setNextReady(false);
                }else {
                    logger.error("Both two segments in {} are not ready!", buffer);
                    return new Result(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL, Status.EXCEPTION);
                }
            }finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()){
            roll +=1;
            if (roll > 10000){
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("Thread {} Interrupted",Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    public List<LeafAlloc> getAllLeafAllocs() {
        return dao.getAllLeafAllocs();
    }

    public Map<String, SegmentBuffer> getCache() {
        return cache;
    }

    public IDAllocDao getDao() {
        return dao;
    }

    public void setDao(IDAllocDao dao) {
        this.dao = dao;
    }
}