package org.hy.common.xml.plugins.analyse.data;

import java.io.File;

import org.hy.common.Date;
import org.hy.common.StringHelp;
import org.hy.common.xml.SerializableDef;





/**
 * 文件及目录信息
 * 
 * 注意：无论Windows、Linux系统，统一均使用 / 符号分隔路径。详见@see org.hy.common.xml.plugins.AppInitConfig
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-11
 * @version     v1.0
 */
public class FileReport extends SerializableDef
{

    private static final long serialVersionUID = -3632970496195709494L;
    
    
    /** 主机名称 */
    private String hostName;
    
    /** 文件或目录所在父目录 */
    private String  filePath;
    
    /** 文件或目录名称 */
    private String  fileName;
    
    /** 是否为目录 */
    private boolean isDirectory;
    
    /** 修改时间 */
    private String  lastTime;
    
    /** 大小 */
    private long    fileSize;
    
    /** 集群均有 */
    private int     clusterHave;
    
    
    
    public FileReport()
    {
        
    }
    
    
    
    public FileReport(File i_File)
    {
        this.hostName    = "";
        this.filePath    = StringHelp.replaceAll(i_File.getPath() ,"\\" ,"/");
        this.fileName    = i_File.getName();
        this.fileSize    = i_File.length();
        this.isDirectory = i_File.isDirectory();
        this.lastTime    = new Date(i_File.lastModified()).getFull();
        this.clusterHave = 1;
                
    }
    
    
    
    /**
     * 获取完整路径
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-11
     * @version     v1.0
     *
     * @return
     */
    public String getFullName()
    {
        if ( this.isDirectory )
        {
            return this.filePath;
        }
        else
        {
            return this.filePath + "/" + this.fileName;
        }
    }

    
    
    /**
     * 获取：主机名称
     */
    public String getHostName()
    {
        return hostName;
    }
    

    
    /**
     * 设置：主机名称
     * 
     * @param hostName 
     */
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }



    /**
     * 获取：文件或目录所在父目录
     */
    public String getFilePath()
    {
        return filePath;
    }
    

    
    /**
     * 获取：文件或目录名称
     */
    public String getFileName()
    {
        return fileName;
    }
    

    
    /**
     * 获取：是否为目录
     */
    public boolean isDirectory()
    {
        return isDirectory;
    }
    

    
    /**
     * 获取：修改时间
     */
    public String getLastTime()
    {
        return lastTime;
    }
    

    
    /**
     * 获取：大小
     */
    public long getFileSize()
    {
        return fileSize;
    }
    

    
    /**
     * 获取：集群均有
     */
    public int getClusterHave()
    {
        return clusterHave;
    }
    

    
    /**
     * 设置：文件或目录所在父目录
     * 
     * @param filePath 
     */
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    

    
    /**
     * 设置：文件或目录名称
     * 
     * @param fileName 
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    

    
    /**
     * 设置：是否为目录
     * 
     * @param isDirectory 
     */
    public void setDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }
    

    
    /**
     * 设置：修改时间
     * 
     * @param lastTime 
     */
    public void setLastTime(String lastTime)
    {
        this.lastTime = lastTime;
    }
    

    
    /**
     * 设置：大小
     * 
     * @param fileSize 
     */
    public void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }
    

    
    /**
     * 设置：集群均有
     * 
     * @param clusterHave 
     */
    public void setClusterHave(int clusterHave)
    {
        this.clusterHave = clusterHave;
    }
    
}
