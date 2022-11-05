package org.hy.common.xml;





/**
 * 获取父级填充方法实际的入参数值的规范接口
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-03
 */
public interface XSQLMethodParam 
{

	/**
	 * 执行后，得到将"子级"对象填充到"父级"对象中的父级填充方法的参数值
	 * 如：行级对象填充到表级对象
	 * 如：列级数值填充表行级对象
	 * 
	 * @param i_Value      填充对象。     可以行级对象 或 列级字段值
	 * @param i_ValueNo    填充对象的编号。当为行级对象时，为行号。  下标从 0 开始。
	 *                                  当为列级字段值时，为列号  下标从 0 开始。
	 * @param i_ValueName  填充对象的名称。当为行级对象时，可为空 null
	 *                                  当为列级字段值时，为字段名称
	 * @return             返回父级填充方法实际的入参数值
	 */
	public Object invoke(Object i_Value ,long i_ValueNo ,String i_ValueName);
	
}
