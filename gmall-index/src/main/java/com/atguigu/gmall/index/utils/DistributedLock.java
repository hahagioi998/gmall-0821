package com.atguigu.gmall.index.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Slf4j
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Timer timer;

    //hash结构：Map<lockName, Map<uuid, state>>
    public Boolean tryLock(String lockName, String uuid, Integer expire){
        String script = "if(redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                "   redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                "   redis.call('expire', KEYS[1], ARGV[2]) " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";
        if(!redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString())){
            try {
                Thread.sleep(50);
                tryLock(lockName,uuid,expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //开启定时任务看门狗线程，实现自动续期
        renewExpire(lockName,uuid,expire);
        return true;
    }

    public void unlock(String lockName, String uuid){
        //lua脚本如果返回值为nil代表要解的锁不存在，或者要解的是别人的锁
        //返回值为0代表出来了一次，state-1
        //返回值为1代表解锁成功，state=0
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) then " +
                "   return nil " +
                "elseif(redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0) then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
        //此处返回值类型不要使用布尔，因为nil在布尔中会被当成false
        Long flag = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        if(flag == null){
            log.error("要解的锁不存在，或者在尝试解别人的锁，锁的名称：{}，锁的uuid：{}", lockName, uuid);
        }else if(flag ==1){
            //解锁成功，取消定时任务
            timer.cancel();
        }
    }

    //定时任务，实现自动续期的看门狗线程/守护线程逻辑
    private void renewExpire(String lockName, String uuid, Integer expire){

        //仍然要使用lua脚本保证判断是否锁是自己的+自动续期定时任务开启的原子性
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                "   return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                "   return 0 " +
                "end";

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString());
            }
        }, expire * 1000 / 3, expire * 1000 / 3);
    }

}
