package com.ryy.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryy.apis.article.IArticleClient;
import com.ryy.apis.schedule.IScheduleClient;
import com.ryy.apis.user.IUserClient;
import com.ryy.model.article.dtos.ArticleDto;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import com.ryy.model.schedule.dtos.Task;
import com.ryy.model.user.pojos.ApUserDetails;
import com.ryy.model.wemedia.pojos.WmChannel;
import com.ryy.model.wemedia.pojos.WmNews;
import com.ryy.wemedia.mapper.WmChannelMapper;
import com.ryy.wemedia.mapper.WmNewsMapper;
import com.ryy.wemedia.service.WmTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@Slf4j
public class WmTaskServiceImpl implements WmTaskService {
    @Autowired
    private IScheduleClient scheduleClient;

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Autowired
    private IArticleClient iArticleClient;

    @Autowired
    private IUserClient iUserClient;
    @Autowired
    private WmChannelMapper channelMapper;


    @Override
    public Long addTask(WmNews wmNews) {
        Task task = new Task();
        task.setTaskType(200);
        task.setPriority(1);
        task.setExecuteTime(wmNews.getPublishTime());

        //减少数据传输量
        WmNews param=new WmNews();
        param.setId(wmNews.getId());
        task.setParameters(JSON.toJSONString(param));
        ResponseResult result= scheduleClient.addTask(task);
        log.info("添加任务返回：{}", JSON.toJSONString(result));
        if(result.getCode()== AppHttpCodeEnum.SUCCESS.getCode()){
            return (Long) result.getData();
        }
        return null;
    }

    @KafkaListener(topics = "TASK_EXEC")
    public void taskExec(String message){
        log.info("收到任务执行消息：{}",message);

       Task task = JSON.parseObject(message,Task.class);
       WmNews wmNews=JSON.parseObject(task.getParameters(),WmNews.class);
        WmNews news=wmNewsMapper.selectById(wmNews.getId());
        saveToApp(news);
    }

    @Override
    public void saveToApp(WmNews news){
        ArticleDto articleDto=new ArticleDto();
        BeanUtils.copyProperties(news,articleDto);
        articleDto.setLayout(news.getType());
        articleDto.setAuthorId(news.getUserId());
        //ApUserDetails apUserDetails=(ApUserDetails) iUserClient.getById(news.getUserId()).getData();
        ObjectMapper mapper = new ObjectMapper();
        ApUserDetails apUserDetails = mapper.convertValue(iUserClient.getById(news.getUserId()).getData(), ApUserDetails.class);
        articleDto.setAuthorName(apUserDetails==null?null:apUserDetails.getName());
        articleDto.setPublishTime(new Date());
        articleDto.setCreatedTime(new Date());
        articleDto.setPowerId(news.getChannelId());
        WmChannel wmChannel = channelMapper.selectById(news.getChannelId());
        articleDto.setPowerName(wmChannel.getName());
        articleDto.setIsAnswered(false);

        //设置文章id，修改需要
        articleDto.setId(news.getArticleId());

        ResponseResult result=iArticleClient.saveArticle(articleDto);
        log.info("调用app端保存文章返回：{}", JSON.toJSONString(result));
        //同步成功
        if(result.getCode()== AppHttpCodeEnum.SUCCESS.getCode()){
            //更新自媒体数据的文章id与状态
            news.setStatus(WmNews.Status.PUBLISHED.getCode());
            news.setArticleId((Long)result.getData());
            wmNewsMapper.updateById(news);
        }
        log.info("文章审核完成:----------------{}",news.getTitle());
    }
}
