package org.ray.datacenter.biz.service.business;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.ray.data.mysql.annotation.TransactionMulti;
import org.ray.datacenter.core.constant.DatacenterConstant;

import java.io.IOException;

public interface DatacenterService {

    /**
     * 执行项目初始化注册
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    @TransactionMulti(dataSourceKey = {"oadk"})
    default void init() throws Exception {
        //1. 获取服务IP
        String serviceAddress = doFindServiceAddress();
        //2. 获取服务版本
        String version = doFindServiceVersion();
        DatacenterConstant.SERVICE_VERSION = version;
        //3. 保存模块信息
        String moduleId = doSaveModule();
        //4. 保存实例信息
        doSaveInstance(moduleId, serviceAddress, version);
        //5. 保存资源
        doSaveResources(moduleId);
    }

    void doSaveResources(String moduleId) throws Exception;

    void doSaveInstance(String moduleId, String serviceAddress, String version);

    String doSaveModule();

    String doFindServiceVersion() throws IOException, XmlPullParserException;

    String doFindServiceAddress();

}
