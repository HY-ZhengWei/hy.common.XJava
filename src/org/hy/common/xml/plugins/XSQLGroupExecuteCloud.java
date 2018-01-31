package org.hy.common.xml.plugins;

import java.util.Map;





/**
 * XSQL组执行云服务计算的Java代码的执行方法的定义模板。
 * 
 * 此接口不用在实体类上实现implements，只须方法的定义样式一样即可（方法入参一样、方法返回一样，方法名称可自行定义）。
 * 
 * 此接口只是用于查看的。不强制要求使用者实现implements接口的原因是：
 *   1. 允许Java类是定义多个XSQL组执行Java的方法。
 *   2. 允许使用者自行定义方法名称，表示更好的含义。
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-01-30
 * @version     v1.0
 */
public interface XSQLGroupExecuteCloud
{
    
    /**
     * XSQL组执行云服务计算的方法接口
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-30
     * @version     v1.0
     *
     * @param io_Params   执行或查询参数。
     * @return            表示是否执行成功。当返回false时，其后的XSQLNode节点将不再执行。
     */
    public boolean executeCloud_XSQLNode(Map<String ,Object> io_Params);
    
}
