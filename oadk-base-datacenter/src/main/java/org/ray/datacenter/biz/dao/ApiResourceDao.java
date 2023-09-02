package org.ray.datacenter.biz.dao;

import org.apache.ibatis.annotations.Param;
import org.ray.data.mysql.annotation.DataSource;
import org.ray.datacenter.biz.domain.po.ApiResource;
import org.ray.datacenter.biz.domain.po.ApiResourceQuery;

import java.util.List;
@DataSource(key = "oadk")
public interface ApiResourceDao {
    int countByExample(ApiResourceQuery example);

    int deleteByExample(ApiResourceQuery example);

    int deleteByPrimaryKey(String id);

    int insert(ApiResource record);

    int insertSelective(ApiResource record);

    List<ApiResource> selectByExample(ApiResourceQuery example);

    ApiResource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiResource record, @Param("example") ApiResourceQuery example);

    int updateByExample(@Param("record") ApiResource record, @Param("example") ApiResourceQuery example);

    int updateByPrimaryKeySelective(ApiResource record);

    int updateByPrimaryKey(ApiResource record);

    void upsert(ApiResource apiResource);
}