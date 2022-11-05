package org.hy.common.xml.event;

import java.util.EventListener;





/**
 * BLob数据传送动作(XSQL)的事件监听器接口
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-03-21
 */
public interface BLobListener extends EventListener 
{
	
	/**
	 * 传送文件之前
	 * 
	 * @param e
	 * @return   返回值表示是否继续传送
	 */
	public boolean blobBefore(BLobEvent e);
	
	

	/**
	 * 传送文件进度
	 * 
	 * @param e
	 * @return   返回值表示是否继续传送
	 */
	public boolean blobProcess(BLobEvent e);
	
	
	
	/**
	 * 传送文件完成之后
	 * 
	 * @param e
	 */
	public void blobAfter(BLobEvent e);
	
}
