package org.hy.common.xml.event;

import org.hy.common.Date;





/**
 * BLob数据传送动作(XSQL)的事件的默认实现
 * 
 * 此类中可以有 setter 方法，主要用于内部。
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-03-21
 */
public class DefaultBLobEvent extends BLobEvent 
{
	
	private static final long serialVersionUID = -5977531975224412040L;



	public DefaultBLobEvent(Object i_Source) 
	{
		super(i_Source);
	}
	
	
	
	public DefaultBLobEvent(Object i_Source ,long i_FileSize) 
	{
		super(i_Source ,i_FileSize);
	}

	
	
	public void setBeginTime(Date i_BeginTime)
	{
		this.beginTime = i_BeginTime;
	}
	
	
	
	public void setBeginTime()
	{
		this.setBeginTime(new Date());
	}
	
	
	
	public void setEndTime(Date i_EndTime) 
	{
		this.endTime = i_EndTime;
	}
	
	
	
	public void setEndTime()
	{
		this.setEndTime(new Date());
	}
	
	
	
	public void setSize(long i_Size)
	{
		this.size = i_Size;
	}
	
	
	
	public void setCompleteSize(long i_CompleteSize) 
	{
		this.completedSize = i_CompleteSize;
	}
	
	
	
	public void setActionType(int i_ActionType)
	{
		this.actionType = i_ActionType;
	}
	
	
	
	/**
	 * 成功完成
	 */
	public void setSucceedFinish()
	{
		this.setEndTime();
		this.setCompleteSize(this.getSize());
	}
	
}
