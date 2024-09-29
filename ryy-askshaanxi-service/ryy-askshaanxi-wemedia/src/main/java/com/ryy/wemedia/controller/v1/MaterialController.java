package com.ryy.wemedia.controller.v1;


import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class MaterialController {
    @Autowired
    private WmMaterialService vmMaterialService;

    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return vmMaterialService.uploadPicture(multipartFile);
    }
}
