package org.hy.common.xml;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;





/**
 * 数据库元数据模块
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-01-12
 * @version     v1.0
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
        XSQL                v_XSQLMetdata = XJava.getXSQL("XSQL_DBMetadata_QueryByName_" + i_XSQL.getDataSourceGroup().getDbProductType());
        Map<String ,String> v_Params      = new HashMap<String ,String>();
        
        v_XSQLMetdata.setDataSourceGroup(i_XSQL.getDataSourceGroup());
        v_Params.put("objectName" ,i_XSQL.getCreateObjectName());
        
        return v_XSQLMetdata.getSQLCount(v_Params) >= 1;
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
    public boolean dropObject(XSQL i_XSQL)
    {
        XSQL                v_XSQLMetdata = XJava.getXSQL("XSQL_DBMetadata_DropByName_" + i_XSQL.getDataSourceGroup().getDbProductType());
        Map<String ,String> v_Params      = new HashMap<String ,String>();
        
        v_XSQLMetdata.setDataSourceGroup(i_XSQL.getDataSourceGroup());
        v_Params.put("objectName" ,i_XSQL.getCreateObjectName());
        
        return v_XSQLMetdata.execute(v_Params);
    }
    
}
