package org.hy.common.xml.junit;


import java.lang.management.ManagementFactory;

import org.junit.Test;

import com.sun.management.OperatingSystemMXBean;





/**
 * 测试单元：系统内存
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
        
        System.out.println(osBean.getFreeMemorySize());
        System.out.println(osBean.getTotalMemorySize());
        System.out.println(osBean.getCpuLoad());
    }
    
}
