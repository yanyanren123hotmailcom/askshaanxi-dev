package com.ryy.search.service.impl;


import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.HistorySearchDto;
import com.ryy.search.pojos.ApUserSearch;
import com.ryy.search.service.ApUserSearchRecordService;
import com.ryy.utils.thread.WmThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApUserSearchRecordServiceImpl implements ApUserSearchRecordService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    @Async("asyncPoolToSearchRecord")
    public void saveRecord(String keyword) {
        Integer userId = WmThreadLocalUtil.getUser();
        if(userId==null){
            log.info("用户未登录，不进行记录");
            return;
        }
        Query query = Query.query(Criteria.where("keyword").is(keyword).and("userId").is(userId));
        ApUserSearch one = mongoTemplate.findOne(query, ApUserSearch.class);
        if(one!=null){
            log.info("该搜索词已存在，进行更新------");
            one.setCreateTime(new Date());
            mongoTemplate.save(one);
        }else{
            log.info("新建搜索词记录------");
            ApUserSearch apUserSearch=new ApUserSearch();
            apUserSearch.setUserId(userId);
            apUserSearch.setKeyword(keyword);
            apUserSearch.setCreateTime(new Date());
            //获取useId历史搜索记录数量
            Query qu = Query.query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,"createTime"));
            List<ApUserSearch> apUserSearches = mongoTemplate.find(qu, ApUserSearch.class);
            if(apUserSearches==null||apUserSearches.size()<10){
                //此用户搜索记录为空或者数量小于10
                log.info("此用户搜索记录为空或者数量小于10,直接插入");
                mongoTemplate.save(apUserSearch);
            }else{
                ApUserSearch toRemoveRecord = apUserSearches.get(apUserSearches.size() - 1);
                mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(toRemoveRecord.getId())),apUserSearch);
            }
        }
    }

    @Override
    public ResponseResult searchRecord() {
        Integer userId=WmThreadLocalUtil.getUser();
        if(userId==null){
            return ResponseResult.okResult(null);
        }
        Query query = Query.query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,"createTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query, ApUserSearch.class);
        return ResponseResult.okResult(apUserSearches);
    }

    @Override
    public ResponseResult delUserSearch(HistorySearchDto historySearchDto) {
        mongoTemplate.remove(Query.query(Criteria.where("id").is(historySearchDto.getId())),ApUserSearch.class);
        return ResponseResult.okResult(null);
    }
}
