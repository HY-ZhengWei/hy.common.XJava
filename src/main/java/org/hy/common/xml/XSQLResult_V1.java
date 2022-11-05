package org.hy.common.xml;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.db.DBNameStyle;
import org.hy.common.db.DBTableMetaData;





/**
 * 解释Xml文件，分析数据库结果集转化为Java实例对象
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-01
 */
public final class XSQLResult_V1 
{
    /** 
     * 正则表达式对：row 关键字的识别 -- 表示行级对象
     * 如：add(row)
     */
    @SuppressWarnings("unused")
    private final static String $REGEX_ROW            = "[ \\(,][Rr][Oo][Ww][ \\),\\.]";
    
    /** 
     * 正则表达式对：row.xxx 关键字的识别 -- 表示行级对象某一属性值的引用
     * 如：put(row.serialNo ,row) 
     */
    @SuppressWarnings("unused")
    private final static String $REGEX_ROW_GETTER     = "[ \\(,][Rr][Oo][Ww]\\.\\w+[ \\),]";
    
    /**
     * 正则表达式对：rowNo 关键字的识别 -- 表示行号
     * 如：addColumnValue(rowNo ,colNo ,colValue)
     */
    @SuppressWarnings("unused")
    private final static String $REGEX_ROWNO          = "[ \\(,][Rr][Oo][Ww][Nn][Oo][ \\),]";
    
    /**
     * 正则表达式对：colNo 关键字的识别 -- 表示列号
     * 如：addColumnValue(rowNo ,colNo ,colValue)
     */
    @SuppressWarnings("unused")
    private final static String $REGEX_COLNO          = "[ \\(,][Cc][Oo][Ll][Nn][Oo][ \\),]";
    
    /**
     * 正则表达式对：colValue 关键字的识别 -- 表示某一列的数值
     * 如：addColumnValue(rowNo ,colNo ,colValue)
     */
    @SuppressWarnings("unused")
    private final static String $REGEX_COLVALUE       = "[ \\(,][Cc][Oo][Ll][Vv][Aa][Ll][Uu][Ee][ \\),]";
    
    /**
     * 正则表达式对：方法名称的识别
     * 如：add(row)
     */
    private final static String $REGEX_METHOD         = "\\w+[\\(]";
    
    /**
     * 正则表达式对：方法填写有效性的验证。
     * 如：xxx(p1 ,p2 ,... pn)
     * 如：xxx(o1.p1 ,o2.p1 ,... on.pn)
     */
    private final static String $REGEX_METHOD_VERIFY  = "^\\w+\\( *((\\w+\\.\\w+ *, *)|(\\w+ *, *))*((\\w+\\.\\w+)|(\\w+)) *\\)$";
    
    /**
     * 正则表达式对：特殊方法名称 setter 关键字的识别 -- 表示 setter 方法，使用反射机制向行级对象中 setter 属性
     * 如：setter(colValue)
     */
    @SuppressWarnings("unused")
    private final static String $REGEX_SETTER         = "[Ss][Ee][Tt][Tt][Ee][Rr][\\(]";
    
    
    
    /** 列级对象填充到行级对象中行级对象的方法类型：固定方法 */
    private final static int    $CFILL_METHOD_FIXED   = 0;
    
    /** 列级对象填充到行级对象中行级对象的方法类型：变化方法 */
    private final static int    $CFILL_METHOD_VARY    = 1;
    
    
    
    /** 表级对象的Class类型 */
    private Class<?>            table;
    
    /** 行级对象的Class类型 */
    private Class<?>            row;
    
    /** 行级对象填充到表级对象的填充方法字符串 */
    private String              fill;
    
    /** 行级对象填充到表级对象的填充方法(解释 fill 后生成) */
    private XSQLMethod          fillMethod;
    
    /** 列级对象填充到行级对象的填充方法字符串 */
    private String              cfill;
    
    /** 
     * 列级对象填充到行级对象中行级对象的方法类型(解释 cfill 后生成)
     * 
     * 1. 固定方法 -- 如：cfill = "addColumnValue(colNo ,colValue)"   -- $CFILL_METHOD_FIXED
     * 2. 变化方法 -- 如：cfill = "setter(colValue)"                  -- $CFILL_METHOD_VARY
     */
    private int                 cfillMethodType;
    
    /** 
     * 列级对象填充到行级对象的填充方法(解释 cfill 后生成)
     * 
     * 当为固定方法时，cfillMethodArr只有一个元素
     * 当为变化方法时，cfillMethodArr的元素个数和顺序与SQL的输出结果相同
     */
    private XSQLMethod []       cfillMethodArr;
    
    /**
     * 表示 cfillMethodArr 数组中有效的下标为哪些。
     * 
     * 当为变化方法时，才有用。
     * 
     * 当结果集的输出字段数，比解释到的 cfillMethod 方法数多时，提高的性能。
     */
    private int        []       cfillMethodArr_ValidIndex;
    
    /**
     * 超级大结果级的最终有效过滤字段数组。
     * 
     * 此数组保存有效的下标值。
     * 
     * 解释用户指定的输出字段 -- 生成最终的过滤字段数组。在 getDatas(XSQLBiggerMemory ... ) 中被生成。
     * 
     * 只有当超级大结果级时，才有用的。用于 getDatasNextPage() 和 getDatasNextRow() 两个方法中。
     * 
     * 其目的是一次解释，多次高效使用。
     */
    private int        []       cfill_ValidIndex_BiggerMemory;
    
    /** 结果集的元数据 */
    private DBTableMetaData     dbMetaData;
    
    /** 是否重新分析。任务一个对外的属性值变化后，都要重新分析。 */
    private boolean             isAgainParse;
    
    /** 将数据库结果集转化为Java实例对象的用时时长(单位：毫秒) */
    private long                getDatasTimes;
    
    /** 将数据库结果集转化为Java实例对象的行数(已读取的行数) */
    private long                getDatasRowSize;
    
    /** 将数据库结果集转化为Java实例对象的列数(有效列数) */
    private int                 getDatasColSize;
    
    
    
    public XSQLResult_V1()
    {
        this.table                         = ArrayList.class;
        this.row                           = ArrayList.class;
        this.fill                          = "add(row)";
        this.fillMethod                    = new XSQLMethod();
        this.cfill                         = "add(colValue)";
        this.cfillMethodType               = $CFILL_METHOD_FIXED;
        this.cfillMethodArr                = null;
        this.cfillMethodArr_ValidIndex     = null;
        this.cfill_ValidIndex_BiggerMemory = null;
        this.dbMetaData                    = new DBTableMetaData(DBNameStyle.$Upper);
        this.isAgainParse                  = true;
        
        this.initTotalInfo();
    }
    
    
    
    /**
     * 初始化统计信息
     */
    public void initTotalInfo()
    {
        this.getDatasTimes   = -1;
        this.getDatasRowSize = 0;
        this.getDatasColSize = 0;
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象
     * 
     * @param i_ResultSet
     * @return
     */
    public synchronized Object getDatas(ResultSet i_ResultSet)
    {
        return this.getDatas(i_ResultSet ,0 ,null ,null);
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象(按输出字段名称过滤填充)
     * 
     * @param i_ResultSet
     * @param i_FilterColNames   过滤输出字段。只对结果集输出字段在这个集合 i_FilterColNames 中的字段才进行填充动作，即输出列可选择。
     * @return
     */
    public synchronized Object getDatas(ResultSet i_ResultSet ,List<String> i_FilterColNames)
    {
        return this.getDatas(i_ResultSet ,1 ,i_FilterColNames ,null);
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象(按输出字段位置过滤填充)
     * 
     * 字段位置下标从零开始。
     * 
     * @param i_ResultSet
     * @param i_FilterColNoArr   过滤输出字段的位置。字段位置下标从零开始。只对结果集输出字段在这个数组 i_FilterColNoArr 中的字段才进行填充动作，即输出列可选择。
     * @return
     */
    public synchronized Object getDatas(ResultSet i_ResultSet ,int [] i_FilterColNoArr)
    {
        return this.getDatas(i_ResultSet ,2 ,null ,i_FilterColNoArr);
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象 -- 超级大结果集的转换
     * 
     * @param i_Bigger           超级大结果集的存储器处理类
     */
    public synchronized void getDatas(XSQLBigger i_Bigger)
    {
        this.getDatas(i_Bigger ,0 ,null ,null);
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象(按输出字段名称过滤填充) -- 超级大结果集的转换
     * 
     * @param i_Bigger           超级大结果集的存储器处理类
     * @param i_FilterColNames   过滤输出字段。只对结果集输出字段在这个集合 i_FilterColNames 中的字段才进行填充动作，即输出列可选择。
     */
    public synchronized void getDatas(XSQLBigger i_Bigger ,List<String> i_FilterColNames)
    {
        this.getDatas(i_Bigger ,1 ,i_FilterColNames ,null);
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象(按输出字段位置过滤填充) -- 超级大结果集的转换
     * 
     * 字段位置下标从零开始。
     * 
     * @param i_Bigger           超级大结果集的存储器处理类
     * @param i_FilterColNoArr   过滤输出字段的位置。字段位置下标从零开始。只对结果集输出字段在这个数组 i_FilterColNoArr 中的字段才进行填充动作，即输出列可选择。
     */
    public synchronized void getDatas(XSQLBigger i_Bigger ,int [] i_FilterColNoArr)
    {
        this.getDatas(i_Bigger ,2 ,null ,i_FilterColNoArr);
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象(私有的)
     * 
     * @param i_ResultSet
     * @param i_FilterType       过滤类型。0: 无过滤填充
     *                                   1: 按输出字段名称过滤填充
     *                                   2: 按输出字段位置过滤填充
     *                             
     * @param i_FilterColNames   过滤输出字段。只对结果集输出字段在这个集合 i_FilterColNames 中的字段才进行填充动作，即输出列可选择。
     *                     
     * @param i_FilterColNoArr   过滤输出字段的位置。字段位置下标从零开始。只对结果集输出字段在这个数组 i_FilterColNoArr 中的字段才进行填充动作，即输出列可选择。
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object getDatas(ResultSet i_ResultSet ,int i_FilterType ,List<String> i_FilterColNames ,int [] i_FilterColNoArr)
    {
        if ( i_ResultSet == null )
        {
            throw new NullPointerException("ResultSet is null.");
        }
        
        
        Object v_Table       = null;
        int [] v_ColArr      = null;
        long   v_RowNo       = 0;
        int    v_ColNo       = 0;
        this.initTotalInfo();
        
        try
        {
            v_Table = this.newTableObject();
            
            if ( this.dbMetaData.getColumnSize() == 0 )
            {
                // 解释数据集元数据
                this.dbMetaData.set(i_ResultSet.getMetaData());
                
                if ( this.cfillMethodType == $CFILL_METHOD_VARY )
                {
                    this.isAgainParse = true;
                }
            }
            else
            {
                DBTableMetaData v_NewMetaData = new DBTableMetaData(DBNameStyle.$Upper);
                v_NewMetaData.set(i_ResultSet.getMetaData());
                
                // 老的元数据与新的元数据不同时，必须重新"全量解释"
                if ( !this.dbMetaData.equals(v_NewMetaData) )
                {
                    this.isAgainParse = true;
                }
            }
            
            
            if ( this.isAgainParse )
            {
                this.parse();
            }
            
            
            
            // 无输出字段过滤的情况
            if ( i_FilterType == 0 || ((i_FilterColNames == null || i_FilterColNames.size() == 0) && (i_FilterColNoArr == null || i_FilterColNoArr.length == 0)) )
            {
                if ( this.cfillMethodType == $CFILL_METHOD_FIXED )
                {
                    v_ColArr = new int[this.dbMetaData.getColumnSize()];
                    for (int i=0; i<this.dbMetaData.getColumnSize(); i++)
                    {
                        v_ColArr[i] = i;
                    }
                }
                else
                {
                    v_ColArr = new int[this.cfillMethodArr_ValidIndex.length];
                    for (int i=0; i<this.cfillMethodArr_ValidIndex.length; i++)
                    {
                        v_ColArr[i] = this.cfillMethodArr_ValidIndex[i];
                    }
                }
            }
            else
            {
                // 解释用户指定的输出字段 -- 固定方法过滤
                List<Integer> v_Temp_ColList = new ArrayList<Integer>();
                
                // 按输出字段名称过滤填充
                if ( i_FilterType == 1 )
                {
                    for (int i=0; i<i_FilterColNames.size(); i++)
                    {
                        int v_Temp_ColNo = this.dbMetaData.getColumnIndex(i_FilterColNames.get(i).trim().toUpperCase());
                        
                        if ( v_Temp_ColNo != -1 )
                        {
                            v_Temp_ColList.add(Integer.valueOf(v_Temp_ColNo));
                        }
                    }
                }
                // 按输出字段位置过滤填充
                else if ( i_FilterType == 2 )
                {
                    for (int i=0; i<i_FilterColNoArr.length; i++)
                    {
                        int v_Temp_ColNo = i_FilterColNoArr[i];
                        if ( 0 <= v_Temp_ColNo && v_Temp_ColNo < this.dbMetaData.getColumnSize() )
                        {
                            v_Temp_ColList.add(Integer.valueOf(v_Temp_ColNo));
                        }
                    }
                }
                else
                {
                    // 如果代码执行到此，表示我的代码开发的有问题。
                    throw new java.lang.RuntimeException("Inner code error.");
                }
                
                
                // 解释用户指定的输出字段 -- 变化方法过滤
                if ( this.cfillMethodType == $CFILL_METHOD_VARY )
                {
                    for (int i=0; i<v_Temp_ColList.size(); i++)
                    {
                        int     v_Temp_ColNo   = v_Temp_ColList.get(i).intValue();
                        boolean v_Temp_IsExist = false; 
                        
                        for (int j=0; j<this.cfillMethodArr_ValidIndex.length && !v_Temp_IsExist; j++)
                        {
                            if ( v_Temp_ColNo == this.cfillMethodArr_ValidIndex[j] )
                            {
                                v_Temp_IsExist = true;
                            }
                        }
                        
                        if ( !v_Temp_IsExist )
                        {
                            v_Temp_ColList.remove(i);
                        }
                    }
                }
                
                // 解释用户指定的输出字段 -- 生成最终的过滤字段数组
                v_ColArr = new int[v_Temp_ColList.size()];
                for (int i=0; i<v_Temp_ColList.size(); i++)
                {
                    v_ColArr[i] = v_Temp_ColList.get(i).intValue();
                }
            }
            
            
            
            this.getDatasColSize = v_ColArr.length;
            Date v_ExecBeginTime = new Date();
            
            // 列级对象填充到行级对象中行级对象的方法类型: 固定方法
            if ( this.cfillMethodType == $CFILL_METHOD_FIXED )
            {
                while ( i_ResultSet.next() ) 
                {
                    Object v_Row = this.newRowObject();
                    
                    for (int i=0; i<v_ColArr.length; i++)
                    {
                        v_ColNo = v_ColArr[i];
                        Object v_ColValue = i_ResultSet.getObject(v_ColNo + 1);
                        
                        this.cfillMethodArr[0].invoke(v_Row ,v_ColValue ,v_ColNo ,this.dbMetaData.getColumnName(v_ColNo));
                    }
                    
                    this.fillMethod.invoke(v_Table ,v_Row ,v_RowNo++ ,null);
                }
            }
            // 列级对象填充到行级对象中行级对象的方法类型: 变化方法 -- setter(colValue)
            else
            {
                while ( i_ResultSet.next() ) 
                {
                    Object v_Row = this.newRowObject();
                    
                    for (int i=0; i<v_ColArr.length; i++)
                    {
                        v_ColNo = v_ColArr[i];
                            
                        Object v_ColValue = this.cfillMethodArr[v_ColNo].getResultSet_Getter().invoke(i_ResultSet ,v_ColNo + 1);
                            
                        v_ColValue = this.cfillMethodArr[v_ColNo].getMachiningValue().getValue(v_ColValue);
                            
                        // 最后的入参可是为null。原因是 setter(colValue) 方法的入参只有一个，且只能是 colValue，所以不需要字段名称
                        this.cfillMethodArr[v_ColNo].invoke(v_Row ,v_ColValue ,v_ColNo ,null);
                    }
                    
                    this.fillMethod.invoke(v_Table ,v_Row ,v_RowNo++ ,null);
                }
            }
            
            
            this.getDatasTimes   = (new Date()).getTime() - v_ExecBeginTime.getTime();
            this.getDatasRowSize = v_RowNo;
        }
        catch (Exception exce)
        {
            this.getDatasTimes   = -1;
            this.getDatasRowSize = v_RowNo;
            
            throw new java.lang.RuntimeException("RowNo=" + v_RowNo + "  ColNo=" + v_ColNo + "  " + exce.getMessage());
        }
        
        return v_Table;
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象(私有的) -- 超级大结果集的转换
     * 
     * @param i_Bigger           超级大结果集的存储器处理类
     * 
     * @param i_FilterType       过滤类型。0: 无过滤填充
     *                                   1: 按输出字段名称过滤填充
     *                                   2: 按输出字段位置过滤填充
     *                             
     * @param i_FilterColNames   过滤输出字段。只对结果集输出字段在这个集合 i_FilterColNames 中的字段才进行填充动作，即输出列可选择。
     *                     
     * @param i_FilterColNoArr   过滤输出字段的位置。字段位置下标从零开始。只对结果集输出字段在这个数组 i_FilterColNoArr 中的字段才进行填充动作，即输出列可选择。
     * @return
     */
    private void getDatas(XSQLBigger i_Bigger ,int i_FilterType ,List<String> i_FilterColNames ,int [] i_FilterColNoArr)
    {
        if ( i_Bigger == null || i_Bigger.getResultSet() == null )
        {
            throw new NullPointerException("XSQLBigger is null.");
        }
        
        
        ResultSet v_ResultSet = i_Bigger.getResultSet();
        this.initTotalInfo();
        
        try
        {
            if ( this.dbMetaData.getColumnSize() == 0 )
            {
                // 解释数据集元数据
                this.dbMetaData.set(v_ResultSet.getMetaData());
                
                if ( this.cfillMethodType == $CFILL_METHOD_VARY )
                {
                    this.isAgainParse = true;
                }
            }
            else
            {
                DBTableMetaData v_NewMetaData = new DBTableMetaData(DBNameStyle.$Upper);
                v_NewMetaData.set(v_ResultSet.getMetaData());
                
                // 老的元数据与新的元数据不同时，必须重新"全量解释"
                if ( !this.dbMetaData.equals(v_NewMetaData) )
                {
                    this.isAgainParse = true;
                }
            }
            
            
            if ( this.isAgainParse )
            {
                this.parse();
            }
            
            
            
            // 无输出字段过滤的情况
            if ( i_FilterType == 0 || ((i_FilterColNames == null || i_FilterColNames.size() == 0) && (i_FilterColNoArr == null || i_FilterColNoArr.length == 0)) )
            {
                if ( this.cfillMethodType == $CFILL_METHOD_FIXED )
                {
                    this.cfill_ValidIndex_BiggerMemory = new int[this.dbMetaData.getColumnSize()];
                    for (int i=0; i<this.dbMetaData.getColumnSize(); i++)
                    {
                        this.cfill_ValidIndex_BiggerMemory[i] = i;
                    }
                }
                else
                {
                    this.cfill_ValidIndex_BiggerMemory = new int[this.cfillMethodArr_ValidIndex.length];
                    for (int i=0; i<this.cfillMethodArr_ValidIndex.length; i++)
                    {
                        this.cfill_ValidIndex_BiggerMemory[i] = this.cfillMethodArr_ValidIndex[i];
                    }
                }
            }
            else
            {
                // 解释用户指定的输出字段 -- 固定方法过滤
                List<Integer> v_Temp_ColList = new ArrayList<Integer>();
                
                // 按输出字段名称过滤填充
                if ( i_FilterType == 1 )
                {
                    for (int i=0; i<i_FilterColNames.size(); i++)
                    {
                        int v_Temp_ColNo = this.dbMetaData.getColumnIndex(i_FilterColNames.get(i).trim().toUpperCase());
                        
                        if ( v_Temp_ColNo != -1 )
                        {
                            v_Temp_ColList.add(Integer.valueOf(v_Temp_ColNo));
                        }
                    }
                }
                // 按输出字段位置过滤填充
                else if ( i_FilterType == 2 )
                {
                    for (int i=0; i<i_FilterColNoArr.length; i++)
                    {
                        int v_Temp_ColNo = i_FilterColNoArr[i];
                        if ( 0 <= v_Temp_ColNo && v_Temp_ColNo < this.dbMetaData.getColumnSize() )
                        {
                            v_Temp_ColList.add(Integer.valueOf(v_Temp_ColNo));
                        }
                    }
                }
                else
                {
                    // 如果代码执行到此，表示我的代码开发的有问题。
                    throw new java.lang.RuntimeException("Inner code error.");
                }
                
                
                // 解释用户指定的输出字段 -- 变化方法过滤
                if ( this.cfillMethodType == $CFILL_METHOD_VARY )
                {
                    for (int i=0; i<v_Temp_ColList.size(); i++)
                    {
                        int     v_Temp_ColNo   = v_Temp_ColList.get(i).intValue();
                        boolean v_Temp_IsExist = false; 
                        
                        for (int j=0; j<this.cfillMethodArr_ValidIndex.length && !v_Temp_IsExist; j++)
                        {
                            if ( v_Temp_ColNo == this.cfillMethodArr_ValidIndex[j] )
                            {
                                v_Temp_IsExist = true;
                            }
                        }
                        
                        if ( !v_Temp_IsExist )
                        {
                            v_Temp_ColList.remove(i);
                        }
                    }
                }
                
                // 解释用户指定的输出字段 -- 生成最终的过滤字段数组
                this.cfill_ValidIndex_BiggerMemory = new int[v_Temp_ColList.size()];
                for (int i=0; i<v_Temp_ColList.size(); i++)
                {
                    this.cfill_ValidIndex_BiggerMemory[i] = v_Temp_ColList.get(i).intValue();
                }
            }
            
        }
        catch (Exception exce)
        {
            throw new java.lang.RuntimeException(exce.getMessage() + " of getDatas(XSQLBigger ... )");
        }
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象 -- 超级大结果集的转换 -- 获取下一分页的数据
     * 
     * @param i_Bigger           超级大结果集的存储器处理类
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object getDatasNextPage(XSQLBigger i_Bigger)
    {
        if ( i_Bigger == null || i_Bigger.getResultSet() == null )
        {
            throw new NullPointerException("XSQLBigger is null.");
        }
        
        if ( i_Bigger.getBiggerMemory() == null )
        {
            throw new NullPointerException("XSQLBiggerMemory is null.");
        }
        
        if ( i_Bigger.getPerSize() < 1 )
        {
            throw new IndexOutOfBoundsException("PerSize min value is 1.");
        }
        
        if ( !i_Bigger.isBiggerNextPage() )
        {
            return i_Bigger.getBiggerMemory().getMemory();
        }
        
        
        ResultSet v_ResultSet = i_Bigger.getResultSet();
        long      v_RowNo     = 0;
        int       v_ColNo     = 0;
        
        try
        {   
            this.getDatasColSize = this.cfill_ValidIndex_BiggerMemory.length;
            v_RowNo              = v_ResultSet.getRow();
            int  v_PerIndex      = 1;
            int  v_PerSize       = i_Bigger.getPerSize();
            Date v_ExecBeginTime = new Date();
            
            // 列级对象填充到行级对象中行级对象的方法类型: 固定方法
            if ( this.cfillMethodType == $CFILL_METHOD_FIXED )
            {
                while ( v_PerIndex <= v_PerSize && v_ResultSet.next() ) 
                {
                    Object v_Row = this.newRowObject();
                    
                    for (int i=0; i<this.cfill_ValidIndex_BiggerMemory.length; i++)
                    {
                        v_ColNo = this.cfill_ValidIndex_BiggerMemory[i];
                        Object v_ColValue = v_ResultSet.getObject(v_ColNo + 1);
                        
                        this.cfillMethodArr[0].invoke(v_Row ,v_ColValue ,v_ColNo ,this.dbMetaData.getColumnName(v_ColNo));
                    }
                    
                    v_PerIndex++;
                    v_RowNo++;
                    
                    i_Bigger.getBiggerMemory().setRowInfo(v_RowNo ,i_Bigger.getRowSize() ,v_Row);
                }
            }
            // 列级对象填充到行级对象中行级对象的方法类型: 变化方法 -- setter(colValue)
            else
            {
                while ( v_PerIndex <= v_PerSize && v_ResultSet.next() ) 
                {
                    Object v_Row = this.newRowObject();
                    
                    for (int i=0; i<this.cfill_ValidIndex_BiggerMemory.length; i++)
                    {
                        v_ColNo = this.cfill_ValidIndex_BiggerMemory[i];
                            
                        Object v_ColValue = this.cfillMethodArr[v_ColNo].getResultSet_Getter().invoke(v_ResultSet ,v_ColNo + 1);
                            
                        v_ColValue = this.cfillMethodArr[v_ColNo].getMachiningValue().getValue(v_ColValue);
                            
                        // 最后的入参可是为null。原因是 setter(colValue) 方法的入参只有一个，且只能是 colValue，所以不需要字段名称
                        this.cfillMethodArr[v_ColNo].invoke(v_Row ,v_ColValue ,v_ColNo ,null);
                    }
                    
                    v_PerIndex++;
                    v_RowNo++;
                    
                    i_Bigger.getBiggerMemory().setRowInfo(v_RowNo ,i_Bigger.getRowSize() ,v_Row);
                }
            }
            
            
            this.getDatasTimes   = (new Date()).getTime() - v_ExecBeginTime.getTime();
            this.getDatasRowSize = v_RowNo;
        }
        catch (Exception exce)
        {
            this.getDatasTimes   = -1;
            this.getDatasRowSize = v_RowNo;
            
            if ( this.cfill_ValidIndex_BiggerMemory != null && this.cfill_ValidIndex_BiggerMemory.length > 0 )
            {
                throw new RuntimeException("RowNo=" + v_RowNo + "  ColNo=" + v_ColNo + "  " + exce.getMessage());
            }
            else
            {
                throw new NullPointerException("Please first call getDatas(XSQLBigger ... ) method.");
            }
        }
        
        return i_Bigger.getBiggerMemory().getMemory();
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象 -- 超级大结果集的转换 -- 获取下一行的数据
     * 
     * 主要用于 FileBiggerMemory 和 FileSerializable 功能中。
     * 
     * 注意：1. 此方法直接返回下一行的行级数据，而不是表级数据。
     *      2. 并且，生成的行级数据不向表级数据填充。
     *      3. 此方法不再统计 getDatasTimes 时长。一行数据，没有什么好统计的。
     *      
     * 
     * @param i_Bigger           超级大结果集的存储器处理类
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object getDatasNextRow(XSQLBigger i_Bigger)
    {
        if ( i_Bigger == null || i_Bigger.getResultSet() == null )
        {
            throw new NullPointerException("XSQLBigger is null.");
        }
        
        if ( i_Bigger.getPerSize() < 1 )
        {
            throw new IndexOutOfBoundsException("PerSize min value is 1.");
        }
        
        if ( !i_Bigger.isBiggerNextPage() )
        {
            return null;
        }
        
        
        ResultSet v_ResultSet = i_Bigger.getResultSet();
        Object    v_Row       = null;
        long      v_RowNo     = 0;
        int       v_ColNo     = 0;
        
        try
        {   
            this.getDatasColSize = this.cfill_ValidIndex_BiggerMemory.length;
            v_RowNo              = v_ResultSet.getRow();
            
            // 列级对象填充到行级对象中行级对象的方法类型: 固定方法
            if ( this.cfillMethodType == $CFILL_METHOD_FIXED )
            {
                if ( v_ResultSet.next() ) 
                {
                    v_Row = this.newRowObject();
                    
                    for (int i=0; i<this.cfill_ValidIndex_BiggerMemory.length; i++)
                    {
                        v_ColNo = this.cfill_ValidIndex_BiggerMemory[i];
                        Object v_ColValue = v_ResultSet.getObject(v_ColNo + 1);
                        
                        this.cfillMethodArr[0].invoke(v_Row ,v_ColValue ,v_ColNo ,this.dbMetaData.getColumnName(v_ColNo));
                    }
                }
            }
            // 列级对象填充到行级对象中行级对象的方法类型: 变化方法 -- setter(colValue)
            else
            {
                if ( v_ResultSet.next() ) 
                {
                    v_Row = this.newRowObject();
                    
                    for (int i=0; i<this.cfill_ValidIndex_BiggerMemory.length; i++)
                    {
                        v_ColNo = this.cfill_ValidIndex_BiggerMemory[i];
                            
                        Object v_ColValue = this.cfillMethodArr[v_ColNo].getResultSet_Getter().invoke(v_ResultSet ,v_ColNo + 1);
                            
                        v_ColValue = this.cfillMethodArr[v_ColNo].getMachiningValue().getValue(v_ColValue);
                            
                        // 最后的入参可是为null。原因是 setter(colValue) 方法的入参只有一个，且只能是 colValue，所以不需要字段名称
                        this.cfillMethodArr[v_ColNo].invoke(v_Row ,v_ColValue ,v_ColNo ,null);
                    }
                }
            }
            
            
            this.getDatasRowSize = v_RowNo + 1;
        }
        catch (Exception exce)
        {
            this.getDatasRowSize = v_RowNo + 1;
            
            throw new java.lang.RuntimeException("RowNo=" + v_RowNo + "  ColNo=" + v_ColNo + "  " + exce.getMessage());
        }
        
        return v_Row;
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象的用时时长(单位：毫秒)
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
     * 将数据库结果集转化为Java实例对象的行数(已读取的行数)
     * 
     * @return
     */
    public long getDatasRowSize()
    {
        return this.getDatasRowSize;
    }
    
    
    
    /**
     * 将数据库结果集转化为Java实例对象的列数(有效列数)
     * 
     * @return
     */
    public int getDatasColSize()
    {
        return this.getDatasColSize;
    }
    
    
    
    /**
     * 全量解释。
     * 
     * 可手动调用。也可被自动调用。
     * 
     * 即，此方法不用人为刻意的被调用。
     */
    public synchronized void parse()
    {
        // 防止被重复解释
        if ( !this.isAgainParse )
        {
            return;
        }
        
        
        if ( this.table == null )
        {
            throw new NullPointerException("Table is null.");
        }
        
        if ( this.row == null )
        {
            throw new NullPointerException("Row is null.");
        }
        
        if ( Help.isNull(this.fill) )
        {
            throw new NullPointerException("Fill is null.");
        }
        
        if ( Help.isNull(this.cfill) )
        {
            throw new NullPointerException("CFill is null.");
        }
        
        
        this.fillMethod.clear();
        this.cfillMethodArr = null;
        
        
        this.parseFill();
        this.parseCFill();
        
        
        this.isAgainParse = false;
    }
    
    
    
    /**
     * 解释 this.fill -- 行级对象填充到表级对象的填充方法字符串
     */
    private void parseFill()
    {
        Pattern      v_Pattern    = Pattern.compile($REGEX_METHOD);
        Matcher      v_Matcher    = v_Pattern.matcher(this.fill);
        String       v_MethodName = "";
        int          v_EndIndex   = 0;
        List<Method> v_MethodList = null;
        String       v_Params     = null;
        String []    v_ParamArr   = null;
        
        
        // 识别行级填充方法名称
        if ( v_Matcher.find() )
        {
            v_MethodName = v_Matcher.group();
            v_MethodName = v_MethodName.substring(0 ,v_MethodName.length() - 1);
            v_EndIndex   = v_Matcher.end();
        }
        else
        {
            throw new RuntimeException("Fill method name[" + this.fill + "] is not exist.");
        }
        
        
        v_Params   = this.fill.substring(v_EndIndex ,this.fill.length() - 1);
        v_ParamArr = v_Params.split(",");
        
        Class<?> [] v_ParamClassArr_int     = new Class[v_ParamArr.length];
        Class<?> [] v_ParamClassArr_Integer = new Class[v_ParamArr.length];
        
        
        // 识别行级填充方法的所有入参
        for (int i=0; i<v_ParamArr.length; i++)
        {
            XSQLMethodParam v_MethodParam = null;
            
            if ( "ROW".equalsIgnoreCase(v_ParamArr[i].trim()) )
            {
                v_MethodParam = XSQLMethodParam_Fill.getInstance(XSQLMethodParam_Fill.$FILL_ROW );
                
                v_ParamClassArr_int[i]     = Object.class;
                v_ParamClassArr_Integer[i] = Object.class;
            }
            else if ( "ROWNO".equalsIgnoreCase(v_ParamArr[i].trim()) )
            {
                v_MethodParam = XSQLMethodParam_Fill.getInstance(XSQLMethodParam_Fill.$FILL_ROW_NO);
                
                v_ParamClassArr_int[i]     = int.class;
                v_ParamClassArr_Integer[i] = Integer.class;
            }
            else if ( "ROW.".equalsIgnoreCase(v_ParamArr[i].trim().substring(0 ,4)) )
            {
                // 识别对象属性的方法名
                String       v_AttrMethodName = "get" + v_ParamArr[i].trim().substring(4);
                List<Method> v_AttrMethodList = MethodReflect.getMethodsIgnoreCase(this.row ,v_AttrMethodName ,0);
                
                if ( v_AttrMethodList.size() == 0 )
                {
                    throw new RuntimeException("Row.Getter method name[" + v_ParamArr[i].trim() + "] is not exist.");
                }
                else if ( v_AttrMethodList.size() > 1 )
                {
                    // 对象属性方法有多个重载方法，无法正确识别
                    throw new RuntimeException("Row.Getter method name[" + v_ParamArr[i].trim() + "] have much override methods.");
                }
                
                v_ParamClassArr_int[i]     = v_AttrMethodList.get(0).getReturnType();
                v_ParamClassArr_Integer[i] = v_AttrMethodList.get(0).getReturnType();
                
                v_MethodParam = XSQLMethodParam_Fill.getInstance(XSQLMethodParam_Fill.$FILL_ROW_GETTER ,v_AttrMethodList.get(0));
            }
            else
            {
                // 方法的参数形式只能是 row、rowNo、row.xxx
                throw new RuntimeException("Fill method[" + this.fill + "] Parameter is not valid. Parameter only 'row' or 'rowNo' or 'row.xxx'.");
            }
            
            this.fillMethod.addParam(v_MethodParam);
        }
        
        
        // 获取行级填充方法 
        v_MethodList = MethodReflect.getMethodsIgnoreCase(this.table ,v_MethodName ,v_ParamArr.length);
        if ( v_MethodList.size() == 1 )
        {
            this.fillMethod.setCall(v_MethodList.get(0));
        }
        else if ( v_MethodList.size() > 1 )
        {
            for (int v_Override=0; v_Override<v_MethodList.size() && this.fillMethod.getCall() == null; v_Override++)
            {
                Class<?> [] v_ClassArr = v_MethodList.get(v_Override).getParameterTypes();
                
                if ( this.equalsMethodParamTypes(v_ClassArr ,v_ParamClassArr_int) )
                {
                    this.fillMethod.setCall(v_MethodList.get(v_Override));
                }
                else if ( this.equalsMethodParamTypes(v_ClassArr ,v_ParamClassArr_Integer) )
                {
                    this.fillMethod.setCall(v_MethodList.get(v_Override));
                }
            }
            
            // 行级填充方法有多个重载方法，无法正确识别
            if ( this.cfillMethodArr[0].getCall() == null )
            {
                throw new RuntimeException("Fill method name[" + this.fill + "] have much override methods.");
            }
        }
        else
        {
            throw new RuntimeException("Fill method name[" + this.fill + "] is not exist.");
        }
        
    }
    
    
    
    /**
     * 解释 this.cfill -- 列级对象填充到行级对象的填充方法字符串
     */
    private void parseCFill()
    {
        Pattern      v_Pattern    = Pattern.compile($REGEX_METHOD);
        Matcher      v_Matcher    = v_Pattern.matcher(this.cfill);
        String       v_MethodName = "";
        int          v_EndIndex   = 0;
        List<Method> v_MethodList = null;
        String       v_Params     = null;
        String []    v_ParamArr   = null;
        
        
        // 识别列级填充方法名称
        if ( v_Matcher.find() )
        {
            v_MethodName = v_Matcher.group();
            v_MethodName = v_MethodName.substring(0 ,v_MethodName.length() - 1);
            v_EndIndex   = v_Matcher.end();
        }
        else
        {
            throw new RuntimeException("CFill method name[" + this.cfill + "] is not exist.");
        }
        
        
        if ( "SETTER".equalsIgnoreCase(v_MethodName) )
        {
            this.cfillMethodType = $CFILL_METHOD_VARY;
            
            int v_ColCount = this.dbMetaData.getColumnSize();
            if ( v_ColCount >= 1 )
            {
                this.cfillMethodArr = new XSQLMethod[v_ColCount];
                List<Integer> v_VaildIndexList = new ArrayList<Integer>();
                
                for (int v_ColNo=0; v_ColNo<v_ColCount; v_ColNo++ )
                {
                    String v_ColName = this.dbMetaData.getColumnName(v_ColNo);
                    
                    v_MethodList = MethodReflect.getMethodsIgnoreCase(this.row ,"set" + v_ColName ,1);
                    
                    if ( v_MethodList.size() >= 1 )
                    {
                        this.cfillMethodArr[v_ColNo] = new XSQLMethod();
                        this.cfillMethodArr[v_ColNo].setCall(v_MethodList.get(0));
                        this.cfillMethodArr[v_ColNo].addParam(XSQLMethodParam_CFill.getInstance(XSQLMethodParam_CFill.$CFILL_COL_VALUE));
                        
                        // 按 call 方法的入参类型，决定 java.sql.ResultSet 获取字段值的方法
                        this.cfillMethodArr[v_ColNo].parseResultSet_Getter();  
                        
                        v_VaildIndexList.add(Integer.valueOf(v_ColNo));
                    }
                    else
                    {
                        this.cfillMethodArr[v_ColNo] = null;
                    }
                }
                
                
                this.cfillMethodArr_ValidIndex = new int[v_VaildIndexList.size()];
                for (int v_VaildNo=0; v_VaildNo<v_VaildIndexList.size(); v_VaildNo++)
                {
                    this.cfillMethodArr_ValidIndex[v_VaildNo] = v_VaildIndexList.get(v_VaildNo).intValue();
                }
            }
        }
        else
        {
            this.cfillMethodArr  = new XSQLMethod[]{new XSQLMethod()};
            this.cfillMethodType = $CFILL_METHOD_FIXED;
            
            v_Params   = this.cfill.substring(v_EndIndex ,this.cfill.length() - 1);
            v_ParamArr = v_Params.split(",");
            
            Class<?> [] v_ParamClassArr_int     = new Class[v_ParamArr.length];
            Class<?> [] v_ParamClassArr_Integer = new Class[v_ParamArr.length];
            
            
            // 识别列级填充方法的所有入参
            for (int i=0; i<v_ParamArr.length; i++)
            {
                XSQLMethodParam v_MethodParam = null;
                
                if ( "COLVALUE".equalsIgnoreCase(v_ParamArr[i].trim()) )
                {
                    v_MethodParam = XSQLMethodParam_CFill.getInstance(XSQLMethodParam_CFill.$CFILL_COL_VALUE);
                    
                    v_ParamClassArr_int    [i] = Object.class;
                    v_ParamClassArr_Integer[i] = Object.class;
                }
                else if ( "COLNO".equalsIgnoreCase(v_ParamArr[i].trim()) )
                {
                    v_MethodParam = XSQLMethodParam_CFill.getInstance(XSQLMethodParam_CFill.$CFILL_COL_NO);
                    
                    v_ParamClassArr_int    [i] = int.class;
                    v_ParamClassArr_Integer[i] = Integer.class;
                }
                else if ( "COLNAME".equalsIgnoreCase(v_ParamArr[i].trim()) )
                {
                    v_MethodParam = XSQLMethodParam_CFill.getInstance(XSQLMethodParam_CFill.$CFILL_COL_NAME);
                    
                    v_ParamClassArr_int    [i] = String.class;
                    v_ParamClassArr_Integer[i] = String.class;
                }
                else
                {
                    // 方法的参数形式只能是 colValue、colNo、colName
                    throw new RuntimeException("CFill method[" + this.cfill + "] Parameter is not valid. Parameter only 'colValue' or 'colNo' or 'colName'.");
                }
                
                this.cfillMethodArr[0].addParam(v_MethodParam);
            }
            
            
            // 获取列级填充方法 
            v_MethodList = MethodReflect.getMethodsIgnoreCase(this.row ,v_MethodName ,v_ParamArr.length);
            if ( v_MethodList.size() == 1 )
            {
                this.cfillMethodArr[0].setCall(v_MethodList.get(0));
            }
            else if ( v_MethodList.size() > 1 )
            {
                for (int v_Override=0; v_Override<v_MethodList.size() && this.cfillMethodArr[0].getCall() == null; v_Override++)
                {
                    Class<?> [] v_ClassArr = v_MethodList.get(v_Override).getParameterTypes();
                    
                    if ( this.equalsMethodParamTypes(v_ClassArr ,v_ParamClassArr_int) )
                    {
                        this.cfillMethodArr[0].setCall(v_MethodList.get(v_Override));
                    }
                    else if ( this.equalsMethodParamTypes(v_ClassArr ,v_ParamClassArr_Integer) )
                    {
                        this.cfillMethodArr[0].setCall(v_MethodList.get(v_Override));
                    }
                }
                
                // 列级填充方法有多个重载方法，无法正确识别
                if ( this.cfillMethodArr[0].getCall() == null )
                {
                    throw new RuntimeException("CFill method name[" + this.cfill + "] have much override methods.");
                }
            }
            else
            {
                throw new RuntimeException("CFill method name[" + this.cfill + "] is not exist.");
            }
        
        }
        
    }
    
    
    
    /**
     * 对比方法入参类型是否完全相等。
     * 
     * 对参数类为 Object.class 做相等处理。
     * 
     * @param i_ClassArr_01
     * @param i_ClassArr_02
     * @return
     */
    private boolean equalsMethodParamTypes(Class<?> [] i_ClassArr_01 ,Class<?> [] i_ClassArr_02)
    {
        if ( i_ClassArr_01.length != i_ClassArr_02.length )
        {
            return false;
        }
        
        
        for (int i=0; i<i_ClassArr_01.length; i++)
        {
            if ( i_ClassArr_01[i] == Object.class || i_ClassArr_02[i] == Object.class )
            {
                // Nothing.
            }
            else if ( i_ClassArr_01[i] != i_ClassArr_02[i] )
            {
                return false;
            }
        }
        
        
        return true;
    }
    
    
    
    /**
     * 方法填写有效性的验证
     * 
     * 如：xxx(p1 ,p2 ,... pn)
     * 如：xxx(o1.p1 ,o2.p1 ,... on.pn)
     * 
     * @param i_Text
     * @return
     */
    private boolean methodVerify(String i_Text)
    {
        Pattern v_Pattern = Pattern.compile($REGEX_METHOD_VERIFY);
        Matcher v_Matcher = v_Pattern.matcher(i_Text);
        
        return v_Matcher.find();
    }
    
    
    
    /**
     * 实例化一个表级对象
     * 
     * @return
     * @throws ClassNotFoundException
     */
    private Object newTableObject() throws ClassNotFoundException
    {
        Object v_TableInstance = null;
        
        try
        {
            v_TableInstance = this.table.newInstance();
        }
        catch (Exception exce)
        {
            throw new RuntimeException("Table Class(" + this.table + ") instantiation is error.");
        }
        
        return v_TableInstance;
    }
    
    
    
    /**
     * 实例化一个行级对象
     * 
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException 
     */
    private Object newRowObject() throws ClassNotFoundException, InstantiationException
    {
        Object v_TableInstance = null;
        
        try
        {
            v_TableInstance = this.row.newInstance();
        }
        catch (Exception exce)
        {
            throw new InstantiationException("Row Class(" + this.row + ") instantiation is error.");
        }
        
        return v_TableInstance;
    }
    
    
    
    /**
     * 获取 ClassName 全路径的 Class 类型
     * 
     * @param i_ClassURL
     * @return
     */
    private Class<?> getClass(String i_ClassURL)
    {
        Class<?> v_Class = null;
        
        try
        {
            v_Class = Help.forName(i_ClassURL);
        }
        catch (Exception exce)
        {
            return null;
        }
        
        return v_Class;
    }
    
    
    
    public String getCfill() 
    {
        return cfill;
    }
    
    

    public void setCfill(String i_CFill) 
    {
        if ( Help.isNull(i_CFill) )
        {
            throw new NullPointerException("CFill is null.");
        }
        
        if ( this.methodVerify(i_CFill.trim()) )
        {
            if ( i_CFill.trim().toUpperCase(Locale.ENGLISH).startsWith("SETTER(") )
            {
                if ( !"SETTER(COLVALUE)".equalsIgnoreCase(i_CFill.trim()) )
                {
                    throw new RuntimeException("CFill[" + i_CFill + "] inconformity standard. Setter method parameter Only 'colValue'.");
                }
            }
            
            this.cfill        = i_CFill.trim();
            this.isAgainParse = true;
        }
        else
        {
            // 填充方法不符合规范
            throw new RuntimeException("CFill[" + i_CFill + "] inconformity standard.");
        }
    }
    
    

    public String getFill() 
    {
        return fill;
    }

    
    
    public void setFill(String i_Fill) 
    {
        if ( Help.isNull(i_Fill) )
        {
            throw new NullPointerException("Fill is null.");
        }
        
        if ( this.methodVerify(i_Fill.trim()) )
        {
            this.fill         = i_Fill.trim();
            this.isAgainParse = true;
        }
        else
        {
            // 填充方法不符合规范
            throw new RuntimeException("Fill[" + i_Fill + "] inconformity standard");
        }
    }
    
    

    public Class<?> getRow()
    {
        return this.row;
    }
    
    
    
    public void setRow(String i_Row)
    {
        if ( Help.isNull(i_Row) )
        {
            throw new NullPointerException("Row is null.");
        }
        
        
        Class<?> v_Class = this.getClass(i_Row.trim());
        
        if ( v_Class != null )
        {
            this.row          = v_Class;
            this.isAgainParse = true;
        }
        else
        {
            throw new NullPointerException("Row Class[" + i_Row + "] is not exist.");
        }
    }
    
    
    
    public Class<?> getTable() 
    {
        return this.table;
    }
    
    
    
    public void setTable(String i_Table)
    {
        if ( Help.isNull(i_Table) )
        {
            throw new NullPointerException("Table is null.");
        }
        
        
        Class<?> v_Class = this.getClass(i_Table.trim());
        
        if ( v_Class != null )
        {
            this.table        = v_Class;
            this.isAgainParse = true;
        }
        else
        {
            throw new NullPointerException("Table Class[" + i_Table + "] is not exist.");
        }
    }
    
    
    
    /**
     * 是否分析过？
     * 或是，是否需要重新分析？
     * 
     * @return
     */
    public boolean isParsed()
    {
        return !this.isAgainParse;
    }
    
}
