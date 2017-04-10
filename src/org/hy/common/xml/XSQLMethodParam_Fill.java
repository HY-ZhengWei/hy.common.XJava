package org.hy.common.xml;

import java.lang.reflect.Method;





/**
 * 解释 fill 字符串后生成的方法的参数信息
 * 
 * 这样只须解释一次，在后面的行级填充动作时，可快速填充，而不用每次都解释 fill 字符串。
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-01
 */
public final class XSQLMethodParam_Fill
{
	/** 行级填充时的常理之一：填充行级对象 */
	public final static int    $FILL_ROW              = 1;
	
	/** 行级填充时的常理之一：填充行号 */
	public final static int    $FILL_ROW_NO           = 2;
	
	/** 
	 * 行级填充时的常理之一：填充行级对象某一属性值的引用
	 * 如：put(row.serialNo ,row) 
	 */
	public final static int    $FILL_ROW_GETTER       = 3;
	
	
	private final static XSQLMethodParam $FILL_PARAM_ROW    = new XSQLMethodParam_Fill_Row();
	
	private final static XSQLMethodParam $FILL_PARAM_ROW_NO = new XSQLMethodParam_Fill_RowNo();
	
	
	
	/**
	 * 获取参数信息的实例
	 * 
	 * @param i_ParamType
	 * @return
	 */
	public static XSQLMethodParam getInstance(int i_ParamType)
	{
		if ( i_ParamType == $FILL_ROW || i_ParamType == $FILL_ROW_NO )
		{
			return $FILL_PARAM_ROW;
		}
		else if ( i_ParamType == $FILL_ROW_NO )
		{
			return $FILL_PARAM_ROW_NO;
		}
		else
		{
			throw new InstantiationError("Param type is error.");
		}
	}
	
	
	
	/**
	 * 获取参数信息的实例
	 * 
	 * @param i_ParamType
	 * @param i_ObjGetter
	 * @return
	 */
	public static XSQLMethodParam getInstance(int i_ParamType ,Method i_ObjGetter)
	{
		if ( i_ParamType == $FILL_ROW || i_ParamType == $FILL_ROW_NO )
		{
			return $FILL_PARAM_ROW;
		}
		else if ( i_ParamType == $FILL_ROW_NO )
		{
			return $FILL_PARAM_ROW_NO;
		}
		else if ( i_ParamType == $FILL_ROW_GETTER )
		{
			if ( i_ObjGetter != null )
			{
				return new XSQLMethodParam_Fill_Row_Getter(i_ObjGetter);
			}
			else
			{
				throw new NullPointerException("Object Getter is null.");
			}
		}
		else
		{
			throw new InstantiationError("Param type is error.");
		}
	}
	
	
	
	/**
	 * 不允许被构造
	 */
	private XSQLMethodParam_Fill()
	{
		
	}
	
}





/**
 * 行级对象为入参参数的情况。
 * 
 * 解释 fill 字符串后生成的方法的参数信息
 * 
 * 这样只须解释一次，在后面的行级填充动作时，可快速填充，而不用每次都解释 fill 字符串。
 * 
 * 独立出本类是为：减少填充数据时 if 语句的判断，改用预先解释好，填充数据时直接调用相关实例化的类，来提高性能
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-08
 */
class XSQLMethodParam_Fill_Row implements XSQLMethodParam
{
	
	/** 行级填充时的常理类型 */
	private int                paramType;
	
	
	
	public XSQLMethodParam_Fill_Row()
	{
		this.paramType = XSQLMethodParam_Fill.$FILL_ROW;
	}
	
	
	
	/**
	 * 执行后，得到将"子级"对象填充到"父级"对象中的父级填充方法的参数值
	 * 如：行级对象填充到表级对象
	 * 如：列级数值填充表行级对象
	 * 
	 * @param i_Row        填充对象。     可以行级对象 或 列级字段值
	 * @param i_RowNo      填充对象的编号。当为行级对象时，为行号。  下标从 0 开始。
	 *                                  当为列级字段值时，为列号  下标从 0 开始。
	 * @param i_RowName    填充对象的名称。当为行级对象时，可为空 null
	 *                                  当为列级字段值时，为字段名称
	 * @return             返回父级填充方法实际的入参数值
	 */
	public Object invoke(Object i_Row ,long i_RowNo ,String i_RowName)
	{
		return i_Row;
	}
	
	
	
	public Method getObjGetter() 
	{
		return null;
	}
	
	
	
	public int getParamType() 
	{
		return this.paramType;
	}
	
}





/**
 * 行号为入参参数的情况。
 * 
 * 解释 fill 字符串后生成的方法的参数信息
 * 
 * 这样只须解释一次，在后面的行级填充动作时，可快速填充，而不用每次都解释 fill 字符串。
 * 
 * 独立出本类是为：减少填充数据时 if 语句的判断，改用预先解释好，填充数据时直接调用相关实例化的类，来提高性能
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-08
 */
class XSQLMethodParam_Fill_RowNo implements XSQLMethodParam
{
	
	/** 行级填充时的常理类型 */
	private int                paramType;
	
	
	
	public XSQLMethodParam_Fill_RowNo()
	{
		this.paramType = XSQLMethodParam_Fill.$FILL_ROW_NO;
	}
	
	
	
	/**
	 * 执行后，得到将"子级"对象填充到"父级"对象中的父级填充方法的参数值
	 * 如：行级对象填充到表级对象
	 * 如：列级数值填充表行级对象
	 * 
	 * @param i_Row        填充对象。     可以行级对象 或 列级字段值
	 * @param i_RowNo      填充对象的编号。当为行级对象时，为行号。  下标从 0 开始。
	 *                                  当为列级字段值时，为列号  下标从 0 开始。
	 * @param i_RowName    填充对象的名称。当为行级对象时，可为空 null
	 *                                  当为列级字段值时，为字段名称
	 * @return             返回父级填充方法实际的入参数值
	 */
	public Object invoke(Object i_Row ,long i_RowNo ,String i_RowName)
	{
		return Long.valueOf(i_RowNo);
	}
	
	
	
	public Method getObjGetter() 
	{
		return null;
	}
	
	
	
	public int getParamType() 
	{
		return this.paramType;
	}
	
}





/**
 * 行级对象的属性作为入参参数的情况。
 * 
 * 解释 fill 字符串后生成的方法的参数信息
 * 
 * 这样只须解释一次，在后面的行级填充动作时，可快速填充，而不用每次都解释 fill 字符串。
 * 
 * 独立出本类是为：减少填充数据时 if 语句的判断，改用预先解释好，填充数据时直接调用相关实例化的类，来提高性能
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-08
 */
class XSQLMethodParam_Fill_Row_Getter implements XSQLMethodParam
{
	
	/** 行级填充时的常理类型 */
	private int                paramType;
	
	/** 当 paramType = FILL_ROW_GETTER 时，此属性才有值 */
	private Method             objGetter;
	
	
	
	public XSQLMethodParam_Fill_Row_Getter(Method i_ObjGetter)
	{
		this.paramType = XSQLMethodParam_Fill.$FILL_ROW_GETTER;
		
		if ( i_ObjGetter != null )
		{
			this.objGetter = i_ObjGetter;
		}
		else
		{
			throw new NullPointerException("Object Getter is null.");
		}
	}
	
	
	
	/**
	 * 执行后，得到将"子级"对象填充到"父级"对象中的父级填充方法的参数值
	 * 如：行级对象填充到表级对象
	 * 如：列级数值填充表行级对象
	 * 
	 * @param i_Row        填充对象。     可以行级对象 或 列级字段值
	 * @param i_RowNo      填充对象的编号。当为行级对象时，为行号。  下标从 0 开始。
	 *                                  当为列级字段值时，为列号  下标从 0 开始。
	 * @param i_RowName    填充对象的名称。当为行级对象时，可为空 null
	 *                                  当为列级字段值时，为字段名称
	 * @return             返回父级填充方法实际的入参数值
	 */
	public Object invoke(Object i_Row ,long i_RowNo ,String i_RowName)
	{
		
		try
		{
			return this.objGetter.invoke(i_Row);
		}
		catch (Exception exce)
		{
			throw new NoSuchMethodError(exce.getMessage());
		}
	}
	
	
	
	public Method getObjGetter() 
	{
		return this.objGetter;
	}
	
	
	
	public int getParamType() 
	{
		return this.paramType;
	}
	
}
