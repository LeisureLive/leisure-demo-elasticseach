package com.baijia.uqun.es.demo.controller;

import com.baijia.uqun.core.entity.base.ReturnEntity;
import com.baijia.uqun.es.demo.service.ElasticSearchService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @date: 2020/9/3 4:27 下午
 * @author: Jie.He, hejie@baijiahulian.com
 */
@RequestMapping("/es")
@RestController
public class ElasticSeachController {


    @Resource
    private ElasticSearchService elasticSearchService;

    @GetMapping(value = "/create")
    public ReturnEntity<String> createIndex() {
        return elasticSearchService.createIndex();
    }

    @GetMapping(value = "/syncData")
    public ReturnEntity<String> syncData(
            @RequestParam("start") Integer start,
            @RequestParam("end") Integer end) {
        return elasticSearchService.syncData(start, end);
    }

    @GetMapping(value = "/addData")
    public ReturnEntity<String> addData() {
        return elasticSearchService.addData();
    }

    @GetMapping(value = "/update")
    public ReturnEntity<String> update() {
        return elasticSearchService.update();
    }

    @GetMapping(value = "/search")
    public ReturnEntity<String> searchData() {
        return elasticSearchService.searchData();
    }

    @GetMapping(value = "/del")
    public ReturnEntity<String> del() {
        return elasticSearchService.del();
    }

    @GetMapping(value = "/updateMapping")
    public ReturnEntity<String> updateMapping() {
        return elasticSearchService.updateMapping();
    }
}
