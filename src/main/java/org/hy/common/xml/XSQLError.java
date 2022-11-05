package org.hy.common.xml;





/**
 * XSQL异常处理机制。
 * 
 * 异常发生时，先回滚、关闭数据库连接后，此类中的方法才会被调用。
 * 
 * 常规情况下，XSQL是Java代码是执行的，自然就有Java原生的异常处理机制Exception。
 *           所以在常规情况下，是不需此类出马的。
 * 
 * 应用场景1：XSQL触发器中，每个触发器均能独立处理异常的能力。
 * 应用场景2：XSQL组中，对重点XSQL异常信息的业务层面的特殊处理。常规情况下XSQL组自身也是有异常处理机制的。
 * 应用场景3：对所有XSQL异常信息的统一处理，如记录在日志文件中。
 *          具体实现方法是：在构建XSQL实例前，定义一个xid = XSQL.$XSQLErrors 的XJava对象，并放在XJava对象池中。
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-02-18
 * @version     v1.0
 */
public interface XSQLError
{
    
    /**
     * 异常处理
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-02-18
     * @version     v1.0
     *
     * @param i_Error   异常信息
     */
    public void errorLog(XSQLErrorInfo i_Error);
    
}
