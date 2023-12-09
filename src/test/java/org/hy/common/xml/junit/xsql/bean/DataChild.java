package org.hy.common.xml.junit.xsql.bean;

import org.hy.common.Help;





public class DataChild
{
    private String id;
    
    private String name;
    
    private String value;

    
    
    public String getId()
    {
        return id;
    }

    
    public void setId(String id)
    {
        this.id = id;
    }


	public String getName() 
	{
		return name;
	}


	public void setName(String name) 
	{
		this.name = name;
	}


	public String getValue() 
	{
		return value;
	}


	public void setValue(String value) 
	{
		this.value = value;
	}
    
	
	public String getValueName() 
	{
		return Help.isNull(value) ? null : "value";
	}
}
