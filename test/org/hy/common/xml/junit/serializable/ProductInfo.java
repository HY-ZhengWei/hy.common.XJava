package org.hy.common.xml.junit.serializable;





/**
 * 产品信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2021-12-23
 * @version     v1.0
 */
public class ProductInfo extends OrderSortInfo
{
    private static final long serialVersionUID = -5841721810703140386L;
    
    /** 产品编号 */
    private String cpbh;
    
    
    
    public ProductInfo()
    {
        super();
    }
    
    
    public ProductInfo(OrderSortInfo i_Order)
    {
        this.initNotNull(i_Order);
    }
    
    
    public String getCpbh()
    {
        return cpbh;
    }

    
    public void setCpbh(String cpbh)
    {
        this.cpbh = cpbh;
    }
    
}
