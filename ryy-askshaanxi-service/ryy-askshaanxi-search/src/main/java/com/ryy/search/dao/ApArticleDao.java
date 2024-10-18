package com.ryy.search.dao;


import com.ryy.model.search.vo.SearchArticleVo;

import java.util.List;

public interface ApArticleDao {


    /**
     * 查询所有的频道列表
     * @return
     */
    List<SearchArticleVo> loadArticleList();

}