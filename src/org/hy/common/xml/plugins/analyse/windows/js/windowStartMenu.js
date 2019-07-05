var v_WindowStartMenuBGColor  = d3.select(".windowStartMenuBar").style("background-color"); /* 开始菜单的背景色 */         
var v_MenuHeight              = 24 + 2 + 2 + 1;                                             /* 菜单的高度 */
var v_WindowTasksBarHeight    = getWindowTasksBarHeight();                                  /* 任务栏的高度 */
var v_WindowStartMenuMaxLevel = d3.selectAll(".windowStartMenuBar").size();                 /* 允许有多少层菜单 */
var v_AppData                 = null;                                                       /* 当前点击选择的App数据 */
var v_AppG                    = null;                                                       /* 当前点击选择的App组件 */
var v_CreateAppDrag           = d3.drag()                                                   /* 创建桌面App图标的拖动 */
.on("start" ,function()
{ 
	/* 点击或拖动时，会受到已打开窗口的影响，固特意将所有窗口最小化 */
	windowPageAllToMin();
	
	v_IsDrag    = true;
	v_DragCount = 0;
	v_XOffset   = 0;
	v_YOffset   = 0;
	
	var v_AppID = d3.select(this).attr("data-appID");
	v_AppData   = v_StartMenuDatas[v_StartMenuIndexes[v_AppID]];
	v_AppG      = getAppG(v_AppID);
	
	if ( v_AppG == null )
	{
		v_AppData.x = 0;
		v_AppData.y = 999999;
		
		if ( v_AppData.backgroundColor == null || v_AppData.backgroundColor == "" )
		{
			v_AppData.backgroundColor = "#4395FF";
		}
		
		v_Apps.push(v_AppData);
		v_AppG = createApp(v_AppData ,null);
	}
	else
	{
		var v_XY = getGXY(v_AppG);
		v_XOffset = d3.event.x - v_Sizes[v_AppData.sizeType].width;
		v_YOffset = d3.event.y;
	}
	
	hideWindowStartMenus(1);
})
.on("drag", function()
{
	v_DragCount++;
	appDragMoving(v_AppG);
})
.on("end" ,function()
{
	if ( v_DragCount <= 0 )
	{
		windowPageAllToShow();
		windowStartMenuOnClickToOpen(v_AppData);
	}
	else
	{
		appDragEnd(v_AppG);
	}
	
	v_DragCount = 0;
});



v_StartMenus = [];
if ( v_StartMenuDatas != null && v_StartMenuDatas.length >= 1 )
{
	/* 建立appID与索引号的关系 */
	for (var i=0; i<v_StartMenuDatas.length; i++)
	{
		v_StartMenuIndexes[v_StartMenuDatas[i].appID] = i;
	}
	
	/* 建立父子关系 */
	for (var i=0; i<v_StartMenuDatas.length; i++)
	{
		var v_SMenu = v_StartMenuDatas[i];
		if ( v_SMenu.superID != null && v_SMenu.superID != "" )
		{
			var v_SMenuSuper = v_StartMenuDatas[v_StartMenuIndexes[v_SMenu.superID]];
			if ( v_SMenuSuper.childMenus == null )
			{
				v_SMenuSuper.childMenus = [];
			}
			
			v_SMenuSuper.childMenus.push(v_SMenu);
		}
		
		if ( v_SMenu.level == 1 )
		{
			v_StartMenus.push(v_SMenu);
		}
	}
}



/**
 * 创建开始菜单及子菜单
 *
 * i_StartMenus  菜单数据
 * i_Bottom      底部位置
 *
 * ZhengWei(HY) Add 2019-07-03
 */
function createWindowStartMenus(i_StartMenus ,i_Bottom)
{
	var v_Level                = parseInt(i_StartMenus[0].level);
	var v_WindowStartMenulevel = d3.select(".windowStartMenuBarLevel_" + v_Level)
	.style("bottom" ,function(d ,i)
	{
		return i_Bottom + "px";
	})
	.style("display" ,"flex");
	
	v_WindowStartMenulevel.selectAll(".windowStartMenu").data(i_StartMenus).enter()
	.append("div")
	.attr("id" ,function(d ,i)
	{
		return "windowStartMenu_" + d.appID;
	})
	.attr("class" ,"windowStartMenu")
	.attr("data-appID" ,function(d ,i)
	{
		return d.appID;
	})
	.on("mouseover" ,function()
	{
		d3.select(this).style("border" ,"1px solid white");
	})
	.on("mouseout" ,function()
	{
		d3.select(this).style("border" ,"1px solid " + v_WindowStartMenuBGColor);
	})
	.on("click" ,function(d ,i)
	{
		if ( d.childMenus != null && d.childMenus.length >= 1 )
		{
			clearnWindowStartMenus(v_Level + 1);
			clearnWindowStartMenuSelected(v_Level);
			
			var v_AfterCount = i_StartMenus.length - i;
			if ( v_AfterCount >= d.childMenus.length )
			{
				v_AfterCount = v_AfterCount;
			}
			
			var v_Bottom = i_Bottom - v_WindowTasksBarHeight;
			v_Bottom = v_Bottom + (v_AfterCount - d.childMenus.length) * v_MenuHeight + v_WindowTasksBarHeight + 1;
			
			if ( v_Bottom < v_WindowTasksBarHeight )
			{
				v_Bottom = v_WindowTasksBarHeight;
			}
			createWindowStartMenus(d.childMenus ,v_Bottom);
			
			d3.select(this).style("background-color" ,"gray");
		}
		else
		{
			windowStartMenuOnClickToOpen(d);
		}
	});
	
	
	v_WindowStartMenulevel.selectAll(".windowStartMenu").data(i_StartMenus).each(function(d ,i)
	{
		var v_MyDiv = d3.select(this);
		createWindowStartMenu(d ,v_MyDiv);
	});
}



/**
 * 创建菜单
 *
 * i_Data    某一菜单的数据
 * i_Div     某一菜单所在Div组件
 *
 * ZhengWei(HY) Add 2019-07-03
 */
function createWindowStartMenu(i_Data ,i_Div)
{
	if ( i_Data.icon == null || i_Data.icon == "" )
	{
		i_Div.append("img")
		.attr("class" ,"windowStartMenuIcon")
		.style("visibility" ,"hidden");
	}
	else
	{
		i_Div.append("img")
		.attr("class" ,"windowStartMenuIcon")
		.attr("src" ,i_Data.icon);
	}
	
	i_Div.append("lable")
	.attr("class" ,"windowStartMenuName")
	.html(i_Data.appName); 
	
	if ( i_Data.childMenus != null && i_Data.childMenus.length >= 1 )
	{
		i_Div.append("lable")
		.attr("class" ,"windowStartMenuNextLevel")
		.html("〉");
	}
	else
	{
		i_Div.call(v_CreateAppDrag);
	}
}



/**
 * 点击菜单（无子菜单的），用于打开窗口的
 *
 * i_Data    某一菜单的数据
 *
 * ZhengWei(HY) Add 2019-07-04
 */
function windowStartMenuOnClickToOpen(i_Data)
{
	clearnWindowStartMenus(1);
	
	if ( i_Data.confirm != null && i_Data.confirm != "" )
	{
		$('#confirmAppTextDiv').html(i_Data.confirm);
		$('#confirmAppDialog').modal('show');
		
		d3.select("#confirmAppBtn").on("click" ,function()
		{
			$('#confirmAppDialog').modal('hide');
			
			openAppToWindowPage(i_Data);
		});
	}
	else
	{
		openAppToWindowPage(i_Data);
	}
}



/**
 * 判定是否显示开始菜单
 * 
 * ZhengWei(HY) Add 2019-07-03
 */
function isShowWindowStartMenus()
{
	return d3.select(".windowStartMenuBarLevel_1").style("display") == "flex";
}



/**
 * 隐藏某一层级及所有子层级的Start菜单
 * 
 * i_Level   隐藏哪个层级的。下标从1开始
 *
 * ZhengWei(HY) Add 2019-07-04
 */
function hideWindowStartMenus(i_Level)
{
	for (var i=i_Level; i<=v_WindowStartMenuMaxLevel; i++)
	{
		var v_ChildLevel = d3.select(".windowStartMenuBarLevel_" + i);
		v_ChildLevel.style("display" ,"none");
	}
}



/**
 * 清空某一层级及所有子层级的Start菜单
 * 
 * i_Level   清空哪个层级的。下标从1开始
 *
 * ZhengWei(HY) Add 2019-07-03
 */
function clearnWindowStartMenus(i_Level)
{
	for (var i=i_Level; i<=v_WindowStartMenuMaxLevel; i++)
	{
		var v_ChildLevel = d3.select(".windowStartMenuBarLevel_" + i);
		if ( !v_ChildLevel.empty() )
		{
			v_ChildLevel.selectAll(".windowStartMenu").remove();
		}
		
		v_ChildLevel.style("display" ,"none");
	}
}


/**
 * 清空某一层级及所有子层级的选择标记
 * 
 * i_Level   清空哪个层级的。下标从1开始
 *
 * ZhengWei(HY) Add 2019-07-03
 */
function clearnWindowStartMenuSelected(i_Level)
{
	for (var i=i_Level; i<=v_WindowStartMenuMaxLevel; i++)
	{
		var v_ChildLevel = d3.select(".windowStartMenuBarLevel_" + i);
		if ( !v_ChildLevel.empty() )
		{
			v_ChildLevel.selectAll(".windowStartMenu").style("background-color" ,v_WindowStartMenuBGColor);
		}
	}
}
