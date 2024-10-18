package com.ryy.search.service.impl;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import com.ryy.model.search.dtos.UserSearchDto;
import com.ryy.search.pojos.ApAssociateWords;
import com.ryy.search.service.ApAssociateWordsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApAssociateWordsServiceImpl implements ApAssociateWordsService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public ResponseResult findAssociate(UserSearchDto userSearchDto) {
        //检查参数
        if(StringUtils.isBlank(userSearchDto.getSearchWords())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //分页查询
        int pageSize=userSearchDto.getPageSize();
        if(pageSize>20){
            pageSize=20;
        }
        //模糊查询
        List<ApAssociateWords> associateWords=mongoTemplate.find(Query.query(Criteria.where("associateWords")
                .regex("^.*"+userSearchDto.getSearchWords()+".*$")).limit(pageSize), ApAssociateWords.class);
        return ResponseResult.okResult(associateWords);
    }
}
