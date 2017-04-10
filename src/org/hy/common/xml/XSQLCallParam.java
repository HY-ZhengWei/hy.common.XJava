package org.hy.common.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.util.List;

import org.hy.common.Date;
import org.hy.common.InterconnectMap;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.StaticReflect;





/**
 * 当调用存储过程或函数时，相关方法及参数对象
 * 
 * 1. 什么是 JDBCTypeID ?
 *    答：java.sql.Types 是定义的常量值。
 *  
 * 2. 什么是 JDBCType Method Name ?
 *    答：java.sql.CallableStatement.getXXX(int) 方法中 xxx 的部分。
 *    
 * 3. 什么是 JDBCTypeName ?
 *    答：当为输入型参数时，JDBCTypeName 为 java.sql.CallableStatement.setXXX(int ,?) 方法中的 xxx 的部分。
 *       当为输出型参数时，JDBCTypeName 为 "java.sql.Types.VARCHAR" 这样的字符串。
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2013-08-01
 */
public final class XSQLCallParam
{
    /** 输入型参数(默认值) */
    public static final String                             $Type_IN     = "IN";
    
    /** 输出型参数 */
    public static final String                             $Type_OUT    = "OUT";
    
    /** 输入、输出型参数 */
    public static final String                             $Type_IN_OUT = "IN OUT";
    
    /** JDBCType ID 与 Statement.getXXX() 方法名称的映射关系 */
    private static      InterconnectMap<Integer ,String>   $Mapping_JDBCType_IDToMethodName;
    
    
    /** 参数名称 */
    private String name;
    
    /** 
     * JDBC类型名称
     * 
     * 1. 当为输入类型参数时，jdbcType = string ，转为 setParamMethod = CallableStatement.setString(int ,String) 方法。
     * 2. 当为输出类型参数时，jdbcType = java.sql.Types.VARCHAR ，转为 jdbcTypeID = 12 。
     */
    private String jdbcType;
    
    /** 
     * JDBC类型ID
     * 
     * 当为输出类型的参数时有效
     * 
     *  参见 java.sql.Types
     */
    private int    jdbcTypeID;
    
    /** 参数类型(IN / OUT) */
    private String type;
    
    /** 
     * 设置入参参数的方法
     * 
     * 当为输入类型的参数时有效
     */
    private Method setParamMethod;
    
    /** 
     * 获取输出参数的方法
     * 
     * 当为输出类型的参数时有效
     */
    private Method getParamMethod;
    
    
    
    public XSQLCallParam()
    {
        this.type       = $Type_IN;
        this.jdbcTypeID = Integer.MIN_VALUE;
    }
    
    
    
    /**
     * 通过 java.sql.Types.VARCHAR 获取JDBCTypeID。
     * 
     * @return
     */
    public synchronized int getJdbcTypeID()
    {
        if ( !this.isOutType() )
        {
            throw new IllegalArgumentException("Call parameter type is not 'OUT' or 'IN OUT'.");
        }
        
        if ( this.jdbcTypeID == Integer.MIN_VALUE )
        {
            StaticReflect v_StaticReflect = null;
            
            try
            {
                v_StaticReflect = new StaticReflect(this.jdbcType);
            }
            catch (Exception exce)
            {
                throw new NullPointerException("Call parameter JDBC type is not vaild.");
            }
            
            this.jdbcType   = v_StaticReflect.getStaticURL();   // 规范化URL
            this.jdbcTypeID = Integer.parseInt(v_StaticReflect.toString());
        }
        
        return this.jdbcTypeID;
    }
    
    
    
    /**
     * 当调用存储过程或函数时，设置入参参数。
     * 
     * @param i_Callable    数据库连接对象
     * @param i_ParamIndex  参数位置。下标从 1 开始。
     * @param i_ParamValue  参数值
     * @throws NoSuchMethodException
     */
    public synchronized void setValue(CallableStatement i_Callable ,int i_ParamIndex ,Object i_ParamValue) throws NoSuchMethodException
    {
        if ( !this.isInType() )
        {
            throw new IllegalArgumentException("Call parameter type is not 'IN' or 'IN OUT'.");
        }
        
        if ( this.setParamMethod == null )
        {
            this.setParamMethod = getSetterMethod(this.jdbcType);
        }
        
        setValue(i_Callable , this.setParamMethod ,i_ParamIndex ,i_ParamValue);
    }
    
    
    
    /**
     * 当调用存储过程或函数时，获取输出的值。
     * 
     * @param i_Callable      数据库连接对象
     * @param i_ParamIndex    参数位置。下标从 1 开始。
     * @throws NoSuchMethodException
     */
    public synchronized Object getValue(CallableStatement i_Callable ,int i_ParamIndex) throws NoSuchMethodException
    {
        if ( !this.isOutType() )
        {
            throw new IllegalArgumentException("Call parameter type is not 'OUT' or 'IN OUT'.");
        }
        
        if ( this.getParamMethod == null )
        {
            this.getParamMethod = getGetterMethod(getJDBCTypeMethodName(this.getJdbcTypeID()));
        }
        
        return getValue(i_Callable ,this.getParamMethod ,i_ParamIndex);
    }
    
    
    
    /**
     * 通过 JDBCTypeID 获取 Statement.getXXX() 的方法名称
     * 
     * @param i_JDBCTypeID
     * @return
     */
    public synchronized static String getJDBCTypeMethodName(int i_JDBCTypeID)
    {
        if ( $Mapping_JDBCType_IDToMethodName == null )
        {
            initJDBCTypeMethodName();
        }
        
        return $Mapping_JDBCType_IDToMethodName.get(i_JDBCTypeID);
    }
    
    
    
    /**
     * 初始化 JDBCType ID 与 Statement.getXXX() 方法名称的映射关系
     * 
     * 懒加载方式
     */
    private static void initJDBCTypeMethodName()
    {
        $Mapping_JDBCType_IDToMethodName = new InterconnectMap<Integer ,String>();
        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.TINYINT             ,"Short");
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.SMALLINT            ,"Short");
        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.INTEGER             ,"Int");
        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.BIGINT              ,"Long");
                                                                                          
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.FLOAT               ,"Float");
        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.REAL                ,"Double");
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.DOUBLE              ,"Double");
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.NUMERIC             ,"Double");
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.DECIMAL             ,"Double");
                                                                                         
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.CHAR                ,"String"); 
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.VARCHAR             ,"String"); 
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.LONGVARCHAR         ,"String"); 
                                                                                        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.NCHAR               ,"NString"); 
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.NVARCHAR            ,"NString");
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.LONGNVARCHAR        ,"NString");
                                                                                         
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.DATE                ,"Date");
                                                                                        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.TIME                ,"Time");   
                                                                                       
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.TIMESTAMP           ,"Timestamp");
                                                                                        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.BINARY              ,"Byte");
                                                                                         
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.VARBINARY           ,"Bytes");   
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.LONGVARBINARY       ,"Bytes");  
                                                                                        
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.JAVA_OBJECT         ,"Object");  
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.STRUCT              ,"Object"); 
        $Mapping_JDBCType_IDToMethodName.put(oracle.jdbc.OracleTypes.CURSOR     ,"Object"); 
                                                                                       
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.ARRAY               ,"Array"); 
                                                                                       
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.BLOB                ,"Blob");  
                                                                                       
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.CLOB                ,"Clob");   
                                                                                       
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.NCLOB               ,"NClob");
                                                                                  
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.REF                 ,"Ref");
                                                                                  
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.BOOLEAN             ,"Boolean"); 
                                                                                
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.ROWID               ,"RowId");
                                                                                    
        $Mapping_JDBCType_IDToMethodName.put(java.sql.Types.SQLXML              ,"SQLXML");
    }
    
    
    
    /**
     * 当调用存储过程或函数时，设置入参参数。
     * 
     * @param i_Callable      数据库连接对象
     * @param i_JDBCTypeName  JDBC类型名称
     * @param i_ParamIndex    参数位置。下标从 1 开始。
     * @param i_ParamValue    参数值
     * @throws NoSuchMethodException
     */
    public static void setValue(CallableStatement i_Callable ,String i_JDBCTypeName ,int i_ParamIndex ,Object i_ParamValue) throws NoSuchMethodException
    {
        if ( i_Callable == null )
        {
            throw new NullPointerException("CallableStatement is null.");
        }
        
        setValue(i_Callable ,getSetterMethod(i_JDBCTypeName) ,i_ParamIndex ,i_ParamValue);
    }
    
    
    
    /**
     * 当调用存储过程或函数时，设置入参参数。
     * 
     * @param i_Callable        数据库连接对象
     * @param i_SetParamMethod  设置入参参数的方法
     * @param i_ParamIndex      参数位置。下标从 1 开始。
     * @param i_ParamValue      参数值
     * @throws NoSuchMethodException
     */
    public static void setValue(CallableStatement i_Callable ,Method i_SetParamMethod ,int i_ParamIndex ,Object i_ParamValue) throws NoSuchMethodException
    {
        if ( i_Callable == null )
        {
            throw new NullPointerException("CallableStatement is null.");
        }
        
        if ( i_SetParamMethod == null )
        {
            throw new NullPointerException("Call parameter method is null.");
        }
        
        try
        {
            if ( Date.class.equals(i_ParamValue.getClass()) )
            {
                i_SetParamMethod.invoke(i_Callable ,new Object [] {i_ParamIndex ,((Date)i_ParamValue).getSQLDate()});
            }
            else if ( java.util.Date.class.equals(i_ParamValue.getClass()) )
            {
                i_SetParamMethod.invoke(i_Callable ,new Object [] {i_ParamIndex ,(new Date((java.util.Date)i_ParamValue)).getSQLDate()});
            }
            else
            {
                i_SetParamMethod.invoke(i_Callable ,new Object [] {i_ParamIndex ,i_ParamValue});
            }
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * 当调用存储过程或函数时，获取输出的值。
     * 
     * @param i_Callable      数据库连接对象
     * @param i_JDBCTypeID    JDBC类型ID
     * @param i_ParamIndex    参数位置。下标从 1 开始。
     * @throws NoSuchMethodException
     */
    public static Object getValue(CallableStatement i_Callable ,int i_JDBCTypeID ,int i_ParamIndex) throws NoSuchMethodException
    {
        if ( i_Callable == null )
        {
            throw new NullPointerException("CallableStatement is null.");
        }
        
        return getValue(i_Callable ,getGetterMethod(getJDBCTypeMethodName(i_JDBCTypeID)) ,i_ParamIndex);
    }
    
    
    
    /**
     * 当调用存储过程或函数时，获取输出的值。
     * 
     * @param i_Callable        数据库连接对象
     * @param i_GetParamMethod  设置输出参数的方法
     * @param i_ParamIndex      参数位置。下标从 1 开始。
     * @throws NoSuchMethodException
     */
    public static Object getValue(CallableStatement i_Callable ,Method i_GetParamMethod ,int i_ParamIndex) throws NoSuchMethodException
    {
        if ( i_Callable == null )
        {
            throw new NullPointerException("CallableStatement is null.");
        }
        
        if ( i_GetParamMethod == null )
        {
            throw new NullPointerException("Call parameter method is null.");
        }
        
        try
        {
            return i_GetParamMethod.invoke(i_Callable ,new Object [] {i_ParamIndex});
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 当调用存储过程或函数时，获取设置入参参数的Java方法。
     * 
     * 1. i_SetMethodName 不区分大小
     * 2. i_SetMethodName 可加set前缀，也可不加
     * 
     * 如：i_SetMethodName = string，   获取CallableStatement.setString(int ,String);
     *    i_SetMethodName = setString，获取CallableStatement.setString(int ,String);
     * 
     * @param i_SetMethodName
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getSetterMethod(String i_SetMethodName) throws NoSuchMethodException
    {
        if ( Help.isNull(i_SetMethodName) )
        {
            throw new NullPointerException("Call parameter JDBC type is null.");
        }
        
        String v_SetMethodName = i_SetMethodName.trim().toLowerCase();
        
        if ( !v_SetMethodName.startsWith("set") )
        {
            v_SetMethodName = "set" + v_SetMethodName;
        }
        
        List<Method> v_SetterMethods = MethodReflect.getMethodsIgnoreCase(CallableStatement.class ,v_SetMethodName ,2);
        
        for (int i=0; i<v_SetterMethods.size(); i++)
        {
            if ( int.class.equals(v_SetterMethods.get(i).getParameterTypes()[0]) )
            {
                return v_SetterMethods.get(i);
            }
        }
        
        throw new NoSuchMethodException("Call parameter JDBC type[" + i_SetMethodName + "] is not exist.");
    }
    
    
    
    /**
     * 当调用存储过程或函数时，获取输出参数的Getter的Java方法。
     * 
     * 1. i_GetMethodName 不区分大小
     * 2. i_GetMethodName 可加get前缀，也可不加
     * 
     * 如：i_GetMethodName = string，   获取CallableStatement.getString(int);
     *    i_GetMethodName = getString，获取CallableStatement.getString(int);
     * 
     * @param i_GetMethodName
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getGetterMethod(String i_GetMethodName) throws NoSuchMethodException
    {
        if ( Help.isNull(i_GetMethodName) )
        {
            throw new NullPointerException("Call parameter JDBC type is null.");
        }
        
        String v_GetMethodName = i_GetMethodName.trim().toLowerCase();
        
        if ( !v_GetMethodName.startsWith("get") )
        {
            v_GetMethodName = "get" + v_GetMethodName;
        }
        
        List<Method> v_GetterMethods = MethodReflect.getMethodsIgnoreCase(CallableStatement.class ,v_GetMethodName ,1);
        
        for (int i=0; i<v_GetterMethods.size(); i++)
        {
            if ( int.class.equals(v_GetterMethods.get(i).getParameterTypes()[0]) )
            {
                return v_GetterMethods.get(i);
            }
        }
        
        throw new NoSuchMethodException("Call parameter JDBC type[" + i_GetMethodName + "] is not exist.");
    }


    
    public String getJdbcType()
    {
        return jdbcType;
    }


    
    public void setJdbcType(String i_JDBCType)
    {
        if ( Help.isNull(i_JDBCType) )
        {
            throw new NullPointerException("Call parameter JDBC type is null.");
        }
        
        this.jdbcType       = i_JDBCType.trim();
        this.jdbcTypeID     = Integer.MIN_VALUE;
        this.setParamMethod = null;
        this.getParamMethod = null;
    }


    
    public String getName()
    {
        return name;
    }


    
    public void setName(String name)
    {
        this.name = name;
    }


    
    public String getType()
    {
        return type;
    }
    
    
    
    /**
     * 是否输入类型的参数
     * 
     * @return
     */
    public boolean isInType()
    {
        return !$Type_OUT.equals(this.type);
    }
    
    
    
    /**
     * 是否输出类型的参数
     * 
     * @return
     */
    public boolean isOutType()
    {
        return !$Type_IN.equals(this.type);
    }


    
    public void setType(String i_Type) throws IllegalAccessException
    {
        if ( Help.isNull(i_Type) )
        {
            throw new NullPointerException("Call parameter type is null.");
        }
        
        this.type = i_Type.trim().toUpperCase();
        
        if ( !$Type_IN    .equals(this.type)
          && !$Type_OUT   .equals(this.type)
          && !$Type_IN_OUT.equals(this.type))
        {
            throw new java.lang.IllegalAccessException("Call parameter type is not 'IN' or 'OUT' or 'IN OUT'.");
        }
    }
    
}
