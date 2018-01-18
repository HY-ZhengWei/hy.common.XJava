package org.hy.common.xml;





/**
 * XSQL大数据接口
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-01-17
 * @version     v1.0
 */
public interface XSQLBigData<R>
{
    
    /**
     * 大数据一行一处理方法 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     *
     * @param i_RowNo        行号。下标从0开始
     * @param i_Row          本行数据
     * @param i_RowPrevious  上一行数据
     * @param i_RowNext      下一行数据
     * @return               是否断续。为false时，将中断其后行的处理，如出现异常情况。
     */
    public boolean row(long i_RowNo ,Object i_Row ,Object i_RowPrevious ,Object i_RowNext);
    
    
    
    /**
     * 所有行均处理完成后的结果
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     *
     * @return
     */
    public R getResult();
    
}
