package com.ryy.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ryy.common.redis.CacheService;
import com.ryy.model.schedule.dtos.Task;
import com.ryy.model.schedule.pojos.Taskinfo;
import com.ryy.model.schedule.pojos.TaskinfoLogs;
import com.ryy.schedule.mapper.TaskinfoLogsMapper;
import com.ryy.schedule.mapper.TaskinfoMapper;
import com.ryy.schedule.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
public class ITaskServiceImpl implements ITaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private CacheService cacheService;
    private  final static String KEY_PREFIX="FUTURE-";

    @Autowired
    private ITaskServiceImpl iTaskService;

    @Override
    public Long addTask(Task task){

        //写数据库
        Taskinfo taskinfo=new Taskinfo();
        BeanUtils.copyProperties(task,taskinfo);
        taskinfoMapper.insert(taskinfo);

        task.setTaskId(taskinfo.getTaskId());

        TaskinfoLogs taskinfoLogs=new TaskinfoLogs();
        BeanUtils.copyProperties(taskinfo,taskinfoLogs);
        taskinfoLogs.setStatus(TaskinfoLogs.SCHEDULED);
        taskinfoLogs.setVersion(1);
        taskinfoLogsMapper.insert(taskinfoLogs);

        //判断执行时间，如果小于等于当前执行，立即执行（mq），如果小于等于预设时间，写zSet
        //预设时间
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long nextTime = calendar.getTime().getTime();
        if(task.getExecuteTime().getTime()<=System.currentTimeMillis()){
            iTaskService.sendMessage(task);
        } else if (task.getExecuteTime().getTime()<=nextTime) {
            String key=buildKey(task);
            //写入到redis中
            cacheService.zAdd(key, JSON.toJSONString(task),task.getExecuteTime().getTime());
        }
        return task.getTaskId();

    }
    
    @Scheduled(cron="0 0/5 * * * ?")
    public void refreshToZSet(){
        log.info("开始执行从数据库读取最近5分钟要执行的数据到zSet");
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE,5);
        
        LambdaQueryWrapper<Taskinfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Taskinfo::getExecuteTime,instance.getTime());
        wrapper.ge(Taskinfo::getExecuteTime,Calendar.getInstance().getTime());
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(wrapper);

        if(CollectionUtils.isEmpty(taskinfos)){
            log.info("没有最近5分钟要执行的任务记录");
            return;
        }
        log.info("从数据库获取到最近5分钟要执行的任务");
        for (Taskinfo taskinfo : taskinfos) {
            Task task=new Task();
            BeanUtils.copyProperties(taskinfo,task);
            String key=buildKey(task);
            cacheService.zAdd(key,JSON.toJSONString(task),task.getExecuteTime().getTime());
        }
    }
    @Scheduled(cron = "0/30 * * * * ?")
    public void checkZSet(){
        log.info("开始执行检查zSet任务是否需要执行---------------------");

        //获取到分布式锁
        String token= cacheService.tryLock("checkSet",20);
        if(token==null){
            log.info("未获取到锁，不执行");
            return ;
        }

        //获取到所有keys
        Set<String> futureKeys=cacheService.scan(KEY_PREFIX+"*");
        if(CollectionUtils.isEmpty(futureKeys)){
            log.info("没有任务队列");
        }
        //keys遍历
        for (String futureKey : futureKeys) {
            //找到key对应的所有score范围内的value
            Set<String> tasks=cacheService.zRangeByScore(futureKey,0,System.currentTimeMillis());
            if(CollectionUtils.isEmpty(tasks)){
                log.info("当前key：{}没有需要执行的任务",futureKey);
                continue;
            }

            log.info("需要执行的任务：{}",tasks);
            for (String task : tasks) {
                iTaskService.sendMessage(JSON.parseObject(task,Task.class));
            }

        }
        log.info("检查zSet任务结束--------------");
    }

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    //发送消息：通知任务时间到
    //利用kafka


    @Transactional
    public void sendMessage(Task task){
        log.info("发送需要执行的任务消息");

        kafkaTemplate.send("TASK_EXEC",JSON.toJSONString(task));

        //更新数据库，删除zSet里面执行的数据
        taskinfoMapper.deleteById(task.getTaskId());

        TaskinfoLogs taskinfoLogs=taskinfoLogsMapper.selectById(task.getTaskId());
        taskinfoLogs.setStatus(TaskinfoLogs.EXECUTED);
        taskinfoLogsMapper.updateById(taskinfoLogs);

        String key=buildKey(task);
        cacheService.zRemove(key,JSON.toJSONString(task));
    }

    private String buildKey(Task task){
        String key=KEY_PREFIX+task.getTaskType()+":"+task.getPriority();
        return key;
    }
}
