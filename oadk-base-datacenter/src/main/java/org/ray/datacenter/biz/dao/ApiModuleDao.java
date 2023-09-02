package org.ray.datacenter.biz.dao;

import org.apache.ibatis.annotations.Param;
import org.ray.data.mysql.annotation.DataSource;
import org.ray.datacenter.biz.domain.po.ApiModule;
import org.ray.datacenter.biz.domain.po.ApiModuleQuery;

import java.util.List;
@DataSource(key = "oadk")
public interface ApiModuleDao {
    int countByExample(ApiModuleQuery example);

    int deleteByExample(ApiModuleQuery example);

    int deleteByPrimaryKey(String id);

    int insert(ApiModule record);

    int insertSelective(ApiModule record);

    List<ApiModule> selectByExample(ApiModuleQuery example);

    ApiModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiModule record, @Param("example") ApiModuleQuery example);

    int updateByExample(@Param("record") ApiModule record, @Param("example") ApiModuleQuery example);

    int updateByPrimaryKeySelective(ApiModule record);

    int updateByPrimaryKey(ApiModule record);
}