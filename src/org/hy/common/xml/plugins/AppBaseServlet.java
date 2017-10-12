package org.hy.common.xml.plugins;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.StringHelp;
import org.hy.common.file.FileHelp;





/**
 * App的基础的Servlet
 *
 * @author      ZhengWei(HY)
 * @createDate  2013-12-19
 * @version     v1.0
 */
public class AppBaseServlet extends HttpServlet
{
    
    private static final long serialVersionUID = -5189383556838396564L;
    
    
    
    public void doPost(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        AppMessage<?> v_AppMsg = AppInterfaces.executeMessage(this ,this.getI(i_Request));
        
        if ( v_AppMsg != null )
        {
            this.responseJson(i_Request ,i_Response ,v_AppMsg.toString());
        }
    }
    
    
    
    /**
     * 客户端发来的请求信息
     * 
     * @param i_Request
     * @return
     */
    protected String getI(HttpServletRequest i_Request)
    {
        String v_Info = this.getPostInfo(i_Request);
        
        if ( v_Info != null )
        {
            v_Info = StringHelp.unescape_toUnicode(v_Info);
            String [] v_Infos = v_Info.split("=");
            
            if ( v_Infos.length == 2 && "i".equals(v_Infos[0]) )
            {
                return v_Infos[1];
            }
            else
            {
                return v_Info;
            }
        }
        
        return "";
    }
    
    
    
    /**
     * 获取Post方式的Http请求信息
     * 
     * @return
     */
    protected String getPostInfo(HttpServletRequest i_Request)
    {
        return FileHelp.getContent(i_Request);
    }
    
    
    
    /**
     * 响应Json字符串
     * 
     * @param i_Request
     * @param i_Response
     * @param i_JsonData
     * @throws ServletException
     * @throws IOException
     */
    protected void responseJson(HttpServletRequest i_Request, HttpServletResponse i_Response ,String i_JsonData) throws ServletException, IOException
    {
        FileHelp.writeHttp(i_JsonData ,i_Response);
    }
    
}
