package org.hy.common.xml;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.db.DataSourceGroup;





/**
 * XSQL功能中存储过程、函数的具体操作与实现。
 * 
 * 独立原因：从XSQL主类中分离的主要原因是：减少XSQL主类的代码量，方便维护。使XSQL主类向外提供统一的操作，本类重点关注实现。
 * 静态原因：用static方法的原因：不想再构建太多的类实例，减少内存负担
 * 接口选择：未使用接口的原因：本类的每个方法的首个入参都有一个XSQL类型，并且都是static方法
 * 
 * @author      ZhengWei(HY)
 * @createDate  2022-05-25
 * @version     v1.0
 */
public class XSQLOPProcedure
{
    
    /**
     * 调用存储过程或函数 -- 简单型
     * 
     * 1. 调用对象无输入参数
     * 2. 调用对象无输出参数(函数自身返回除外)
     * 3. 存储过程返回Boolean类型，表示是否执行成功
     * 4. 函数返回返回值类型，但返回值类型必须是Integer、Varchar中的一种，其它类型出错。或请您使用高级的call方法。
     * 
     * @param i_SQLCallName      存储过程或函数的名称
     */
    public static Object call(final XSQL i_XSQL ,final String i_SQLCallName)
    {
        DataSourceGroup   v_DSG       = null;
        Connection        v_Conn      = null;
        CallableStatement v_Statement = null;
        long              v_BeginTime = i_XSQL.request().getTime();
        boolean           v_IsError   = false;
        
        try
        {
            if ( !XSQL.$Type_Procedure.equals(i_XSQL.getType()) && !XSQL.$Type_Function.equals(i_XSQL.getType()) )
            {
                throw new IllegalArgumentException("Type is not 'P' or 'F' of XSQL.");
            }
            
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + v_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQLCallName) )
            {
                throw new NullPointerException("SQLCallName is null of XSQL.");
            }
            
            v_Conn = i_XSQL.getConnection(v_DSG);
            if ( XSQL.$Type_Procedure.equals(i_XSQL.getType()) )
            {
                v_Statement = v_Conn.prepareCall("{call " + i_SQLCallName + "()}");
                
                v_Statement.execute();
                i_XSQL.log("{call " + i_SQLCallName + "()}");
                Date v_EndTime = Date.getNowTime();
                i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                
                return true;
            }
            else
            {
                int v_RetType = -1;
                v_Statement = v_Conn.prepareCall("{? = call " + i_SQLCallName + "()}");
                
                try
                {
                    v_Statement.registerOutParameter(1 ,java.sql.Types.INTEGER);
                    v_Statement.execute();
                    v_RetType = 1;
                }
                catch (Exception exce)
                {
                    v_Statement.registerOutParameter(1 ,java.sql.Types.VARCHAR);
                    v_Statement.execute();
                    v_RetType = 2;
                }
                
                i_XSQL.log("{call " + i_SQLCallName + "()}");
                Date v_EndTime = Date.getNowTime();
                i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                
                if ( v_RetType == 1 )
                {
                    return v_Statement.getInt(1);
                }
                else if ( v_RetType == 2 )
                {
                    return v_Statement.getString(1);
                }
                else
                {
                    return null;
                }
            }
        }
        catch (Exception exce)
        {
            v_IsError = true;
            XSQL.erroring("{call " + i_SQLCallName + "()}" ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(null ,v_Statement ,v_Conn);
            
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 无输入参数型
     * 
     * 1. 支持游标输出
     * 2. 支持多个输出参数
     * 
     * @return  1. 当调用对象为存储过程时，当无输出参数时，返回是否执行成功(true、false)
     *          2. 当调用对象为存储过程时，当有一个输出类型时，返回输出值。
     *                                如果输出类型为游标，则可以按XJava的概念直接转为实例对象。
     *          3. 当调用对象为存储过程时，当有多个输出类型时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                                如果输出类型为游标，则每个游标都按XJava的概念直接转为实例对象。
     *          4. 当调用函数时，当无输出参数时，返回函数的返回值。
     *          5. 当调用函数时，除自身返回值外还有一个或多个输出参数时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                        List集合的首个元素为函数自身的返回值。
     */
    public static Object call(final XSQL i_XSQL)
    {
        return XSQLOPProcedure.call(i_XSQL ,(Object)null);
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 输入参数为XJava对象
     * 
     * 1. 支持游标输出
     * 2. 支持多个输出参数
     * 
     * @param i_SQLCallName      存储过程或函数的名称
     * @param i_ParamObj         入参参数对象
     * 
     * @return  1. 当调用对象为存储过程时，当无输出参数时，返回是否执行成功(true、false)
     *          2. 当调用对象为存储过程时，当有一个输出类型时，返回输出值。
     *                                如果输出类型为游标，则可以按XJava的概念直接转为实例对象。
     *          3. 当调用对象为存储过程时，当有多个输出类型时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                                如果输出类型为游标，则每个游标都按XJava的概念直接转为实例对象。
     *          4. 当调用函数时，当无输出参数时，返回函数的返回值。
     *          5. 当调用函数时，除自身返回值外还有一个或多个输出参数时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                        List集合的首个元素为函数自身的返回值。
     */
    @SuppressWarnings("resource")
    public static Object call(final XSQL i_XSQL ,final Object i_ParamObj)
    {
        DataSourceGroup   v_DSG            = null;
        Connection        v_Conn           = null;
        CallableStatement v_Statement      = null;
        ResultSet         v_Resultset      = null;
        List<Integer>     v_OutParamIndexs = new ArrayList<Integer>();
        long              v_BeginTime      = i_XSQL.request().getTime();
        boolean           v_IsError        = false;
        StringBuilder     v_Buffer         = new StringBuilder();
        
        try
        {
            if ( !XSQL.$Type_Procedure.equals(i_XSQL.getType()) && !XSQL.$Type_Function.equals(i_XSQL.getType()) )
            {
                throw new IllegalArgumentException("Type is not 'P' or 'F' of XSQL.");
            }
            
            if ( i_XSQL.getCallParams() == null )
            {
                i_XSQL.setAddParam(null);
            }
            
            if ( i_XSQL.getContent() == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + v_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup())) )
            {
                throw new NullPointerException("SQLCallName is null of XSQL.");
            }
            
            // 存储过程或函数有入参时，i_ParamObj不能为空。否则，可则为空
            if ( i_XSQL.getCallParamInCount() >= 1 && i_ParamObj == null )
            {
                throw new NullPointerException("Call [" + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "] parameters values is null.");
            }
            
            // 数据函数调用时
            if ( XSQL.$Type_Function.equals(i_XSQL.getType()) )
            {
                // 必须有返回值
                if ( i_XSQL.getCallParamOutCount() <= 0 )
                {
                    throw new NullPointerException("Call [" + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "] Return parameter is invalid.");
                }
                
                // 返回值的配置必须为第一个<addParam>标记
                if ( !i_XSQL.getCallParams().get(0).isOutType() )
                {
                    throw new NullPointerException("Call [" + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "] Return parameter is not first '<addParam>'.");
                }
            }
            
            
            // 生成 {call xxx(? ,? ,? ...)}
            int v_StartIndex = 0;
            if ( XSQL.$Type_Procedure.equals(i_XSQL.getType()) )
            {
                v_Buffer.append("{call ");
                v_StartIndex = 0;
            }
            else
            {
                v_Buffer.append("{? = call ");
                v_StartIndex = 1;
            }
            v_Buffer.append(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()).trim()).append("(");
            for (int i=v_StartIndex; i<i_XSQL.getCallParams().size(); i++)
            {
                if ( i > v_StartIndex )
                {
                    v_Buffer.append(" ,");
                }
                v_Buffer.append("?");
            }
            v_Buffer.append(")}");
            
            
            v_Conn      = i_XSQL.getConnection(v_DSG);
            v_Statement = v_Conn.prepareCall(v_Buffer.toString());
            
            
            for (int v_ParamIndex=1; v_ParamIndex<=i_XSQL.getCallParams().size(); v_ParamIndex++)
            {
                XSQLCallParam v_CallParam = i_XSQL.getCallParams().get(v_ParamIndex - 1);
                
                // 输入类型的参数
                if ( v_CallParam.isInType() )
                {
                    Object v_ParamValue = MethodReflect.getGetMethod(i_ParamObj.getClass() ,v_CallParam.getName() ,true).invoke(i_ParamObj);
                    
                    if ( v_ParamValue == null )
                    {
                        throw new NullPointerException("Call " + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "(" + v_CallParam.getName() + ") parameter is null.");
                    }
                    
                    v_CallParam.setValue(v_Statement ,v_ParamIndex ,v_ParamValue);
                }
                
                // 输出类型的参数
                if ( v_CallParam.isOutType() )
                {
                    v_Statement.registerOutParameter(v_ParamIndex ,v_CallParam.getJdbcTypeID());
                    v_OutParamIndexs.add(Integer.valueOf(v_ParamIndex));
                }
            }
            
            if ( v_OutParamIndexs.size() <= 0 )
            {
                v_Statement.execute();
                i_XSQL.log(v_Buffer.toString());
                Date v_EndTime = Date.getNowTime();
                i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                return true;
            }
            else
            {
                v_Statement.execute();
                i_XSQL.log(v_Buffer.toString());
                
                if ( v_OutParamIndexs.size() == 1 )
                {
                    XSQLCallParam v_CallParam = i_XSQL.getCallParams().get(v_OutParamIndexs.get(0).intValue() - 1);
                    Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(0).intValue());
                    
                    if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                    {
                        v_Resultset = (ResultSet)v_OutValue;
                        XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset);
                        Date v_EndTime = Date.getNowTime();
                        i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
                        return v_Ret.getDatas();
                    }
                    else
                    {
                        Date v_EndTime = Date.getNowTime();
                        i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                        return v_OutValue;
                    }
                }
                else
                {
                    List<Object> v_Rets   = new ArrayList<Object>();
                    long         v_RCount = 0L;
                    
                    for (int i=0; i<v_OutParamIndexs.size(); i++)
                    {
                        XSQLCallParam v_CallParam = i_XSQL.getCallParams().get(v_OutParamIndexs.get(i).intValue() - 1);
                        Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(i).intValue());
                        
                        if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                        {
                            v_Resultset = (ResultSet)v_OutValue;
                            XSQLData v_Datas = i_XSQL.getResult().getDatas(v_Resultset);
                            v_RCount += v_Datas.getRowCount();
                            v_Rets.add(v_Datas.getDatas());
                        }
                        else
                        {
                            v_Rets.add(v_OutValue);
                        }
                    }
                    
                    Date v_EndTime = Date.getNowTime();
                    i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_RCount);
                    return v_Rets;
                }
            }
        }
        catch (Exception exce)
        {
            v_IsError = true;
            XSQL.erroring(v_Buffer.toString() ,exce ,i_XSQL);
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_Buffer.toString() ,exce ,i_XSQL).setValuesObject(i_ParamObj));
            }
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            v_OutParamIndexs.clear();
            v_OutParamIndexs = null;
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
            
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_ParamObj);
            }
        }
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 输入参数为Map对象
     * 
     * Map.key    应于XSQLCallParam.name相互匹配。
     * Map.value  为输入参数的值
     * 
     * 1. 支持游标输出
     * 2. 支持多个输出参数
     * 
     * @param i_SQLCallName      存储过程或函数的名称
     * @param i_ParamObj         入参参数值集合
     * 
     * @return  1. 当调用对象为存储过程时，当无输出参数时，返回是否执行成功(true、false)
     *          2. 当调用对象为存储过程时，当有一个输出类型时，返回输出值。
     *                                如果输出类型为游标，则可以按XJava的概念直接转为实例对象。
     *          3. 当调用对象为存储过程时，当有多个输出类型时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                                如果输出类型为游标，则每个游标都按XJava的概念直接转为实例对象。
     *          4. 当调用函数时，当无输出参数时，返回函数的返回值。
     *          5. 当调用函数时，除自身返回值外还有一个或多个输出参数时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                        List集合的首个元素为函数自身的返回值。
     */
    @SuppressWarnings("resource")
    public static Object call(final XSQL i_XSQL ,final Map<String ,?> i_ParamValues)
    {
        DataSourceGroup   v_DSG            = null;
        Connection        v_Conn           = null;
        CallableStatement v_Statement      = null;
        ResultSet         v_Resultset      = null;
        List<Integer>     v_OutParamIndexs = new ArrayList<Integer>();
        long              v_BeginTime      = i_XSQL.request().getTime();
        boolean           v_IsError        = false;
        StringBuilder     v_Buffer         = new StringBuilder();
        
        try
        {
            if ( !XSQL.$Type_Procedure.equals(i_XSQL.getType()) && !XSQL.$Type_Function.equals(i_XSQL.getType()) )
            {
                throw new IllegalArgumentException("Type is not 'P' or 'F' of XSQL.");
            }
            
            if ( i_XSQL.getContent() == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + v_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup())) )
            {
                throw new NullPointerException("SQLCallName is null of XSQL.");
            }
            
            // 存储过程或函数有入参时，i_ParamObj不能为空。否则，可则为空
            if ( i_XSQL.getCallParamInCount() >= 1 && Help.isNull(i_ParamValues) )
            {
                throw new NullPointerException("Call [" + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "] parameters values is null.");
            }
            
            // 数据函数调用时
            if ( XSQL.$Type_Function.equals(i_XSQL.getType()) )
            {
                // 必须有返回值
                if ( i_XSQL.getCallParamOutCount() <= 0 )
                {
                    throw new NullPointerException("Call [" + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "] Return parameter is invalid.");
                }
                
                // 返回值的配置必须为第一个<addParam>标记
                if ( !i_XSQL.getCallParams().get(0).isOutType() )
                {
                    throw new NullPointerException("Call [" + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "] Return parameter is not first '<addParam>'.");
                }
            }
            
            
            // 生成 {call xxx(? ,? ,? ...)}
            int v_StartIndex = 0;
            if ( XSQL.$Type_Procedure.equals(i_XSQL.getType()) )
            {
                v_Buffer.append("{call ");
                v_StartIndex = 0;
            }
            else
            {
                v_Buffer.append("{? = call ");
                v_StartIndex = 1;
            }
            v_Buffer.append(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()).trim()).append("(");
            for (int i=v_StartIndex; i<i_XSQL.getCallParams().size(); i++)
            {
                if ( i > v_StartIndex )
                {
                    v_Buffer.append(" ,");
                }
                v_Buffer.append("?");
            }
            v_Buffer.append(")}");
            
            
            v_Conn      = i_XSQL.getConnection(v_DSG);
            v_Statement = v_Conn.prepareCall(v_Buffer.toString());
            
            
            for (int v_ParamIndex=1; v_ParamIndex<=i_XSQL.getCallParams().size(); v_ParamIndex++)
            {
                XSQLCallParam v_CallParam = i_XSQL.getCallParams().get(v_ParamIndex - 1);
                
                // 输入类型的参数
                if ( v_CallParam.isInType() )
                {
                    if ( !i_ParamValues.containsKey(v_CallParam.getName().trim()) )
                    {
                        throw new NullPointerException("Call " + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "(" + v_CallParam.getName() + ") parameter is not exist.");
                    }
                    
                    Object v_ParamValue = i_ParamValues.get(v_CallParam.getName().trim());
                    
                    if ( v_ParamValue == null )
                    {
                        throw new NullPointerException("Call " + i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) + "(" + v_CallParam.getName() + ") parameter is null.");
                    }
                    
                    v_CallParam.setValue(v_Statement ,v_ParamIndex ,v_ParamValue);
                }
                
                // 输出类型的参数
                if ( v_CallParam.isOutType() )
                {
                    v_Statement.registerOutParameter(v_ParamIndex ,v_CallParam.getJdbcTypeID());
                    v_OutParamIndexs.add(Integer.valueOf(v_ParamIndex));
                }
            }
            
            if ( v_OutParamIndexs.size() <= 0 )
            {
                v_Statement.execute();
                i_XSQL.log(v_Buffer.toString());
                Date v_EndTime = Date.getNowTime();
                i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                return true;
            }
            else
            {
                v_Statement.execute();
                i_XSQL.log(v_Buffer.toString());
                
                if ( v_OutParamIndexs.size() == 1 )
                {
                    XSQLCallParam v_CallParam = i_XSQL.getCallParams().get(v_OutParamIndexs.get(0).intValue() - 1);
                    Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(0).intValue());
                    
                    if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                    {
                        v_Resultset = (ResultSet)v_OutValue;
                        XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset);
                        Date v_EndTime = Date.getNowTime();
                        i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
                        return v_Ret.getDatas();
                    }
                    else
                    {
                        Date v_EndTime = Date.getNowTime();
                        i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                        return v_OutValue;
                    }
                }
                else
                {
                    List<Object> v_Rets   = new ArrayList<Object>();
                    long         v_RCount = 0L;
                    
                    for (int i=0; i<v_OutParamIndexs.size(); i++)
                    {
                        XSQLCallParam v_CallParam = i_XSQL.getCallParams().get(v_OutParamIndexs.get(i).intValue() - 1);
                        Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(i).intValue());
                        
                        if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                        {
                            v_Resultset = (ResultSet)v_OutValue;
                            XSQLData v_Datas = i_XSQL.getResult().getDatas(v_Resultset);
                            v_RCount += v_Datas.getRowCount();
                            v_Rets.add(v_Datas.getDatas());
                        }
                        else
                        {
                            v_Rets.add(v_OutValue);
                        }
                    }
                    
                    Date v_EndTime = Date.getNowTime();
                    i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_RCount);
                    return v_Rets;
                }
            }
        }
        catch (Exception exce)
        {
            v_IsError = true;
            XSQL.erroring(v_Buffer.toString() ,exce ,i_XSQL);
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_Buffer.toString() ,exce ,i_XSQL).setValuesMap(i_ParamValues));
            }
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            v_OutParamIndexs.clear();
            v_OutParamIndexs = null;
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
            
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_ParamValues);
            }
        }
    }
    
    
    
    /**
     * 本类不允许构建
     */
    private XSQLOPProcedure()
    {
        
    }
    
}
