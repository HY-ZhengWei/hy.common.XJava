package org.hy.common.xml;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.hy.common.db.DBNameStyle;
import org.hy.common.db.DBTableMetaData;
import org.hy.common.file.FileBiggerMemory;





/**
 * 超级大结果集的存储器处理类
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-12
 */
public final class XSQLBigger implements FileBiggerMemory
{
	/** 总记录数 */
	private long             rowSize;
	
	/** 数据库结果集 */
	private ResultSet        resultSet;
	
	/** 结果集的元数据 */
	private DBTableMetaData  dbMetaData;  
	
	/** 解释Xml文件，执行占位符SQL，再分析数据库结果集转化为Java实例对象 */
	private XSQL             xsql;
	
	/** 每次返回的记录数 */
	private int              perSize;
	
	/**
	 * 获取存储器中保存的记录数。即，从数据库中已读取的记录数。
	 * 
	 * 此数与“总记录数”是两个不同的概念。
	 */
	private long             memorySize;
	
	/** 超级大结果集的存储器接口 */
	private XSQLBiggerMemory biggerMemory;
	
	/** 
	 * 将数据库结果集转化为Java实例对象的用时时长(单位：毫秒)
	 * 
	 * 此处与 XSQLResult.getDatasTimes 含义大体相同，
	 * 但，对于超级大结果集的情况，做了时间累加计算。
	 */
	private long             getDatasTimes;
	
	
	
	/**
	 * 
	 * @param i_XSQL
	 * @param i_ResultSet
	 * @param i_RowSize
	 * @param i_QueryCountTimes   查询 Select Count(1) From (...) 语句所用的时间
	 */
	public XSQLBigger(XSQL i_XSQL ,ResultSet i_ResultSet ,long i_RowSize ,long i_QueryCountTimes)
	{
		if ( i_XSQL == null )
		{
			throw new NullPointerException("XSQL is null of XSQLBigger.");
		}
		
		if ( i_ResultSet == null )
		{
			throw new NullPointerException("ResultSet is null of XSQLBigger.");
		}
		
		if ( i_QueryCountTimes < 0 )
		{
			throw new NullPointerException("QueryCountTimes min value is 0 of XSQLBigger.");
		}
		
		
		this.rowSize       = i_RowSize;
		this.resultSet     = i_ResultSet;
		this.xsql          = i_XSQL;
		this.perSize       = 32;
		this.memorySize    = 0;
		this.biggerMemory  = null;
		this.getDatasTimes = i_QueryCountTimes;
		this.dbMetaData    = new DBTableMetaData(DBNameStyle.$Upper);
		
		try
		{
			this.dbMetaData.set(this.resultSet.getMetaData());
		}
		catch (Exception exce)
		{
			throw new RuntimeException(exce.getMessage());
		}
	}
	
	
	
	/**
	 * 获取总记录数
	 * 
	 * @return
	 */
	public long getRowSize()
	{
		return this.rowSize;
	}
	
	
	
	/**
	 * 获取数据库结果集
	 * 
	 * @return
	 */
	public ResultSet getResultSet()
	{
		return this.resultSet;
	}
	
	
	
	/**
	 * 获取超大结果集的下一页数据。
	 * 
	 * 再调用此方法前，应当已调用过 queryBigger(...) 这样的方法。
	 * 
	 * @return
	 */
	public Object getBiggerNextPage()
	{
		if ( this.biggerMemory == null )
		{
			throw new NullPointerException("XSQLBiggerMemory is null of XSQLBigger.");
		}
		
		if ( !this.isBiggerNextPage() )
		{
			return this.biggerMemory.getMemory();
		}
		
		
		XSQLData v_Ret = this.xsql.getResult().getDatasNextPage(this);
		
		this.getDatasTimes += v_Ret.getTimeLen();
		this.memorySize     = v_Ret.getRowCount();
		
		return v_Ret.getDatas();
	}
	
	
	
	/**
	 * 获取超大结果集的下一页数据。
	 * 
	 * 再调用此方法前，应当已调用过 queryBigger(...) 这样的方法。
	 * 
	 * 主要用于 FileBiggerMemory 和 FileSerializable 功能中。
	 * 
	 * 注意：1. 此方法直接返回下一行的行级数据，而不是表级数据。
	 *      2. 并且，生成的行级数据不向表级数据填充。
	 *      3. 此方法不再统计 getDatasTimes 时长。一行数据，没有什么好统计的。
	 * 
	 * @return
	 */
	public Object getBiggerNextRow()
	{
		if ( this.biggerMemory == null )
		{
			throw new NullPointerException("XSQLBiggerMemory is null of XSQLBigger.");
		}
		
		if ( this.memorySize >= this.rowSize )
		{
			return null;
		}
		
		
		Object v_Ret = this.xsql.getResult().getDatasNextRow(this);
		
		if ( v_Ret != null )
		{
			this.memorySize++;
		}
		
		return v_Ret; 
	}
	
	
	
	/**
	 * 获取一行的数据信息
	 * 
	 * 其返回值的类型只能是如下几种，否则会抛异常
	 * 第一种：org.hy.common.file.FileSerializable
	 * 第二种：java.util.List
	 * 
	 * @param i_RowIndex  行号。下标从0开始
	 * @return
	 */
	public Object getRowInfo(long i_RowIndex) throws Exception
	{	
		return this.getBiggerNextRow();
	}
	
	
	
	/**
	 * 判断是否下一页是否还有数据
	 * 
	 * @return
	 */
	public boolean isBiggerNextPage()
	{
		return this.rowSize > 0 && this.memorySize < this.rowSize;
	}
	
	
	
	/**
	 * 获取每次返回的记录数，即每次调用 setRowInfo(...) 的次数。
	 * 
	 * 最小值应当为 1。
	 * 
	 * 如果小于 1 就不调用 setRowInfo(...) 方法。
	 * 
	 * @return
	 */
	public int getPerSize()
	{
		return this.perSize;
	}
	
	
	
	/**
	 * 获取存储器中保存的记录数。即，从数据库中已读取的记录数。
	 * 
	 * 此数与“总记录数”是两个不同的概念。
	 * 
	 * @return
	 */
	public long getMemorySize()
	{
		return this.memorySize;
	}
	
	
	
	/**
	 * 设置每次返回的记录数
	 * 
	 * @param i_PerSize
	 */
	public void setPerSize(int i_PerSize)
	{
		if ( i_PerSize <= 0 )
		{
			throw new IndexOutOfBoundsException("PerSize min value is 1.");
		}
		
		this.perSize = i_PerSize;
	}
	
	
	
	/**
	 * 将数据库结果集转化为Java实例对象的用时时长(单位：毫秒)
	 * 
	 * 此处与 XSQLResult.getDatasTimes 含义大体相同，
	 * 但，对于超级大结果集的情况，做了时间累加计算。
	 * 
	 * 返回 -1 表示异常
	 * 
	 * @return
	 */
	public long getDatasTimes()
	{
		return this.getDatasTimes;
	}
	
	
	
	/**
	 * 结果集的元数据
	 * 
	 * @return
	 */
	public DBTableMetaData getMetaData() 
	{
		return dbMetaData;
	}
	
	
	
	/**
	 * 关闭结果集
	 */
	public void close()
	{
		if ( this.resultSet != null )
		{
			Connection v_Conn      = null;
			Statement  v_Statement = null;
			
			try
			{
				v_Statement = this.resultSet.getStatement();
				if ( v_Statement != null )
				{
				    v_Conn = v_Statement.getConnection();
				}
			}
			catch (Exception exce)
			{
				throw new RuntimeException(exce.getMessage());
			}
			
			
		   	try
	    	{
		    	if ( this.resultSet != null )
			    {
		    		this.resultSet.close();
		    		this.resultSet = null;
			    }
	    	}
	    	catch (Exception exce)
	    	{
	    		throw new RuntimeException(exce.getMessage());
	    	}
	    	finally
	    	{
	    		this.resultSet = null;
	    	}
	    	
	    	
	    	try
	    	{
		    	if ( v_Statement != null )
			    {
			    	v_Statement.close();
			    	v_Statement = null;
			    }
	    	}
	    	catch (Exception exce)
	    	{
	    		throw new RuntimeException(exce.getMessage());
	    	}
	    	finally
	    	{
	    		v_Statement = null;
	    	}
	    	
	    	
	    	try
	    	{
		    	if ( v_Conn != null )
			    {
		    		v_Conn.close();
		    		v_Conn = null;
			    }
	    	}
	    	catch (Exception exce)
	    	{
	    		throw new RuntimeException(exce.getMessage());
	    	}
	    	finally
	    	{
	    		v_Conn = null;
	    	}
		}
	}
	
	
	
	public XSQLBiggerMemory getBiggerMemory() 
	{
		return biggerMemory;
	}



	public void setBiggerMemory(XSQLBiggerMemory<?> biggerMemory) 
	{
		this.biggerMemory = biggerMemory;
	}



	/*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
	protected void finalize() throws Throwable 
	{
		this.close();
		
		super.finalize();
	}
	*/
	
}
