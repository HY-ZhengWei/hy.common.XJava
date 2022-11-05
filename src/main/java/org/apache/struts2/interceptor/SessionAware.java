package org.apache.struts2.interceptor;

import java.util.Map;





public interface SessionAware
{
    
  public void setSession(Map<String, Object> paramMap);
  
}