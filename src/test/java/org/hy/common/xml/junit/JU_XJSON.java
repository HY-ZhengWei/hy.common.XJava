package org.hy.common.xml.junit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.ExpireMap;
import org.hy.common.Help;
import org.hy.common.Queue;
import org.hy.common.Queue.QueueType;
import org.hy.common.StringHelp;
import org.hy.common.file.FileHelp;
import org.hy.common.net.data.Command;
import org.hy.common.thread.Jobs;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJSONToJava;
import org.hy.common.xml.log.Logger;
import org.junit.Test;

import net.minidev.json.JSONStyle;





/**
 * 测试单元
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-10
 * @version     v1.0
 */
public class JU_XJSON
{
    
    private static final Logger $Logger = new Logger(JU_XJSON.class ,true);
    
    private String     valueString;
    
    private String     valueStrDate;
    
    private Date       valueDate;
    
    private Object     valueObject;
    
    private byte    [] valueByteArr;
    
    private int     [] valueIntArr;
    
    private Integer [] valueIntegerArr;
    
    private String  [] valueStringArr;
    
    private Boolean    upper;
    
    
    
    @Test
    public void 剪影字幕提取() throws Exception
    {
        FileHelp          v_FileHelp = new FileHelp();
        String            v_FileName = "C:\\Users\\hyzhe\\Desktop\\draft_content.json";   // 请将剪影字幕转一下格式后再使用哈
        String            v_JsonData = v_FileHelp.getContent(v_FileName ,"GBK");
        XJSON             v_XJson    = new XJSON();
        
        v_JsonData = StringHelp.replaceAll(v_JsonData ,"�" ,"");
        
        JU_JSON_Materials v_Data = (JU_JSON_Materials)v_XJson.toJava(v_JsonData ,"materials" ,JU_JSON_Materials.class);
        
        for (JU_JSON_Text v_Text : v_Data.getTexts())
        {
            System.out.println(v_Text.getContent());
        }
    }
    
    
    
    /**
     * 测试Getter方法的Object返回结果
     * 
     * 如下报文是Java生成的报文
        {
            "valueDate": "2021-10-08 11:06:16",
            "valueObject@java.util.HashMap": {
                "b@org.hy.common.xml.junit.JU_XJSON": {
                    "valueDate": "2021-10-07 08:30:00",
                    "valueObject@java.lang.String": "DataB：常规对象类型",
                    "valueString": "DataB：子对象中的字符串"
                },
                "c@org.hy.common.xml.junit.JU_XJSON": {
                    "valueDate": "2021-10-08 09:00:00",
                    "valueObject@java.util.ArrayList@org.hy.common.xml.junit.JU_XJSON": [
                        {
                            "valueDate": "2021-10-08 10:00:00",
                            "valueObject@java.lang.String": "DataD：常规对象类型",
                            "valueString": "DataD：子对象中的字符串"
                        }
                    ],
                    "valueString": "DataC：子对象中的字符串"
                }
            },
            "valueString": "字符串"
        }
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-09-30
     * @version     v1.0
     * 
     * @throws Exception
     */
    @Test
    public void test_Json() throws Exception
    {
        List<Object>        v_DataList = new ArrayList<Object>();
        Map<String ,Object> v_DataMap  = new HashMap<String ,Object>();
        JU_XJSON            v_DataA    = new JU_XJSON();
        JU_XJSON            v_DataB    = new JU_XJSON();
        JU_XJSON            v_DataC    = new JU_XJSON();
        JU_XJSON            v_DataD    = new JU_XJSON();
        JU_XJSON            v_DataNew  = null;
        
        v_DataD.setValueString("DataD：子对象中的字符串");
        v_DataD.setValueDate(new Date("2021-10-08 10:00:00"));
        v_DataD.setValueObject("DataD：常规对象类型");
        v_DataD.setValueByteArr(v_DataD.getValueObject().toString().getBytes());
        v_DataD.setValueIntArr(new int[]{1 ,2});
        v_DataD.setValueIntegerArr(new Integer[]{1 ,2});
        v_DataD.setValueStringArr(new String[] {"D" ,"Arr"});
        v_DataD.setValueStrDate("2022/05/05");
        
        v_DataList.add(v_DataD);
        
        v_DataC.setValueString("DataC：子对象中的字符串");
        v_DataC.setValueDate(new Date("2021-10-08 09:00:00"));
        v_DataC.setValueObject(v_DataList);                    // 对象套娃
        v_DataC.setValueByteArr(v_DataC.getValueObject().toString().getBytes());
        v_DataC.setValueIntArr(new int[]{3 ,4});
        v_DataC.setValueIntegerArr(new Integer[]{3 ,4});
        v_DataC.setValueStringArr(new String[] {"C" ,"Arr"});
        
        v_DataB.setValueString("DataB：子对象中的字符串");
        v_DataB.setValueDate(new Date("2021-10-07 08:30:00"));
        v_DataB.setValueObject("DataB：常规对象类型");
        v_DataB.setValueByteArr(v_DataB.getValueObject().toString().getBytes());
        v_DataB.setValueIntArr(new int[]{5 ,6});
        v_DataB.setValueIntegerArr(new Integer[]{5 ,6});
        v_DataB.setValueStringArr(new String[] {"B" ,"Arr"});
        
        v_DataMap.put("b" ,v_DataB);
        v_DataMap.put("c" ,v_DataC);
        
        v_DataA.setValueString("字符串");
        v_DataA.setValueDate(new Date());
        v_DataA.setValueObject(v_DataMap);                     // 对象套娃
        v_DataA.setValueByteArr(v_DataA.getValueObject().toString().getBytes());
        v_DataA.setValueIntArr(new int[]{7 ,8});
        v_DataA.setValueIntegerArr(new Integer[]{7 ,8});
        v_DataA.setValueStringArr(new String[] {"A" ,"Arr"});
        
        XJSON    v_Json       = new XJSON();
        String   v_JsonString = "";
        
        $Logger.info("按普通Json转换");
        v_JsonString = v_Json.toJson(v_DataA ,"data").toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,"data" ,JU_XJSON.class);
        
        v_JsonString = v_Json.toJson(v_DataA).toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,JU_XJSON.class);

        $Logger.info(v_JsonString);
        $Logger.info(v_DataNew.getValueString());
        $Logger.info(v_DataNew.getValueDate());
        $Logger.info(v_DataNew.getValueObject() + " -> " + v_DataNew.getValueObject().getClass().getName());
        
        
        
        $Logger.info("\n\n支持Object对象的Json转换");
        v_Json.setSerializable(true);
        
        v_JsonString = v_Json.toJson(v_DataA).toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,JU_XJSON.class);
        
        $Logger.info(v_JsonString);
        $Logger.info(v_DataNew.getValueString());
        $Logger.info(v_DataNew.getValueDate());
        $Logger.info(v_DataNew.getValueObject() + " -> " + v_DataNew.getValueObject().getClass().getName());
    }
    
    
    
    /**
     * 数组JSON转对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-04-21
     * @version     v1.0
     *
     * @throws Exception
     */
    @Test
    public void test_JsonArray001() throws Exception
    {
        String v_JsonString = """
[
    {
        "code": "A001",
        "name": "螺丝"
    },
    {
        "code": "B001",
        "name": "螺母"
    }
]
                              """;
        XJSON  v_Json = new XJSON();
        Object v_Java = v_Json.toJava(v_JsonString);
        System.out.println(v_Java);
    }
    
    
    
    /**
     * 数组JSON转对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-04-21
     * @version     v1.0
     *
     * @throws Exception
     */
    @Test
    public void test_JsonArray002() throws Exception
    {
        String v_JsonString = """
[
    {
        "code": "A001",
        "name": "螺丝",
        "else": {
            "weight": 15,
            "unit": "g" 
        } 
    },
    {
        "code": "B001",
        "name": "螺母",
        "else": {
            "weight": 30,
            "unit": "g" 
        } 
    }
]
                              """;
        XJSON  v_Json = new XJSON();
        Object v_Java = v_Json.toJava(v_JsonString ,"else");
        System.out.println(v_Java);
    }
    
    
    
    /**
     * 数组JSON转对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-04-21
     * @version     v1.0
     *
     * @throws Exception
     */
    @Test
    public void test_JsonArray003() throws Exception
    {
        String v_JsonString = """
{
    "datas": [
        {
            "code": "A001",
            "name": "螺丝"
        },
        {
            "code": "B001",
            "name": "螺母"
        }
    ]
}
                              """;
        XJSON  v_Json = new XJSON();
        Object v_Java = v_Json.toJava(v_JsonString ,"datas");
        System.out.println(v_Java);
    }
    
    
    
    @Test
    public void test_JsonDate() throws Exception
    {
        JU_XJSON v_DataA   = new JU_XJSON();
        JU_XJSON v_DataNew = null;
        
        v_DataA.setValueString("日期字符串的/号符转义");
        v_DataA.setValueStrDate("2022/05/05");
        $Logger.info(v_DataA.getValueStrDate());
        
        
        XJSON    v_Json       = new XJSON();
        String   v_JsonString = "";
        
        $Logger.info("按种样式的区别");
        $Logger.info(v_Json.toJson(v_DataA ,"data").toJSONString());
        $Logger.info(v_Json.toJson(v_DataA ,"data").toJSONString(new JSONStyle(JSONStyle.FLAG_AGRESSIVE)));
        $Logger.info(v_Json.toJson(v_DataA ,"data").toJSONString(new JSONStyle(JSONStyle.FLAG_IGNORE_NULL)));
        $Logger.info(v_Json.toJson(v_DataA ,"data").toJSONString(new JSONStyle(JSONStyle.FLAG_PROTECT_4WEB)));
        $Logger.info(v_Json.toJson(v_DataA ,"data").toJSONString(new JSONStyle(JSONStyle.FLAG_PROTECT_KEYS)));
        $Logger.info(v_Json.toJson(v_DataA ,"data").toJSONString(new JSONStyle(JSONStyle.FLAG_PROTECT_VALUES)));
        
        v_JsonString = v_Json.toJson(v_DataA ,"data").toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,"data" ,JU_XJSON.class);
        
        v_JsonString = v_Json.toJson(v_DataA).toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,JU_XJSON.class);
        

        $Logger.info(v_JsonString);
        $Logger.info(v_DataNew.getValueString());
        $Logger.info(v_DataNew.getValueDate());
        $Logger.info(v_DataNew.getValueObject() + " -> " + v_DataNew.getValueObject().getClass().getName());
    }
    
    
    
    @Test
    public void test_JsonToJava_20211209() throws Exception
    {
        XJSON   v_XJson   = new XJSON();
        String  v_JsonStr = "";
        Command v_Command = null;
        
        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"cloneFileUpload\",\"params\":{\"0@java.lang.String\":\"$WebHome\\/META-INF\",\"1@org.hy.common.file.FileDataPacket\":{\"dataByte\":\"TWFuaWZlc3QtVmVyc2lvbjogMS4wDQpCdWlsZC1KZGstU3BlYzogMTUNCkltcGxlbWVudGF0aW9uLVRpdGxlOiB0aW1pbmcNCkltcGxlbWVudGF0aW9uLVZlcnNpb246IDEuMC4wDQpDcmVhdGVkLUJ5OiBNYXZlbiBJbnRlZ3JhdGlvbiBmb3IgRWNsaXBzZQ0KDQo=\",\"dataCount\":\"1\",\"dataNo\":\"1\",\"name\":\"MANIFEST.MF\",\"size\":\"149\"}}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);
        
        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"zipFile\",\"params\":{\"0@java.lang.String\":\"$WebHome\",\"1@java.lang.String\":\"windows\",\"2@java.lang.String\":\"20211213153722613\"}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);
        
        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"calcFileSize\",\"params\":{\"0@java.lang.String\":\"$WebHome\",\"1@java.lang.String\":\"windows\"}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);

        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"cloneFileUpload\",\"params\":{\"0@java.lang.String\":\"$WebHome\",\"1@org.hy.common.file.FileDataPacket\":{\"dataCount\":\"1\",\"dataNo\":\"1\",\"name\":\"video.html\",\"size\":\"1376\"}}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);
        
        $Logger.info(v_Command);
    }
    
    
    
    @Test
    public void test_JsonToJava_20260425() throws Exception
    {
        String v_Json = """
{
    "retInt": 200,
    "retList": [
        {
            "Z18": 0,
            "RZR14": "0.000000",
            "Z17": 0,
            "RZR13": "0.000000",
            "RZR16": "0.000000",
            "Z19": 0,
            "RZR15": "0.000000",
            "RZR10": "0.000000",
            "RZR12": "0.000000",
            "RZR11": "0.000000",
            "Z21": 0,
            "Z20": 0,
            "Z23": 0,
            "Z22": 0,
            "Z25": 0,
            "RZR18": "0.000000",
            "Z24": 0,
            "RZR17": "0.000000",
            "Z27": 0,
            "Z26": 0,
            "RZR19": "0.000000",
            "Z29": 0,
            "Z28": 0,
            "RYR30": "0.000000",
            "Z30": 0,
            "Y11": 0,
            "Y10": 0,
            "Y13": 0,
            "Y12": 0,
            "Y15": 0,
            "Y14": 0,
            "Y17": 0,
            "Y16": 0,
            "Y19": 0,
            "Y18": 0,
            "RZ10": "0.000000",
            "RZR1": "-3.140000",
            "RZ17": "0.000000",
            "RZR8": "0.000000",
            "RZ18": "0.000000",
            "RZR9": "0.000000",
            "RZ15": "0.000000",
            "RZR6": "0.000000",
            "RZR30": "0.000000",
            "RZ16": "0.000000",
            "RZR7": "0.000000",
            "RZ13": "0.000000",
            "RZR4": "-1.176000",
            "RZ14": "0.000000",
            "RZR5": "1.301000",
            "RZ11": "0.000000",
            "RZR2": "0.121000",
            "RZ12": "0.000000",
            "RZR3": "2.370000",
            "Y20": 0,
            "RZ19": "0.000000",
            "Y22": 0,
            "Y21": 0,
            "Y24": 0,
            "Y23": 0,
            "Y26": 0,
            "Y25": 0,
            "Y28": 0,
            "Y27": 0,
            "RZR25": "0.000000",
            "Y29": 0,
            "RZR24": "0.000000",
            "RZR27": "0.000000",
            "RZR26": "0.000000",
            "RZR21": "0.000000",
            "RZR20": "0.000000",
            "RZR23": "0.000000",
            "RZR22": "0.000000",
            "X10": 0,
            "Y30": 0,
            "X12": 0,
            "X11": 0,
            "X14": 0,
            "X13": 0,
            "X16": 0,
            "RZR29": "0.000000",
            "X15": 0,
            "RZR28": "0.000000",
            "X18": 0,
            "X17": 0,
            "RY10": "0.000000",
            "RYR1": "0.007000",
            "X19": 0,
            "RY11": "0.000000",
            "RYR2": "0.038000",
            "RZ30": "0.000000",
            "RXR30": "0.000000",
            "RY18": "0.000000",
            "RYR9": "0.000000",
            "RY19": "0.000000",
            "RY16": "0.000000",
            "RYR7": "0.000000",
            "RY17": "0.000000",
            "RYR8": "0.000000",
            "RY14": "0.000000",
            "RYR5": "-1.125000",
            "RY15": "0.000000",
            "RYR6": "0.000000",
            "RY12": "0.000000",
            "RYR3": "-2.017000",
            "RY13": "0.000000",
            "RYR4": "1.192000",
            "X21": 0,
            "X20": 0,
            "X23": 0,
            "X22": 0,
            "X25": 0,
            "X24": 0,
            "X27": 0,
            "X26": 0,
            "X29": 0,
            "X28": 0,
            "RZ20": "0.000000",
            "RXR22": "0.000000",
            "RZ21": "0.000000",
            "RXR23": "0.000000",
            "RXR24": "0.000000",
            "RXR25": "0.000000",
            "RXR20": "0.000000",
            "RXR21": "0.000000",
            "RZ28": "0.000000",
            "RZ29": "0.000000",
            "RX1": "0.177000",
            "RZ26": "0.000000",
            "ISDEL": 0,
            "RZ27": "0.000000",
            "RX3": "0.121000",
            "RZ24": "0.000000",
            "RX2": "-0.087000",
            "RZ25": "0.000000",
            "RX5": "0.082000",
            "RZ22": "0.000000",
            "RX4": "0.218000",
            "RZ23": "0.000000",
            "RX7": "0.000000",
            "RX6": "0.000000",
            "RX9": "0.000000",
            "RX8": "0.000000",
            "X30": 0,
            "RXR26": "0.000000",
            "RXR27": "0.000000",
            "RXR28": "0.000000",
            "RXR29": "0.000000",
            "RX11": "0.000000",
            "RXR2": "-1.388000",
            "RYR24": "0.000000",
            "RX12": "0.000000",
            "RXR3": "-0.039000",
            "RYR23": "0.000000",
            "RY30": "0.000000",
            "RYR26": "0.000000",
            "RX10": "0.000000",
            "RXR1": "0.004000",
            "RYR25": "0.000000",
            "RYR20": "0.000000",
            "RYR22": "0.000000",
            "RYR21": "0.000000",
            "RX19": "0.000000",
            "RX17": "0.000000",
            "RY2": "-0.154000",
            "RXR8": "0.000000",
            "RX18": "0.000000",
            "RY1": "-0.115000",
            "RXR9": "0.000000",
            "RX15": "0.000000",
            "RY4": "0.123000",
            "RXR6": "0.000000",
            "RX16": "0.000000",
            "RY3": "-0.016000",
            "RXR7": "0.000000",
            "RX13": "0.000000",
            "RY6": "0.000000",
            "RXR4": "-1.243000",
            "RX14": "0.000000",
            "RY5": "-0.134000",
            "RXR5": "-1.067000",
            "RY8": "0.000000",
            "RY7": "0.000000",
            "RY9": "0.000000",
            "X1": -1002575,
            "X2": -1274704,
            "X3": -1071611,
            "SAFEX": 0,
            "X4": -369994,
            "SAFEY": 0,
            "RYR28": "0.000000",
            "X5": -1699957,
            "SAFEZ": 0,
            "RYR27": "0.000000",
            "X6": 0,
            "X7": 0,
            "RYR29": "0.000000",
            "X8": 0,
            "RY21": "0.000000",
            "RYR13": "0.000000",
            "X9": 0,
            "RY22": "0.000000",
            "RYR12": "0.000000",
            "RYR15": "0.000000",
            "RY20": "0.000000",
            "RYR14": "0.000000",
            "RYR11": "0.000000",
            "RYR10": "0.000000",
            "RY29": "0.000000",
            "RZ1": "0.462000",
            "RY27": "0.000000",
            "RZ3": "1.027000",
            "RY28": "0.000000",
            "RZ2": "1.046000",
            "RY25": "0.000000",
            "RZ5": "1.032000",
            "RY26": "0.000000",
            "RZ4": "1.056000",
            "RY23": "0.000000",
            "RZ7": "0.000000",
            "RY24": "0.000000",
            "RZ6": "0.000000",
            "RZ9": "0.000000",
            "RZ8": "0.000000",
            "Y1": -644421,
            "Y2": -1312523,
            "Y3": -57587,
            "Y4": -937474,
            "Y5": -587617,
            "RYR17": "0.000000",
            "Y6": 0,
            "RYR16": "0.000000",
            "Y7": 0,
            "RYR19": "0.000000",
            "Y8": 0,
            "RYR18": "0.000000",
            "Y9": 0,
            "RX30": "0.000000",
            "Z1": 0,
            "Z2": 0,
            "Z3": 0,
            "Z4": 0,
            "ID": "20260424082421090003900a73785e11cdc92f9",
            "Z5": 0,
            "Z6": 0,
            "Z7": 0,
            "Z8": 0,
            "Z9": 0,
            "RX22": "0.000000",
            "RX23": "0.000000",
            "RX20": "0.000000",
            "RX21": "0.000000",
            "RX28": "0.000000",
            "RX29": "0.000000",
            "RX26": "0.000000",
            "RX27": "0.000000",
            "RX24": "0.000000",
            "RX25": "0.000000",
            "RXR11": "0.000000",
            "RXR12": "0.000000",
            "RXR13": "0.000000",
            "RXR14": "0.000000",
            "RXR10": "0.000000",
            "COMMENT": "工艺配方说明",
            "RXR19": "0.000000",
            "RXR15": "0.000000",
            "RXR16": "0.000000",
            "RXR17": "0.000000",
            "RXR18": "0.000000",
            "PRODUCTNO": "产品编号",
            "POINTCOUNT": 5,
            "Z10": 0,
            "Z12": 0,
            "Z11": 0,
            "Z14": 0,
            "Z13": 0,
            "Z16": 0,
            "Z15": 0
        }
    ]
}
                        """;
        XJSON  v_XJson = new XJSON();
        
        ReturnData v_Data = (ReturnData)v_XJson.toJava(v_Json ,ReturnData.class);
        System.out.println(v_Data);
    }
    
    
    
    @Test
    public void test_executeToJava() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
    {
        Object   v_Value      = Long.valueOf(1);
        Object   v_JavaValue  = null;
        Class<?> v_ParamClass = Long.class;
        
        v_JavaValue = XJSONToJava.executeToJava(new XJSON() ,v_Value.toString() ,v_ParamClass ,null);
        
        $Logger.info(v_JavaValue);
    }
    
    
    
    /**
     * null转Java的测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-10
     * @version     v1.0
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_001() throws Exception
    {
        String json = "{\"maching\":[{\"PART_PLAN_NO\":null,\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/25\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-24\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0014\",\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/21\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-29\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"AM30781-205-103C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0567\",\"GY_TYPE\":\"部件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\" \",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"上膜盖部件\",\"RECEIVE_MATERIAL_QTY\":\" \",\"ITEM_CODE\":\"8Z51-5100-1\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0566\",\"GY_TYPE\":\"下料件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-21\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"推杆\",\"RECEIVE_MATERIAL_QTY\":\"10\",\"ITEM_CODE\":\"8Z52-106-405\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0004\",\"GY_TYPE\":\"精铸\",\"FINISH_DATE\":\"2018\\/06\\/01\",\"FINISH_QTY\":\"5\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-23\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"RECEIVE_MATERIAL_QTY\":\"5\",\"ITEM_CODE\":\"ABM30421-207-004\",\"CL\":\" \"}],\"casting\":[{\"ZJ_KF_RKRQ2\":\"2018-04-20 17:14:53\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0004\",\"ZJ_WGS\":\"5\",\"ZJ_TCS\":\"5\",\"ZJ_SX\":\"5\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"ITEM_CODE\":\"ABM30421-207-004\",\"ZJ_KF_RKS\":\"5\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-3-0013\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2018-05-29 13:46:08\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0014\",\"ZJ_WGS\":\"1\",\"ZJ_TCS\":\"1\",\"ZJ_SX\":\"1\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"ITEM_CODE\":\"AM30781-205-103C\",\"ZJ_KF_RKS\":\"1\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"14\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"6\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"}]}";
        
        XJSON v_XJson = new XJSON();
        
        // 当需要Map.value为NULL时，请在转换前设置此句this.setObjectClass(HashMap.class);
        // 否则，NULL的Json字符串将转成""空字符串写入Hashtable中。
        v_XJson.setObjectClass(HashMap.class);
        
        Map<String, Object> v_Datas = (Map<String, Object>)v_XJson.toJava(json);
        
        Help.print(v_Datas);
    }
    
    
    
    @Test
    public void test_IsJson()
    {
        String v_Json = "{\"maching\":[{\"PART_PLAN_NO\":null,\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/25\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-24\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0014\",\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/21\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-29\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"AM30781-205-103C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0567\",\"GY_TYPE\":\"部件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\" \",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"上膜盖部件\",\"RECEIVE_MATERIAL_QTY\":\" \",\"ITEM_CODE\":\"8Z51-5100-1\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0566\",\"GY_TYPE\":\"下料件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-21\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"推杆\",\"RECEIVE_MATERIAL_QTY\":\"10\",\"ITEM_CODE\":\"8Z52-106-405\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0004\",\"GY_TYPE\":\"精铸\",\"FINISH_DATE\":\"2018\\/06\\/01\",\"FINISH_QTY\":\"5\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-23\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"RECEIVE_MATERIAL_QTY\":\"5\",\"ITEM_CODE\":\"ABM30421-207-004\",\"CL\":\" \"}],\"casting\":[{\"ZJ_KF_RKRQ2\":\"2018-04-20 17:14:53\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0004\",\"ZJ_WGS\":\"5\",\"ZJ_TCS\":\"5\",\"ZJ_SX\":\"5\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"ITEM_CODE\":\"ABM30421-207-004\",\"ZJ_KF_RKS\":\"5\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-3-0013\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2018-05-29 13:46:08\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0014\",\"ZJ_WGS\":\"1\",\"ZJ_TCS\":\"1\",\"ZJ_SX\":\"1\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"ITEM_CODE\":\"AM30781-205-103C\",\"ZJ_KF_RKS\":\"1\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"14\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"6\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"}]}";
        
        $Logger.info(XJSON.isJson(v_Json));
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_复杂对象ToJson() throws Exception
    {
        Queue<String> v_Data  = new Queue<String>(QueueType.$First_IN_Last_OUT);
        Queue<String> v_New   = null;
        XJSON         v_XJson = new XJSON();
        
        String v_Json = v_XJson.toJson(v_Data).toJSONString();
        v_New = (Queue<String>)v_XJson.toJava(v_Json ,v_Data.getClass());
        $Logger.info(v_Json);
        $Logger.info(v_New);
        $Logger.info("由构造器初始化的对象，并且未开放对应的Setter方法时，无法做到Json的反序列化"); // TODO
    }
    
    
    
    @Test
    public void test_复杂对象JobsToJson() throws Exception
    {
        Jobs  v_Data  = new Jobs();
        Jobs  v_New   = null;
        XJSON v_XJson = new XJSON();
        
        String v_Json = v_XJson.toJson(v_Data).toJSONString();
        v_New = (Jobs)v_XJson.toJava(v_Json ,v_Data.getClass());
        $Logger.info(v_Json);
        $Logger.info(v_New);
    }
    
    
    
    @Test
    public void test_JsonToMap() throws Exception
    {
        String v_Json  = "{\"body\":{\"compareValue\":\"\",\"currentPage\":\"\",\"limit\":\"\",\"pagePerCount\":\"\",\"rowKey\":\"\",\"start\":\"\",\"startIndex\":\"\",\"syncMacCount\":\"50000\",\"t\":\"\",\"token\":\"\"},\"encry\":\"\",\"format\":\"\",\"rc\":\"0\",\"result\":\"\",\"ri\":\"\",\"serialNo\":\"\",\"session\":\"\",\"sid\":\"I012A001\",\"sidv\":\"\",\"sign\":\"\",\"sysid\":\"capmsplan\",\"tokenSec\":\"\"}";
        XJSON  v_XJson = new XJSON();
        
        Map<? ,?> v_Mes = (Map<? ,?>)v_XJson.parser(v_Json);
        
        $Logger.info(v_Mes);
    }
    
    
    
    @Test
    public void test_JsonToMap2() throws Exception
    {
        String v_Json  = """
                        {
                            "code": "200",
                            "message": "成功",
                            "data": {
                                "datas": [
                                    {
                                        "id": "XAppD3C7DC6FC44E49E1B9F84F41729D6980",
                                        "appName": "数据物联",
                                        "xid": "App_IOT",
                                        "comment": "数据物联",
                                        "createTime": "2024-07-03 17:37:29",
                                        "updateTime": "2024-07-03 17:37:29",
                                        "isDel": 0,
                                        "updateUserID": "ZhengWei",
                                        "createUserID": "ZhengWei"
                                    },
                                    {
                                        "id": "XApp6225396736E347B08EC1994D9CFBFF8C",
                                        "appName": "数据中台",
                                        "xid": "App_Data_Center",
                                        "comment": "数据中台",
                                        "createTime": "2024-07-19 15:25:17",
                                        "updateTime": "2024-07-19 15:25:17",
                                        "isDel": 0,
                                        "updateUserID": "ZhengWei",
                                        "createUserID": "ZhengWei"
                                    }
                                ]
                            },
                            "totalCount": 2,
                            "dataCount": 2
                        }
                        """;
        XJSON  v_XJson = new XJSON();
        
        Map<? ,?> v_Mes = (Map<? ,?>)v_XJson.toJava(v_Json);
        
        $Logger.info(v_Mes);
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_JsonToMap3() throws Exception
    {
        String v_JonsText = """
                            {"code":"200","message":"成功","data":{"data":{"StationStatus":4,"Automatic":2,"TaskNo":0,"Task":0,"Year":0,"Month":0,"Day":0,"Hour":0,"Min":0,"Second":0,"RFID":""}}}
                            """;
        
        XJSON v_Json = new XJSON();
        Map<String ,Object> v_Value = (Map<String ,Object>) v_Json.toJava(v_JonsText ,Map.class);
        
        $Logger.info(v_Value);
    }
    
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void test_JsonToList()
    {
        String v_Json = "{\r\n"
                + " \"body\":[\r\n"
                + "  \"XF1C047F76A9664B1B9F21207C0085B1BF\",\r\n"
                + "  \"XF228E1D462E3A469D90A975226133E6A1\",\r\n"
                + "  \"XF41E0045D7DC841F89B220E6B60EBB6F7\",\r\n"
                + "  \"XF4E072FAAD23143FA99BDFFACC6A327B8\",\r\n"
                + "  \"XF5BEBEDC292B344B5A4CFA5B6811A2875\",\r\n"
                + "  \"XF6F8A9FDB90D14E059EBD2B42C5704505\",\r\n"
                + "  \"XF7AF4C1761A33417389E3DC7CA3B9FBC2\",\r\n"
                + "  \"XFAC742E2387854F3696D837BD6D1BD58D\",\r\n"
                + "  \"XFC564D445AF2549BDAAB92FC9E54B5B5A\"\r\n"
                + " ],\r\n"
                + " \"encry\":\"\",\r\n"
                + " \"format\":\"\",\r\n"
                + " \"rc\":\"0\",\r\n"
                + " \"result\":\"true\",\r\n"
                + " \"ri\":\"\",\r\n"
                + " \"serialNo\":\"2022061314145136656728XFlowWeb\",\r\n"
                + " \"session\":\"\",\r\n"
                + " \"sid\":\"I004QueryWorkIDs\",\r\n"
                + " \"sidv\":\"\",\r\n"
                + " \"sign\":\"\",\r\n"
                + " \"sysid\":\"xx\",\r\n"
                + " \"tokenSec\":\"\"\r\n"
                + "}";
        
        XJSON  v_XJson = new XJSON();
        
        try
        {
            List<String> v_Datas = (List<String>)v_XJson.parser(v_Json ,"body" ,String.class);
            
            Help.print(v_Datas);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void test_ExpireMap() throws Exception
    {
        ExpireMap<String ,Date> v_ExpireMapOld = new ExpireMap<String ,Date>();
        ExpireMap<String ,Date> v_ExpireMapNew = new ExpireMap<String ,Date>();
        
        long   v_Second = 5L;
        Date   v_Now    = new Date();
        Date   v_Time   = new Date(v_Now.getTime() + v_Second * 1000L);
        String v_Key    = v_Time.getFullMilli();
        
        v_ExpireMapOld.put(v_Key ,v_Time ,v_Second);
        
        XJSON  v_XJson = new XJSON();
        v_XJson.setSerializable(true);
        String v_JsonData = v_XJson.toJson(v_ExpireMapOld ,"XJSON").toJSONString();
        
        $Logger.info(v_JsonData);
        $Logger.info("-- 原集合能否得到值：" + v_ExpireMapOld.get(v_Key));
        
        ExpireMap<String ,Date> v_JsonToJava = (ExpireMap<String ,Date>) v_XJson.toJava(v_JsonData ,"XJSON" ,ExpireMap.class);
        $Logger.info("-- Json序列化后能否得到值：" + v_JsonToJava.get(v_Key) + " 过期时长：" + v_JsonToJava.getExpireTimeLen(v_Key));
        
        // 等待过期
        Thread.sleep((v_Second + 2) * 1000L);
        
        v_ExpireMapNew.putAll(v_JsonToJava);

        $Logger.info("-- 过期后，复制到新集合中");
        $Logger.info("-- 原集合能否得到值：" + v_ExpireMapOld.get(v_Key));
        $Logger.info("-- 新集合能否得到值：" + v_ExpireMapNew.get(v_Key));
        $Logger.info("-- 序列化能否得到值：" + v_JsonToJava  .get(v_Key));
    }
    
    
    
    @Test
    public void test_Upper() throws Exception
    {
        String v_Text = """
                        {
                            "Upper": true
                        }
                        """;
        
        XJSON v_XJson = new XJSON();
        JU_XJSON v_Object = (JU_XJSON) v_XJson.toJava(v_Text ,JU_XJSON.class);
        System.out.println(v_Object.getUpper());
    }
    
    
    
    @Test
    public void test_Map() throws Exception
    {
        Map<String ,Object> v_Datas = new HashMap<String ,Object>();
        v_Datas.put("value" ,201);
        
        XJSON v_Json = new XJSON();
        System.out.println(v_Json.toJson(v_Datas).toJSONString(JSONStyle.NO_COMPRESS));
    }
    
    
    
    @Test
    public void test_format()
    {
        String v_Value = """
                         {
                            "value":201,
                            "deviceXID":":Rets.myself.valueString",
                            "cflowXID":":Rets.valueString",
                            "userID":"ZhengWei",
                            "datas"::dataValues
                         }
                         """;
        
        System.out.println(XJSON.format(v_Value ,"    " ," " ,"\n"));
    }

    
    
    public Boolean getUpper()
    {
        return upper;
    }



    public void setUpper(Boolean i_Upper)
    {
        this.upper = i_Upper;
    }



    public String getValueString()
    {
        return valueString;
    }


    public void setValueString(String valueString)
    {
        this.valueString = valueString;
    }

    
    public Date getValueDate()
    {
        return valueDate;
    }

    
    public void setValueDate(Date valueDate)
    {
        this.valueDate = valueDate;
    }


    public Object getValueObject()
    {
        return valueObject;
    }

    
    public void setValueObject(Object valueObject)
    {
        this.valueObject = valueObject;
    }

    
    public byte [] getValueByteArr()
    {
        return valueByteArr;
    }

    
    public void setValueByteArr(byte [] valueByteArr)
    {
        this.valueByteArr = valueByteArr;
    }

    
    public int [] getValueIntArr()
    {
        return valueIntArr;
    }

    
    public void setValueIntArr(int [] valueIntArr)
    {
        this.valueIntArr = valueIntArr;
    }


    public Integer [] getValueIntegerArr()
    {
        return valueIntegerArr;
    }

    
    public void setValueIntegerArr(Integer [] valueIntegerArr)
    {
        this.valueIntegerArr = valueIntegerArr;
    }

    
    public String [] getValueStringArr()
    {
        return valueStringArr;
    }

    
    public void setValueStringArr(String [] valueStringArr)
    {
        this.valueStringArr = valueStringArr;
    }

    
    public String getValueStrDate()
    {
        return valueStrDate;
    }

    
    public void setValueStrDate(String valueStrDate)
    {
        this.valueStrDate = valueStrDate;
    }
    
}
