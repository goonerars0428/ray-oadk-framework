package org.ray.oadk.core.exception;

/**
 * @auth ray_cong
 * @date 2019/12/21 13:20
 * @description 自定义异常扩展接口，可以通过工厂类扩展或定义枚举实现
 */
public interface ErrorCodeInterface {

    Integer getErrorCode();

    String getErrorMessage();
}
