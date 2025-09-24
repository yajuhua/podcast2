package io.github.yajuhua.podcast2.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * Bean 拷贝工具类
 * 功能：只拷贝 source 中非 null 的字段到 target
 */
public class BeanUtilsEx {
    /**
     * 获取对象中值为 null 的属性名
     */
    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    /**
     * 拷贝属性，只复制非 null 的字段
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

}
