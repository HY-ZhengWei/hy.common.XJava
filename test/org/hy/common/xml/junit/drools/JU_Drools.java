package org.hy.common.xml.junit.drools;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.XJava;
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
    
    
    
    public JU_Drools() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
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
        XRule v_XRule = XJava.getXRule("Rule_01");
        
        Message message = new Message();
        message.setMessage( "Hello World" );
        message.setStatus( Message.HELLO );
        
        v_XRule.execute(message);
        
        System.out.println(message); 
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
    
}
