package org.hy.common.xml.junit.template.junit;

import java.io.IOException;

import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.junit.template.DatabaseToJson;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





@Xjava(XType.XML) 
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_DatabaseToJson
{
    
    private static boolean $isInit = false;
    
    private static String $JsonPath = "D:/WorkSpace_SearchDesktop/XJava/test/org/hy/common/xml/junit/template/junit/json";
    
    private static String $JavaPath = "D:/WorkSpace_SearchDesktop/XJava/test/org/hy/common/xml/junit/template/junit/java";
    
    
    
    public JU_DatabaseToJson() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    public void test01_dbToJson() throws IOException
    {
        DatabaseToJson v_DatabaseToJson = new DatabaseToJson();
        v_DatabaseToJson.dbToJson((DataSourceGroup)XJava.getObject("DSG") ,$JsonPath);
    }
    
    
    
    @Test
    public void test02_jsonToJava()
    {
        DatabaseToJson v_DatabaseToJson = new DatabaseToJson();
        v_DatabaseToJson.jsonsToJavas($JsonPath ,$JavaPath);
    }
    
}
