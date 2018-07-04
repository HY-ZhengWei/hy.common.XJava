package org.hy.common.xml;

import java.io.Serializable;

import org.hy.common.PianoKeyboardPool;

import com.greenpineyu.fel.FelEngineImpl;





/**
 * Fel队列缓存池。
 * 
 * 只为了提高性能。
 * 
 * 测试数据：创建1000个Fel用时23秒，但1000次计算只需0.200秒。
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-03
 * @version     v1.0
 */
public class FelPool implements Serializable
{
    
    private static final long serialVersionUID = 7206404311347157226L;
    
    
    
    public static final PianoKeyboardPool<FelEngineImpl> $Fels = new PianoKeyboardPool<FelEngineImpl>(FelEngineImpl.class ,100);
    
    
    
    private FelPool()
    {
        
    }
    
}
