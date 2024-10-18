package com.ryy.search.controller.v1;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.UserSearchDto;
import com.ryy.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/article/search")
public class ArticleSearchController {
    @Autowired
    private ApUserSearchService searchService;

    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto dto) throws IOException{
        return searchService.search(dto);
    }
}
