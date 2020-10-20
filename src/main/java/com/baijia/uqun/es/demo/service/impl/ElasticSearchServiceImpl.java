package com.baijia.uqun.es.demo.service.impl;

import com.baijia.uqun.core.entity.base.ReturnEntity;
import com.baijia.uqun.es.demo.dao.mapper.InviteCardTaskInfoPOMapper;
import com.baijia.uqun.es.demo.dao.po.InviteCardTaskInfoPO;
import com.baijia.uqun.es.demo.service.ElasticSearchService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

/**
 * @date: 2020/9/3 4:32 下午
 * @author: Jie.He, hejie@baijiahulian.com
 */
@Slf4j
@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private static final String INDEX_NAME = "user_id_mapping";

    private static ExecutorService executor = new ThreadPoolExecutor(10, 30, 200L,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(),
            new ThreadFactoryBuilder().setNameFormat("calLabel-task-executor").build());

    @Resource(name = "highLevelClient")
    private RestHighLevelClient client;

    @Resource
    private InviteCardTaskInfoPOMapper inviteCardTaskInfoPOMapper;

    @Override
    public ReturnEntity<String> createIndex() {
        // 配置setting，分区数、副本数、缓存刷新时间.
        Map<String, Object> setting = new HashMap<>();
        setting.put("number_of_shards", 5);
        setting.put("number_of_replicas", 1);
        setting.put("refresh_interval", "5s");

        // 定义类型.
        Map<String, Object> keyword = new HashMap<>();
        keyword.put("type", "keyword");
        keyword.put("ignore_above", 128);
        Map<String, Object> lon = new HashMap<>();
        lon.put("type", "long");
        Map<String, Object> secondAccuracyDate = new HashMap<>();
        secondAccuracyDate.put("type", "date");
        secondAccuracyDate.put("format", "yyyy-MM-dd HH:mm:ss");

        Map<String, Object> dayAccuracyDate = new HashMap<>();
        dayAccuracyDate.put("type", "date");
        dayAccuracyDate.put("format", "yyyy-MM-dd");

        Map<String, Object> text = new HashMap<>();
        text.put("type", "text");

        // 设置mapping.
        Map<String, Object> properties = new HashMap<>();
//        properties.put("id", lon);
//        properties.put("account_id", lon);
//        properties.put("team_id", lon);
//        properties.put("data_date", dayAccuracyDate);
//        properties.put("task_id", lon);
//        properties.put("channel_id", lon);
//        properties.put("app_id", keyword);
//        properties.put("launch_num", lon);
//        properties.put("help_num", lon);
//        properties.put("un_follow_num", lon);
//        properties.put("invite_suc", lon);
//        properties.put("h5_num", lon);
//        properties.put("poster_num", lon);
//        properties.put("share_help_num", lon);
//        properties.put("create_time", secondAccuracyDate);
//        properties.put("update_time", secondAccuracyDate);
        properties.put("wechat_id", keyword);
        properties.put("open_id", keyword);
        properties.put("app_id", keyword);
        properties.put("union_id", keyword);
        properties.put("source", keyword);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);

        // 检查索引是否存在，存在就不创建了.
        GetIndexRequest getRequest = new GetIndexRequest(INDEX_NAME);
        getRequest.local(false);
        getRequest.humanReadable(true);
        try {
            boolean exists = client.indices().exists(getRequest, RequestOptions.DEFAULT);
            if (exists) {
                log.info("索引库已经存在, indexName: {}", INDEX_NAME);
                return ReturnEntity.buildErrorResponse("索引库已经存在");
            }
        } catch (Exception e) {
            log.info("查询索引失败, exception:", e);
        }

        // 开始创建索引.
        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        try {
            // 设置setting参数.
            request.settings(setting);
            // 设置mapping参数.
            request.mapping(mapping);
            // 设置别名
            // request.alias(new Alias("user_info_hejie_test"));
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            boolean falg = createIndexResponse.isAcknowledged();
            if (falg) {
                log.info("创建索引库成功, indexName: {}", INDEX_NAME);
            }
        } catch (Exception e) {
            log.info("创建索引失败, exception:", e);
            return ReturnEntity.buildErrorResponse("创建索引失败");
        }
        return ReturnEntity.buildSuccessResponse("success");
    }

    @Override
    public ReturnEntity<String> syncData(Integer start, Integer end) {
        // 起10个线程，均分需要迁移的数据.
        int total = end - start;
        if (total <= 0) {
            return ReturnEntity.buildSuccessResponse("success");
        }
        int sliceSize = (int) Math.ceil(total / (10 + 0.00F));
        int i = 0;
        while (i < 10) {
            int sliceStart = start + sliceSize * i;
            int sliceEnd = start + Math.min(total, sliceSize * (i + 1));
            executor.execute(() -> sycSliceDataToEs(sliceStart, sliceEnd));
            i++;
        }
        return ReturnEntity.buildSuccessResponse("success");
    }

    private void sycSliceDataToEs(Integer sliceStart, Integer sliceEnd) {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        int cur = sliceStart;
        int size = 500;
        for (; cur < sliceEnd; ) {
            int endId = Math.min(cur + size, sliceEnd);
            List<InviteCardTaskInfoPO> taskInfoPOS = inviteCardTaskInfoPOMapper.listByIdRange(cur, endId);
            taskInfoPOS.forEach(item -> {
                request.id(String.valueOf(item.getId()));
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("id", item.getId());
                jsonMap.put("account_id", item.getAccountId());
                jsonMap.put("task_id", item.getTaskId());
                jsonMap.put("channel_id", item.getChannelId());
                jsonMap.put("app_id", item.getAppId());
                jsonMap.put("data_date", item.getDataDate());
                jsonMap.put("launch_num", item.getLaunchNum());
                jsonMap.put("help_num", item.getHelpNum());
                jsonMap.put("un_follow_num", item.getUnFollowNum());
                jsonMap.put("invite_suc", item.getInviteSuc());
                jsonMap.put("h5_num", item.getH5Num());
                jsonMap.put("poster_num", item.getPosterNum());
                jsonMap.put("share_help_num", item.getShareHelpNum());
                jsonMap.put("create_time", item.getCreateTime());
                jsonMap.put("update_time", item.getUpdateTime());
                request.source(jsonMap);
                try {
                    client.index(request, RequestOptions.DEFAULT);
                } catch (Exception e) {
                    log.info("添加数据失败, id: {}", item.getId());
                }
            });
            cur = endId;
        }
        log.info("id段{}-{}导入es完成", sliceStart, sliceEnd);
    }

    @Override
    public ReturnEntity<String> addData() {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        request.id("1");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("uid", 1234);
        jsonMap.put("phone", "12345678");
        jsonMap.put("msgcode", 1);
        jsonMap.put("sendtime", "2019-03-14 01:57:04");
        jsonMap.put("message", "Study Elasticsearch");
        request.source(jsonMap);
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.info("添加数据失败, content: {}", jsonMap);
        }
        return ReturnEntity.buildSuccessResponse("success");
    }

    @Override
    public ReturnEntity<String> update() {
        Map<String, Object> jsonMap = new HashMap<>();
        try {
            UpdateRequest upateRequest = new UpdateRequest(INDEX_NAME, "1");

            // 依旧可以使用Map这种集合作为更新条件.
            jsonMap.put("uid", 12345);
            jsonMap.put("phone", "987654321");
            jsonMap.put("msgcode", 2);
            upateRequest.doc(jsonMap);
            // upsert 方法表示如果数据不存在，那么就新增一条.
            upateRequest.docAsUpsert(true);
            client.update(upateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.info("更新数据失败, content: {}", jsonMap);
            return ReturnEntity.buildErrorResponse("更新数据失败");
        }
        return ReturnEntity.buildSuccessResponse("success");
    }

    @Override
    public ReturnEntity<String> searchData() {
        // 查询指定的索引库
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置查询条件
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("uid", "1234", "12345");
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("phone", "987654321");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(termsQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        sourceBuilder.query(boolQueryBuilder);
        // 设置起止和结束
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(sourceBuilder);
        log.info("对应的DSL查询语句: {}", sourceBuilder.toString());
        SearchResponse searchResponse;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.info("搜索数据失败,exception: ", e);
            return ReturnEntity.buildErrorResponse("搜索数据失败");
        }
        if (searchResponse != null && searchResponse.getHits().getHits().length != 0) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : hits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                log.info("命中的数据: {}", sourceAsMap);
            }
        }
        return ReturnEntity.buildSuccessResponse("success");
    }

    @Override
    public ReturnEntity<String> del() {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME);
        deleteRequest.id("1");
        // 设置超时时间
        deleteRequest.timeout(TimeValue.timeValueMinutes(2));
        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        // 同步删除.
        try {
            client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.info("删除数据失败，indexName:{}", INDEX_NAME);
            return ReturnEntity.buildErrorResponse("删除数据失败");
        }
        return ReturnEntity.buildSuccessResponse("success");
    }

    @Override
    public ReturnEntity<String> updateMapping() {
        PutMappingRequest putMappingRequest = new PutMappingRequest(INDEX_NAME);
        Map<String, Object> keyword = new HashMap<>();
        keyword.put("type", "keyword");
        keyword.put("ignore_above", 128);
        Map<String, Object> properties = new HashMap<>();
        properties.put("source", keyword);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        putMappingRequest.source(mapping);
        try {
            client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
        }catch (Exception e){
            log.info("修改mapping失败，indexName:{}", INDEX_NAME);
            return ReturnEntity.buildErrorResponse("修改mapping失败");
        }

        return ReturnEntity.buildSuccessResponse("success");
    }
}
