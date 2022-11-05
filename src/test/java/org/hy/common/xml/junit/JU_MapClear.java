package org.hy.common.xml.junit;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Date;
import org.junit.Test;





public class JU_MapClear
{
    
    @Test
    public void test()
    {
        Map<String ,Date> v_Map = new HashMap<String ,Date>();
        
        v_Map.put("1" ,new Date());
        
        Date v_Date = v_Map.get("1");
        
        v_Map.clear();
        v_Map = null;
        
        System.out.println(v_Date.getFull());
    }
    
}
