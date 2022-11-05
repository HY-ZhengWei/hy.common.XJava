/**
 * 显示退出确认对话窗口
 *
 * i_StartMenus  菜单数据
 * i_Bottom      底部位置
 *
 * ZhengWei(HY) Add 2019-09-09
 */
function showLogoutDialog()
{
	$('#logoutDialog').modal('show');
}



/**
 * 隐藏退出确认对话窗口
 *
 * i_StartMenus  菜单数据
 * i_Bottom      底部位置
 *
 * ZhengWei(HY) Add 2019-09-09
 */
function hideLogoutDialog()
{
	$('#logoutDialog').modal('hide');
}



/**
 * 退出确认对话窗口中的确定按钮的点击事件
 *
 * ZhengWei(HY) Add 2019-09-09
 */
d3.select("#logoutOK").on("click" ,function()
{
	hideLogoutDialog();
	commitLogout();
});