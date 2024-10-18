package com.ryy.search.controller.v1;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.HistorySearchDto;
import com.ryy.search.service.ApUserSearchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vi/history")
public class ApUserSearchController {

    @Autowired
    private ApUserSearchRecordService recordService;

    @PostMapping("/load")
    public ResponseResult findUserSearch(){
        return recordService.searchRecord();
    }

    @PostMapping("/del")
    public ResponseResult deleteRecord(@RequestBody HistorySearchDto historySearchDto){
        return recordService.delUserSearch(historySearchDto);
    }
}
