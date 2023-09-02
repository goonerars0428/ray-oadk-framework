package org.ray.datacenter.biz.service.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.ray.data.mysql.utils.AopTargetUtils;
import org.ray.datacenter.biz.domain.po.ApiModule;
import org.ray.datacenter.biz.domain.po.ApiResource;
import org.ray.datacenter.biz.service.function.ApiInstanceService;
import org.ray.datacenter.biz.service.function.ApiModuleService;
import org.ray.datacenter.biz.service.function.ApiResourceService;
import org.ray.datacenter.core.constant.DatacenterConstant;
import org.ray.oadk.core.config.ApplicationContextConfig;
import org.ray.oadk.core.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatacenterServiceImpl implements DatacenterService {

    @Autowired
    private DatacenterConstant datacenterConstant;
    @Autowired
    private InetUtils inetUtils;
    @Autowired
    private ApiModuleService apiModuleService;
    @Autowired
    private ApiInstanceService apiInstanceService;
    @Autowired
    private ApiResourceService apiResourceService;

    @Override
    public void doSaveResources(String moduleId) throws Exception {
        //1. 扫描所有资源
        List<ApiResource> apiResources = doScanResource(moduleId);
        //2. 存储资源
        doUpsertResource(apiResources);
    }

    private void doUpsertResource(List<ApiResource> apiResources) {
        for (ApiResource apiResource : apiResources) {
            apiResourceService.upsert(apiResource);
        }
    }

    private List<ApiResource> doScanResource(String moduleId) throws Exception {
        List<ApiResource> resources = new ArrayList<>();
        //1. 获取服务上下文
        ApplicationContext applicationContext = ApplicationContextConfig.getApplicationContext();
        //2. 获取所有controller名称
        String[] controllerNames = applicationContext.getBeanNamesForAnnotation(Controller.class);
        //3. 遍历controller获取内容
        for (String controllerName : controllerNames) {
            //存储请求路径
            StringBuilder controllerPath = new StringBuilder();
            //4. 获取controller的class描述
            Object bean = applicationContext.getBean(controllerName);
            Class<?> clazz = AopTargetUtils.getTarget(bean).getClass();
            //5. 获取controller类上的requestMapping注解
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                String[] controllerRequestMapping = clazz.getAnnotation(RequestMapping.class).value();
                if (controllerRequestMapping != null && controllerRequestMapping.length != 0) {
                    String controllerMapping = controllerRequestMapping[0];
                    if (!controllerMapping.startsWith("/")) {
                        controllerPath.append("/");
                    }
                    controllerPath.append(controllerMapping);
                }
            }
            if (controllerPath.toString().contains("$")) {
                continue;
            }
            //6. 获取controller中所以public方法
            Method[] declaredMethods = clazz.getMethods();
            //7. 遍历所有方法，挑选接口方法
            for (Method declaredMethod : declaredMethods) {
                if (!(declaredMethod.isAnnotationPresent(RequestMapping.class) ||
                        declaredMethod.isAnnotationPresent(GetMapping.class) ||
                        declaredMethod.isAnnotationPresent(PostMapping.class) ||
                        declaredMethod.isAnnotationPresent(PutMapping.class) ||
                        declaredMethod.isAnnotationPresent(DeleteMapping.class))) {
                    continue;
                }
                StringBuilder path = new StringBuilder(controllerPath);
                String resourceName = null;
                String method = null;
                String[] methodRequestMapping = null;
                if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                    methodRequestMapping = declaredMethod.getAnnotation(RequestMapping.class).value();
                    RequestMethod[] methods = declaredMethod.getAnnotation(RequestMapping.class).method();
                    method = methods[0].name();
                    resourceName = declaredMethod.getAnnotation(RequestMapping.class).name();
                } else if (declaredMethod.isAnnotationPresent(GetMapping.class)) {
                    methodRequestMapping = declaredMethod.getAnnotation(GetMapping.class).value();
                    method = RequestMethod.GET.name();
                    resourceName = declaredMethod.getAnnotation(GetMapping.class).name();
                } else if (declaredMethod.isAnnotationPresent(PostMapping.class)) {
                    methodRequestMapping = declaredMethod.getAnnotation(PostMapping.class).value();
                    method = RequestMethod.POST.name();
                    resourceName = declaredMethod.getAnnotation(PostMapping.class).name();
                } else if (declaredMethod.isAnnotationPresent(PutMapping.class)) {
                    methodRequestMapping = declaredMethod.getAnnotation(PutMapping.class).value();
                    method = RequestMethod.PUT.name();
                    resourceName = declaredMethod.getAnnotation(PutMapping.class).name();
                } else if (declaredMethod.isAnnotationPresent(DeleteMapping.class)) {
                    methodRequestMapping = declaredMethod.getAnnotation(DeleteMapping.class).value();
                    method = RequestMethod.DELETE.name();
                    resourceName = declaredMethod.getAnnotation(DeleteMapping.class).name();
                }
                if (methodRequestMapping != null && methodRequestMapping.length != 0) {
                    String methodMapping = methodRequestMapping[0];
                    if (!methodMapping.startsWith("/")) {
                        path.append("/");
                    }
                    path.append(methodMapping);
                }
                //8. 获取接口方法参数
                StringBuilder paramUrl = new StringBuilder();
                StringBuilder paramBody = new StringBuilder();
                StringBuilder paramPath = new StringBuilder();
                Parameter[] parameters = declaredMethod.getParameters();
                for (Parameter parameter : parameters) {
                    Type parameterizedType = parameter.getParameterizedType();
                    String typeName = parameterizedType.getTypeName();
                    String type = typeName.substring(typeName.lastIndexOf(".") + 1);
                    String name = parameter.getName();
                    if (parameter.isAnnotationPresent(RequestBody.class)) {
                        //请求体参数
                        doAppendParam(paramBody, type, name);
                    } else if (parameter.isAnnotationPresent(PathVariable.class)) {
                        //路径参数
                        doAppendParam(paramPath, type, name);
                    } else {
                        //地址栏参数
                        doAppendParam(paramUrl, type, name);
                    }
                }
                String paramUrlStr = doHandleParam(paramUrl);
                String paramBodyStr = doHandleParam(paramBody);
                String paramPathStr = doHandleParam(paramPath);
                //区分系统模块和应用模块
                if (controllerName.startsWith("oadk")) {
                    //系统模块统一注册moduleId=0
//                    continue;
                    moduleId = "oadk";
                }
                String id = UUIDUtil.uuid();
                String name = null;
                if (StringUtils.isNotBlank(resourceName)) {
                    String[] split = resourceName.split("@oadk@");
                    if (split.length == 2) {
                        id = split[0];
                        name = split[1];
                    } else {
                        name = resourceName;
                    }
                }
                //封装资源对象
                ApiResource resource = new ApiResource(id, path.toString(), method, name, moduleId, paramUrlStr, paramBodyStr, paramPathStr);
                resources.add(resource);
            }
        }
        return resources;
    }

    private void doAppendParam(StringBuilder param, String type, String name) {
        param.append(type).append(":").append(name).append(",");
    }

    private String doHandleParam(StringBuilder param) {
        String result = null;
        if (StringUtils.isNotBlank(param)) {
            result = param.substring(0, param.length() - 1);
        }
        return result;
    }

    @Override
    public void doSaveInstance(String moduleId, String serviceAddress, String version) {
        apiInstanceService.upsert(moduleId, serviceAddress, datacenterConstant.getPort(), version);
    }

    @Override
    public String doSaveModule() {
        String id = null;
        //获取模块
        ApiModule apiModule = apiModuleService.select(datacenterConstant.getApplicationName());
        if (apiModule == null) {
            id = UUIDUtil.uuid();
            //不存在执行插入
            apiModuleService.insert(id, datacenterConstant.getApplicationName());
        } else {
            id = apiModule.getId();
        }
        return id;
    }

    @Override
    public String doFindServiceVersion() throws IOException, XmlPullParserException {
        //file:/usr/local/src/idmoadk.jar!/BOOT-INF/class!/
        String jarPath = DatacenterServiceImpl.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");
        try {
            //file:/usr/local/src/idmoadk.jar
            String path = jarPath.split("!")[0];
            //jar:file:/usr/local/src/idmoadk.jar
            path = path.replace("file", "jar:file");
            StringBuilder sb = new StringBuilder(path).append("!/META-INF/maven/").append(datacenterConstant.getGroupId()).append("/").append(datacenterConstant.getArtifactId()).append("/pom.xml");
            URL url = new URL(sb.toString());
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(url.openStream());
            String version = model.getVersion();
            if (StringUtils.isBlank(version)) {
                version = model.getParent().getVersion();
            }
            return version;
        } catch (Exception e) {
            //开发过程中查看pom.xml版本号
            MavenXpp3Reader reader = new MavenXpp3Reader();
            String basePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            if (isWindows() && basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }
            if (basePath.indexOf("/target/") != -1) {
                basePath = basePath.substring(0, basePath.indexOf("/target/"));
            }
            Model model = reader.read(new FileReader(new File(basePath + "\\pom.xml")));
            String version = model.getVersion();
            if (StringUtils.isBlank(version)) {
                version = model.getParent().getVersion();
            }
            return version;
        }
    }

    /**
     * 是否是windows系统
     *
     * @return
     */
    private static boolean isWindows() {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().indexOf("win") >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public String doFindServiceAddress() {
        return inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
    }
}
