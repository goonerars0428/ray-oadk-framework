package org.ray.datacenter.biz.dao;

import org.apache.ibatis.annotations.Param;
import org.ray.data.mysql.annotation.DataSource;
import org.ray.datacenter.biz.domain.po.ApiInstance;
import org.ray.datacenter.biz.domain.po.ApiInstanceQuery;

import java.util.List;
@DataSource(key = "oadk")
public interface ApiInstanceDao {
    int countByExample(ApiInstanceQuery example);

    int deleteByExample(ApiInstanceQuery example);

    int deleteByPrimaryKey(String id);

    int insert(ApiInstance record);

    int insertSelective(ApiInstance record);

    List<ApiInstance> selectByExample(ApiInstanceQuery example);

    ApiInstance selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiInstance record, @Param("example") ApiInstanceQuery example);

    int updateByExample(@Param("record") ApiInstance record, @Param("example") ApiInstanceQuery example);

    int updateByPrimaryKeySelective(ApiInstance record);

    int updateByPrimaryKey(ApiInstance record);

    void upsert(ApiInstance apiInstance);
}