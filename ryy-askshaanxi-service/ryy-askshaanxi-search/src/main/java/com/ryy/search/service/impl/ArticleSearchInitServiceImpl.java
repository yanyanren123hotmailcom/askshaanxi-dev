package com.ryy.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.vo.SearchArticleVo;
import com.ryy.search.dao.ApArticleDao;
import com.ryy.search.service.ArticleSearchInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;


@Service
@Slf4j
public class ArticleSearchInitServiceImpl implements ArticleSearchInitService {
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Resource
    private ApArticleDao apArticleDao;
    @Override
    public ResponseResult init(){
        //注意不能一次性load，如果数据量过多的话会出来内存溢出OOM，需要分页查询
        //List<SearchArticleVo> articleVoList=apArticleMapper.loadArticleList();
        RestHighLevelClient highLevelClient=new RestHighLevelClient(RestClient.builder(
                new HttpHost("192.168.200.130",9200,"http")));
        try{
            List<SearchArticleVo> articleVoList=apArticleDao.loadArticleList();
            if(CollectionUtils.isEmpty(articleVoList)){
                log.info("数据为空");
                return ResponseResult.okResult(null);
            }
            BulkRequest bulkRequest=new BulkRequest();

            for (SearchArticleVo articleVo : articleVoList) {
                IndexRequest indexRequest=new IndexRequest("app_info_article").id(articleVo.getId()+"")
                        .source(JSON.toJSONString(articleVo), XContentType.JSON);
                bulkRequest.add(indexRequest);
            }
            log.info(highLevelClient.toString());
            BulkResponse bulkResponse = highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                // Handle failures
                log.info("Bulk operation has failures有错误:{}",bulkResponse.buildFailureMessage());
            } else {
                log.info("Bulk operation completed successfully成功");
            }
            return ResponseResult.okResult("app_info_article");
        }catch (Exception e){
            log.error("初始化出现问题:",e);
        }
        return ResponseResult.okResult(null);
    }
}
