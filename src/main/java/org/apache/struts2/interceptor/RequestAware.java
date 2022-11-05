package org.apache.struts2.interceptor;

import java.util.Map;





public interface RequestAware
{
    
  public void setRequest(Map<String, Object> paramMap);
  
}