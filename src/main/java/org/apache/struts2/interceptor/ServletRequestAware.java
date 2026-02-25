package org.apache.struts2.interceptor;

import jakarta.servlet.http.HttpServletRequest;





public interface ServletRequestAware
{
    
  public void setServletRequest(HttpServletRequest paramHttpServletRequest);
  
}