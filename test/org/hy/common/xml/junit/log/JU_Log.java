package org.hy.common.xml.junit.log;

import org.hy.common.xml.log.Logger;
import org.hy.common.xml.log.LoggerFactory;





public class JU_Log
{
    
    public static void main(String[] args) 
    {
        // 如果配置文件名为log4j.properties，且放在类加载路径下，以下两行代码可以不写
        // URL url = ClassLoader.getSystemResource("log4j.properties");
        // PropertyConfigurator.configure(url);

        Logger logger = LoggerFactory.getLogger(JU_Log.class);
        logger.info("info msg");
        logger.debug("debug msg");
        logger.warn("warn msg");
        logger.error("error msg");
        
        logger.info("info msg" ,"123456");
        logger.info("info msg" ,"123456" ,"654321");
    }
    
}
