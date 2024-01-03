package org.hy.common.xml.junit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.TablePartitionRID;
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
    
    
    @Test
    public void test_TablePartitionRID()
    {
        TablePartitionRID<String ,String> v_Table = new TablePartitionRID<String ,String>();
        
        v_Table.putRow("P1" ,"ID1.1" ,"V1.1");
        v_Table.putRow("P1" ,"ID1.2" ,"V1.2");
        v_Table.putRow("P2" ,"ID2.1" ,"V2.1");
        v_Table.putRow("P2" ,"ID2.2" ,"V2.2");
        
        Map<String ,String> v_Map      = v_Table.get("P1");
        Map<String ,String> v_CloneMap = new LinkedHashMap<String ,String>(v_Map);
        
        v_Table.clear();
        Help.print(v_CloneMap);
    }
    
}
