package org.hy.common.xml.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.hy.common.StringHelp;





/**
 * App的基础的Action
 * 
 * @author  ZhengWei(HY)
 * @version 2013-11-19
 */
public class AppBaseAction extends BaseAction<Object>
{
    
    private static final long serialVersionUID = -214783262787842188L;
    
    /** 是否允许Http的Get请求访问 */
    private boolean allowHttpGet;
    
    /** 客户端发来的请求信息 */
    private String i;
    
    /** 向客户端响应的返回信息 */
    private String r;
    
    
    
    public AppBaseAction()
    {
        super();
        
        this.allowHttpGet = false;
    }
    
    
    
    /**
     * 获取Post方式的Http请求信息
     * 
     * @return
     */
    private String getPostInfo()
    {
        if ( this.servletRequest != null )
        {
            BufferedReader v_Input = null;
            try
            {
                StringBuilder v_Buffer = new StringBuilder();
                String        v_Line   = "";
                v_Input  = new BufferedReader(new InputStreamReader(this.servletRequest.getInputStream()));
                while ( (v_Line = v_Input.readLine())!= null )
                {
                    v_Buffer.append(v_Line);
                }
                
                return v_Buffer.toString();
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            finally
            {
                if ( v_Input != null )
                {
                    try
                    {
                        v_Input.close();
                    }
                    catch (Exception exce)
                    {
                        exce.printStackTrace();
                    }
                }
                v_Input = null;
            }
        }
        
        return null;
    }
    


    public String getI()
    {
        if ( this.i != null )
        {
            return this.i;
        }
        
        String v_Info = this.getPostInfo();
        if ( v_Info != null )
        {
            String [] v_Infos = v_Info.split("=");
            
            if ( v_Infos.length == 2 && "i".equals(v_Infos[0]) )
            {
                this.i = v_Infos[1];
            }
            else
            {
                this.i = "";
            }
        }
        
        return this.i;
    }


    
    public void setI(String i_Info)
    {
        if ( this.isAllowGet() )
        {
            this.i = i_Info;
        }
        else
        {
            this.i = null;
        }
    }


    
    public String getR()
    {
        if ( this.r != null )
        {
            return StringHelp.escape_toUnicode(this.r);
        }
        else
        {
            return "";
        }
    }


    
    public void setR(String r)
    {
        this.r = r;
    }


    
    public boolean isAllowGet()
    {
        return allowHttpGet;
    }


    
    public void setAllowGet(boolean allowHttpGet)
    {
        this.allowHttpGet = allowHttpGet;
    }
    
}
