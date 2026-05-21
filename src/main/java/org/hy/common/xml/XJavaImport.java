package org.hy.common.xml;

import java.util.List;





/**
 * 众包引用的接口类
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-05-21
 * @version     v1.0
 */
public interface XJavaImport
{
    
    /** name：映射到XML中的节点名称；  className：引用类的全路径 */
    public record Import(String name ,String className) {}
    
    
    
    /**
     * 获取可引用的类信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-05-21
     * @version     v1.0
     *
     * @return
     */
    public List<Import> getImports();
    
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-05-21
     * @version     v1.0
     *
     * @return
     */
    public String getComment();
    
}
