package org.hy.common.xml.junit.xcql.bean;

import org.hy.common.Date;
import org.hy.common.XJavaID;





/**
 * 数据源配置
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-08-18
 * @version     v1.0
 */
public class DataSourceConfig implements XJavaID
{
    
    /** 主键 */
    private String  id;
    
    /** 逻辑ID */
    private String  xid;
           
    /** 数据源类型。参见：DataSourceType */
    private String  dataSourceType;
           
    /** 连接驱动 */
    private String  driverClassName;
           
    /** 数据源IP地址 */
    private String  hostName;
           
    /** 数据源端口 */
    private Integer port;
           
    /** 用户名称 */
    private String  userName;
           
    /** 访问密码 */
    private String  password;
    
    /** 数据库名称 */
    private String  databaseName;
    
    /** 登录超时时长（单位：秒） */
    private Integer loginTimeout;
    
    /** 是否快速失败。主要用于测试 */
    private Boolean failFast;
    
    /** 初始大小 */
    private Integer initialSize;
    
    /** 最小空闲数量 */
    private Integer minIdle;
           
    /** 最大活动数量 */
    private Integer maxActive;
           
    /** 最大等待数量 */
    private Integer maxWait;
    
    /** 是否允许CDC功能 */
    private Integer allowCDC;
    
    /** 是否开启CDC */
    private Integer enabledCDC;
    
    /** 备注说明 */
    private String  comment;
    
    /** 创建时间 */
    private Date    createTime;

    
    
    /**
     * 获取：主键
     */
    public String getId()
    {
        return id;
    }

    
    /**
     * 设置：主键
     * 
     * @param i_Id
     */
    public void setId(String i_Id)
    {
        this.id = i_Id;
    }

    
    /**
     * 获取：逻辑ID
     */
    public String getXid()
    {
        return xid;
    }

    
    /**
     * 设置：逻辑ID
     * 
     * @param i_Xid
     */
    public void setXid(String i_Xid)
    {
        this.xid = i_Xid;
    }

    
    /**
     * 获取：数据源类型。参见：DataSourceType
     */
    public String getDataSourceType()
    {
        return dataSourceType;
    }

    
    /**
     * 设置：数据源类型。参见：DataSourceType
     * 
     * @param i_DataSourceType
     */
    public void setDataSourceType(String i_DataSourceType)
    {
        this.dataSourceType = i_DataSourceType;
    }

    
    /**
     * 获取：连接驱动
     */
    public String getDriverClassName()
    {
        return driverClassName;
    }

    
    /**
     * 设置：连接驱动
     * 
     * @param i_DriverClassName
     */
    public void setDriverClassName(String i_DriverClassName)
    {
        this.driverClassName = i_DriverClassName;
    }

    
    /**
     * 获取：数据源IP地址
     */
    public String getHostName()
    {
        return hostName;
    }

    
    /**
     * 设置：数据源IP地址
     * 
     * @param i_HostName
     */
    public void setHostName(String i_HostName)
    {
        this.hostName = i_HostName;
    }

    
    /**
     * 获取：数据源端口
     */
    public Integer getPort()
    {
        return port;
    }

    
    /**
     * 设置：数据源端口
     * 
     * @param i_Port
     */
    public void setPort(Integer i_Port)
    {
        this.port = i_Port;
    }

    
    /**
     * 获取：用户名称
     */
    public String getUserName()
    {
        return userName;
    }

    
    /**
     * 设置：用户名称
     * 
     * @param i_UserName
     */
    public void setUserName(String i_UserName)
    {
        this.userName = i_UserName;
    }

    
    /**
     * 获取：访问密码
     */
    public String getPassword()
    {
        return password;
    }

    
    /**
     * 设置：访问密码
     * 
     * @param i_Password
     */
    public void setPassword(String i_Password)
    {
        this.password = i_Password;
    }

    
    /**
     * 获取：初始大小
     */
    public Integer getInitialSize()
    {
        return initialSize;
    }

    
    /**
     * 设置：初始大小
     * 
     * @param i_InitialSize
     */
    public void setInitialSize(Integer i_InitialSize)
    {
        this.initialSize = i_InitialSize;
    }

    
    /**
     * 获取：最小空闲数量
     */
    public Integer getMinIdle()
    {
        return minIdle;
    }

    
    /**
     * 设置：最小空闲数量
     * 
     * @param i_MinIdle
     */
    public void setMinIdle(Integer i_MinIdle)
    {
        this.minIdle = i_MinIdle;
    }

    
    /**
     * 获取：最大活动数量
     */
    public Integer getMaxActive()
    {
        return maxActive;
    }

    
    /**
     * 设置：最大活动数量
     * 
     * @param i_MaxActive
     */
    public void setMaxActive(Integer i_MaxActive)
    {
        this.maxActive = i_MaxActive;
    }

    
    /**
     * 获取：最大等待数量
     */
    public Integer getMaxWait()
    {
        return maxWait;
    }

    
    /**
     * 设置：最大等待数量
     * 
     * @param i_MaxWait
     */
    public void setMaxWait(Integer i_MaxWait)
    {
        this.maxWait = i_MaxWait;
    }


    /**
     * 获取：数据库名称
     */
    public String getDatabaseName()
    {
        return databaseName;
    }


    /**
     * 设置：数据库名称
     * 
     * @param i_DatabaseName
     */
    public void setDatabaseName(String i_DatabaseName)
    {
        this.databaseName = i_DatabaseName;
    }
    
    
    /**
     * 获取：是否允许CDC功能
     */
    public Integer getAllowCDC()
    {
        return allowCDC;
    }

    
    /**
     * 设置：是否允许CDC功能
     * 
     * @param i_AllowCDC
     */
    public void setAllowCDC(Integer i_AllowCDC)
    {
        this.allowCDC = i_AllowCDC;
    }


    /**
     * 获取：是否开启CDC
     */
    public Integer getEnabledCDC()
    {
        return enabledCDC;
    }

    
    /**
     * 设置：是否开启CDC
     * 
     * @param i_EnabledCDC
     */
    public void setEnabledCDC(Integer i_EnabledCDC)
    {
        this.enabledCDC = i_EnabledCDC;
    }
    
    
    /**
     * 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的。
     * 
     * @param i_XJavaID
     */
    @Override
    public void setXJavaID(String i_XJavaID)
    {
        this.xid = i_XJavaID;
    }
    
    
    
    /**
     * 获取XJava池中对象的ID标识。
     * 
     * @return
     */
    @Override
    public String getXJavaID()
    {
        return this.xid;
    }
    
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     * 
     * @param i_Comment
     */
    @Override
    public void setComment(String i_Comment)
    {
        this.comment = i_Comment;
    }
    
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     *
     * @return
     */
    @Override
    public String getComment()
    {
        return this.comment;
    }


    
    /**
     * 获取：登录超时时长（单位：秒）
     */
    public Integer getLoginTimeout()
    {
        return loginTimeout;
    }


    
    /**
     * 设置：登录超时时长（单位：秒）
     * 
     * @param i_LoginTimeout 登录超时时长（单位：秒）
     */
    public void setLoginTimeout(Integer i_LoginTimeout)
    {
        this.loginTimeout = i_LoginTimeout;
    }


    
    /**
     * 获取：是否快速失败。主要用于测试
     */
    public Boolean getFailFast()
    {
        return failFast;
    }


    
    /**
     * 设置：是否快速失败。主要用于测试
     * 
     * @param i_FailFast 是否快速失败。主要用于测试
     */
    public void setFailFast(Boolean i_FailFast)
    {
        this.failFast = i_FailFast;
    }


    
    /**
     * 获取：创建时间
     */
    public Date getCreateTime()
    {
        return createTime;
    }

    
    
    /**
     * 设置：创建时间
     * 
     * @param i_CreateTime 创建时间
     */
    public void setCreateTime(Date i_CreateTime)
    {
        this.createTime = i_CreateTime;
    }
    
}
