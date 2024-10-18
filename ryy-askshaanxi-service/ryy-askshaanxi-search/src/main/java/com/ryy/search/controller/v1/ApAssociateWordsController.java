package com.ryy.search.controller.v1;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.UserSearchDto;
import com.ryy.search.service.ApAssociateWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/associate")
public class ApAssociateWordsController {

    @Autowired
    private ApAssociateWordsService associateWordsService;
    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto searchDto){
        return associateWordsService.findAssociate(searchDto);
    }
}
