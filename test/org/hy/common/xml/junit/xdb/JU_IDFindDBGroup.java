package org.hy.common.xml.junit.xdb;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.TablePartition;
import org.junit.Test;

public class JU_IDFindDBGroup
{
    
    @Test
    public void test_UUID()
    {
        TablePartition<Long ,String> v_Datas = new TablePartition<Long ,String>();
        
        for (int i=1; i<=10000000; i++)
        {
            String v_ID     = StringHelp.getUUID();
            long   v_IDArea = stringToLong(v_ID);
            
            v_Datas.putRow(v_IDArea ,v_ID);
        }
        
        List<Long> v_IDAreas = new ArrayList<Long>(v_Datas.keySet());
        Help.toSort(v_IDAreas);
        int v_Index = 0;
        
        for (Long v_IDArea : v_IDAreas)
        {
            System.out.println(++v_Index + "\t\t" + v_IDArea + "\t\t" + v_Datas.get(v_IDArea).size());
        }
    }
    
    
    
    public long stringToLong(String i_ID)
    {
        long v_Ret = 0L;
        
        for (int i=i_ID.length()-1; i>=0; i--)
        {
            v_Ret += i_ID.charAt(i);
        }
        
        return v_Ret;
    }
    
}
