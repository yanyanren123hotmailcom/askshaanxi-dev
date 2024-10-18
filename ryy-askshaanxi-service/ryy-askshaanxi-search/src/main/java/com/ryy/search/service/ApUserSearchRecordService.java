package com.ryy.search.service;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.HistorySearchDto;

public interface ApUserSearchRecordService {

    void saveRecord(String keyword);
    ResponseResult searchRecord();

    ResponseResult delUserSearch(HistorySearchDto historySearchDto);
}
