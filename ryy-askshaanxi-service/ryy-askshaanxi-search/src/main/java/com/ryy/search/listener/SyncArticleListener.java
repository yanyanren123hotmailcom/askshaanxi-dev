package com.ryy.search.listener;


import com.alibaba.fastjson.JSON;
import com.ryy.common.constants.ArticleConstants;
import com.ryy.model.search.vo.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient highLevelClient;

    @KafkaListener(topics = ArticleConstants.TOPIC_SYNC_ES)
    public void syncArticle(String message) {
        log.info("收到文章同步消息，正在写入es------");
        try{
            IndexRequest indexRequest=new IndexRequest("app_info_article");

            SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);
            indexRequest.id(searchArticleVo.getId()+"").source(searchArticleVo, XContentType.class);
            highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }catch (Exception e){
            log.error("文章同步失败：写入es失败-----");
            //TODO 写数据库，同步错误表
        }

    }
}
