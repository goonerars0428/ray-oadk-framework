package org.ray.datacenter.biz.domain.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.ray.oadk.core.exception.ErrorCodeException;
import org.ray.oadk.core.exception.ErrorCodeInterface;

import java.io.Serializable;

/**
 * @auth ray_cong
 * @date 2019/12/21 12:39
 * @description response返回统一格式
 */
public class ResponseDTO<T> implements Serializable {

    /**
     * 请求状态码
     */
    private Integer errcode;
    /**
     * 请求状态描述
     */
    private String errmsg;
    /**
     * 请求执行时间，如果是null不展示
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long executeTime;
    /**
     * 请求数据，如果是null不展示
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CommonDTO<T> data;

    /**
     * 标准返回
     *
     * @param errcode
     * @param errmsg
     * @param executeTime
     * @param data
     */
    public ResponseDTO(Integer errcode, String errmsg, Long executeTime, CommonDTO data) {
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.executeTime = executeTime;
        this.data = data;
    }

    /**
     * 正常返回
     *
     * @param data
     */
    public ResponseDTO(CommonDTO data) {
        this.errcode = ErrorCodeException.ErrorCode.OK.getCode();
        this.errmsg = ErrorCodeException.ErrorCode.OK.getMsg();
        this.data = data;
    }

    /**
     * 正常返回，只包含状态码
     */
    public ResponseDTO() {
        this.errcode = ErrorCodeException.ErrorCode.OK.getCode();
        this.errmsg = ErrorCodeException.ErrorCode.OK.getMsg();
    }

    /**
     * 异常返回
     *
     * @param errorCodeInterface
     */
    public ResponseDTO(ErrorCodeInterface errorCodeInterface) {
        this.errcode = errorCodeInterface.getErrorCode();
        this.errmsg = errorCodeInterface.getErrorMessage();
    }

    public Integer getErrcode() {
        return errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public Long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Long executeTime) {
        this.executeTime = executeTime;
    }

    public CommonDTO getData() {
        return data;
    }

    public void setData(CommonDTO data) {
        this.data = data;
    }


}
