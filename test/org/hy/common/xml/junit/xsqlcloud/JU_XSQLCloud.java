package org.hy.common.xml.junit.xsqlcloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.XSQLGroup;
import org.junit.Test;





/**
 * 测试单元：XSQL云计算
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-02-22
 * @version     v1.0
 */
@Xjava(value=XType.XML)
public class JU_XSQLCloud
{
    private static boolean $isInit = false;
    
    
    
    public JU_XSQLCloud() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_XSQLCloud()
    {
        System.out.println("-- " + Date.getNowTime() + " 开始云计算");
        
        Map<String ,Object> v_Param = new HashMap<String ,Object>();
        List<Param>         v_Datas     = new ArrayList<Param>();
        XSQLGroup           v_XSQLGroup = XJava.getXSQLGroup("GXSQL_Test_XSQLCloud").setLog(true);
        int                 v_CloudSize = 200;   // 云计算发起次数
        
        for (int i=0; i<v_CloudSize; i++)
        {
            v_Datas.add(new Param("" + i ,"" + i));
        }
        
        v_Param.put("collectionID_Test_Datas" ,v_Datas);
        
        v_XSQLGroup.executes(v_Param);
    }
    
    
    
    /**
     * XSQL组执行云服务计算的方法接口
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-22
     * @version     v1.0
     *
     * @param io_Params   执行或查询参数。
     * @return            表示是否执行成功。当返回false时，其后的XSQLNode节点将不再执行。
     */
    public boolean JU_XSQLCloud_CloudXSQLNode(Map<String ,Object> io_Params)
    {
        try
        {
            System.out.println(io_Params.toString());
            Thread.sleep(1000);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return true;
    }
    
}
