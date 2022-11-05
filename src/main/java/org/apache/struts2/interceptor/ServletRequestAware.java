package org.apache.struts2.interceptor;

import javax.servlet.http.HttpServletRequest;





public interface ServletRequestAware
{
    
  public void setServletRequest(HttpServletRequest paramHttpServletRequest);
  
}