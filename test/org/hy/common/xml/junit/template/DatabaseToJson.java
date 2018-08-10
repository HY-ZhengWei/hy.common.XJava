package org.hy.common.xml.junit.template;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.AppInitConfig;





public class DatabaseToJson extends AppInitConfig
{
    
    public final static String $DSG = "DSG_Template";
    
    private static String $Template_Java;
    
    private static String $Template_Attribute;
    
    private static String $Template_Method;
    
    
    
    /**
     * 初始化脚本
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-05-12
     * @version     v1.0
     *
     * @param i_DataSourceGroup
     */
    public void initConfig(DataSourceGroup i_DataSourceGroup)
    {
        if ( null != i_DataSourceGroup )
        {
            XJava.putObject($DSG ,i_DataSourceGroup);
            
            this.init("db.xml");
        }
        
        this.init("dbType.transform.xml");
        
        try
        {
            FileHelp v_FileHelp = new FileHelp();
            $Template_Java      = v_FileHelp.getContent(this.getClass().getResourceAsStream("java.01.class.tjava")     ,"UTF-8" ,true);
            $Template_Attribute = v_FileHelp.getContent(this.getClass().getResourceAsStream("java.02.attribute.tjava") ,"UTF-8" ,true);
            $Template_Method    = v_FileHelp.getContent(this.getClass().getResourceAsStream("java.03.method.tjava")    ,"UTF-8" ,true);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    /**
     * 查询数据库所有表的表结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-05-12
     * @version     v1.0
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<PackageInfo> queryTables()
    {
        List<TableInfo>   v_Tables   = (List<TableInfo>)XJava.getXSQL("XSQL_TablesColumns").query();
        List<PackageInfo> v_Packages = new ArrayList<PackageInfo>();
        
        if ( Help.isNull(v_Tables) )
        {
            return v_Packages;
        }
        
        for (TableInfo v_Table : v_Tables)
        {
            PackageInfo v_Package = new PackageInfo();
            
            v_Package.setTable(v_Table);
            
            v_Packages.add(v_Package);
        }
        
        return v_Packages;
    }
    
    
    
    /**
     * 查询数据库表结构，并转为Json格式
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-05-11
     * @version     v1.0
     *
     * @param i_DataSourceGroup  数据库连接池组
     * @param i_JsonFolderName   保存Json文件的目录路径
     * @return
     */
    public void dbToJson(DataSourceGroup i_DataSourceGroup ,String i_JsonFolderName)
    {
        this.initConfig(i_DataSourceGroup);
        
        List<PackageInfo> v_Packages = this.queryTables();
        
        if ( Help.isNull(v_Packages) )
        {
            return;
        }
        
        XJSON v_XJson = new XJSON();
        
        try
        {
            for (PackageInfo v_Package : v_Packages)
            {
                String v_Json = XJSON.format(v_XJson.parser(v_Package).toJSONString());
                
                this.saveFile(i_JsonFolderName + Help.getSysPathSeparator() + v_Package.getTable().getNameDB() + ".json" ,v_Json);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return;
    }
    
    
    
    public void jsonsToJavas(String i_JsonFolder ,String i_JavaFolder)
    {
        File v_JsonFolder = new File(i_JsonFolder);
        if ( v_JsonFolder.isDirectory() )
        {
            File [] v_ChildFiles = v_JsonFolder.listFiles();
            if ( v_ChildFiles != null )
            {
                for (File v_JsonFile : v_ChildFiles)
                {
                    String v_JsonName = v_JsonFile.getName();
                    if ( v_JsonName.toLowerCase().endsWith(".json") )
                    {
                        jsonToJava(v_JsonFile.toString() ,i_JavaFolder + Help.getSysPathSeparator() + v_JsonName.substring(0 ,v_JsonName.length()-5) + ".java");
                    }
                }
            }
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    public void jsonToJava(String i_JsonFile ,String i_JavaFile)
    {
        this.initConfig(null);
        
        XJSON v_XJson = new XJSON();
        
        try
        {
            FileHelp      v_FileHelp   = new FileHelp();
            PackageInfo   v_Package    = (PackageInfo)v_XJson.parser(v_FileHelp.getContent(i_JsonFile ,"UTF-8") ,PackageInfo.class);
            StringBuilder v_Attributes = new StringBuilder();
            StringBuilder v_Methods    = new StringBuilder();
            
            
            for (ColumnInfo v_Column : v_Package.getTable().getTableColumns())
            {
                AttributeInfo v_Attribute = new AttributeInfo();
                
                v_Attribute.setAttributeName(     v_Column.getName());
                v_Attribute.setAttributeNameUpper(StringHelp.toUpperCaseByFirst(v_Column.getName()));
                v_Attribute.setAttributeDesc(     v_Column.getDescription());
                v_Attribute.setAttributeType(     v_Column.getTypeJava());
                
                Map<String ,String> v_AttributeMap = (Map<String ,String>)Help.toPlaceholders(v_Attribute.toMap() ,":" ,"");
                
                v_Attributes.append(StringHelp.replaceAll($Template_Attribute ,v_AttributeMap));
                v_Methods   .append(StringHelp.replaceAll($Template_Method    ,v_AttributeMap));
            }
            
            JavaInfo v_JavaInfo = new JavaInfo();
            
            v_JavaInfo.setPackageName(       v_Package.getPackageName());
            v_JavaInfo.setClassDesc(Help.NVL(v_Package.getTable().getDescription() ,"TODO(请详细描述类型的作用。描述后请删除todo标签) "));
            v_JavaInfo.setClassName(         v_Package.getTable().getNameJava());
            v_JavaInfo.setAuthor(   Help.NVL(v_Package.getTable().getAuthor() ,"作者"));
            v_JavaInfo.setCreateDate(Date.getNowTime().getYMD());
            v_JavaInfo.setAttributes(v_Attributes.toString());
            v_JavaInfo.setMethods(v_Methods.toString());
            
            String v_JavaFileContent = StringHelp.replaceAll($Template_Java ,(Map<String ,String>)Help.toPlaceholders(v_JavaInfo.toMap() ,":" ,""));
            
            this.saveFile(i_JavaFile ,v_JavaFileContent);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    /**
     * Json字符保存为文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-05-11
     * @version     v1.0
     *
     * @param i_FileName    文件名称的全路径
     * @param i_FileContent 文件内容
     * @throws IOException
     */
    public boolean saveFile(String i_FileName ,String i_FileContent) throws IOException
    {
        FileHelp v_FileHelp = new FileHelp();
        
        v_FileHelp.setOverWrite(false);
        
        try
        {
            v_FileHelp.create(i_FileName ,Help.NVL(i_FileContent) ,"UTF-8");
            
            return true;
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        return false;
    }
    
}
