package org.hy.common.xml.junit;


public class JU_FelObject
{
    
    private static int i = 0;
    
    
    private synchronized static void add()
    {
        i++;
    }
    
    
    
    public JU_FelObject()
    {
        try
        {
            Thread.sleep(100);
            add();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    public int calc()
    {
        try
        {
            Thread.sleep(5);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        return 0;
    }
    
}
