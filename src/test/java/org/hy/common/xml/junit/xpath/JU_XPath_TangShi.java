package org.hy.common.xml.junit.xpath;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.xml.XHttp;
import org.junit.Test;





public class JU_XPath_TangShi
{
    
    @Test
    public void testTangShis() throws XPatherException
    {
        String [] v_URLs = new String [] {
                 "shiwenv_7dddb391682f.aspx"
                ,"shiwenv_af4715c0208f.aspx"
                ,"shiwenv_a00028c4f0be.aspx"
                ,"shiwenv_fda1fda7b79f.aspx"
                ,"shiwenv_e95e4acad23a.aspx"
                ,"shiwenv_ff54370baf0f.aspx"
                ,"shiwenv_3e43768c1d5c.aspx"
                ,"shiwenv_917dfe1988fd.aspx"
                ,"shiwenv_fbdaf48358a2.aspx"
                ,"shiwenv_6c25baf854f7.aspx"
                ,"shiwenv_8074b3dafa95.aspx"
                ,"shiwenv_1104052ed0fc.aspx"
                ,"shiwenv_fdd3d225717e.aspx"
                ,"shiwenv_a02709de12f2.aspx"
                ,"shiwenv_a4d089e3c67c.aspx"
                ,"shiwenv_711db8a03e9b.aspx"
                ,"shiwenv_b5c0bb811d97.aspx"
                ,"shiwenv_587c09fc01d2.aspx"
                ,"shiwenv_f649c32eb639.aspx"
                ,"shiwenv_75d82c0ac549.aspx"
                ,"shiwenv_f1f50c61d00a.aspx"
                ,"shiwenv_10663e0d896a.aspx"
                ,"shiwenv_9236b601a746.aspx"
                ,"shiwenv_edd47d575a62.aspx"
                ,"shiwenv_b62512ad6673.aspx"
                ,"shiwenv_0aa84f69357f.aspx"
                ,"shiwenv_059fd4503f32.aspx"
                ,"shiwenv_a81debbfec24.aspx"
                ,"shiwenv_8fbc12c35787.aspx"
                ,"shiwenv_951923a130e9.aspx"
                ,"shiwenv_bd8bcb6f2d16.aspx"
                ,"shiwenv_daea625010b2.aspx"
                ,"shiwenv_6a8344dd20d5.aspx"
                ,"shiwenv_21165b39cb03.aspx"
                ,"shiwenv_a2c953453cb7.aspx"
                ,"shiwenv_b3db5ffb89d9.aspx"
                ,"shiwenv_c0b49aa817f9.aspx"
                ,"shiwenv_7c170d5debdf.aspx"
                ,"shiwenv_5fb735ca535e.aspx"
                ,"shiwenv_d52e05980359.aspx"
                ,"shiwenv_01e7144da0bb.aspx"
                ,"shiwenv_9f10336eb9b5.aspx"
                ,"shiwenv_85def5451d88.aspx"
                ,"shiwenv_877b2ff3be41.aspx"
                ,"shiwenv_e21f667cdcaf.aspx"
                ,"shiwenv_986da243bcf8.aspx"
                ,"shiwenv_17d16170b441.aspx"
                ,"shiwenv_3cce9f252672.aspx"
                ,"shiwenv_04f7c4ccfa13.aspx"
                ,"shiwenv_5f18244ab524.aspx"
                ,"shiwenv_d08246ab36e2.aspx"
                ,"shiwenv_cc65f1e46118.aspx"
                ,"shiwenv_bf758d053fca.aspx"
                ,"shiwenv_a584311f2198.aspx"
                ,"shiwenv_e8e3acf26a6a.aspx"
                ,"shiwenv_cfeddc2459c2.aspx"
                ,"shiwenv_a640d462c006.aspx"
        };
        
        String [] v_Titles = new String [] {
                 "学弈"
                ,"杨氏之子(刘义庆)"
                ,"伯牙鼓琴/伯牙绝弦"
                ,"叶公好龙(刘向)"
                ,"陈元方候袁公(刘义庆)"
                ,"孟母三迁(刘向)"
                ,"揠苗助长"
                ,"一毛不拔(邯郸淳)"
                ,"愚人食盐(僧伽斯那)"
                ,"刻舟求剑/楚人涉江"
                ,"读书要三到(朱熹)"
                ,"精卫填海"
                ,"世无良猫(乐钧)"
                ,"画蛇添足(刘向)"
                ,"掩耳盗铃(吕不韦撰)"
                ,"文侯与虞人期猎(刘向)"
                ,"郑人买履(韩非)"
                ,"晏子使楚(刘向)"
                ,"滥竽充数(韩非)"
                ,"富人之子(苏轼)"
                ,"人有亡斧者(吕不韦撰)"
                ,"北人食菱(江盈科)"
                ,"铁杵成针/铁杵磨针(郑之珍)"
                ,"父善游(吕不韦撰)"
                ,"晏子谏杀烛邹(刘向)"
                ,"三人成虎(刘向)"
                ,"曾子杀彘/曾子烹彘(韩非)"
                ,"邴原泣学(礼赞)"
                ,"朝三暮四"
                ,"截竿入城(邯郸淳撰)"
                ,"吴起守信(宋濂)"
                ,"荀巨伯探病友/荀巨(刘义庆)"
                ,"嫦娥奔月/嫦娥飞天(刘安撰)"
                ,"师旷撞晋平公(韩非)"
                ,"枭逢鸠/枭将东徙(刘向)"
                ,"杀驼破瓮(伽腽肭)"
                ,"外科医生(江盈科)"
                ,"书戴嵩画牛/杜处士(苏轼)"
                ,"问说(刘开)"
                ,"自相矛盾/矛与盾(韩非)"
                ,"人有负盐负薪者(李延寿)"
                ,"承宫樵薪苦学"
                ,"鹬蚌相争(刘向)"
                ,"多歧亡羊(列御寇)"
                ,"画地学书(欧阳修)"
                ,"虎求百兽(刘向)"
                ,"王勃故事(宋祁)"
                ,"二翁登泰山"
                ,"古人谈读书三则"
                ,"寇准读书"
                ,"破瓮救友"
                ,"林琴南敬师"
                ,"守株待兔"
                ,"桑生李树"
                ,"董行成"
                ,"王戎不取道旁李"
                ,"囊萤夜读"
        };
        
        for (int i=0; i<v_URLs.length; i++)
        {
            testTangShi(v_URLs[i] ,v_Titles[i]);
        }
    }
    
    
    
    public void testTangShi(String i_URL ,String i_Title) throws XPatherException
    {
        XHttp v_XHttp = new XHttp();
        
        v_XHttp.setProtocol("https");
        v_XHttp.setIp("so.gushiwen.org");
        v_XHttp.setPort(443);
        v_XHttp.setUrl(i_URL);
        v_XHttp.setHaveQuestionMark(false);
        
        Return<?>   v_Resonse     = v_XHttp.request();
        HtmlCleaner v_HtmlCleaner = new HtmlCleaner();
        TagNode     v_TagNode     = v_HtmlCleaner.clean(v_Resonse.paramStr);
        Object []   v_Titles      = v_TagNode.evaluateXPath("//div[@class='cont']/h1/text()");
        Object []   v_Authors     = v_TagNode.evaluateXPath("//p[@class='source']/text()");
        Object []   v_Contents    = v_TagNode.evaluateXPath("//div[@class='contson']/text()");
        String      v_RetTitle    = "";
        String      v_RetAuthor   = "";
        String      v_RetContent  = "";
        
        for (Object v_Title : v_Titles)
        {
            v_RetTitle = v_Title.toString().trim();
            if ( !Help.isNull(v_RetTitle) ) { break; }
        }
        
        for (Object v_Author : v_Authors)
        {
            v_RetAuthor = v_Author.toString().trim();
            if ( !Help.isNull(v_RetAuthor) ) { break; }
        }
        
        for (Object v_Content : v_Contents)
        {
            v_RetContent = v_Content.toString().trim();
            if ( !Help.isNull(v_RetContent) ) { break; }
        }
        
        System.out.println("{ name: '" + i_Title + "' ,content: '" + v_RetTitle + "\\n\\n" + v_RetAuthor + "\\n\\n" + StringHelp.replaceAll(v_RetContent ,new String[] {"," ,"，" ,"." ,"。" ,"?" ,"？"} ,new String[] {",\\n" ,"，\\n" ,".\\n" ,"。\\n" ,"?\\n" ,"？\\n"}) + "' },");
    }
    
    
}
