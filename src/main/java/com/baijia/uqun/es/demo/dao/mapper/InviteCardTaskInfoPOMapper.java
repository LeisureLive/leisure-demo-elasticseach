package com.baijia.uqun.es.demo.dao.mapper;

import com.baijia.uqun.es.demo.dao.po.InviteCardTaskInfoPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface InviteCardTaskInfoPOMapper {

    List<InviteCardTaskInfoPO> listByIdRange(@Param("start") Integer start, @Param("end") Integer end);

}