package org.hy.common.xml.junit;

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
 * @createDate  2018-08-10
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_XSQL_Condition
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XSQL_Condition() throws Exception
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
        v_Params.put("orgName"  ,"我的部门");
        v_Params.put("uSeRnAmE" ,"admin");
        
        v_Params.put("pAsSwOrD" ,"123");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为1234时的运行SQL：" + v_SQL);
        
        v_Params.put("pAsSwOrD" ,"12345678");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678时的运行SQL：" + v_SQL);
        
        v_Params.put("pAsSwOrD" ,"12345678901234567");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678901234567时的运行SQL：" + v_SQL);
    }
    
}
