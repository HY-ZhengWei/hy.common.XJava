package org.hy.common.xml.junit.xsql;


import java.util.List;

import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.junit.xsql.bean.DataChild;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * Insert语句的XSQL测试单元。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2023-11-25
 * @version     v1.0
 */
public class JU_XSQL_InsertSQL extends AppInitConfig
{
    private static Logger  $Logger = new Logger(JU_XSQL_InsertSQL.class ,true);
    
    private static boolean $isInit = false;
    
    
    
    @SuppressWarnings("unchecked")
    public JU_XSQL_InsertSQL()
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
     * @createDate  2023-11-25
     * @version     v1.0
     */
    @Test
    public void insertSQL01()
    {
    	XSQL      v_XSQL   = XJava.getXSQL("XSQL_Junit_InsertSQLValue_01");
    	DataChild v_Params = new DataChild();
    	
    	v_Params.setId("id123");
    	v_Params.setName("nameABC");
    	v_Params.setValue("value中国");
    	$Logger.info(v_XSQL.getContent().getSQL(v_Params));
    	
    	v_Params.setValue(null);
    	$Logger.info(v_XSQL.getContent().getSQL(v_Params));
    }
    
}
