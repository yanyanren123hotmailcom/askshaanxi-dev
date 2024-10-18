package com.ryy.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.UserSearchDto;
import com.ryy.search.service.ApUserSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Override
    public ResponseResult search(UserSearchDto dto) {
        try{

            //构造请求
            SearchRequest request=new SearchRequest("app_info_article");
            //构造sourceBuilder
            SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
            //构造查询条件
            BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
            //如果搜索关键词为空,则查询全部
            if(StringUtils.isEmpty(dto.getSearchWords())){
                boolQueryBuilder.must(QueryBuilders.matchAllQuery());
            }else{
                //对搜索关键词进行分词查询QueryBuilders.queryStringQuery,只查询titile列
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(dto.getSearchWords()).field("title"));
            }
            if(dto.getMinBehotTime()!=null){
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("publishTime").lt(dto.getMinBehotTime()));
            }
            //构造标亮条件
            HighlightBuilder highlightBuilder=new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<font style='color:red;font-size:inherit'>");
            highlightBuilder.preTags("</font>");
            sourceBuilder.highlighter(highlightBuilder);
            sourceBuilder.query(boolQueryBuilder);
            //设置条数
            if(dto.getPageSize()>0){
                sourceBuilder.from(0).size(dto.getPageSize());
            }

            request.source(sourceBuilder);

            //查询
            SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);

            //解析结果
            SearchHit[] hits = response.getHits().getHits();
            List<Map> resultList=new ArrayList<>();
            for (SearchHit hit : hits) {
                Map map = JSON.parseObject(hit.getSourceAsString(), Map.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if(!CollectionUtils.isEmpty(highlightFields)){
                    map.put("h_title",map.get("title"));
                }else{
                    HighlightField highLightTitle = highlightFields.get("title");
                    Text[] fragments = highLightTitle.getFragments();
                    map.put("h_title", org.apache.commons.lang3.StringUtils.join(fragments));
                }
                resultList.add(map);
            }
            return ResponseResult.okResult(resultList);
        }catch (Exception e){
            log.error("查询失败: {}",e);
        }
        return ResponseResult.okResult(null);
    }
}
