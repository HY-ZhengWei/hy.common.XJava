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
	
	$.post("addWidnowDesktopApp.page"
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
	
	$.post("editWidnowDesktopApp.page"
		,i_EditApp
		,function(data)
		{
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
	
	$.post("editWidnowDesktopXYColorSize.page"
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
	
	$.post("editWidnowDesktopAppName.page"
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
	
	$.post("delWidnowDesktopApp.page"
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
	
	$.post("recoveryWidnowDesktopApp.page"
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
	
	i_DesktopBG.userID = v_UserID;
	
	$.post("desktopBG.page"
		   ,i_DesktopBG
		   ,function(data)
		   {
		   });
}