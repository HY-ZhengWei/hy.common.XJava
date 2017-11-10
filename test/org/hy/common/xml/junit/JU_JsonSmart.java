package org.hy.common.xml.junit;

import org.hy.common.xml.XJSONObject;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;





/**
 * 测试单元：关于Json-Smart包的测试
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-11-10
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_JsonSmart
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_JsonSmart() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_JsonSmart.class.getName());
        }
    }
    
    
    
    /**
     * 属性值中包含:冒号的测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-11-10
     * @version     v1.0
     *
     */
    @Test
    public void test_JsonSmart_01()
    {
        String      v_JsonString = XJava.getParam("JsonSmart01").getValue();
        JSONParser  v_JsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        XJSONObject v_JSONRoot   = null;
        
        try
        {
            v_JSONRoot = new XJSONObject((JSONObject) v_JsonParser.parse(v_JsonString));
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        if ( v_JSONRoot == null )
        {
            System.err.println("解释冒号异常.");
        }
        else
        {
            System.out.println("解释冒号成功.");
        }
    }
    
}
