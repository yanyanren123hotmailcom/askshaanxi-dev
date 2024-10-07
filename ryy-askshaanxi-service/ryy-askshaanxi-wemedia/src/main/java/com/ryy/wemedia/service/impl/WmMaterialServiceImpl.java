package com.ryy.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryy.file.service.FileStorageService;
import com.ryy.model.common.dtos.PageResponseResult;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.wemedia.dtos.WmMaterialDto;
import com.ryy.model.wemedia.pojos.WmMaterial;
import com.ryy.utils.thread.WmThreadLocalUtil;
import com.ryy.wemedia.mapper.WmMaterialMapper;
import com.ryy.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial>  implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Override
    public ResponseResult uploadPicture(MultipartFile file){
        String url="";
        try{
            //获取原始文件名
            String originalFilename=file.getOriginalFilename();
            //重新构造上传minio的文件名称，防止文件名重复，被覆盖，aa.jpg--->uuid.jpg
            String filePrefix= UUID.randomUUID().toString().replace("-", "");
            //获取文件后缀
            String postfix=originalFilename.substring(originalFilename.lastIndexOf("."));
            url=fileStorageService.uploadImgFile("",filePrefix+postfix,file.getInputStream());
        }catch (Exception e){
            log.error("上传图片异常",e);
            throw  new RuntimeException("上传图片异常");
        }
        WmMaterial wmMaterial=new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser());
        wmMaterial.setCreatedTime(new Date());
        wmMaterial.setUrl(url);
        wmMaterial.setType((short)0);
        wmMaterial.setIsCollection((short)0);
        wmMaterialMapper.insert(wmMaterial);
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList( WmMaterialDto dto){
        //参数检查
        dto.checkParam();

        IPage page=new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<WmMaterial> wrapper=new LambdaQueryWrapper<>();
        if(dto.getIsCollection()!=null && dto.getIsCollection()==1){
            wrapper.eq(WmMaterial::getIsCollection,dto.getIsCollection());
        }

        wrapper.eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser());

        //按时间倒序
        wrapper.orderByDesc(WmMaterial::getCreatedTime);
        super.page(page,wrapper);
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }
}
