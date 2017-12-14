package org.hy.common.xml.junit.app.msg;

import org.hy.common.Return;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.junit.app.bean.Version2;
import org.hy.common.xml.junit.app.common.BaseMSG;
import org.hy.common.xml.junit.app.interfaces.IVersionMSG;





/**
 * 用户的网络消息
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-23
 */
@Xjava
public class VersionMSG extends BaseMSG implements IVersionMSG
{

    public VersionMSG()
    {
        super();
    }


    public Return<?> version(Version2 i_Version)
    {
        return this.sendMsg("XHTTP_Avplan" ,$SID_Version ,1 ,i_Version);
    }

}
