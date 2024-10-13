package com.ryy.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ryy.apis.article.IArticleClient;
import com.ryy.apis.user.IUserClient;
import com.ryy.common.aliyun.GreenImageScan;
import com.ryy.common.aliyun.GreenTextScan;
import com.ryy.file.service.FileStorageService;
import com.ryy.model.wemedia.pojos.WmNews;
import com.ryy.model.wemedia.pojos.WmSensitive;
import com.ryy.utils.common.SensitiveWordUtil;
import com.ryy.wemedia.mapper.WmChannelMapper;
import com.ryy.wemedia.mapper.WmNewsMapper;
import com.ryy.wemedia.mapper.WmSensitiveMapper;
import com.ryy.wemedia.service.WmNewsAutoScanService;
import com.ryy.wemedia.service.WmTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private WmChannelMapper channelMapper;

    @Autowired
    private GreenTextScan textScan;

    @Autowired
    private GreenImageScan imageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private IArticleClient iArticleClient;

    @Autowired
    private IUserClient iUserClient;
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    @Autowired
    private WmTaskService wmTaskService;

    @Override
    @Async("asyncPool")//此方法异步执行
    public void autoScanWmNews(WmNews news, List<String> contentImages,String textInImages) {
        //审核内容
//        boolean flag = scanText(news,textInImages);
//        if(!flag){
//            log.info("文本审核失败：内容违规或需人工审核");
//            return;
//        }
//
//        //审核图片
//        flag = scanImages(news,contentImages);
//        if(!flag){
//            log.info("图片审核失败：内容违规或需人工审核");
//            return;
//        }
        log.info("文章审核完成，提交任务-----");
        if(news.getPublishTime()!=null){
            log.info("延迟发布，提交任务");
            wmTaskService.addTask(news);
        }else{
            //同步文章到app端article
            wmTaskService.saveToApp(news);
        }
    }

    private boolean scanText(WmNews news,String textInImages){
        boolean flag=true;
        try{
            //提取文本内容
            String content=getTextFromContent(news)+textInImages;

            //自管理的敏感词过滤
            boolean isSensitive = handleSensitiveScan(content,news);
            if (!isSensitive) {
                return flag;
            }

            Map map = textScan.greeTextScan(content);
            if(map!=null) {
                String suggestion = map.get("suggestion") + "";
                String reson=map.get("label")+"";
                if ("block".equals(suggestion)) {
                    //审核失败，修改文章状态
                    updateWmNews(news, WmNews.Status.FAIL.getCode(), reson);
                    flag=false;
                } else if ("review".equals(suggestion)) {
                    //人工审核
                    updateWmNews(news, WmNews.Status.ADMIN_AUTH.getCode(), "当前文章需要人工审核");
                    flag=false;
                }
            }
        }catch (Exception e){
            log.error("审核失败，调用接口出错");
            //TODO 转人工
            updateWmNews(news, WmNews.Status.ADMIN_AUTH.getCode(), "当前文章需要人工审核");
            flag=false;
        }
        return flag;
    }
    private boolean handleSensitiveScan(String content, WmNews wmNews) {

        boolean flag = true;

        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if (map.size() > 0) {
            updateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "当前文章中存在违规内容" + map);
            flag = false;
        }

        return flag;
    }
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }
    private String getTextFromContent(WmNews news) {
        StringBuilder text=new StringBuilder();
        if(StringUtils.isEmpty(news.getContent())){
            return "";
        }
        List<Map> contentList = JSONArray.parseArray(news.getContent(), Map.class);
        for(Map map:contentList){
            String type=map.get("type")+"";
            if("text".equals(type)){
                text.append("-");
                text.append(map.get("value"));
            }
        }
        //添加标题
        text.append("-"+news.getTitle());
        return text.toString();
    }

    private boolean scanImages(WmNews news,List<String> contentImages){
        boolean flag=true;
        List<String> allImages=new ArrayList<>();
        if(!CollectionUtils.isEmpty(contentImages)){
            allImages.addAll(contentImages);
        }
        //获取封面图片
        String images=news.getImages();
        if(!StringUtils.isEmpty(images)){
            String[] coverImages=StringUtils.commaDelimitedListToStringArray(images);
            allImages.addAll(Arrays.asList(coverImages));
        }

        //图片去重
        List<String> collect = allImages.stream().distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(collect)){
            return  flag;
        }

        for (String imageUrl : collect) {
            byte[] image = fileStorageService.downLoadFile(imageUrl);
            try{
                Map map = imageScan.imageScan(Arrays.asList(image));
                if(map==null){
                    //人工审核
                    news.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
                    wmNewsMapper.updateById(news);
                    flag=false;
                    return flag;
                }
                String suggestion = map.get("suggestion") + "";
                String reson=map.get("label")+"";
                if ("block".equals(suggestion)) {
                    //审核失败，修改文章状态
                    news.setReason(reson);
                    news.setStatus(WmNews.Status.FAIL.getCode());
                    wmNewsMapper.updateById(news);
                    flag=false;
                } else if ("review".equals(suggestion)) {
                    //人工审核
                    news.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
                    wmNewsMapper.updateById(news);
                    flag=false;
                }
            }catch (Exception e){
                log.error("审核失败，调用接口出错");
                //TODO 转人工
                news.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
                wmNewsMapper.updateById(news);
                flag=false;
            }
        }
        return flag;
    }

}
