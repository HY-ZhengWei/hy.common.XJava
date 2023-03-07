package org.hy.common.xml.junit.xsql;

import java.util.List;

import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.junit.xsql.dao.IQuerySQLValueDAO;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * XSQL的测试单元。方法返回类型是String、Double、Float、BigDecimal、Date的，则按第一行第一列数据返回
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-03-07
 * @version     v1.0
 */
public class JU_XSQL_QuerySQLValue extends AppInitConfig
{
    private static Logger  $Logger = new Logger(JU_XSQL_QuerySQLValue.class ,true);
    
    private static boolean $isInit = false;
    
    
    
    @SuppressWarnings("unchecked")
    public JU_XSQL_QuerySQLValue()
    {
        if ( !$isInit )
        {
            $isInit = true;
            
            String v_XmlRoot = this.getClass().getResource("").getFile();
            
            this.loadXML("startup.Config.xml"                          ,v_XmlRoot);
            this.loadXML((List<Param>)XJava.getObject("StartupConfig") ,v_XmlRoot);
            this.loadClasses("org.hy.common.xml.junit.xsql");
        }
    }
    
    
    
    /**
     * 测试查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-03-07
     * @version     v1.0
     * @throws InterruptedException
     */
    @Test
    public void querySQLServerVarbinary() throws InterruptedException
    {
        IQuerySQLValueDAO v_DAO = (IQuerySQLValueDAO) XJava.getObject("QuerySQLValueDAO");
        
        $Logger.info("queryInteger    = " + v_DAO.queryInteger());
        $Logger.info("queryInt        = " + v_DAO.queryInt());
        $Logger.info("queryLong       = " + v_DAO.queryLong());
        $Logger.info("querylong       = " + v_DAO.querylong());
        $Logger.info("queryDouble     = " + v_DAO.queryDouble());
        $Logger.info("querydouble     = " + v_DAO.querydouble());
        $Logger.info("queryFloat      = " + v_DAO.queryFloat());
        $Logger.info("queryfloat      = " + v_DAO.queryfloat());
        $Logger.info("queryDate       = " + v_DAO.queryDate());
        $Logger.info("queryDateJava   = " + v_DAO.queryDateJava());
        $Logger.info("queryString     = " + v_DAO.queryString());
        $Logger.info("queryBigDecimal = " + v_DAO.queryBigDecimal());
    }
    
}
