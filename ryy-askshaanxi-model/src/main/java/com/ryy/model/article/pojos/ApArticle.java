package com.ryy.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文章信息表，存储已发布的文章
 * </p>
 *
 * @author itheima
 */

@Data
@TableName("ap_article")
public class ApArticle implements Serializable {

    @TableId(value = "id",type = IdType.ID_WORKER)
    private Long id;


    /**
     * 标题
     */
    private String title;

    /**
     * 作者id
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 作者名称
     */
    @TableField("author_name")
    private String authorName;

    /**
     * 频道id
     */
    @TableField("power_id")
    private Integer powerId;

    /**
     * 频道名称
     */
    @TableField("power_name")
    private String powerName;

    /**
     * 文章布局  0 无图文章   1 单图文章    2 多图文章
     */
    private Short layout;

    /**
     * 文章标记  0 需人工审核   1 正常发布   2 发布失败   3 未提交
     */
    private Byte flag;
    /**
     * 文章状态  0 草稿   1 提交
     */
    private Byte type;
    /**
     * 文章状态  0 未解决   1 已解决
     */
    @TableField("is_answered")
    private Boolean isAnswered;

    /**
     * 文章封面图片 多张逗号分隔
     */
    private String images;

    /**
     * 标签
     */
    private String labels;

    /**
     * 投票数量
     */
    private Integer votes;

    /**
     * 评论数量
     */
    private Integer comments;

    /**
     * 阅读数量
     */
    private Integer views;


    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private Date publishTime;

    /**
     * 同步状态
     */
    @TableField("sync_status")
    private Boolean syncStatus;

    /**
     * 静态页面地址
     */
    @TableField("static_url")
    private String staticUrl;
}