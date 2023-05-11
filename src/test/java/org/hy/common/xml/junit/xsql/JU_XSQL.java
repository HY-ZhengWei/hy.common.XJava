package org.hy.common.xml.junit.xsql;

import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.junit.xsql.bean.DataInfo;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * XSQL的测试单元
 *
 * @author      ZhengWei(HY)
 * @createDate  2020-10-27
 * @version     v1.0
 */
public class JU_XSQL extends AppInitConfig
{
    
    private static Logger  $Logger = new Logger(JU_XSQL.class ,true);
    
    private static boolean $isInit = false;
    
    
    
    @SuppressWarnings("unchecked")
    public JU_XSQL()
    {
        if ( !$isInit )
        {
            $isInit = true;
            
            String v_XmlRoot = this.getClass().getResource("").getFile();
            
            this.loadXML("startup.Config.xml"                          ,v_XmlRoot);
            this.loadXML((List<Param>)XJava.getObject("StartupConfig") ,v_XmlRoot);
        }
    }
    
    
    
    /**
     * 测试查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-10-27
     * @version     v1.0
     * @throws InterruptedException
     */
    @Test
    public void querySQLServerVarbinary() throws InterruptedException
    {
        XSQL v_XSQL = XJava.getXSQL("XSQL_Junit_Query_SQLServer_Varbinary");
        
        v_XSQL.query(new DataInfo());
        
        Thread.sleep(60 * 1000);
    }
    
    
    
    /**
     * 测试查询COUNT(1) or COUNT(*)
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-05-11
     * @version     v1.0
     *
     */
    @Test
    public void querySQLCount()
    {
        Map<String ,Object> v_SQLs = XJava.getObjects("XSQL_Junit_QuerySQLCount_");
        
        if ( !Help.isNull(v_SQLs) )
        {
            for (Object v_XSQL : v_SQLs.values())
            {
                XSQL v_XSQLObj = (XSQL) v_XSQL;
                $Logger.info(v_XSQLObj.getXJavaID() + " : " + v_XSQLObj.querySQLCount());
            }
        }
    }
    
}
