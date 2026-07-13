package org.hy.common.xml.junit.xjavaBuilder;

import java.net.MalformedURLException;
import java.net.URL;





/**
 * 测试对象：Builder创建对象实例
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-07-13
 * @version     v1.0
 */
public class DataBuilder
{
    
    
    private DataBuilder(String i_Host ,int i_Post ,URL i_URL)
    {
        System.out.println("create DataBuilder");
    }
    
    

    public static Builder builderX()
    {
        return new Builder();
    }
    
    
    
    public static final class Builder
    {

        private String  host;
        
        private Integer post;
        
        private URL     url;
        
        

        public Builder()
        {
            
        }

        public Builder host(String i_Host) 
        {
            System.out.println(i_Host);
            this.host = i_Host;
            return this;
        }
        
        public Builder post(int i_Post) 
        {
            System.out.println(i_Post);
            this.post = i_Post;
            return this;
        }
        
        public Builder endpoint(URL i_Url)
        {
            System.out.println(i_Url);
            this.url = i_Url;
            return this;
        }
        
        public Builder endpoint(String i_Url) throws MalformedURLException
        {
            System.out.println(i_Url);
            this.url = new URL(i_Url);
            return this;
        }
        
        public Builder endpoint(String i_Host ,int i_Post ,String i_Url) throws MalformedURLException
        {
            System.out.println("http://" + i_Host + ":" + i_Post + i_Url);
            this.url = new URL("http://" + i_Host + ":" + i_Post + i_Url);
            return this;
        }

        public DataBuilder buildX()
        {
            return new DataBuilder(this.host ,this.post ,this.url);
        }
    }
    
}
