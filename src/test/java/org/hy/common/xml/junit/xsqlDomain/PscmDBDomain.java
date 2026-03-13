package org.hy.common.xml.junit.xsqlDomain;

import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQLDomain;
import org.hy.common.xml.log.Logger;





/**
 * @author LH
 * @title: MES系统反填采购物资数据时，动态切换数据源控制域
 *
 * @projectName mes_code
 * @description: TODO
 * @date 2022/5/21 16:16
 */
public class PscmDBDomain implements XSQLDomain
{
    private static final Logger $Logger = new Logger(PscmDBDomain.class);

    
    
    /**
     * 按用户公司切换数据库连接池组
     */
    @Override
    public DataSourceGroup getDataSourceGroup() {
        try
        {
            String companyCode = "";   // 获取用户所在的公司
            $Logger.debug("-------------进入数据源选择------------"+companyCode+"-------------");
            if ( "A_LS".equals(companyCode) )
            {
                return (DataSourceGroup) XJava.getObject("DSG_mssql_pscmLS");  // A公司
            }
            else if ( "B_f".equals(companyCode) )
            {
                return (DataSourceGroup) XJava.getObject("DSG_pscm_f");        // B公司
            }
            else if ( "C_actuator".equals(companyCode) )
            {
                return (DataSourceGroup) XJava.getObject("DSG_pscm_actuator"); // C公司
            }
            else if ( "D_ic".equals(companyCode) )
            {
                return (DataSourceGroup) XJava.getObject("DSG_pscm_ic");       // D公司
            }
        }
        catch (Exception exce)
        {
            $Logger.error(exce ,"采购数据库分域异常");
        }

        // 使用默认的
        return null;
    }
}
