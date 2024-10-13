package com.ryy.wemedia.service;

import com.ryy.model.wemedia.pojos.WmNews;

public interface WmTaskService {

    Long addTask(WmNews wmNews);
    void saveToApp(WmNews news);
}
