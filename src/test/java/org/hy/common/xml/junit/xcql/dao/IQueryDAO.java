package org.hy.common.xml.junit.xcql.dao;

import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xcql;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.annotation.Xparam;
import org.hy.common.xml.junit.xcql.bean.DataSourceConfig;





/**
 * CQL的查询类
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-06-24
 * @version     v1.0
 */
@Xjava(id="QueryDAO" ,value=XType.XCQL)
public interface IQueryDAO
{
    
    /**
     * 无需定义Java Bean
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @return
     */
    @Xcql("XCQL_Query_001_ReturnListMap")
    public List<Map<String ,Object>> queryListMap();
    
    
    
    /**
     * 返回Java Bean的集合
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @return
     */
    @Xcql("XCQL_Query_002_ReturnObject")
    public List<DataSourceConfig> queryObjects();
    
    
    
    /**
     * 分页查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_Params
     * @return
     */
    @Xcql(id="XCQL_Query_002_ReturnObject" ,paging=true)
    public List<DataSourceConfig> queryPaging(Map<String ,Object> i_Params);
    
    
    
    /**
     * 带条件的查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_DatabaseName
     * @return
     */
    @Xcql("XCQL_Query_003_Where")
    public List<DataSourceConfig> queryByName(@Xparam(id="databaseName" ,notNull=true) String i_DatabaseName);
    
    
    
    /**
     * 动态条件的查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_DatabaseName
     * @return
     */
    @Xcql(id="XCQL_Query_003_Where")
    public List<DataSourceConfig> queryDynamics(@Xparam("databaseName") String i_DatabaseName);
    
    
    
    /**
     * 返回首行记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_DatabaseName
     * @return
     */
    @Xcql(id="XCQL_Query_003_Where" ,returnOne=true)
    public DataSourceConfig queryFirst(@Xparam("databaseName") String i_DatabaseName);
    
    
    
    /**
     * 查总行数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @return
     */
    @Xcql("XCQL_Query_004_Count")
    public int queryCount();
    
    
    
    /**
     * 返回数字类型的一个属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_DSC
     * @return
     */
    @Xcql("XCQL_Query_005_ID")
    public int queryID(@Xparam(notNulls={"databaseName"}) DataSourceConfig i_DSC);
    
    
    
    
    /**
     * 返回字符串类型的一个属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_DSC
     * @return
     */
    @Xcql("XCQL_Query_006_CreateTime")
    public String queryString(DataSourceConfig i_DSC);
    
    
    
    /**
     * 返回时间类型的一个属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-06-25
     * @version     v1.0
     *
     * @param i_DSC
     * @return
     */
    @Xcql("XCQL_Query_006_CreateTime")
    public Date queryCreateTime(DataSourceConfig i_DSC);
    
}
