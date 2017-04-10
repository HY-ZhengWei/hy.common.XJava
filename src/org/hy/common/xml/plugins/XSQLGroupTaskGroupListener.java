package org.hy.common.xml.plugins;

import org.hy.common.thread.event.TaskGroupEvent;
import org.hy.common.thread.event.TaskGroupListener;





/**
 * TODO(请详细描述类型的作用。描述后请删除todo标签) 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-22
 * @version     v1.0
 */
public class XSQLGroupTaskGroupListener implements TaskGroupListener
{
    
    public XSQLGroupTaskGroupListener()
    {
        
    }
    
    
    
    /**
     * 启用任务组所有任务的事件
     * 
     * @param e
     */
    public void startupAllTask(TaskGroupEvent e)
    {
        // Nothing.
    }
    
    
    
    /**
     * 任务组中任务都完成后的事件
     * 
     * @param e
     */
    public void finishAllTask(TaskGroupEvent e)
    {
        
    }
    
}
