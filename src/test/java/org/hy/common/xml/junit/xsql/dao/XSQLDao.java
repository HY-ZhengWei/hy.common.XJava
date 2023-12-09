package org.hy.common.xml.junit.xsql.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Help;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.log.Logger;





/**
 * 测试支持@Xjava的集合同一类型的整体的注入
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-03-08
 * @version     v1.0
 */
@Xjava(id="XSQLDao")
public class XSQLDao
{
    private static Logger $Logger = new Logger(XSQLDao.class ,true);
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_Integer")     // 获取具体的XID对象
    public XSQL xsqlQuerySQLValueInteger;
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_")            // XID前缀匹配为 XSQL_Junit_QuerySQLValue_ 的所有XSQL对象的集合
    public List<XSQL> allXSQLList;
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_")            // XID前缀匹配为 XSQL_Junit_QuerySQLValue_ 的所有XSQL对象的集合
    public Set<XSQL> allXSQLSet;
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_")            // XID前缀匹配为 XSQL_Junit_QuerySQLValue_ 的所有XSQL对象的集合
    public Map<String ,XSQL> allXSQLMap;
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_")            // XID前缀匹配为 XSQL_Junit_QuerySQLValue_ 的所有XSQL对象的集合
    public void setList(List<XSQL> i_List)
    {
        $Logger.info("List" + (Help.isNull(i_List) ? "注入错误" : "注入成功"));
    }
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_")            // XID前缀匹配为 XSQL_Junit_QuerySQLValue_ 的所有XSQL对象的集合
    public void setSet(Set<XSQL> i_Set)
    {
        $Logger.info("Set " + (Help.isNull(i_Set) ? "注入错误" : "注入成功"));
    }
    
    @Xjava(ref="XSQL_Junit_QuerySQLValue_")            // XID前缀匹配为 XSQL_Junit_QuerySQLValue_ 的所有XSQL对象的集合
    public void setMap(Map<String ,XSQL> i_Map)
    {
        $Logger.info("Map " + (Help.isNull(i_Map) ? "注入错误" : "注入成功"));
    }
    
    public boolean test()
    {
        return this.xsqlQuerySQLValueInteger != null
            && this.allXSQLList != null
            && this.allXSQLList.size() >= 1
            && this.allXSQLSet  != null
            && this.allXSQLSet .size() >= 1
            && this.allXSQLMap  != null
            && this.allXSQLMap .size() >= 1;
    }
    
    
    
    
}
