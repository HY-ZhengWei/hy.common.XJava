package org.hy.common.xml.junit.jar;

import java.io.IOException;

import org.hy.common.file.FileHelp;
import org.hy.common.xml.log.Logger;
import org.junit.Test;






public class JU_Jar
{
    private static final Logger $Logger = new Logger(JU_Jar.class);
    
    
    
    @Test
    public void test_ReadJar() throws ClassNotFoundException, IOException
    {
        FileHelp v_FileHelp = new FileHelp();
        
        String v_Content = v_FileHelp.getContent("C:\\WorkSpace\\School\\Flink\\hy.microservice.flink\\target\\hy.microservice.flink-1.1.2.jar!\\BOOT-INF\\classes\\config\\flink\\ms.flink.sys.Config.xml");
        
        $Logger.info(v_Content);
    }
    
}
