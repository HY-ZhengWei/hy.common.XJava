package org.hy.common.xml.junit;

import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.junit.Test;





/**
 * 测试单元：Oracle Clob字段写入测试
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-02
 * @version     v1.0
 */
public class JU_Clob
{
    
    @Test
    public void test()
    {
        XSQL v_XSQL = new XSQL();
        
        v_XSQL.setDataSourceGroup((DataSourceGroup)XJava.getObject("数据库连接池对象"));
        
        v_XSQL.executeUpdate("INSERT INTO TClob(id ,text) VALUES (1 ,EMPTY_CLOB())");
        v_XSQL.executeUpdateCLob("SELECT text FROM TClob WHERE id = 1 FOR UPDATE" ,"{'':[{'PART_PLAN_NO':'1807-2-C0550','CGRK_DATE':'','ITEM_NAME':'缠绕垫片 4000','ITEM_CODE':'D94XD80X3.2','FLAG':'未完成 ','LASTINDATE':'2017\07\01'}]}");
    }
    
}
