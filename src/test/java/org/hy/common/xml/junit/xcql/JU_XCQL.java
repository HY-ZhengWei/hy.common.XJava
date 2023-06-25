package org.hy.common.xml.junit.xcql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.junit.xcql.bean.DataSourceConfig;
import org.hy.common.xml.junit.xcql.dao.IQueryDAO;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * XCQL的测试单元
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-06-24
 * @version     v1.0
 */
public class JU_XCQL extends AppInitConfig
{
    
    private static Logger  $Logger = new Logger(JU_XCQL.class ,true);
    
    private static boolean $isInit = false;
    
    
    
    @SuppressWarnings("unchecked")
    public JU_XCQL()
    {
        if ( !$isInit )
        {
            $isInit = true;
            
            String v_XmlRoot = this.getClass().getResource("").getFile();
            
            this.loadXML("startup.Config.xml"                          ,v_XmlRoot);
            this.loadXML((List<Param>)XJava.getObject("StartupConfig") ,v_XmlRoot);
            this.loadClasses(JU_XCQL.class.getPackage().getName());
        }
    }
    
    
    
    /**
     * 测试查询，返回List<Map>结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-24
     * @version     v1.0
     */
    @Test
    public void queryReturnListMap()
    {
        IQueryDAO                 v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        List<Map<String ,Object>> v_Datas    = v_QueryDAO.queryListMap();
        
        $Logger.info("查询到 " + v_Datas.size() + " 条数据");
    }
    
    
    
    /**
     * 测试查询，返回List<Object>结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryReturnObject()
    {
        IQueryDAO              v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        List<DataSourceConfig> v_Datas    = v_QueryDAO.queryObjects();
        
        $Logger.info("查询到 " + v_Datas.size() + " 条数据");
    }
    
    
    
    /**
     * 测试查询：分页
     */
    @Test
    public void queryPaging()
    {
        Map<String ,Object> v_Param = new HashMap<String ,Object>();
        v_Param.put("startIndex"   ,0);    // 从第几行分页。有效下标从0开始
        v_Param.put("pagePerCount" ,2);    // 每页显示数量
        
        IQueryDAO              v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        List<DataSourceConfig> v_Datas    = v_QueryDAO.queryPaging(v_Param);
        
        for (DataSourceConfig v_Item : v_Datas)
        {
            $Logger.info(v_Item.getDatabaseName());
        }
        
        $Logger.info("查询到 " + v_Datas.size() + " 条数据");
    }
    
    
    
    /**
     * 测试查询，带查询条件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryByName()
    {
        IQueryDAO              v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        List<DataSourceConfig> v_Datas    = v_QueryDAO.queryByName("dataCenter_10010");
        
        $Logger.info("查询到 " + v_Datas.size() + " 条数据");
    }
    
    
    
    /**
     * 测试查询，动态查询条件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryDynamics()
    {
        IQueryDAO              v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        List<DataSourceConfig> v_Datas    = v_QueryDAO.queryDynamics(null);
        
        $Logger.info("查询到 " + v_Datas.size() + " 条数据");
    }
    
    
    
    /**
     * 测试查询，返回首行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryFirst()
    {
        IQueryDAO        v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        DataSourceConfig v_Data     = v_QueryDAO.queryFirst("dataCenter_10010");
        
        $Logger.info("查询到 " + v_Data.getDatabaseName() + " 数据");
    }
    
    
    
    /**
     * 测试查询，返回总行数（返回值类型：整数）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryCount()
    {
        IQueryDAO v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        int       v_Count    = v_QueryDAO.queryCount();
        
        $Logger.info("查询到 " + v_Count + " 条数据");
    }
    
    
    
    /**
     * 测试查询，返回图库的ID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryID()
    {
        DataSourceConfig v_DSC = new DataSourceConfig();
        v_DSC.setDatabaseName("dataCenter_10010");
        
        IQueryDAO v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        int       v_ID       = v_QueryDAO.queryID(v_DSC);
        
        $Logger.info("查询到 id = " + v_ID);
    }
    
    
    
    /**
     * 测试查询，返回时间
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryString()
    {
        DataSourceConfig v_DSC = new DataSourceConfig();
        v_DSC.setDatabaseName("dataCenter_10010");
        
        IQueryDAO v_QueryDAO = (IQueryDAO) XJava.getObject("QueryDAO");
        String    v_Text     = v_QueryDAO.queryString(v_DSC);
        
        $Logger.info("查询到 " + v_Text);
    }
    
    
    
    /**
     * 测试查询，返回时间
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     */
    @Test
    public void queryCreateTime()
    {
        DataSourceConfig v_DSC = new DataSourceConfig();
        v_DSC.setDatabaseName("dataCenter_10010");
        
        IQueryDAO v_QueryDAO   = (IQueryDAO) XJava.getObject("QueryDAO");
        Date      v_CreateTime = v_QueryDAO.queryCreateTime(v_DSC);
        
        $Logger.info("查询到 " + v_CreateTime.getFull());
    }
    
}
