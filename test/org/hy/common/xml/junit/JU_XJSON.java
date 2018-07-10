package org.hy.common.xml.junit;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.xml.XJSON;
import org.junit.Test;





/**
 * 测试单元
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-10
 * @version     v1.0
 */
public class JU_XJSON
{
    
    /**
     * null转Java的测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-10
     * @version     v1.0
     *
     * @throws Exception
     */
    @Test
    public void test_001() throws Exception
    {
        String json = "{\"maching\":[{\"PART_PLAN_NO\":null,\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/25\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-24\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0014\",\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/21\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-29\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"AM30781-205-103C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0567\",\"GY_TYPE\":\"部件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\" \",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"上膜盖部件\",\"RECEIVE_MATERIAL_QTY\":\" \",\"ITEM_CODE\":\"8Z51-5100-1\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0566\",\"GY_TYPE\":\"下料件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-21\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"推杆\",\"RECEIVE_MATERIAL_QTY\":\"10\",\"ITEM_CODE\":\"8Z52-106-405\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0004\",\"GY_TYPE\":\"精铸\",\"FINISH_DATE\":\"2018\\/06\\/01\",\"FINISH_QTY\":\"5\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-23\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"RECEIVE_MATERIAL_QTY\":\"5\",\"ITEM_CODE\":\"ABM30421-207-004\",\"CL\":\" \"}],\"casting\":[{\"ZJ_KF_RKRQ2\":\"2018-04-20 17:14:53\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0004\",\"ZJ_WGS\":\"5\",\"ZJ_TCS\":\"5\",\"ZJ_SX\":\"5\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"ITEM_CODE\":\"ABM30421-207-004\",\"ZJ_KF_RKS\":\"5\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-3-0013\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2018-05-29 13:46:08\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0014\",\"ZJ_WGS\":\"1\",\"ZJ_TCS\":\"1\",\"ZJ_SX\":\"1\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"ITEM_CODE\":\"AM30781-205-103C\",\"ZJ_KF_RKS\":\"1\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"14\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"6\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"}]}";
        
        XJSON v_XJson = new XJSON();
        
        v_XJson.setObjectClass(HashMap.class);
        
        Map<String, Object> v_Datas = (Map<String, Object>)v_XJson.toJava(json);
        
        Help.print(v_Datas);
    }
    
}
