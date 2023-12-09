package org.hy.common.xml.junit.xsql.dao;

import org.hy.common.xml.XSQLData;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.annotation.Xparam;
import org.hy.common.xml.annotation.Xsql;
import org.hy.common.xml.junit.xsql.bean.DataChild;





/**
 * Insert语句DAO。
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-11-25
 * @version     v1.0
 */
@Xjava(id="InsertSQLDAO" ,value=XType.XSQL)
public interface IInsertSQLDAO
{
 
    @Xsql("XSQL_Junit_InsertSQLValue_01")
    public XSQLData insert01(@Xparam("id")        String i_ID
    		               ,@Xparam("name")      String i_Name
    		               ,@Xparam("valueName") String i_ValueName
    		               ,@Xparam("value")     String i_Value);
    
    
    
    @Xsql("XSQL_Junit_InsertSQLValue_01")
    public XSQLData insert01(DataChild i_Data);
    
}
