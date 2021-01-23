package org.hy.common.xml.junit;

import org.junit.Test;

public class JU_Split
{
    
    @Test
    public void test_Split()
    {
        String v_Str = "http://wx.wzyb.com.cn/wx/wechat/template/send?keyword1=2018-12-11&keyword3=519%E4%BA%BA&keyword4=475%E4%BA%BA&keyword5=44%E4%BA%BA&remark=%E7%BC%BA%E5%8B%A4%EF%BC%9A44%E4%BA%BA%EF%BC%9B%0A%E8%BF%9F%E5%88%B0%EF%BC%9A57%E4%BA%BA%EF%BC%9B%0A%E6%97%A9%E9%80%80%EF%BC%9A51%E4%BA%BA%EF%BC%9B%0A%E5%87%BA%E5%85%A5%E5%BC%82%E5%B8%B8%EF%BC%9A27%E4%BA%BA%EF%BC%9B%0A%E8%AF%B7%E5%81%87%EF%BC%9A15%E4%BA%BA%EF%BC%9B%0A%E5%85%B7%E4%BD%93%E4%BA%BA%E5%91%98%E8%AF%B7%E6%9F%A5%E7%9C%8B%E8%AF%A6%E6%83%85&openid=ohwW61SyoGm9v1AP592xvY3tqTDI";
        
        String v_Srt02 = "A?B";
        System.out.println("indexOf=" + v_Str.indexOf("?"));
        System.out.println("split="   + v_Srt02.split("\\?")[0]);
    }
    
    
    
    @Test
    public void test_byte()
    {
        System.out.println((byte)3);
    }
    
}
