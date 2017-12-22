package org.hy.common.xml.plugins;

import java.util.Map;





/**
 * XSQL组执行Java代码的执行方法的定义模板。
 * 
 * 此接口不用在实体类上实现implements，只须方法的定义样式一样即可（方法入参一样、方法返回一样，方法名称可自行定义）。
 * 
 * 此接口只是用于查看的。不强制要求使用者实现implements接口的原因是：
 *   1. 允许DAO类是定义多个XSQL组执行Java的方法。
 *   2. 允许使用者自行定义方法名称，表示更好的含义。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-22
 * @version     v1.0
 */
public interface XSQLGroupExecuteJava
{
    
    /**
     * XSQL组执行Java的方法接口
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-22
     * @version     v1.0
     *
     * @param i_Control   XSQL组的控制中心。如，统一事务提交、统一事务回滚。
     * @param io_Params   执行或查询参数。
     * @param io_Returns  通过returnID标记的，返回出去的多个查询结果集。
     * @return            表示是否执行成功。当返回false时，其后的XSQLNode节点将不再执行。
     */
    public boolean executeJava_XSQLNode(XSQLGroupControl i_Control ,Map<String ,Object> io_Params ,Map<String ,Object> io_Returns);
    
}
