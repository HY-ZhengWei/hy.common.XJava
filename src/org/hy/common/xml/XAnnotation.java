package org.hy.common.xml;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.hy.common.ClassInfo;
import org.hy.common.ClassReflect;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.xml.annotation.Xjava;





/**
 * Xjava注解相关的反射。
 * 
 * @author      ZhengWei(HY)
 * @version     v2.0  
 * @createDate  2014-12-05
 */
public class XAnnotation
{
    
    /**
     * 获取指定参数个数的，并且为XJava注解的所有方法对象
     * 
     * 只获取"本类自己"的方法
     * 只获取公共方法，即 Public 方法
     * 获取任一参数个数的方法
     * 
     * @param i_Class
     * @param i_AnnotationClass   注解类型的元类型
     * @param i_ParamSize
     * @return
     */
    public static List<Method> getAnnotationMethods(Class<?> i_Class)
    {
        return MethodReflect.getAnnotationMethods(i_Class ,Xjava.class ,-1);
    }
    
    
    
    /**
     * 获取指定参数个数的，并且为XJava注解的所有方法对象
     * 
     * 只获取"本类自己"的方法
     * 只获取公共方法，即 Public 方法
     * 
     * @param i_Class
     * @param i_AnnotationClass   注解类型的元类型
     * @param i_ParamSize         参数个数。小于0表示：无参数个数限制
     * @return
     */
    public static List<Method> getAnnotationMethods(Class<?> i_Class ,int i_ParamSize)
    {
        return MethodReflect.getAnnotationMethods(i_Class ,Xjava.class ,i_ParamSize);
    }
    
    
    
    /**
     * 从指定集合i_Classes中，挑出有XJava注解的Class。并分类存储。
     * 
     * @param i_Classes          Java元类型的集合
     * @param i_AnnotationClass  注解类型的元类型
     * @return
     */
    public static PartitionMap<ElementType ,ClassInfo> getAnnotations(List<Class<?>> i_Classes)
    {
        return ClassReflect.getAnnotations(i_Classes ,Xjava.class);
    }
    
    
    
    /**
     * 获取为XJava注解的所有属性对象
     * 
     * 只获取"本类自己"的属性
     * 
     * @param i_Class
     * @param i_AnnotationClass   注解类型的元类型
     * @return
     */
    public static List<Field> getAnnotationFields(Class<?> i_Class)
    {
        return ClassReflect.getAnnotationFields(i_Class ,Xjava.class);
    }
    
}
