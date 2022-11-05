package org.hy.common.xml.junit.drools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.XRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：规则引擎
 *
 * @author      ZhengWei(HY)
 * @createDate  2020-05-25
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_Drools
{
    
    private static boolean $isInit = false;
    
    private Date begin;
    
    private Date loadEnd;
    
    
    
    public JU_Drools() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            begin   = new Date();
            XJava.parserAnnotation(this.getClass().getName());
            loadEnd = new Date();
        }
    }
    
    
    
    /**
     * 测试单条数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-25
     * @version     v1.0
     *
     */
    @Test
    public void test_Drools()
    {
        XRule v_XRule01 = XJava.getXRule("Rule_01");
        XRule v_XRule02 = XJava.getXRule("Rule_02");
        
        Message message01 = new Message();
        message01.setMessage( "Hello World" );
        message01.setStatus( Message.HELLO );
        
        Message message02 = new Message();
        message02.setMessage( "Hello World" );
        message02.setStatus( Message.HELLO );
        
        v_XRule01.execute(message01);
        v_XRule02.execute(message02);
        
        System.out.println(message01); 
        System.out.println(message02); 
    }
    
    
    
    /**
     * 测试多条数据。
     * 
     * 所有规则1执行后才执行所有规则2
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-25
     * @version     v1.0
     *
     */
    @Test
    public void test_Drools_List()
    {
        XRule v_XRule = XJava.getXRule("Rule_01");
        
        List<Message> v_Datas = new ArrayList<Message>();
        for (int i=1; i<=5; i++)
        {
            Message message = new Message();
            message.setMessage( "Hello World" );
            message.setStatus( Message.HELLO );
            
            v_Datas.add(message);
        }
        
        v_XRule.execute(v_Datas);
        
        Help.print(v_Datas); 
    }
    
    
    
    /**
     * 测试循环用时
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-26
     * @version     v1.0
     */
    @Test
    public void test_Drools_Time()
    {
        Date  v_Begin   = new Date();
        XRule v_XRule01 = XJava.getXRule("Rule_01");
        int   v_Count   = 100;
        
        for (int i=1; i<=v_Count; i++)
        {
            Message message01 = new Message();
            message01.setMessage( "Hello World" );
            message01.setStatus( Message.HELLO );
            
            v_XRule01.execute(message01);
        }
        
        Date v_End = new Date();
        System.out.println("全部用时：" + Date.toTimeLen(v_End  .differ(begin)));
        System.out.println("加载用时：" + Date.toTimeLen(loadEnd.differ(begin)));
        System.out.println("执行用时：" + Date.toTimeLen(v_End  .differ(v_Begin)));
        System.out.println("执行平均：" + Date.toTimeLen(v_End  .differ(v_Begin) /  v_Count));
    }
    
    
    
    /**
     * 测试Map集合的使用
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-27
     * @version     v1.0
     *
     */
    @Test
    public void test_Drools_Map()
    {
        Map<String ,Object> v_Datas = new HashMap<String ,Object>();
        
        for (int i=1; i<=10; i++)
        {
            v_Datas.put("key" + StringHelp.lpad(i ,2 ,"0") ,i);
        }
        
        XRule v_XRule03 = XJava.getXRule("Rule_03");
        XRule v_XRule04 = XJava.getXRule("Rule_04");
        XRule v_XRule05 = XJava.getXRule("Rule_05");
        
        v_XRule03.execute(v_Datas);
        v_XRule04.execute(v_Datas);
        v_XRule05.execute(v_Datas);
        
        System.out.println("回现最终的Map");
        Help.print(v_Datas);
    }
    
    
    
    /**
     * 包名称解释、引用类的测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-02
     * @version     v1.0
     *
     */
    @Test
    public void test_Drools_Package_Imports()
    {
        XRule v_XRule04 = XJava.getXRule("Rule_04");
        
        System.out.println("PackageName = " + v_XRule04.getPackage());
        System.out.println("Import");
        Help.print(v_XRule04.getImports());
        
        
        
        XSQL v_XSQL = new XSQL();
        
        v_XSQL.setBeforeRule(v_XRule04);
        
        System.out.println("XSQL's rule PackageName = " + v_XSQL.getBeforeRule().getPackage());
        System.out.println("XSQL's rule Import");
        Help.print(v_XSQL.getBeforeRule().getImports());
        
        System.out.println("XSQL's rule vlaue");
        System.out.println(v_XSQL.getBeforeRule().getValue());
        v_XSQL.getBeforeRule().initRule();
    }
    
}
