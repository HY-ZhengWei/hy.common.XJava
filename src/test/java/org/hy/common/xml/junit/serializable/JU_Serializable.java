package org.hy.common.xml.junit.serializable;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.log.Logger;
import org.junit.Test;





public class JU_Serializable
{
    private final static Logger $Logger = new Logger(JU_Serializable.class);
    
    
    
    /**
     * 测试两对象相互初始赋值的性能
     */
    @Test
    public void test_initNotNull()
    {
        OrderSortInfo     v_Source      = new OrderSortInfo();
        ItemCodeInfo      v_SourceChild = new ItemCodeInfo();
        List<ProductInfo> v_Targets     = new ArrayList<ProductInfo>();
        
        v_Source.setOrderNo("2");
        v_Source.setOrderNo8("2");
        v_Source.setSubmit(1);
        v_Source.setGroupNum(1);
        v_Source.setSortID(1);
        v_Source.setRoughSortID(1);
        v_Source.setProductClass("2");
        v_Source.setPressure("2");
        v_Source.setDg("2");
        v_Source.setProductFlag("2");
        v_Source.setProcessingType("2");
        v_Source.setRequiredDate("2");
        v_Source.setMachinePlanStart(new Date());
        v_Source.setMachinePlanFinish(new Date());
        v_Source.setAssemblePlanStart(new Date());
        v_Source.setAssemblePlanFinish(new Date());
        v_Source.setAssembleDepartCode("2");
        v_Source.setAssembleDepartName("2");
        v_Source.setRequiredCount(1);
        v_Source.setPtCount(1);
        v_Source.setMaxStockUseRate(1);
        v_Source.setQtrStockUseRate(1);
        v_Source.setRoughMaxStockUseRate(1);
        v_Source.setRoughQtrStockUseRate(1);
        v_Source.setMyQtrStockUseRate(1);
        v_Source.setBuyProdQtrStockUseRate(1);
        v_Source.setItemCount(1);
        v_Source.setItemTypeCount(1);
        v_Source.setUsableTypeCount(1);
        v_Source.setMyItemCount(1);
        v_Source.setMyItemTypeCount(1);
        v_Source.setMyUsableTypeCount(1);
        v_Source.setBuyProdItemCount(1);
        v_Source.setBuyProdItemTypeCount(1);
        v_Source.setBuyProdUsableTypeCount(1);
        v_Source.setRoughItemTypeCount(1);
        v_Source.setRoughUsableTypeCount(1);
        v_Source.setItemNameConfig("2");
        v_Source.setItemList(new ArrayList<ItemCodeInfo>());
        
        v_SourceChild.setId("2");
        v_SourceChild.setOrderNo("2");
        v_SourceChild.setOrderNo8("2");
        v_SourceChild.setSubmit(1);
        v_SourceChild.setGroupNum(1);
        v_SourceChild.setSortID(1);
        v_SourceChild.setRoughSortID(1);
        v_SourceChild.setStCode("2");
        v_SourceChild.setItemCode("2");
        v_SourceChild.setItemName("2");
        v_SourceChild.setRoughItemCode("2");
        v_SourceChild.setTechnicsType("2");
        v_SourceChild.setClassSecType("2");
        v_SourceChild.setItemIsMy(1);
        v_SourceChild.setItemIsBuy(1);
        v_SourceChild.setItemIsBinding(1);
        v_SourceChild.setItemIsBuyProduct(1);
        v_SourceChild.setItemOrderQuantity(1);
        v_SourceChild.setItemQuantity(1);
        v_SourceChild.setItemStocks(1);
        v_SourceChild.setItemStocksGiveTo(1);
        v_SourceChild.setItemStocksSurplus(1);
        v_SourceChild.setRoughItemStocks(1);
        v_SourceChild.setRoughItemStocksGiveTo(1);
        v_SourceChild.setRoughItemStocksSurplus(1);
        
        for (int x=1; x<=100 ; x++)
        {
            v_Source.getItemList().add(v_SourceChild);
        }
        
        int v_Count = 10000 * 10;
        for (int x=1; x<=v_Count ; x++)
        {
            v_Targets.add(new ProductInfo());
        }
        
        $Logger.info("初始完成，准备开始测试");

        
        Date v_BTime  = new Date();
        Date v_ETime  = null;
        for (int x=0; x<v_Count ; x++)
        {
            v_Targets.get(x).initNotNull(v_Source);
        }
        
        v_ETime = new Date();
        
        $Logger.info("循环 " + v_Count + " 次，总用时：" + Date.toTimeLen(v_ETime.getTime() - v_BTime.getTime()));
        
        XJSON v_Json = new XJSON();
        try
        {
            $Logger.info(v_Json.toJson(v_Targets.get(0)).toJSONString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    @Test
    public void test_ListAdd()
    {
        List<String> v_Datas = new ArrayList<String>();
        
        for (int x=0; x<10; x++)
        {
            v_Datas.add(null);
        }
        
        v_Datas.set(2 ,"2");
        
        Help.print(v_Datas);
    }
    
}
