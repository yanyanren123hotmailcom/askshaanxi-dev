package com.ryy.apis.article.fallback;

import com.ryy.apis.article.IArticleClient;
import com.ryy.model.article.dtos.ArticleDto;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class IArticleClientFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败1");
    }
}
