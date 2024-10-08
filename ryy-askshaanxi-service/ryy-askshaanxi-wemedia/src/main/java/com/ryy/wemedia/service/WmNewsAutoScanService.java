package com.ryy.wemedia.service;

import com.ryy.model.wemedia.pojos.WmNews;

import java.util.List;

public interface WmNewsAutoScanService {

    /**
     * 自媒体文章审核
     * @param news  自媒体文章信息
     */
    public void autoScanWmNews(WmNews news, List<String> images);
}
