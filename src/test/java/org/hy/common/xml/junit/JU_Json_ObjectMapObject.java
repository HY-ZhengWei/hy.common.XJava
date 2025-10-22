package org.hy.common.xml.junit;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.hy.common.MethodReflect;
import org.hy.common.xml.XJSON;
import org.junit.Test;





/**
 * 测试单元：对象中的Map成员中的对象
 *
 * @author      ZhengWei(HY)
 * @createDate  2025-10-22
 * @version     v1.0
 */
public class JU_Json_ObjectMapObject
{
    
    @Test
    public void test_JsonObjectMapObject() throws NoSuchFieldException, SecurityException
    {
        SpectrumVO v_Object = new SpectrumVO();
        v_Object.getElements().put("A" ,new SpectrumElementVO());
        v_Object.getElements().get("A").setName("N123");
        
        
        Field field = SpectrumVO.class.getDeclaredField("elements");
        Class<?> v_Class = MethodReflect.getGenerics(field ,1);
        
        
        Map<String ,SpectrumElementVO> v_Maps = new HashMap<String ,SpectrumElementVO>();
        ParameterizedType v_ParameterizedType = (ParameterizedType)(v_Maps.getClass().getGenericInterfaces()[0]);
        Type[] actualTypeArguments = v_ParameterizedType.getActualTypeArguments();
        // 第一个参数是键的类型，第二个是值的类型
        Type keyType = actualTypeArguments[0];
        Type valueType = actualTypeArguments[1];
        System.out.println("Map键的泛型类型：" + keyType.getTypeName());
        System.out.println("Map值的泛型类型：" + valueType.getTypeName());
        
        String v_JsonText = """
                                {
                                    "BatchNo": "123",
                                    "Grade": "k157",
                                    "DateTime": "2025-10-22 20:53:55",
                                    "Elements": {
                                        "碳": {
                                            "Name": "碳",
                                            "Symbol": "C",
                                            "Value": "4.7"
                                        },
                                        "硅": {
                                            "Name": "硅",
                                            "Symbol": "Si",
                                            "Value": "1.5",
                                            "childs": {
                                                "锰": {
                                                    "Name": "锰",
                                                    "Symbol": "Mn",
                                                    "Value": "0.352"
                                                },
                                                "磷": {
                                                    "Name": "磷",
                                                    "Symbol": "P",
                                                    "Value": "0.032"
                                                }
                                            }
                                        }
                                    }
                                }
                            """;
        
        XJSON      v_XJson = new XJSON();
        SpectrumVO v_Data  = (SpectrumVO) v_XJson.toJava(v_JsonText ,SpectrumVO.class);
        System.out.println(v_Data);
    }
    
}
