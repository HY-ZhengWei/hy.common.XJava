package org.hy.common.xml.event;

import java.util.EventListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.Return;
import org.hy.common.xml.plugins.AppMessage;





/**
 * Web请求接口 @XRequest 的事件监监听器接口
 * 
 * <O>  实现方通过 this.before() 方法的返回，自定的数据对象。之后的事件方法均携带此对象。
 *      目的是方便实现方在事件机制的多个方法间中 "连续" 处理数据
 * 
 * @author      ZhengWei(HY)
 * @createDate  2023-08-16
 * @version     v1.0
 */
public interface XRequestListener extends EventListener 
{
	
	/**
     * 在执行前一刻被触发。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-08-16
     * @version     v1.0
     *
     * @param i_Request      请求对象
     * @param i_Response     响应对象
     * @param io_RequestData 请求数据
     * @param i_Message      请求数据的原始报文
	 * @return               当返回 false 时，中断 "执行"。
	 *                       当返回 true  时，执行接口调用
	 */
	public Return<Object> before(HttpServletRequest  i_Request
			                    ,HttpServletResponse i_Response 
			                    ,AppMessage<?>       io_RequestData 
			                    ,String              i_Message);
	
	
	
	/**
     * 执行成功后被触发。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-08-16
     * @version     v1.0
     *
     * @param i_RequestData   请求数据
     * @param i_ResponseData  响应数据
     * @param i_Other         用户自定义的数据
	 */
	public void succeed(AppMessage<?> i_RequestData ,AppMessage<?> i_ResponseData ,Object i_Other);
	
	
	
	/**
     * 执行异常后被触发。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-08-16
     * @version     v1.0
     *
     * @param i_RequestData   请求数据
     * @param i_Exception     异常数据
     * @param i_Other         用户自定义的数据
	 */
	public void fail(AppMessage<?> i_RequestData ,Exception i_Exception ,Object i_Other);
}
