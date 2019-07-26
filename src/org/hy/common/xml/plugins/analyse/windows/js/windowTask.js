$(".windowTaskShowDesktop").tooltip();
$(".windowTaskStartMenu")  .tooltip();



/**
 * 创建窗口的任务栏
 * 
 * i_WindowPage   d3创建的窗口对象
 * i_Data         App数据
 *
 * ZhengWei(HY) Add 2019-07-01
 */
function createWindowTask(i_WindowPage ,i_Data)
{
	var v_Div = d3.select("#windowTasksBar").append("div")
	.attr("id"                ,"windowTask_" + i_Data.appID)
	.attr("class"             ,"windowTask")
	.attr("data-toggle"       ,"tooltip")
	.attr("data-placement"    ,"top")
	.attr("title"             ,i_Data.appName)
	.style("background-color" ,i_Data.backgroundColor)
	.on("click" ,function()
	{
		if ( i_WindowPage.style("z-index") == 101 && i_WindowPage.style("left") != "-99999px" )
		{
			windowPageToMin(i_WindowPage);
		}
		else
		{
			windowPageToShow(i_WindowPage);
			windowPageToTop(i_WindowPage);
		}
	});
	
	v_Div.append("img")
	.attr("src" ,(i_Data.icon != null && i_Data.icon != "") ? i_Data.icon : v_DefaultWindowIcon);
	
	$("#" + v_Div.attr("id")).tooltip();
}



/**
 * 窗口置顶显示时，任务栏的窗口相互呼应的闪动
 * 
 * i_AppID         App的标识ID
 *
 * ZhengWei(HY) Add 2019-07-01
 */
function windowTaskToTop(i_AppID)
{
	d3.select("#windowTask_" + i_AppID)
	.transition().duration(120)
	.style("opacity" ,0.5)
	.transition().duration(120)
	.style("opacity" ,1)
	.transition().duration(120)
	.style("opacity" ,0.5)
	.transition().duration(120)
	.style("opacity" ,1)
	.transition().duration(120)
	.style("opacity" ,0.5)
	.transition().duration(120)
	.style("opacity" ,1);
}



/**
 * 获取任务栏的高度
 * 
 * ZhengWei(HY) Add 2019-07-03
 */
function getWindowTasksBarHeight()
{
	return parseInt(d3.select("#windowTasksBar").style("height").replace("px" ,""));
}



/**
 * 显示桌面按钮的点击事件
 * 
 * ZhengWei(HY) Add 2019-07-02
 */
d3.select(".windowTaskShowDesktop")
.on("mouseover" ,function()
{
	d3.select(this).style("background-color" ,"royalblue");
})
.on("mouseout" ,function()
{
	d3.select(this)
	.style("background-color" ,"gainsboro")
	.transition().duration(200)
	.style("background-color" ,"gainsboro");
})
.on("click" ,function()
{
	hideAppDesktopMenu();
	hideAppMenu();
	hideColorPicker();
	
	if ( windowPageIsAllMin() )
	{
		windowPageAllToShow();
	}
	else
	{
		windowPageAllToMin();
	}
	
	d3.select(this)
	.transition().duration(50)
	.style("background-color" ,"gainsboro")
	.transition().duration(100)
	.style("background-color" ,"royalblue");
	
	$(".windowTaskShowDesktop").tooltip('hide');
});



/**
 * 桌面开始菜单的点击事件
 * 
 * ZhengWei(HY) Add 2019-07-03
 */
d3.select(".windowTaskStartMenu")
.on("mouseover" ,function()
{
	d3.select(this)
	.transition().duration(200)
	.style("background-color" ,v_WindowStartMenuBGColor);
})
.on("mouseout" ,function()
{
	d3.select(this)
	.transition().duration(200)
	.style("background-color" ,"gainsboro");
})
.on("click" ,function()
{
	$(".windowTaskStartMenu").tooltip('hide');
	hideAppDesktopMenu();
	hideAppMenu();
	hideColorPicker();
	
	if ( isShowWindowStartMenus() )
	{
		clearnWindowStartMenus(1);
	}
	else
	{
		clearnWindowStartMenus(1);
		createWindowStartMenus(v_StartMenus ,v_WindowTasksBarHeight);
	}
});
