package org.hy.common.xml.junit;

import java.util.Map;

import org.hy.common.Date;
import org.hy.common.DualChannelPool;
import org.hy.common.Help;
import org.hy.common.PartitionMap;
import org.hy.common.PianoKeyboardPool;
import org.hy.common.QueuePool;
import org.hy.common.StringHelp;
import org.hy.common.TablePartitionRID;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.UnitConvert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;





/**
 * 测试Fel轻量级高效表达式计算引擎
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-01-21
 * @version     v1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JU_Fel
{
    
    @Test
    public void test_Calc()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("C" ,1);
        v_FelContext.set("N" ,2);
        System.out.println(v_Fel.eval("4 * (C + N)"));
        
    }
    
    
    
    @Test
    public void test_Like()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("A" ,"23320102A1");
        System.out.println(v_Fel.eval("A.indexOf(\"32\") == 2"));
        
        v_FelContext.set("A" ,"232X0102A1");
        System.out.println(v_Fel.eval("A.indexOf(\"32\") == 2"));
    }
    
    
    
    @Test
    public void test_IndexOf()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("A" ,"ABCDEFG");
        System.out.println(v_Fel.eval("A.indexOf(\"BCD\") >= 0"));
    }
    
    
    
    @Test
    public void test_Length()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("A" ,"ABCDEFG");
        System.out.println(v_Fel.eval("A.length() >= 0"));
    }
    
    
    
    @Test
    public void test_Fel_Pool()
    {
        this.test_Fel_QueuePool();
        this.test_Fel_DualChannelPool();
        
        this.test_Fel_QueuePool();
        this.test_Fel_DualChannelPool();
    }
    
    
    
    @Test
    public void test_Fel_JU_FelObject()
    {
        Date v_BeginTime = null;
        Date v_EndTime   = null;
        
        
        v_BeginTime = new Date();
        PianoKeyboardPool<JU_FelObject> v_FelPool = new PianoKeyboardPool<JU_FelObject>(JU_FelObject.class ,10);
        v_EndTime = new Date();
        System.out.println("PianoKeyboardPool创建Fel队列缓存池的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        
        
        v_BeginTime = new Date();
        for (int i=1; i<=1000; i++)
        {
            JU_FelObject  v_Fel        = v_FelPool.get();
            
            v_Fel.calc();
        }
        v_EndTime = new Date();
        System.out.println("PianoKeyboardPool执行Fel计算的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        System.out.println("队列池的大小：" + v_FelPool.size());
        System.out.println("取对象的总次数：" + v_FelPool.getReadCount());
        System.out.println("队列池中无资源时，额外创建的对象次数：" + v_FelPool.getReadNewCount());
        System.out.println(Date.toTimeLen(v_FelPool.getReadTime()) + "获取对象的总用时");
        System.out.println(Date.toTimeLen(v_FelPool.getReadNewTime()) + "获取对象时，队列池中无资源时，额外创建对象用时");
        System.out.println(Date.toTimeLen((v_FelPool.getReadTime() - v_FelPool.getReadNewTime())) + "排除额外创建对象的用时");
    }
    
    
    
    
    @Test
    public void test_Fel_PianoKeyboardPool()
    {
        Date v_BeginTime = null;
        Date v_EndTime   = null;
        
        long v_Time = System.currentTimeMillis();
        new FelEngineImpl();
        System.out.println("1个对象" + Date.toTimeLen(System.currentTimeMillis() - v_Time));
        
        v_Time = System.currentTimeMillis();
        new FelEngineImpl();
        System.out.println("2个对象" + Date.toTimeLen(System.currentTimeMillis() - v_Time));
        
        v_Time = System.currentTimeMillis();
        new FelEngineImpl();
        System.out.println("3个对象" + Date.toTimeLen(System.currentTimeMillis() - v_Time));
        
        v_Time = System.currentTimeMillis();
        new FelEngineImpl();
        System.out.println("4个对象" + Date.toTimeLen(System.currentTimeMillis() - v_Time));
        
        long v_Total = 0;
        for (int i=0; i<100; i++)
        {
            long v_Time2 = System.currentTimeMillis();
            new FelEngineImpl();
            v_Total += System.currentTimeMillis() - v_Time2;
        }
        System.out.println("100个对象" + Date.toTimeLen(v_Total));
        
        v_BeginTime = new Date();
        PianoKeyboardPool<FelEngineImpl> v_FelPool = new PianoKeyboardPool<FelEngineImpl>(FelEngineImpl.class ,100);
        v_EndTime = new Date();
        System.out.println("PianoKeyboardPool创建Fel队列缓存池的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        
        
        v_BeginTime = new Date();
        for (int i=1; i<=1000; i++)
        {
            FelEngine  v_Fel        = v_FelPool.get();
            FelContext v_FelContext = v_Fel.getContext();
            
            v_FelContext.set("A" ,i);
            
            v_Fel.eval("A == 1");
        }
        v_EndTime = new Date();
        System.out.println("PianoKeyboardPool执行Fel计算的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        System.out.println("队列池的大小：" + v_FelPool.size());
        System.out.println("取对象的总次数：" + v_FelPool.getReadCount());
        System.out.println("队列池中无资源时，额外创建的对象次数：" + v_FelPool.getReadNewCount());
        System.out.println(Date.toTimeLen(v_FelPool.getReadTime()) + "获取对象的总用时");
        System.out.println(Date.toTimeLen(v_FelPool.getReadNewTime()) + "获取对象时，队列池中无资源时，额外创建对象用时");
        System.out.println(Date.toTimeLen((v_FelPool.getReadTime() - v_FelPool.getReadNewTime())) + "排除额外创建对象的用时");
    }
    
    
    
    @Test
    public void test_Fel_DualChannelPool()
    {
        Date v_BeginTime = null;
        Date v_EndTime   = null;
        
        
        v_BeginTime = new Date();
        DualChannelPool<FelEngineImpl> v_FelPool = new DualChannelPool<FelEngineImpl>(FelEngineImpl.class ,1000 ,1000);
        v_EndTime = new Date();
        System.out.println("DualChannelPool创建Fel队列缓存池的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        
        
        v_BeginTime = new Date();
        for (int i=1; i<=1000; i++)
        {
            FelEngine  v_Fel        = v_FelPool.get();
            FelContext v_FelContext = v_Fel.getContext();
            
            v_FelContext.set("A" ,i);
            
            v_Fel.eval("A == 1");
        }
        v_EndTime = new Date();
        System.out.println("DualChannelPool执行Fel计算的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        System.out.println(v_FelPool.size());
    }
    
    
    
    @Test
    public void test_Fel_QueuePool()
    {
        Date v_BeginTime = null;
        Date v_EndTime   = null;
        
        
        v_BeginTime = new Date();
        QueuePool<FelEngineImpl> v_FelPool = new QueuePool<FelEngineImpl>(FelEngineImpl.class ,1100 ,1000);
        v_EndTime = new Date();
        System.out.println("QueuePool创建Fel队列缓存池的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        
        
        v_BeginTime = new Date();
        for (int i=1; i<=1000; i++)
        {
            FelEngine  v_Fel        = v_FelPool.get();
            FelContext v_FelContext = v_Fel.getContext();
            
            v_FelContext.set("A" ,i);
            
            v_Fel.eval("A == 1");
        }
        v_EndTime = new Date();
        System.out.println("QueuePool执行Fel计算的用时\t" + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        System.out.println(v_FelPool.size());
    }
    
    
    
    /**
     * 性能测试
     */
    @Test
    public void test_Fel_BigData()
    {
        Date v_BeginTime = null;
        Date v_EndTime   = null;
        int  v_Size      = 1000;
        
        v_BeginTime = new Date();
        for (int i=1; i<=v_Size; i++)
        {
            FelEngine  v_Fel        = new FelEngineImpl();
            FelContext v_FelContext = v_Fel.getContext();
            
            v_FelContext.set("A" ,i);
            
            v_Fel.eval("A == 1");
        }
        v_EndTime = new Date();
        System.out.println(Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        
        
        
        FelEngine  v_Fel        = new FelEngineImpl();
        v_BeginTime = new Date();
        for (int i=1; i<=v_Size; i++)
        {
            FelContext v_FelContext = v_Fel.getContext();
            v_FelContext.set("A" ,i);
            
            v_Fel.eval("A == 1");
        }
        v_EndTime = new Date();
        System.out.println(Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        
        
        
        v_Fel        = new FelEngineImpl();
        FelContext v_FelContext = v_Fel.getContext();
        v_BeginTime = new Date();
        for (int i=1; i<=v_Size; i++)
        {
            v_FelContext.set("A" ,i);
            
            v_Fel.eval("A == 1");
        }
        v_EndTime = new Date();
        System.out.println(Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
    }
    
    
    
    @Test
    public void test_Fel_IF()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("tableName" ,"ORDER_PRODUCT_PLAN_DETAIL");
        //v_FelContext.set("接线盒" ,"304");
        v_FelContext.set("A" ,new Date());
        //v_FelContext.set("fcProject.actionType" ,"NEW");
        
        System.out.println(v_Fel.eval("tableName == 'ORDER_PRODUCT_PLAN_DETAIL'"));
        System.out.println(v_Fel.eval("A == NULL || A == ''"));
        System.out.println(v_Fel.eval("A != NULL && A != ''"));
        System.out.println(v_Fel.eval("接线盒==304"));
        
        System.out.println(v_Fel.eval("fcProject.actionType == 'NEW'"));
    }
    
    
    
    /**
     * 解释占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     */
    public void test001_parsePlaceholders()
    {
        Help.print(StringHelp.parsePlaceholders(":c01=='1' && :c02=='2'"));
    }
    
    
    
    public void test002_Fel()
    {
        String                        v_Placeholders   = ":c01=='1' && :c02=='1'";
        PartitionMap<String ,Integer> v_PlaceholderMap = StringHelp.parsePlaceholders(v_Placeholders);
        FelEngine                     v_Fel            = new FelEngineImpl();
        FelContext                    v_FelContext     = v_Fel.getContext();
        int                           v_Index          = 2;
        
        for (String v_Key : v_PlaceholderMap.keySet())
        {
            v_FelContext.set(v_Key ,String.valueOf(v_Index--));
            v_Placeholders = StringHelp.replaceAll(v_Placeholders ,":" + v_Key ,v_Key);
        }
        
        System.out.println(v_Placeholders);
        System.out.println(v_Fel.eval(v_Placeholders));
    }
    
    
    
    /**
     * 动态表达式
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-29
     * @version     v1.0
     */
    public void test003_Fel()
    {
        FelEngine  v_Fel        = new FelEngineImpl();
        FelContext v_FelContext = v_Fel.getContext();
        v_FelContext.set("单价", 2.12345);
        v_FelContext.set("数量", 10);
        v_FelContext.set("运费", 10);
        Object result = v_Fel.eval("单价*(数量+运费)");
        System.out.println(result);
    }
    
    
    
    /**
     * 测试Fel不相等的判断
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-03-03
     * @version     v1.0
     *
     */
    public void test004_Fel()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("No02_IsExists" ,1);
        v_FelContext.set("No02_Train_No" ,"123");
        v_FelContext.set("TRAIN_NO"      ,"123");
        
        System.out.println(v_Fel.eval("No02_IsExists == 1 && No02_Train_No != TRAIN_NO"));
    }
    
    
    
    /**
     * 测试Fel对中文的支持
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-09-12
     * @version     v1.0
     *
     */
    @Test
    public void test005_中文()
    {
        FelEngine           v_Fel            = new FelEngineImpl();
        FelContext          v_FelContext     = v_Fel.getContext();
        
        v_FelContext.set("中文变量" ,"中国");
        
        System.out.println(v_Fel.eval("中文变量 == '中国'"));
    }
    
    
    
    /**
     * 单位转换测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-29
     * @version     v1.0
     *
     */
    @Test
    public void test005_UnitConvert()
    {
        TablePartitionRID<String ,Number> v_JUDatas    = new TablePartitionRID<String ,Number>();
        String                            v_SourceUnit = null;
        String                            v_TargetUnit = null;
        Number                            v_Value      = null;
        Number                            v_Ret        = null;
        Number                            v_RetRev     = null;
        
        v_JUDatas.putRow("摄氏度"       ,"华氏度"        ,36.6);
        v_JUDatas.putRow("毫米"         ,"米"           ,123);
        v_JUDatas.putRow("MPa.g"       ,"MPa.a"        ,0.45);
        
        for (Map.Entry<String ,Map<String ,Number>> v_PData : v_JUDatas.entrySet())
        {
            v_SourceUnit = v_PData.getKey();
            
            for (Map.Entry<String ,Number> v_RData : v_PData.getValue().entrySet())
            {
                v_TargetUnit = v_RData.getKey();
                v_Value      = v_RData.getValue();
                v_Ret        = UnitConvert.convert(v_SourceUnit ,v_TargetUnit ,v_Value);
                v_RetRev     = UnitConvert.convert(v_TargetUnit ,v_SourceUnit ,v_Ret);
                
                System.out.println("-- " + v_Value + v_SourceUnit + " = " + v_Ret    + v_TargetUnit);
                System.out.println("-- " + v_Ret   + v_TargetUnit + " = " + v_RetRev + v_SourceUnit);
                System.out.println();
            }
        }
    }
    
    
    
    /**
     * 单位转换测试。
     * 
     * 输出按 1 相互转换单位的数据。
     * 当相互转换后的结果不等于 1 时，就表示是有错误的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-29
     * @version     v1.0
     *
     */
    @SuppressWarnings("unchecked")
    public void test006_UnitConvert()
    {
        // 先初始化一下数据
        UnitConvert.convert("1" ,"2" ,"3");
        System.out.println(UnitConvert.convert("m^3/h"       ,"kg/h"         ,"4000" ,"61"));
        System.out.println(UnitConvert.convert("操作密度"     ,"分子量"         ,"100" ,"-3" ,"2.8"));
        
        TablePartitionRID<String ,String> v_JUDatas    = (TablePartitionRID<String ,String>)XJava.getObject("UnitConvertExpressions");
        String                            v_SourceUnit = null;
        String                            v_TargetUnit = null;
        Number                            v_Value      = null;
        Number                            v_Ret        = null;
        Number                            v_RetRev     = null;
        int                               v_Digit01    = 9;     // 第一次，保留几位小数
        int                               v_Digit02    = 4;     // 第二次，保留几位小数
        int                               v_Count      = 0;
        
        System.out.println("-- " + Date.getNowTime().getFullMilli() + " ：开始遍历");
        
        for (Map.Entry<String ,Map<String ,String>> v_PData : v_JUDatas.entrySet())
        {
            v_SourceUnit = v_PData.getKey();
            
            for (Map.Entry<String ,String> v_RData : v_PData.getValue().entrySet())
            {
                // System.out.println("-- " + v_RData.getValue());
                
                v_TargetUnit = v_RData.getKey();
                v_Value      = 1;
                v_Ret        = UnitConvert.convert(v_SourceUnit ,v_TargetUnit ,v_Digit01 ,v_Value ,1);
                v_RetRev     = UnitConvert.convert(v_TargetUnit ,v_SourceUnit ,v_Digit02 ,v_Ret   ,1);
                
//                if ( v_RetRev.doubleValue() != 1 )
//                {
                    v_Count++;
                    System.out.println("-- " + v_Value + v_SourceUnit + " = " + v_Ret    + v_TargetUnit);
                    System.out.println("-- " + v_Ret   + v_TargetUnit + " = " + v_RetRev + v_SourceUnit);
                    System.out.println();
//                }
            }
        }
        
        System.out.println("-- " + Date.getNowTime().getFullMilli() + " ：共 " + v_Count + " 组");
    }
    
}
