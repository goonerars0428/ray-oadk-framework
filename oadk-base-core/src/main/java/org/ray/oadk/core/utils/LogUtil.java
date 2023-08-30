package org.ray.oadk.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.ray.oadk.core.config.ApplicationContextConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auth ray_cong
 * @date 2019/12/21 23:12
 * @description 日志输出工具类
 */
@Component
@RefreshScope
public class LogUtil {

    public static Map<String, String> LOG_MAP = new ConcurrentHashMap<>();

    static Logger logger = LoggerFactory.getLogger(LogUtil.class);

    /**
     * 输出日志开关
     */
    private Integer LOG_BUTTON_PRINT;
    /**
     * 打印错误堆栈开关
     */
    private Integer LOG_BUTTON_STACK;
    /**
     * 程序输出级别
     */
    private String LOG_LEVEL_PRINT;
    /**
     * 日志显示级别
     */
    private String LOG_LEVEL_VIEW;
    /**
     * 日志输出格式
     */
    private String LOG_STANDARD_FORMAT;

    public Integer getLOG_BUTTON_STACK() {
        return LOG_BUTTON_STACK;
    }

    @Value("${constant.sys.logging.button.stack:1}")
    public void setLOG_BUTTON_STACK(Integer LOG_BUTTON_STACK) {
        this.LOG_BUTTON_STACK = LOG_BUTTON_STACK;
    }

    public Integer getLOG_BUTTON_PRINT() {
        return LOG_BUTTON_PRINT;
    }

    @Value("${constant.sys.logging.button.print:1}")
    public void setLOG_BUTTON_PRINT(Integer LOG_BUTTON_PRINT) {
        this.LOG_BUTTON_PRINT = LOG_BUTTON_PRINT;
    }

    public String getLOG_LEVEL_PRINT() {
        return LOG_LEVEL_PRINT;
    }

    @Value("${constant.sys.logging.level.print:info}")
    public void setLOG_LEVEL_PRINT(String LOG_LEVEL_PRINT) {
        this.LOG_LEVEL_PRINT = LOG_LEVEL_PRINT;
    }

    public String getLOG_LEVEL_VIEW() {
        return LOG_LEVEL_VIEW;
    }

    @Value("${constant.sys.logging.level.view:info}")
    public void setLOG_LEVEL_VIEW(String LOG_LEVEL_VIEW) {
        this.LOG_LEVEL_VIEW = LOG_LEVEL_VIEW;
    }

    public String getLOG_STANDARD_FORMAT() {
        return LOG_STANDARD_FORMAT;
    }

    @Value("${constant.sys.logging.standard_format:{}@{}>>>{}:{}}")
    public void setLOG_STANDARD_FORMAT(String LOG_STANDARD_FORMAT) {
        this.LOG_STANDARD_FORMAT = LOG_STANDARD_FORMAT;
    }

    /**
     * 动态级别输出
     *
     * @param key
     * @param value
     */
    public static void log(String key, Object value) {
        log(getLogUtil().getLOG_LEVEL_PRINT(), key, value);
    }

    public static void log(String key) {
        log(getLogUtil().getLOG_LEVEL_PRINT(), key, null);
    }

    /**
     * 指定级别输出
     *
     * @param logLevel
     * @param key
     * @param value
     */
    public static void log(LogLevel logLevel, String key, Object value) {
        log(logLevel.value, key, value);
    }

    public static void log(LogLevel logLevel, String key) {
        log(logLevel.value, key, null);
    }

    public static void log(String level, String key, Object value) {
        //获取当前线程堆栈，确认调用者信息
        StackTraceElement callerObject = getCallerObject(2);
        String logUser = new StringBuilder(callerObject.getClassName()).append(".").append(callerObject.getMethodName()).toString();
        String logLevel = LOG_MAP.get(logUser);
        if (StringUtils.isBlank(logLevel)) {
            //LogUtil调用者注册
            LOG_MAP.put(logUser, level);
        } else {
            //如果已注册，可通过接口修改日志级别，nacos修改将不生效
            level = logLevel;
        }
        if (getLogUtil().getLOG_BUTTON_PRINT() == 0) {
            return;
        }
        if (level.equalsIgnoreCase(LogLevel.DEBUG.value)) {
            logger.debug(getLogUtil().getLOG_STANDARD_FORMAT(), getThreadInfo(), logUser, key, value);
            return;
        }
        if (level.equalsIgnoreCase(LogLevel.INFO.value)) {
            logger.info(getLogUtil().getLOG_STANDARD_FORMAT(), getThreadInfo(), logUser, key, value);
            return;
        }
        if (level.equalsIgnoreCase(LogLevel.WARN.value)) {
            logger.warn(getLogUtil().getLOG_STANDARD_FORMAT(), getThreadInfo(), logUser, key, value);
            return;
        }
        if (level.equalsIgnoreCase(LogLevel.ERROR.value)) {
            if (value instanceof Exception) {
                Exception e = (Exception) value;
                logger.error(getLogUtil().getLOG_STANDARD_FORMAT(), getThreadInfo(), logUser, key, e.getMessage());
                if (getLogUtil().getLOG_BUTTON_STACK() == 1) {
                    e.printStackTrace();
                }
            } else {
                logger.error(getLogUtil().getLOG_STANDARD_FORMAT(), getThreadInfo(), logUser, key, value);
            }
            return;
        }
        //默认info级别输出
        logger.info(getLogUtil().getLOG_STANDARD_FORMAT(), key, value);
    }


    /**
     * 日志级别枚举类型
     */
    public enum LogLevel {
        DEBUG(0, "DEBUG"),
        INFO(1, "INFO"),
        WARN(2, "WARN"),
        ERROR(3, "ERROR");
        private Integer key;
        private String value;

        LogLevel(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 获取要输出日志的线程信息
     *
     * @return
     */
    private static String getThreadInfo() {
        return new StringBuilder("ray@").append(Thread.currentThread().getId()).toString();
    }

    private static LogUtil getLogUtil() {
        return ApplicationContextConfig.getApplicationContext().getBean(LogUtil.class);
    }

    // level=0, is the method-name who call getCallerMethodName; =1 is the caller's name of the fun who call getCallerMethodName
    public static StackTraceElement getCallerObject(int level) {
        // (getStackTrace, getCallerMethodName, the-caller, ...)
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        level += 2;
        if (level >= stackTrace.length) {
            level = stackTrace.length - 1;
        }
        StackTraceElement ele = stackTrace[level];
        return ele;
//        return ele.getMethodName();
    }

}
