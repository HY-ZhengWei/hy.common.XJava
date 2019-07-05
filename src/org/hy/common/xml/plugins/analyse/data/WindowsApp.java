package org.hy.common.xml.plugins.analyse.data;

import org.hy.common.xml.SerializableDef;





/**
 * 云桌面的App信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-07-05
 * @version     v1.0
 */
public class WindowsApp extends SerializableDef
{
    
    private static final long serialVersionUID = 1528394740450653848L;

    /** App标识ID */
    private String  appID;
    
    /** App名称 */
    private String  appName;
    
    /** App图标路径（允许为空） */
    private String  icon;
    
    /** App图标的背景色 */
    private String  backgroundColor;
    
    /** App访问地址 */
    private String  url;
    
    /** App访问类型 */
    private String  actionType;
    
    /** App访问确认 */
    private String  confirm;
    
    /** App图标大小的类型 */
    private String  sizeType;
    
    /** App的X坐标 */
    private Double  x;
    
    /** App的Y坐标 */
    private Double  y;
    
    /** App的层次级别 */
    private Integer level;
    
    /** App的父层次ID */
    private String  superID;
    
    /** App是否显示在桌面上 */
    private boolean desktopShow;
    
    
    
    public WindowsApp()
    {
        this.actionType  = "open";
        this.sizeType    = "middle";
        this.level       = 1;
        this.desktopShow = false;
    }

    
    
    /**
     * 获取：App标识ID
     */
    public String getAppID()
    {
        return appID;
    }

    
    /**
     * 获取：App名称
     */
    public String getAppName()
    {
        return appName;
    }

    
    /**
     * 获取：App图标路径（允许为空）
     */
    public String getIcon()
    {
        return icon;
    }

    
    /**
     * 获取：App图标的背景色
     */
    public String getBackgroundColor()
    {
        return backgroundColor;
    }

    
    /**
     * 获取：App访问地址
     */
    public String getUrl()
    {
        return url;
    }

    
    /**
     * 获取：App访问类型
     */
    public String getActionType()
    {
        return actionType;
    }

    
    /**
     * 获取：App访问确认
     */
    public String getConfirm()
    {
        return confirm;
    }

    
    /**
     * 获取：App图标大小的类型
     */
    public String getSizeType()
    {
        return sizeType;
    }

    
    /**
     * 获取：App的X坐标
     */
    public Double getX()
    {
        return x;
    }

    
    /**
     * 获取：App的Y坐标
     */
    public Double getY()
    {
        return y;
    }

    
    /**
     * 获取：App的层次级别
     */
    public Integer getLevel()
    {
        return level;
    }

    
    /**
     * 获取：App的父层次ID
     */
    public String getSuperID()
    {
        return superID;
    }

    
    /**
     * 设置：App标识ID
     * 
     * @param appID 
     */
    public void setAppID(String appID)
    {
        this.appID = appID;
    }

    
    /**
     * 设置：App名称
     * 
     * @param appName 
     */
    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    
    /**
     * 设置：App图标路径（允许为空）
     * 
     * @param icon 
     */
    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    
    /**
     * 设置：App图标的背景色
     * 
     * @param backgroundColor 
     */
    public void setBackgroundColor(String backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    
    /**
     * 设置：App访问地址
     * 
     * @param url 
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    
    /**
     * 设置：App访问类型
     * 
     * @param actionType 
     */
    public void setActionType(String actionType)
    {
        this.actionType = actionType;
    }

    
    /**
     * 设置：App访问确认
     * 
     * @param confirm 
     */
    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    
    /**
     * 设置：App图标大小的类型
     * 
     * @param sizeType 
     */
    public void setSizeType(String sizeType)
    {
        this.sizeType = sizeType;
    }

    
    /**
     * 设置：App的X坐标
     * 
     * @param x 
     */
    public void setX(Double x)
    {
        this.x = x;
    }

    
    /**
     * 设置：App的Y坐标
     * 
     * @param y 
     */
    public void setY(Double y)
    {
        this.y = y;
    }

    
    /**
     * 设置：App的层次级别
     * 
     * @param level 
     */
    public void setLevel(Integer level)
    {
        this.level = level;
    }

    
    /**
     * 设置：App的父层次ID
     * 
     * @param superID 
     */
    public void setSuperID(String superID)
    {
        this.superID = superID;
    }

    
    /**
     * 获取：App是否显示在桌面上
     */
    public boolean isDesktopShow()
    {
        return desktopShow;
    }

    
    /**
     * 设置：App是否显示在桌面上
     * 
     * @param desktopShow 
     */
    public void setDesktopShow(boolean desktopShow)
    {
        this.desktopShow = desktopShow;
    }
    
}
