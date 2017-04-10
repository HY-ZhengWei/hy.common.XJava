package org.hy.common.xml;





/**
 * 超级大结果集的存储器接口
 * 
 * <MR>  表示行级对象的类型
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-12
 */
public interface XSQLBiggerMemory<MR>
{
		
	/**
	 * 设置每一行的数据
	 * 
	 * @param i_RowNo    行号。下标从 1 开始
	 * @param i_RowSize  总记录数
	 * @param i_RowInfo  行级对象
	 */
	public void setRowInfo(long i_RowNo ,long i_RowSize ,MR i_RowInfo);
	
	
	
	/**
	 * 获取存储器对象。即，表级对象
	 * 
	 * @return
	 */
	public Object getMemory();
	
}
