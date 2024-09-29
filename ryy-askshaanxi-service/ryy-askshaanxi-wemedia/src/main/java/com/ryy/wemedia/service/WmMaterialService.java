package com.ryy.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.wemedia.dtos.WmMaterialDto;
import com.ryy.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {

    /**
     * 图片上传
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 素材列表查询
     * @param dto
     * @return
     */
    public ResponseResult findList( WmMaterialDto dto);


}