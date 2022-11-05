package org.hy.common.xml.junit;


import java.lang.management.ManagementFactory;

import org.junit.Test;

import com.sun.management.OperatingSystemMXBean;





/**
 * TODO(请详细描述类型的作用。描述后请删除todo标签) 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-09
 * @version     v1.0
 */
public class JU_OperatingSystemMXBean
{
    
    @Test
    public void test01()
    {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        
        System.out.println(osBean.getFreePhysicalMemorySize());
        System.out.println(osBean.getTotalPhysicalMemorySize());
        System.out.println(osBean.getSystemCpuLoad());
    }
    
}
