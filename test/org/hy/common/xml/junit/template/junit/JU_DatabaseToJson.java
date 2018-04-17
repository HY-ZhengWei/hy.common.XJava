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
    
    private static String $JsonPath = "/Users/hy/WSS/WorkSpace_SearchDesktop/SearchDesktop/src/org/hy/common/template/junit/json";
    
    private static String $JavaPath = "/Users/hy/WSS/WorkSpace_SearchDesktop/SearchDesktop/src/org/hy/common/template/junit/java";
    
    
    
    public JU_DatabaseToJson() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test01_dbToJson() throws IOException
    {
        DatabaseToJson v_DatabaseToJson = new DatabaseToJson();
        v_DatabaseToJson.dbToJson((DataSourceGroup)XJava.getObject("DSG") ,$JsonPath);
    }
    
    
    
    public void test02_jsonToJava()
    {
        DatabaseToJson v_DatabaseToJson = new DatabaseToJson();
        v_DatabaseToJson.jsonToJava($JsonPath + "/ht_Outline.json" ,$JavaPath + "/HTOutline.java");
    }
    
}
