package com.ryy.schedule;

import com.ryy.common.redis.CacheService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private CacheService cacheService;

    public void testZSet(){
        cacheService.zAdd("zKey","001",80);//相当于stringRedisTemplate.opsForZSet().add(key, value, score);
        cacheService.zAdd("zKey","002",60);
        cacheService.zAdd("zKey","003",50);
        cacheService.zAdd("zKey","004",70);


        //相当于stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
        Set<String> key=cacheService.zRangeByScore("zKey",50,70);//查找分数>=50 && <=70的分数的value

        //删除
        //相当于stringRedisTemplate.opsForZSet().remove(key, values);
        cacheService.zRemove("zKey","002");
    }
}
