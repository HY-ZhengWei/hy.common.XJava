package org.hy.common.xml.junit;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：SQL注入
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-31
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_XSQL_Attack
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XSQL_Attack() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_XSQL_Attack.class.getName());
        }
    }
    
    
    
    @Test
    public void attack01()
    {
        Map<String ,String> v_Params = new HashMap<String ,String>();
        
        v_Params.put("userName" ,"' OR 1 = 1 OR 1 = '");
        v_Params.put("password" ,"2");
        
        System.out.println(XJava.getXSQL("XSQL_User_QueryAttack").getContent().getSQL(v_Params));
    }
    
    
    
    @Test
    public void attack02()
    {
        Map<String ,String> v_Params = new HashMap<String ,String>();
        
        v_Params.put("userName" ,"= -- '");
        v_Params.put("password" ,"2");
        
        System.out.println(XJava.getXSQL("XSQL_User_QueryAttack").getContent().getSQL(v_Params));
    }
    
    
    
    @Test
    public void attack03()
    {
        Map<String ,String> v_Params = new HashMap<String ,String>();
        
        v_Params.put("userName" ,"' TRUNCATE Table ...");
        v_Params.put("password" ,"2");
        
        long v_BeginTime = Date.getNowTime().getTime();
        System.out.println(XJava.getXSQL("XSQL_User_QueryAttack").getContent().getSQL(v_Params));
        System.out.println("用时：" + (Date.getNowTime().getTime() - v_BeginTime));
    }
}
