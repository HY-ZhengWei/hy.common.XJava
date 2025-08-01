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
