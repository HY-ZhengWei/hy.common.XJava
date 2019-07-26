var v_MWindowMenuBGColor  = d3.select(".MWindowMenuBar").style("background-color");     /* 菜单的背景色 */ 
var v_MenuHeight          = 20 + 2 + 2 + 2 + 1;                                         /* 菜单的高度 */
var v_MWindowMenuMaxLevel = d3.selectAll(".MWindowMenuBar").size();                     /* 允许有多少层菜单 */
var v_MoveAppID           = "";                                                         /* 移动菜单时的AppID */
var v_MoveDefaultIcon     = d3.select("#urlMWindowDrag").select("img").attr("src");     /* 移动菜单时的默认图标 */
var v_BodyX = 0;
var v_BodyY = 0;
d3.select("body")
.on("mousemove" ,function()
{
	v_BodyX = d3.mouse(this)[0];
	v_BodyY = d3.mouse(this)[1];
});

var v_MWindowMenuIsDrag = false;
var v_MWindowMenuX      = 0;
var v_MWindowMenuY      = 0;
var v_MWindowMenuDrag   = d3.drag()
.on("start" ,function()
{ 
	dragHideMWindowsMenusBar();
	
	v_MoveAppID = d3.select(this).attr("data-appID");
	
	v_MWindowMenuIsDrag = true;
	v_MWindowMenuX      = null;
	v_MWindowMenuY      = null;
	
	var v_UrlMWindow = d3.select("#urlMWindowDrag")
	.style("left" ,v_BodyX + "px")
	.style("top"  ,v_BodyY + "px");
	
	var v_App = v_StartMenuDatas[v_StartMenuIndexes[v_MoveAppID]];
	if ( v_App.icon == null || v_App.icon == "" )
	{
		v_UrlMWindow.select("img").attr("src" ,v_MoveDefaultIcon);
	}
	else
	{
		v_UrlMWindow.select("img").attr("src" ,v_App.icon);
	}
})
.on("drag", function()
{
	if ( v_MWindowMenuX == null )
	{
		v_MWindowMenuX = d3.event.x - 8;
	}
	if ( v_MWindowMenuY == null )
	{
		v_MWindowMenuY = d3.event.y - 8;
	}
	
	var v_UrlMWindow = d3.select("#urlMWindowDrag")
	.style("left" ,(v_BodyX + d3.event.x - v_MWindowMenuX) + "px")
	.style("top"  ,(v_BodyY + d3.event.y - v_MWindowMenuY) + "px");
})
.on("end" ,function()
{
	v_MWindowMenuIsDrag = false;
	d3.select("#urlMWindowDrag")
	.style("left" ,"-999999px")
	.style("top"  ,"-999999px");
	
	if ( v_ContentIDMove == null )
	{
		return;
	}
	if ( v_MoveAppID == null || v_MoveAppID == "" )
	{
		return;
	}
	
	var v_App       = v_StartMenuDatas[v_StartMenuIndexes[v_MoveAppID]];
	var v_MWContent = d3.select("#" + v_ContentIDMove);
	var v_MWControl = d3.select("#" + v_ContentIDMove + "_Control");
	
	v_MWContent.attr("data-url" ,v_App.url);
	v_MWControl.attr("data-url" ,v_App.url);
	
	loadMWindowContent(v_MWContent);
	
	v_MWControl
	.style("background-color" ,null)
	.style("opacity"          ,1);
	
	dragShowMWindowsMenusBar();
});



/**
 * 初始化菜单的的父子关系
 *
 * ZhengWei(HY) Add 2019-07-16
 */
function initMWindowsMenusForRef()
{
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
}



/**
 * 创建开始菜单及子菜单
 *
 * i_MenusConfig  菜单数据
 * i_Top          顶部位置
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function createMWindowMenus(i_MenusConfig ,i_Top)
{
	var v_MWControlBarHeight = getMWindowControlBarHeight();
	var v_Level              = parseInt(i_MenusConfig[0].level);
	var v_MWMenulevel        = d3.select(".MWindowMenuBarLevel_" + v_Level)
	.style("top" ,function(d ,i)
	{
		return i_Top + "px";
	})
	.style("display" ,"flex");
	
	v_MWMenulevel.selectAll(".MWindowMenu").data(i_MenusConfig).enter()
	.append("div")
	.attr("id" ,function(d ,i)
	{
		return "MWindowMenu_" + d.appID;
	})
	.attr("class" ,"MWindowMenu")
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
		d3.select(this).style("border" ,"1px solid " + v_MWindowMenuBGColor);
	})
	.on("click" ,function(d ,i)
	{
		if ( d.childMenus != null && d.childMenus.length >= 1 )
		{
			clearnMWindowsMenus(v_Level + 1);
			clearnMWindowsMenuSelected(v_Level);
			
			var v_Top = i_Top;
			v_Top = v_Top + i * v_MenuHeight + 1;
			
			createMWindowMenus(d.childMenus ,v_Top);
			
			d3.select(this).style("background-color" ,"gray");
		}
	});
	
	
	v_MWMenulevel.selectAll(".MWindowMenu").data(i_MenusConfig).each(function(d ,i)
	{
		var v_MyDiv = d3.select(this);
		createMWindowMenu(d ,v_MyDiv);
	});
}



/**
 * 创建菜单
 *
 * i_Data    某一菜单的数据
 * i_Div     某一菜单所在Div组件
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function createMWindowMenu(i_Data ,i_Div)
{
	if ( i_Data.icon == null || i_Data.icon == "" )
	{
		i_Div.append("img")
		.attr("class" ,"MWindowMenuIcon")
		.style("visibility" ,"hidden");
	}
	else
	{
		i_Div.append("img")
		.attr("class" ,"MWindowMenuIcon")
		.attr("src" ,i_Data.icon);
	}
	
	i_Div.append("lable")
	.attr("class" ,"MWindowMenuName")
	.html(i_Data.appName); 
	
	if ( i_Data.childMenus != null && i_Data.childMenus.length >= 1 )
	{
		i_Div.append("lable")
		.attr("class" ,"MWindowMenuNextLevel")
		.html("〉");
	}
	else
	{
		i_Div.call(v_MWindowMenuDrag);
	}
}



/**
 * 判定是否显示菜单
 * 
 * ZhengWei(HY) Add 2019-07-17
 */
function isShowMWindowsMenus()
{
	return d3.select(".MWindowMenuBarLevel_1").style("display") == "flex";
}



/**
 * 移动菜单时，临时性隐藏菜单Bar
 * 
 * ZhengWei(HY) Add 2019-07-17
 */
function dragHideMWindowsMenusBar()
{
	d3.selectAll(".MWindowMenuBar").style("display" ,"none");
}



/**
 * 移动菜单时，对临时性隐藏菜单Bar的显示
 * 
 * ZhengWei(HY) Add 2019-07-17
 */
function dragShowMWindowsMenusBar()
{
	d3.selectAll(".MWindowMenuBar").style("display" ,"flex");
}




/**
 * 隐藏某一层级及所有子层级的菜单
 * 
 * i_Level   隐藏哪个层级的。下标从1开始
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function hideMWindowsMenus(i_Level)
{
	for (var i=i_Level; i<=v_MWindowMenuMaxLevel; i++)
	{
		var v_ChildLevel = d3.select(".MWindowMenuBarLevel_" + i);
		v_ChildLevel.style("display" ,"none");
	}
}



/**
 * 清空某一层级及所有子层级的菜单
 * 
 * i_Level   清空哪个层级的。下标从1开始
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function clearnMWindowsMenus(i_Level)
{
	for (var i=i_Level; i<=v_MWindowMenuMaxLevel; i++)
	{
		var v_ChildLevel = d3.select(".MWindowMenuBarLevel_" + i);
		if ( !v_ChildLevel.empty() )
		{
			v_ChildLevel.selectAll(".MWindowMenu").remove();
		}
		
		v_ChildLevel.style("display" ,"none");
	}
}


/**
 * 清空某一层级及所有子层级的选择标记
 * 
 * i_Level   清空哪个层级的。下标从1开始
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function clearnMWindowsMenuSelected(i_Level)
{
	for (var i=i_Level; i<=v_MWindowMenuMaxLevel; i++)
	{
		var v_ChildLevel = d3.select(".MWindowMenuBarLevel_" + i);
		if ( !v_ChildLevel.empty() )
		{
			v_ChildLevel.selectAll(".MWindowMenu").style("background-color" ,v_MWindowMenuBGColor);
		}
	}
}



initMWindowsMenusForRef();