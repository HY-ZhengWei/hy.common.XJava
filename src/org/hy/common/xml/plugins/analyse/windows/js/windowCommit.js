/**
 * 添加桌面应用
 * 
 * @param i_NewApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-19
 */
function commitWindowAppCreate(i_NewApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_NewApp.userID   = v_UserNo;   /* 工号 */
    i_NewApp.userCode = v_UserID;   /* 登录账号 */
    
    console.log(i_NewApp);
    
    $.post("addWindowDesktopApp.page"
        ,i_NewApp
        ,function(data)
        {
        });
}



/**
 * 修改桌面应用
 * 
 * @param i_EditApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-21
 */
function commitWindowAppEdit(i_EditApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_EditApp.userID   = v_UserNo;
    i_EditApp.userCode = v_UserID;
    
    $.ajax({
         async: true
        ,dataType: "json"
        ,type: "post"
        ,url: "editWindowDesktopApp.page"
        ,data: i_EditApp
        ,success: function(data) 
        {
            console.log(data);
        }
        ,error: function (message) 
        {
            console.log(message);
        }
    });
}



/**
 * 修改桌面应用的位置、颜色、大小
 * 
 * @param i_EditApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-21
 */
function commitWindowAppXXColorSize(i_EditApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_EditApp.userID   = v_UserNo;
    i_EditApp.userCode = v_UserID;
    
    $.post("editWindowDesktopXYColorSize.page"
        ,i_EditApp
        ,function(data)
        {
        });
}



/**
 * 修改桌面应用的名称
 * 
 * @param i_EditApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-21
 */
function commitWindowAppName(i_EditApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_EditApp.userID   = v_UserNo;
    i_EditApp.userCode = v_UserID;
    
    $.post("editWindowDesktopAppName.page"
        ,i_EditApp
        ,function(data)
        {
        });
}



/**
 * 删除桌面应用
 * 
 * @param i_DelApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-21
 */
function commitWindowAppDel(i_DelApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_DelApp.userID   = v_UserNo;
    i_DelApp.userCode = v_UserID;
    
    $.post("delWindowDesktopApp.page"
        ,i_DelApp
        ,function(data)
        {
        });
}



/**
 * 恢复被删除桌面应用
 * 
 * @param i_RecoveryApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-21
 */
function commitWindowAppRecovery(i_RecoveryApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_RecoveryApp.userID   = v_UserNo;
    i_RecoveryApp.userCode = v_UserID;
    
    $.post("recoveryWindowDesktopApp.page"
        ,i_RecoveryApp
        ,function(data)
        {
        });
}



/**
 * 记录访问应用的次数
 * 
 * @param i_OpenApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-21
 */
function commitWindowAppOpenCount(i_OpenApp)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_OpenApp.userID   = v_UserNo;
    i_OpenApp.userCode = v_UserID;
    
    $.post("openCountApp.page"
        ,i_OpenApp
        ,function(data)
        {
        });
}



/**
 * 提交桌面的配置信息
 * 
 * @param i_DesktopBG
 * @returns
 * 
 * ZhengWei(HY) Add 2019-09-06
 */
function commitDesktopBG(i_DesktopBG)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    i_DesktopBG.userID   = v_UserNo;
    i_DesktopBG.userCode = v_UserID;
    
    $.post("desktopBG.page"
           ,i_DesktopBG
           ,function(data)
           {
           });
}



/**
 * 用户退出
 * 
 * @returns
 * 
 * ZhengWei(HY) Add 2019-09-09
 */
function commitLogout()
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    $.post("../login/logout.page"
    ,{}
    ,function(data)
    {
        window.location.href = "../home/index.page";
    });
}



/**
 * 保存Window应用与系统的关系
 * 
 * @param i_EditApp
 * @returns
 * 
 * ZhengWei(HY) Add 2019-09-08
 */
function commitSaveAppBySystem(i_EditApp)
{
    if ( !v_IsCommit )
    {
        return;
   }
    
    i_EditApp.userID   = v_UserNo;
    i_EditApp.userCode = v_UserID;
    
    $.post(
        "saveAppBySystem.page"
       ,i_EditApp
       ,function(data)
       {
           if ( data )
           {
               if ( data.datas == '1' )
               {
                   alert("同步记录完成");
               }
               else if ( data.datas == '-1' )
               {
                   alert("您尚未取得权限，可向管理员申请");
               }
               else
               {
                   alert("数据不完整或异常，请稍的重试");
               }
           }
           else
           {
               alert("数据不完整或异常，请稍的重试");
           }
           
           console.log(data);
       }
    );
}



/**
 * 同步云端桌面
 * 
 * @param i_DesktopBG
 * @returns
 * 
 * ZhengWei(HY) Add 2020-08-27
 */
function commitSyncCloudDesktop(i_DesktopBG)
{
    if ( !v_IsCommit )
    {
        return;
    }
    
    $.post("../login/syncCloudDesktop.page"
           ,{
               userID: v_UserNo
            }
           ,function(data)
           {
               console.log("同步云端桌面 = " + data);
               location.reload();
           });
}