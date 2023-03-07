package org.hy.common.xml.junit.xsql.dao;

import java.math.BigDecimal;

import org.hy.common.Date;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.annotation.Xsql;





/**
 * 测试DAO。方法返回类型是String、Double、Float、BigDecimal、Date的，则按第一行第一列数据返回
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-03-07
 * @version     v1.0
 */
@Xjava(id="QuerySQLValueDAO" ,value=XType.XSQL)
public interface IQuerySQLValueDAO
{
    
    @Xsql("XSQL_Junit_QuerySQLValue_Integer")
    public Integer queryInteger();
    
    @Xsql("XSQL_Junit_QuerySQLValue_Integer")
    public int queryInt();
    
    
    
    @Xsql("XSQL_Junit_QuerySQLValue_Long")
    public Long queryLong();
    
    @Xsql("XSQL_Junit_QuerySQLValue_Long")
    public long querylong();
    
    
    
    @Xsql("XSQL_Junit_QuerySQLValue_Double")
    public Double queryDouble();
    
    @Xsql("XSQL_Junit_QuerySQLValue_Double")
    public double querydouble();
    
    
    
    @Xsql("XSQL_Junit_QuerySQLValue_Double")
    public Float queryFloat();
    
    @Xsql("XSQL_Junit_QuerySQLValue_Double")
    public float queryfloat();
    
    
    
    @Xsql("XSQL_Junit_QuerySQLValue_String")
    public String queryString();
    
    
    
    @Xsql("XSQL_Junit_QuerySQLValue_Date")
    public Date queryDate();
    
    @Xsql("XSQL_Junit_QuerySQLValue_Date")
    public java.util.Date queryDateJava();
    
    
    
    @Xsql("XSQL_Junit_QuerySQLValue_BigDecimal")
    public BigDecimal queryBigDecimal();
    
}
