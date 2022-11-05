package org.hy.common.xml.plugins;

import org.hy.common.xml.XJava;





/**
 * 缓存数据
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-14
 * @version     v1.0
 * @param <O>
 */
public abstract class CacheData<O>
{
    
    private String cacheName;
    
    
    
    public CacheData()
    {
        this.cacheName = this.getClass().getSimpleName();
    }
    
    
    
    /**
     * 创建或生成数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-14
     * @version     v1.0
     *
     * @return
     */
    protected abstract O createData();
    
    
    
    /**
     * 缓存数据，当数据不存在时调用生成方法createData()生成数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-14
     * @version     v1.0
     *
     */
    public synchronized void cacheData()
    {
        O v_Data = this.createData();
        
        if ( v_Data != null )
        {
            XJava.putObject(this.cacheName ,v_Data);
        }
    }
    
    
    
    /**
     * 获取数据。外界获取数据的唯一方法，而不是通过createData()方法获取数据。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-14
     * @version     v1.0
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized O getData()
    {
        O v_Data = (O)XJava.getObject(this.cacheName);
        
        if ( v_Data == null )
        {
            cacheData();
        }
        
        return v_Data;
    }
    
}
