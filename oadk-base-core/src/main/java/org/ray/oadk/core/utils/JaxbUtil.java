package org.ray.oadk.core.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @auth congr@inspur.com
 * @date 2019/1/23
 * @description XML和java对象转换工具类
 */
public class JaxbUtil {

    public static String ENCODING = "GBK";

    public static Boolean FORMATTED_OUTPUT = true;

    public static Boolean USE_CUSTOM_HEADER = false;

    public static String CUSTOM_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * XML转换java对象
     *
     * @param is    输入流
     * @param clazz 指定类型
     * @return
     * @throws JAXBException
     */
    public static <T> T xml2Bean(InputStream is, Class<T> clazz) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(is);
    }

    /**
     * java对象转换XML
     *
     * @param t 待转换对象
     * @return
     * @throws JAXBException
     */
    public static <T> String bean2Xml(T t) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(t.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        //指定输出是否格式化成xml标准格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, FORMATTED_OUTPUT);
        //指定编码格式
        marshaller.setProperty(Marshaller.JAXB_ENCODING, ENCODING);
        if (USE_CUSTOM_HEADER) {
            //去除standalone属性
//            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
//            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", CUSTOM_HEADER);
        }

        StringWriter writer = new StringWriter();
        marshaller.marshal(t, writer);
        StringBuilder result = new StringBuilder();
        if (USE_CUSTOM_HEADER) {
            result.append(CUSTOM_HEADER);
            if(FORMATTED_OUTPUT) {
                //如果需要标准格式输出，增加换行符
                result.append("\n");
            }
        }
        result.append(writer.toString());
        return result.toString();
    }
}
