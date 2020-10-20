package com.baijia.uqun.es.demo.service;

import com.baijia.uqun.core.entity.base.ReturnEntity;

/**
 * @date: 2020/9/3 4:33 下午
 * @author: Jie.He, hejie@baijiahulian.com
 */
public interface ElasticSearchService {

    ReturnEntity<String> createIndex();

    ReturnEntity<String> syncData(Integer start, Integer end);

    ReturnEntity<String> addData();

    ReturnEntity<String> update();

    ReturnEntity<String> searchData();

    ReturnEntity<String> del();

    ReturnEntity<String> updateMapping();

}
