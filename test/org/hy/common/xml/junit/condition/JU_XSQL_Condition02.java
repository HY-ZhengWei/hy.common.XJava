package org.hy.common.xml.junit.condition;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：占位符按条件取值
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-09-16
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_XSQL_Condition02
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XSQL_Condition02() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_001()
    {
        String              v_SQL    = null;
        Map<String ,Object> v_Params = new HashMap<String ,Object>();
        v_Params.put("prepareMaterialDate" ,"1");
        
        v_SQL = XJava.getXSQL("XSQL_User_Condition_002").getContent().getSQL(v_Params);
        System.out.println("prepareMaterialDate = 1时的运行SQL：" + v_SQL);
    }
    
}
