package org.hy.common.xml.junit.serializable;





/**
 * 零件信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2021-11-10
 * @version     v1.0
 */
public class ItemCodeInfo extends BaseModel
{
    
    private static final long serialVersionUID = 6133620538305757327L;
    
    private String  id;
    
    private String  orderNo;
    
    private String  orderNo8;
    
    private Integer submit;
    
    private Integer groupNum;
    
    private Integer sortID;
    
    /** 半成品排序号 */
    private Integer roughSortID;
    
    /** 库方编号：内存缓存使用 */
    private String  stCode;

    /** 零件编号 */
    private String  itemCode;
    
    /** 零件名称 */
    private String  itemName;
    
    /** 半成品（毛坯外协件、砂铸、精铸）专属图号 */
    private String  roughItemCode;
    
    /** 工艺分类 */
    private String  technicsType;
    
    /** 组件类型 */
    private String  classSecType;
    
    /** 是否自制 */
    private Integer itemIsMy;
    
    /** 是否采购件 */
    private Integer itemIsBuy;
    
    /** 专供件：是否零件绑定合同，专用的零件，不可代用 */
    private Integer itemIsBinding;
    
    /** 外购产品：除外购零件的其它采购件，包括外网附件、外购执行机构、外购阀体组件 */
    private Integer itemIsBuyProduct;
    
    /** 零件需求数量（订单需求） */
    private Integer itemOrderQuantity;
    
    /** 零件需求数量（实际需求，排除已成套的） */
    private Integer itemQuantity;
    
    /** 零件库存数量 */
    private Integer itemStocks;
    
    /** 零件库存分配数量 */
    private Integer itemStocksGiveTo;
    
    /** 零件库存分配后剩余 */
    private Integer itemStocksSurplus;
    
    /** 半成品零件库存数量 */
    private Integer roughItemStocks;
    
    /** 半成品零件库存分配数量 */
    private Integer roughItemStocksGiveTo;
    
    /** 半成品零件库存分配后剩余 */
    private Integer roughItemStocksSurplus;

    
    
    public String getItemCode()
    {
        return itemCode;
    }

    
    public void setItemCode(String itemCode)
    {
        this.itemCode = itemCode;
    }

    
    public Integer getItemQuantity()
    {
        return itemQuantity;
    }

    
    public void setItemQuantity(Integer itemQuantity)
    {
        this.itemQuantity = itemQuantity;
    }

    
    public Integer getItemStocks()
    {
        return itemStocks;
    }

    
    public void setItemStocks(Integer itemStocks)
    {
        this.itemStocks = itemStocks;
    }

    
    public Integer getItemStocksGiveTo()
    {
        return itemStocksGiveTo;
    }

    
    public void setItemStocksGiveTo(Integer itemStocksGiveTo)
    {
        this.itemStocksGiveTo = itemStocksGiveTo;
    }

    
    public Integer getItemIsMy()
    {
        return itemIsMy;
    }

    
    public void setItemIsMy(Integer itemIsMy)
    {
        this.itemIsMy = itemIsMy;
    }

    
    public String getItemName()
    {
        return itemName;
    }

    
    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    
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

    
    public Integer getItemIsBuy()
    {
        return itemIsBuy;
    }


    public void setItemIsBuy(Integer itemIsBuy)
    {
        this.itemIsBuy = itemIsBuy;
    }


    public String getTechnicsType()
    {
        return technicsType;
    }

    
    public void setTechnicsType(String technicsType)
    {
        this.technicsType = technicsType;
    }

    
    public String getClassSecType()
    {
        return classSecType;
    }

    
    public void setClassSecType(String classSecType)
    {
        this.classSecType = classSecType;
    }

    
    public Integer getItemStocksSurplus()
    {
        return itemStocksSurplus;
    }

    
    public void setItemStocksSurplus(Integer itemStocksSurplus)
    {
        this.itemStocksSurplus = itemStocksSurplus;
    }

    
    public Integer getItemIsBinding()
    {
        return itemIsBinding;
    }

    
    public void setItemIsBinding(Integer itemIsBinding)
    {
        this.itemIsBinding = itemIsBinding;
    }

    
    public String getId()
    {
        return id;
    }

    
    public void setId(String id)
    {
        this.id = id;
    }


    public Integer getItemIsBuyProduct()
    {
        return itemIsBuyProduct;
    }


    public void setItemIsBuyProduct(Integer itemIsBuyProduct)
    {
        this.itemIsBuyProduct = itemIsBuyProduct;
    }

    
    public Integer getItemOrderQuantity()
    {
        return itemOrderQuantity;
    }

    
    public void setItemOrderQuantity(Integer itemOrderQuantity)
    {
        this.itemOrderQuantity = itemOrderQuantity;
    }


    public String getRoughItemCode()
    {
        return roughItemCode;
    }

    
    public void setRoughItemCode(String roughItemCode)
    {
        this.roughItemCode = roughItemCode;
    }

    
    public Integer getRoughItemStocks()
    {
        return roughItemStocks;
    }

    
    public void setRoughItemStocks(Integer roughItemStocks)
    {
        this.roughItemStocks = roughItemStocks;
    }

    
    public Integer getRoughItemStocksGiveTo()
    {
        return roughItemStocksGiveTo;
    }

    
    public void setRoughItemStocksGiveTo(Integer roughItemStocksGiveTo)
    {
        this.roughItemStocksGiveTo = roughItemStocksGiveTo;
    }

    
    public Integer getRoughItemStocksSurplus()
    {
        return roughItemStocksSurplus;
    }

    
    public void setRoughItemStocksSurplus(Integer roughItemStocksSurplus)
    {
        this.roughItemStocksSurplus = roughItemStocksSurplus;
    }

    
    public Integer getRoughSortID()
    {
        return roughSortID;
    }

    
    public void setRoughSortID(Integer roughSortID)
    {
        this.roughSortID = roughSortID;
    }

    
    public String getStCode()
    {
        return stCode;
    }

    
    public void setStCode(String stCode)
    {
        this.stCode = stCode;
    }
    
}
