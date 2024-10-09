package com.ryy.apis.article;

import com.ryy.apis.article.fallback.IArticleClientFallback;
import com.ryy.model.article.dtos.ArticleDto;
import com.ryy.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "askshaanxi-article",fallback = IArticleClientFallback.class)
public interface IArticleClient {

    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto);


}
