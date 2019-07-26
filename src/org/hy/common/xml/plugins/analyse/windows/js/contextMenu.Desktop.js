/**
 * 新建App图标
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#newApp").on("click" ,function()
{
	hideAppDesktopMenu();
	
	showNewAppDialog();
});



/**
 * 新建App图标
 *
 * ZhengWei(HY) Add 2019-07-17
 */
d3.select("#newMultiWindows").on("click" ,function()
{
	hideAppDesktopMenu();
	
	showNewAppDialog("MWT_" + (new Date()).getTime());
});



/**
 * 恢复App图标
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#recovery").on("click" ,function()
{
	hideAppDesktopMenu();
	
	var v_DataIndex = v_Recoverys[v_Recoverys.length - 1];
	var v_Data      = v_Apps[v_DataIndex];
	var v_X         = v_Data.x;
	var v_Y         = v_Data.y;
	
	v_Data.x = v_Sizes[v_Data.sizeType].width  * -1;
	v_Data.y = v_Sizes[v_Data.sizeType].height * -1;
	
	var v_MyG = createApp(v_Data ,null);
	v_Data.x  = v_X;
	v_Data.y  = v_Y;
	
	v_MyG
	.transition()
	.duration(1500)
	.attr("transform", function() 
	{
	    return "translate(" + v_Data.x + "," + v_Data.y + ")";
	});
	
	v_Recoverys.pop(v_DataIndex);
	commitWindowAppRecovery(v_Data);
});



/**
 * 隐藏桌面的右击菜单
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function hideAppDesktopMenu()
{
	$("#appDesktopMenuBar").css("left" ,"-99999px");     
	$("#appDesktopMenuBar").css("top"  ,"-99999px");
	$("#appDesktopMenuBar").css("opacity" ,0);
	
	v_ContextMenu = false;
}