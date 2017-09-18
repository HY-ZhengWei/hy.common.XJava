package org.hy.common.xml;

import org.hy.common.db.DataSourceGroup;





/**
 * 数据库连接的域。
 * 
 * 实现相同数据库结构下的，多个数据库间的分域功能。
 * 
 * 多个数据库间的相同SQL语句，不用重复写多次，只须通过"分域"动态改变数据库连接池组即可。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-09-18
 * @version     v1.0
 */
public interface XSQLDomain
{
    
    public DataSourceGroup getDataSourceGroup();
    
}
