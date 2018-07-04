package org.hy.common.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;





/**
 * 数据库大字段类型Clob的处理类
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-06-14
 * @version     v1.0
 */
public final class XSQLClob
{
    
    private XSQLClob()
    {
        
    }
    
    
    
    /**
     * Clob字符转字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-06-14
     * @version     v1.0
     *
     * @param i_Clob
     * @return
     * @throws SQLException 
     * @throws IOException 
     */
    public static String toString(Clob i_Clob)
    {
        BufferedReader v_Reader = null;  
        StringBuilder  v_Buffer = new StringBuilder();
        String         v_Line   = null;
        
        try
        {
            v_Reader = new BufferedReader(i_Clob.getCharacterStream());
            
            while ((v_Line = v_Reader.readLine()) != null) 
            {  
                v_Buffer.append(v_Line);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            if ( v_Reader != null )
            {
                try
                {
                    v_Reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        return v_Buffer.toString();
    }
    
}
