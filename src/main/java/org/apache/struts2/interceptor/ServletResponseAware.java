package org.apache.struts2.interceptor;

import jakarta.servlet.http.HttpServletResponse;





public interface ServletResponseAware
{
    
  public void setServletResponse(HttpServletResponse paramHttpServletResponse);
  
}