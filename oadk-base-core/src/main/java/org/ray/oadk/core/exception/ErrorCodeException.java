package org.ray.oadk.core.exception;

/**
 * @auth ray_cong
 * @date 2019/12/21 12:51
 * @description 自定义错误码异常
 */
public class ErrorCodeException extends RuntimeException {

    ErrorCodeInterface ei;

    public ErrorCodeInterface getEi() {
        return ei;
    }

    public ErrorCodeException() {
    }

    public ErrorCodeException(ErrorCodeInterface errorCodeInterface) {
        super(errorCodeInterface.getErrorMessage());
        this.ei = errorCodeInterface;
    }

    /**
     * 自定义异常枚举类
     */
    public enum ErrorCode implements ErrorCodeInterface {
        //系统错误码
        OK(0, "OK", "请求成功"),
        SYS_ERROR(1001, "SYS ERROR", "系统错误，500"),
        SYS_REQ_RESOURCE_NOT_EXIST_ERROR(1002, "SYS REQ RESOURCE NOT EXIST", "接口请求的资源不存在，404"),
        SYS_REQ_METHOD_ERROR(1003, "SYS REQ METHOD ERROR", "接口请求方式错误，POST/GET方式调用错误"),
        SYS_REQ_PARAM_MISS_ERROR(1004, "SYS REQ HEADER MISS ERROR", "接口请求核心参数缺失，header中参数缺失"),
        SYS_REQ_IP_ERROR(1005, "SYS REQ IP ERROR", "接口请求IP地址非法"),
        SYS_REQ_BUSY_ERROR(1006, "SYS REQ BUSY ERROR", "接口请求超出流量限制"),
        SYS_REQ_EXPIRE_ERROR(1007, "SYS REQ EXPIRE ERROR", "接口请求过期"),
        SYS_REQ_PARTNER_ERROR(1008, "SYS REQ PARTNER ERROR", "接口请求方身份非法，不存在的appID"),
        SYS_REQ_PARTNER_DISABLE_ERROR(1009, "SYS REQ PARTNER AUTH DISABLE", "接口请求方权限未启用,合作方状态被禁用"),
        SYS_REQ_AUTH_ERROR(1010, "SYS REQ AUTH ERROR", "接口请求方无该接口权限"),
        SYS_REQ_SIGN_ERROR(1011, "SYS REQ PARTNER AUTH ERROR", "接口请求方身份核验错误,没有密钥或签名错误"),
        BUS_REQ_PARAM_ERROR(1012, "SYS REQ PARAM CHECK ERROR", "接口请求报文核验失败，解密和必传参数缺失错误"),
        SYS_REQ_SECRET_EXCESS_ERROR(1019, "SYS REQ SECRET EXCESS ERROR", "密钥数量超过限制"),
        SYS_REQ_CONTENT_TYPE_ERROR(1020, "SYS REQ CONTENT TYPE ERROR", "content-type和请求体不匹配"),

        //测试系统错误码
        TEST_SYS_ERROR(4001, "TEST SYS ERROR", "测试系统错误"),
        TEST_NOT_EXIST_TASK_ERROR(4002, "TEST TASK NOT EXIST", "不存在的测试任务"),
        TEST_SCENE_CODE_ERROR(4003, "TEST SCENE CODE ERROR", "测试码和测试场景不匹配");


        private Integer code;
        private String msg;
        private String description;

        ErrorCode(Integer code, String msg, String description) {
            this.code = code;
            this.msg = msg;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public Integer getErrorCode() {
            return this.code;
        }

        @Override
        public String getErrorMessage() {
            return this.msg;
        }
    }
}
