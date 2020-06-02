package org.hy.common.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;





/**
 * 数据库元数据模块
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-01-12
 * @version     v1.0
 *              v2.0  2019-06-11  添加：获取数据库对象
 */
@Xjava(value=XType.XML)
public class XSQLDBMetadata
{
    
    private static boolean $isInit = false;
    
    
    
    public XSQLDBMetadata() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    /**
     * 获取数据库对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-06-11
     * @version     v1.0
     *
     * @param i_DSG
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<String> getObjects(DataSourceGroup i_DSG)
    {
        XSQL                v_XSQLMetdata = XJava.getXSQL("XSQL_DBMetadata_QueryByName_" + i_DSG.getDbProductType());
        Map<String ,String> v_Params      = new HashMap<String ,String>();
        
        v_XSQLMetdata.setDataSourceGroup(i_DSG);
        v_Params.put("objectName" ,null);
        
        List<Map<String ,String>> v_Datas = (List<Map<String ,String>>)v_XSQLMetdata.query(v_Params);
        
        if ( Help.isNull(v_Datas) )
        {
            return new ArrayList<String>();
        }
        
        return (List<String>) Help.toList(v_Datas ,"ONAME");
    }
    
    
    
    
    /**
     * 判定对象是否存在。
     * 
     * 此不作过多的验证，交给使用者如XSQL.setCreate(...)来验证。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-12
     * @version     v1.0
     *
     * @param i_XSQL
     * @return
     */
    public boolean isExists(XSQL i_XSQL)
    {
        return this.isExists(i_XSQL.getDataSourceGroup() ,i_XSQL.getCreateObjectName());
    }
    
    
    
    /**
     * 判定对象是否存在。
     * 
     * 此不作过多的验证，交给使用者来验证。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-02
     * @version     v1.0
     *
     * @param i_DataSourceGroup  数据库连接池组
     * @param i_DBObjectName     数据库对象名称
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isExists(DataSourceGroup i_DataSourceGroup ,String i_DBObjectName)
    {
        XSQL                v_XSQLMetdata = XJava.getXSQL("XSQL_DBMetadata_QueryByName_" + i_DataSourceGroup.getDbProductType());
        Map<String ,String> v_Params      = new HashMap<String ,String>();
        
        v_XSQLMetdata.setDataSourceGroup(i_DataSourceGroup);
        v_Params.put("objectName" ,i_DBObjectName);
        
        List<Map<String ,Object>> v_Datas = (List<Map<String ,Object>>)v_XSQLMetdata.query(v_Params);
        
        return !Help.isNull(v_Datas);
    }
    
    
    
    /**
     * 删除对象
     * 
     * 此不作过多的验证，交给使用者来验证。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-11
     * @version     v1.0
     *
     * @param i_XSQL
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean dropObject(XSQL i_XSQL)
    {
        XSQL                v_XSQLMetdata = XJava.getXSQL("XSQL_DBMetadata_DropByName_" + i_XSQL.getDataSourceGroup().getDbProductType());
        Map<String ,String> v_Params      = new HashMap<String ,String>();
        
        v_XSQLMetdata.setDataSourceGroup(i_XSQL.getDataSourceGroup());
        v_Params.put("objectName" ,i_XSQL.getCreateObjectName());
        
        List<List<String>> v_Datas = (List<List<String>>)v_XSQLMetdata.query(v_Params);
        
        if ( Help.isNull(v_Datas) )
        {
            return false;
        }
        else
        {
            return v_XSQLMetdata.execute(v_Datas.get(0).get(0));
        }
    }
    
}
