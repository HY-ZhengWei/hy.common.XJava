package org.apache.struts2.interceptor;

import java.util.Map;





public interface ApplicationAware
{
    
  public void setApplication(Map<String, Object> paramMap);
  
}