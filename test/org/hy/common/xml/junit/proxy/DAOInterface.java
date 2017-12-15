package org.hy.common.xml.junit.proxy;

import java.util.List;

import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.annotation.Xsql;





@Xjava(id="DAOIF" ,value=XType.XSQL)
public interface DAOInterface
{
    
    @Xsql("XSQL_XSQLProxy_001")
    public List<Object> query();
    
    
    
    @Xsql
    public List<Object> queryDefaultXID();
    
    
    
    @Xsql(id="XSQL_XSQLProxy_002" ,names={"userName"} ,returnOne=true)
    public Object queryByNamePwd(String i_Name);
    
}
