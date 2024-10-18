package com.ryy.search.controller.v1;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.search.service.ArticleSearchInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/article/search")
public class ArticleSearchInitController {
    @Autowired
    private ArticleSearchInitService service;
    @GetMapping("/init")
    public ResponseResult search() throws IOException {
        return service.init();
    }
}
