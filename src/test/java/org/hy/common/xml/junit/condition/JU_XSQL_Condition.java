package org.hy.common.xml.junit.condition;

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
    
    private String userName;
    
    private String password;
    
    private String orgName;
    
    private Double price;
    
    private Date   createTime;
    
    
    
    public JU_XSQL_Condition() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    /**
     * 入参类型是Map时，在处理NULL与入参类型是Object，是不同的。
     *   1. Map填充为""空的字符串。
     *   2. Object填充为 "NULL" ，可以支持空值针的写入。
     *   
     *   但上方两种均可以通过配置<condition><name>占位符名称<name></condition>，向数据库写入空值针。
     */
    @Test
    public void test_001()
    {
        String              v_SQL    = null;
        Map<String ,Object> v_Params = new HashMap<String ,Object>();
        v_Params.put("orgName"    ,"根目录");
        v_Params.put("uSeRnAmE"   ,"admin");
        v_Params.put("createTime" ,(Date)null);
        
        v_Params.put("pAsSwOrD" ,"123");
        v_Params.put("isBind" ,"1");
        v_SQL = XJava.getXSQL("XSQL_OPCDD_Model_Query").getContent().getSQL(v_Params);
        System.out.println("密码为1234时的运行SQL：" + v_SQL);
        
        v_Params.put("pAsSwOrD" ,"123");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为1234时的运行SQL：" + v_SQL);
        
        v_Params.put("pAsSwOrD" ,"12345678");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678时的运行SQL：" + v_SQL);
        
        v_Params.put("pAsSwOrD" ,"12345678901234567");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678901234567时的运行SQL：" + v_SQL);
        
        v_Params.put("pAsSwOrD" ,"12345678");
        v_Params.put("orgName"  ,"我的部门");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678时的运行SQL：" + v_SQL);
    }
    
    
    
    /**
     * 入参类型是Map时，在处理NULL与入参类型是Object，是不同的。
     *   1. Map填充为""空的字符串。
     *   2. Object填充为 "NULL" ，可以支持空值针的写入。
     *   
     *   但上方两种均可以通过配置<condition><name>占位符名称<name></condition>，向数据库写入空值针。
     */
    @Test
    public void test_002() throws Exception
    {
        String            v_SQL    = null;
        JU_XSQL_Condition v_Params = new JU_XSQL_Condition();
        v_Params.setOrgName("根目录");
        v_Params.setUserName("admin");
        v_Params.setCreateTime(null);
        
        v_Params.setPassword("123");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为1234时的运行SQL：" + v_SQL);
        
        v_Params.setPassword("12345678");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678时的运行SQL：" + v_SQL);
        
        v_Params.setPassword("12345678901234567");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678901234567时的运行SQL：" + v_SQL);
        
        v_Params.setPassword("12345678");
        v_Params.setOrgName("我的部门");
        v_SQL = XJava.getXSQL("XSQL_User_Condition").getContent().getSQL(v_Params);
        System.out.println("密码为12345678时的运行SQL：" + v_SQL);
    }
    
    
    
    public String getUserName()
    {
        return userName;
    }

    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    
    public String getPassword()
    {
        return password;
    }


    public void setPassword(String password)
    {
        this.password = password;
    }

    
    public String getOrgName()
    {
        return orgName;
    }


    public void setOrgName(String orgName)
    {
        this.orgName = orgName;
    }

    
    public Date getCreateTime()
    {
        return createTime;
    }


    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    
    public Double getPrice()
    {
        return price;
    }


    public void setPrice(Double price)
    {
        this.price = price;
    }
    
}
