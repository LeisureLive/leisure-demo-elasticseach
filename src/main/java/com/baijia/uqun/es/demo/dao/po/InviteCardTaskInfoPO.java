package com.baijia.uqun.es.demo.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: 邀请卡活动数据.
 *
 * @date: 2020/9/17 5:05 下午
 * @author: Jie.He, hejie@baijiahulian.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteCardTaskInfoPO {

    private Integer id;

    private Integer accountId;

    private Integer taskId;

    private Integer channelId;

    private String appId;

    private String dataDate;

    private Long launchNum;

    private Long helpNum;

    private Long unFollowNum;

    private Long inviteSuc;

    private Long h5Num;

    private Long posterNum;

    private Long shareHelpNum;

    private String createTime;

    private String updateTime;

}
