package org.hy.common.xml.junit.serializable;

import java.util.List;

import org.hy.common.Date;





/**
 * 合同排序计划
 *
 * @author      ZhengWei(HY)
 * @createDate  2021-11-10
 * @version     v1.0
 */
public class OrderSortInfo extends BaseModel
{

    private static final long serialVersionUID = 4955415846477351706L;
    
    private String  orderNo;
    
    private String  orderNo8;
    
    private Integer submit;
    
    private Integer groupNum;
    
    private Integer sortID;
    
    /** 半成品排序号 */
    private Integer roughSortID;
    
    /** 产品型号 */
    private String  productClass;
    
    /** 压力等级 */
    private String  pressure;
    
    /** 口径 */
    private String  dg;
    
    /** 产品标记（成品、单供、备件） */
    private String  productFlag;
    
    /** 制成类型 */
    private String  processingType;
    
    /** 需求日期 */
    private String  requiredDate;
    
    /** 机加计划开始时间 */
    private Date    machinePlanStart;
    
    /** 机加计划结束时间 */
    private Date    machinePlanFinish;
    
    /** 装配计划开始时间 */
    private Date    assemblePlanStart;
    
    /** 装配计划结束时间 */
    private Date    assemblePlanFinish;
    
    /** 装配部门编号 */
    private String  assembleDepartCode;
    
    /** 装配部门名称 */
    private String  assembleDepartName;
    
    /** 需求数量 */
    private Integer requiredCount;
    
    /** 成套数量 */
    private Integer ptCount;
    
    /** 齐库率 */
    private Integer maxStockUseRate;
    
    /** 齐套率 */
    private Integer qtrStockUseRate;
    
    /** 半成品的齐库率 */
    private Integer roughMaxStockUseRate;
    
    /** 半成品的齐套率 */
    private Integer roughQtrStockUseRate;
    
    /** 只含自制件的齐套率 */
    private Integer myQtrStockUseRate;
    
    /** 只含外购产品的齐套率 */
    private Integer buyProdQtrStockUseRate;
    
    /** 零件需求 */
    private Integer itemCount;
    
    /** 零件种类 */
    private Integer itemTypeCount;
    
    /** 零件满足种类 */
    private Integer usableTypeCount;
    
    /** 自制需求 */
    private Integer myItemCount;
    
    /** 自制种类 */
    private Integer myItemTypeCount;
    
    /** 自制满足种类 */
    private Integer myUsableTypeCount;
    
    /** 外购产品需求 */
    private Integer buyProdItemCount;
    
    /** 外购产品种类 */
    private Integer buyProdItemTypeCount;
    
    /** 外购产品满足种类 */
    private Integer buyProdUsableTypeCount;
    
    /** 半成品种类 */
    private Integer roughItemTypeCount;
    
    /** 半成品满足种类 */
    private Integer roughUsableTypeCount;
    
    /** 零件BOM。Map.key为零件图号 */
    private List<ItemCodeInfo> itemList;
    
    /** 配置过滤的图号 */
    private String itemNameConfig;

    
    
    public String getOrderNo()
    {
        return orderNo;
    }

    
    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    
    public String getOrderNo8()
    {
        return orderNo8;
    }

    
    public void setOrderNo8(String orderNo8)
    {
        this.orderNo8 = orderNo8;
    }

    
    public Integer getSubmit()
    {
        return submit;
    }

    
    public void setSubmit(Integer submit)
    {
        this.submit = submit;
    }

    
    public Integer getGroupNum()
    {
        return groupNum;
    }

    
    public void setGroupNum(Integer groupNum)
    {
        this.groupNum = groupNum;
    }

    
    public Integer getSortID()
    {
        return sortID;
    }

    
    public void setSortID(Integer sortID)
    {
        this.sortID = sortID;
    }


    public Integer getMaxStockUseRate()
    {
        return maxStockUseRate;
    }

    
    public void setMaxStockUseRate(Integer maxStockUseRate)
    {
        this.maxStockUseRate = maxStockUseRate;
    }

    
    public List<ItemCodeInfo> getItemList()
    {
        return itemList;
    }


    public void setItemList(List<ItemCodeInfo> itemList)
    {
        this.itemList = itemList;
    }

    
    public Integer getQtrStockUseRate()
    {
        return qtrStockUseRate;
    }

    
    public void setQtrStockUseRate(Integer qtrStockUseRate)
    {
        this.qtrStockUseRate = qtrStockUseRate;
    }

    
    public Integer getMyQtrStockUseRate()
    {
        return myQtrStockUseRate;
    }

    
    public void setMyQtrStockUseRate(Integer myQtrStockUseRate)
    {
        this.myQtrStockUseRate = myQtrStockUseRate;
    }

    
    public Integer getItemCount()
    {
        return itemCount;
    }

    
    public void setItemCount(Integer itemCount)
    {
        this.itemCount = itemCount;
    }

    
    public Integer getItemTypeCount()
    {
        return itemTypeCount;
    }

    
    public void setItemTypeCount(Integer itemTypeCount)
    {
        this.itemTypeCount = itemTypeCount;
    }

    
    public Integer getUsableTypeCount()
    {
        return usableTypeCount;
    }

    
    public void setUsableTypeCount(Integer usableTypeCount)
    {
        this.usableTypeCount = usableTypeCount;
    }


    public Integer getMyItemCount()
    {
        return myItemCount;
    }

    
    public void setMyItemCount(Integer myItemCount)
    {
        this.myItemCount = myItemCount;
    }

    
    public Integer getMyItemTypeCount()
    {
        return myItemTypeCount;
    }

    
    public void setMyItemTypeCount(Integer myItemTypeCount)
    {
        this.myItemTypeCount = myItemTypeCount;
    }

    
    public Integer getMyUsableTypeCount()
    {
        return myUsableTypeCount;
    }

    
    public void setMyUsableTypeCount(Integer myUsableTypeCount)
    {
        this.myUsableTypeCount = myUsableTypeCount;
    }

    
    public Integer getRequiredCount()
    {
        return requiredCount;
    }

    
    public void setRequiredCount(Integer requiredCount)
    {
        this.requiredCount = requiredCount;
    }


    public Integer getPtCount()
    {
        return ptCount;
    }

    
    public void setPtCount(Integer ptCount)
    {
        this.ptCount = ptCount;
    }


    public Date getMachinePlanStart()
    {
        return machinePlanStart;
    }

    
    public void setMachinePlanStart(Date machinePlanStart)
    {
        this.machinePlanStart = machinePlanStart;
    }

    
    public Date getMachinePlanFinish()
    {
        return machinePlanFinish;
    }

    
    public void setMachinePlanFinish(Date machinePlanFinish)
    {
        this.machinePlanFinish = machinePlanFinish;
    }

    
    public Date getAssemblePlanStart()
    {
        return assemblePlanStart;
    }

    
    public void setAssemblePlanStart(Date assemblePlanStart)
    {
        this.assemblePlanStart = assemblePlanStart;
    }

    
    public Date getAssemblePlanFinish()
    {
        return assemblePlanFinish;
    }

    
    public void setAssemblePlanFinish(Date assemblePlanFinish)
    {
        this.assemblePlanFinish = assemblePlanFinish;
    }

    
    public String getProductClass()
    {
        return productClass;
    }

    
    public void setProductClass(String productClass)
    {
        this.productClass = productClass;
    }

    
    public String getPressure()
    {
        return pressure;
    }

    
    public void setPressure(String pressure)
    {
        this.pressure = pressure;
    }

    
    public String getDg()
    {
        return dg;
    }

    
    public void setDg(String dg)
    {
        this.dg = dg;
    }

    
    public String getAssembleDepartCode()
    {
        return assembleDepartCode;
    }

    
    public void setAssembleDepartCode(String assembleDepartCode)
    {
        this.assembleDepartCode = assembleDepartCode;
    }

    
    public String getAssembleDepartName()
    {
        return assembleDepartName;
    }

    
    public void setAssembleDepartName(String assembleDepartName)
    {
        this.assembleDepartName = assembleDepartName;
    }

    
    public String getProductFlag()
    {
        return productFlag;
    }

    
    public void setProductFlag(String productFlag)
    {
        this.productFlag = productFlag;
    }

    
    public String getProcessingType()
    {
        return processingType;
    }

    
    public void setProcessingType(String processingType)
    {
        this.processingType = processingType;
    }

    
    public String getRequiredDate()
    {
        return requiredDate;
    }

    
    public void setRequiredDate(String requiredDate)
    {
        this.requiredDate = requiredDate;
    }

    
    public String getItemNameConfig()
    {
        return itemNameConfig;
    }

    
    public void setItemNameConfig(String itemNameConfig)
    {
        this.itemNameConfig = itemNameConfig;
    }

    
    public Integer getBuyProdItemCount()
    {
        return buyProdItemCount;
    }

    
    public void setBuyProdItemCount(Integer buyProdItemCount)
    {
        this.buyProdItemCount = buyProdItemCount;
    }

    
    public Integer getBuyProdItemTypeCount()
    {
        return buyProdItemTypeCount;
    }

    
    public void setBuyProdItemTypeCount(Integer buyProdItemTypeCount)
    {
        this.buyProdItemTypeCount = buyProdItemTypeCount;
    }

    
    public Integer getBuyProdUsableTypeCount()
    {
        return buyProdUsableTypeCount;
    }

    
    public void setBuyProdUsableTypeCount(Integer buyProdUsableTypeCount)
    {
        this.buyProdUsableTypeCount = buyProdUsableTypeCount;
    }


    public Integer getBuyProdQtrStockUseRate()
    {
        return buyProdQtrStockUseRate;
    }

    
    public void setBuyProdQtrStockUseRate(Integer buyProdQtrStockUseRate)
    {
        this.buyProdQtrStockUseRate = buyProdQtrStockUseRate;
    }

    
    public Integer getRoughMaxStockUseRate()
    {
        return roughMaxStockUseRate;
    }

    
    public void setRoughMaxStockUseRate(Integer roughMaxStockUseRate)
    {
        this.roughMaxStockUseRate = roughMaxStockUseRate;
    }

    
    public Integer getRoughQtrStockUseRate()
    {
        return roughQtrStockUseRate;
    }

    
    public void setRoughQtrStockUseRate(Integer roughQtrStockUseRate)
    {
        this.roughQtrStockUseRate = roughQtrStockUseRate;
    }

    
    public Integer getRoughItemTypeCount()
    {
        return roughItemTypeCount;
    }

    
    public void setRoughItemTypeCount(Integer roughItemTypeCount)
    {
        this.roughItemTypeCount = roughItemTypeCount;
    }

    
    public Integer getRoughUsableTypeCount()
    {
        return roughUsableTypeCount;
    }

    
    public void setRoughUsableTypeCount(Integer roughUsableTypeCount)
    {
        this.roughUsableTypeCount = roughUsableTypeCount;
    }

    
    public Integer getRoughSortID()
    {
        return roughSortID;
    }

    
    public void setRoughSortID(Integer roughSortID)
    {
        this.roughSortID = roughSortID;
    }
    
}
