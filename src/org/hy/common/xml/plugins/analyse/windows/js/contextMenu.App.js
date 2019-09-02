var v_AppMenus = null;



/**
 * 鼠标右击App图标，显示右击菜单
 *
 * i_G     哪个App图标
 * i_Data  App图标数据
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function appOnContextmenu(i_G ,i_Data)
{
	hideAppDesktopMenu();
	hideColorPicker();
	hideWindowStartMenus(1);
	hideAppMenu();
	
	if ( i_Data != null && i_G != null )
	{
		var v_MySize = v_Sizes[i_Data.sizeType];
		var v_XY     = getGXY(i_G);
		
		for (var i=0; i<v_AppMenusSizeConfig.length; i++)
		{
			v_AppMenusSizeConfig[i].valid = true;
		}
		
		if ( i_Data.sizeType == "max" )
		{
			v_XY[0] = Number(v_XY[0]) - 9;
			v_XY[1] = Number(v_XY[1]) - 6;
			v_AppMenusSizeConfig[1].valid = false;
		}
		else if ( i_Data.sizeType == "longWidth" )
		{
			v_XY[0] = Number(v_XY[0]) - 9;
			v_XY[1] = Number(v_XY[1]) - 64;
			v_AppMenusSizeConfig[2].valid = false;
		}
		else if ( i_Data.sizeType == "longHeight" )
		{
			v_XY[0] = Number(v_XY[0]) - 64;
			v_XY[1] = Number(v_XY[1]) - 9;
			v_AppMenusSizeConfig[3].valid = false;
		}
		else if ( i_Data.sizeType == "middle" )
		{
			v_XY[0] = Number(v_XY[0]) - 64;
			v_XY[1] = Number(v_XY[1]) - 64;
			v_AppMenusSizeConfig[4].valid = false;
		}
		else if ( i_Data.sizeType == "min" )
		{
			v_XY[0] = Number(v_XY[0]) - 92;
			v_XY[1] = Number(v_XY[1]) - 92;
			v_AppMenusSizeConfig[5].valid = false;
		}
		
		
		v_AppMenus = v_SVG.append("g");
		createSmartContextMenu(v_AppMenus ,30 ,v_AppMenusConfig);
		v_AppMenus.attr("transform", "translate(" + v_XY[0] + "," + v_XY[1] + ")");
		
		v_ContextMenu = true;
		v_ContextG    = i_G;
		v_ContextData = i_Data;
	}
}



/**
 * 隐藏App图标的右击菜单
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function hideAppMenu()
{
	if ( v_AppMenus != null )
	{
		v_AppMenus.attr("transform", "translate(-99999,-99999)");
		disposeSmartContextMenu(v_AppMenus);
		v_AppMenus.remove();  /* 每次删除后重新创建的原因：为了保证永远在最上层 */
	}
	v_ContextMenu = false;
}



/**
 * 复制App图标
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickCopyApp()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		var v_NewData = $.extend(true, {}, v_ContextData);
		v_NewData.userID   = v_UserID;
		v_NewData.appID    = "APP" + v_UserID + "_" + (new Date()).getTime();
		v_NewData.appName += "-复本"; 
		v_NewData.x        = parseFloat(v_NewData.x) + 20;
		v_NewData.y        = parseFloat(v_NewData.y) + 20;
		v_NewData.appIndex = -1;
		
		/*
		v_NewData.x               = v_ContextData.x + 20;
		v_NewData.y               = v_ContextData.y + 20;
		v_NewData.appName         = v_ContextData.appName + "-复本"; 
		v_NewData.icon            = v_ContextData.icon;
		v_NewData.backgroundColor = v_ContextData.backgroundColor;
		v_NewData.sizeType        = v_ContextData.sizeType;
		v_NewData.actionType      = v_ContextData.actionType;
		v_NewData.url             = v_ContextData.url;
		*/
		
		v_Apps.push(v_NewData);
		var v_MyG = createApp(v_NewData ,null);
		v_MyG
		.attr("opacity" ,0)
		.transition()
		.duration(1200)
		.attr("opacity" ,1);
		
		commitWindowAppCreate(v_NewData);
	}
}



/**
 * 删除App图标
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickDelApp()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		$('#delAppTextDiv').html("确认删除 " + v_ContextData.appName + " ？");
		$('#delAppDialog').modal('show');
	}
}



/**
 * App图标改颜色
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickReColor()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		showColorPicker(v_ContextData.backgroundColor ,d3.event.pageX ,d3.event.pageY ,function(i_Color)
		{
			v_ContextData.backgroundColor = i_Color;
			v_ContextG.select("rect").attr("fill" ,v_ContextData.backgroundColor);
			
			commitWindowAppXXColorSize(v_ContextData);
		});
	}
}



/**
 * 显示App图标改变大小的聪明右击菜单 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickShowSize()
{
	v_ContextMenu = false;
	disposeSmartContextMenu(v_AppMenus);
	setTimeout(function()                /* 延时处理的原因：点击也会同时触发桌面的点击事件 */
	{
		createSmartContextMenu(v_AppMenus ,30 ,v_AppMenusSizeConfig);
		v_ContextMenu = true;
	} ,100);
}



/**
 * App图标改变大小的聪明右击菜单的返回
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickSizeGoBack()
{
	v_ContextMenu = false;
	disposeSmartContextMenu(v_AppMenus);
	setTimeout(function()                /* 延时处理的原因：点击也会同时触发桌面的点击事件 */
	{
		createSmartContextMenu(v_AppMenus ,30 ,v_AppMenusConfig);
		v_ContextMenu = true;
	} ,100);
}



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickResizeMin()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		changeAppSize(v_ContextG ,v_ContextData ,"min");
	}
}



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickResizeMiddle()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		changeAppSize(v_ContextG ,v_ContextData ,"middle");
	}
}



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickResizeLongWidth()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		changeAppSize(v_ContextG ,v_ContextData ,"longWidth");
	}
}



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickResizeLongHeight()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		changeAppSize(v_ContextG ,v_ContextData ,"longHeight");
	}
}



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickResizeMax()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		changeAppSize(v_ContextG ,v_ContextData ,"max");
	}
}



/**
 * App图标重命名 
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickRename()
{
	hideAppMenu();
	
	if ( v_ContextG != null && v_ContextData != null )
	{
		$('#renameText').val(v_ContextData.appName);
		$('#renameDialog').modal('show');
	}
}



/**
 * App的属性
 *
 * ZhengWei(HY) Add 2019-09-02
 */
function menuOnClickEditApp()
{
	hideAppMenu();
	showEditAppDialog();
}



var v_AppMenusConfig      = [{fontSize:16 ,onClick:menuOnClickEditApp  ,name:"编辑"} 
                            ,{fontSize:14 ,onClick:menuOnClickRename   ,name:"重命名"} 
                            ,{fontSize:14 ,onClick:menuOnClickReColor  ,name:"颜色"} 
                            ,{fontSize:14 ,onClick:menuOnClickCopyApp  ,name:"复制"} 
                            ,{fontSize:14 ,onClick:menuOnClickDelApp   ,name:"删除"}
                            ,{fontSize:14 ,onClick:menuOnClickShowSize ,name:"大小"}];

var v_AppMenusSizeConfig = [{fontSize:16 ,onClick:menuOnClickSizeGoBack       ,name:"返回"} 
                           ,{fontSize:14 ,onClick:menuOnClickResizeMax        ,name:"大"} 
                           ,{fontSize:14 ,onClick:menuOnClickResizeLongWidth  ,name:"宽"} 
                           ,{fontSize:14 ,onClick:menuOnClickResizeLongHeight ,name:"高"} 
                           ,{fontSize:14 ,onClick:menuOnClickResizeMiddle     ,name:"中"}
                           ,{fontSize:14 ,onClick:menuOnClickResizeMin        ,name:"小"}];

hideAppMenu();