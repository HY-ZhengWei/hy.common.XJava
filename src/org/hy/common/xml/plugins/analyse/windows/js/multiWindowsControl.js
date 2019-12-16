/**
 * 控制栏的鼠标划入自动显示事件
 *
 * ZhengWei(HY) Add 2019-07-11
 */
d3.select(".controlBar")
.on("mouseover" ,function()
{
	showMWindowControlBar();
})
.on("mouseout" ,function()
{
	if ( !v_IsSettingMW )
	{
		hideMWindowControlBar();
	}
});



/**
 * 获取控制栏的高度
 * 
 * ZhengWei(HY) Add 2019-07-16
 */
function getMWindowControlBarHeight()
{
	return 38;
}



/**
 * 隐藏控制栏
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function hideMWindowControlBar()
{
	d3.select(".controlBar").style("height" ,"10px");
	d3.select(".controlBarBackground").style("display" ,"none");
	
	clearnMWindowsMenus(1);
}



/**
 * 显示控制栏
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function showMWindowControlBar()
{
	d3.select(".controlBar").style("height" ,"auto");
	d3.select(".controlBarBackground").style("display" ,"flex");
}



/**
 * 控制层菜单的点击事件
 *
 * ZhengWei(HY) Add 2019-07-17
 */
d3.select("#menuMWindowsBtn").on("click" ,function()
{
	if ( isShowMWindowsMenus() )
	{
		clearnMWindowsMenus(1);
	}
	else
	{
		clearnMWindowsMenus(1);
		createMWindowMenus(v_StartMenus ,getMWindowControlBarHeight());
	}
});



d3.select("#defaultLayoutsBtn").on("click.d3" ,function()
{
	clearnMWindowsMenus(1);
});



/**
 * 编辑多屏窗口按钮的点击事件
 *
 * ZhengWei(HY) Add 2019-07-11
 */
d3.select("#editMWindowsBtn").on("click" ,function()
{
	v_IsSettingMW = true;
	v_IsFullMW    = false;
	var v_Color10 = d3.scaleOrdinal(d3.schemeCategory10);
	
	d3.selectAll(".MWindowMenusBtns")
	.attr("aria-disabled" ,"true")
	.style("opacity" ,1)
	.transition().duration(300)
	.style("opacity" ,0);
	
	d3.selectAll(".MWindowMenusBtns")
	.transition().delay(300)
	.style("display" ,"none");
	
	d3.selectAll(".MWindowSettingBtns")
	.style("opacity" ,0)
	.transition().delay(300).duration(300)
	.style("opacity" ,1)
	.style("display" ,"inline");
	
	/* 保存设计前的数据 */
	v_CacheDatas = {};
	d3.select("#MWindowBody").selectAll(".MWindowComponent")
	.filter(function()
	{
		var v_ID    = d3.select(this).attr("id")
		var v_Class = d3.select(this).attr("class");
		
		return (v_Class.indexOf("MWindowH") < 0 && v_Class.indexOf("MWindowV") < 0 && v_ID.indexOf("MWindowBody") < 0 );
	})
	.each(function(d ,i)
	{
		var v_MWContent = d3.select(this);
		
		v_CacheDatas[v_MWContent.attr("id")] = {width:      v_MWContent.style("width") 
			                                   ,height:     v_MWContent.style("height")
			                                   ,lockWidth:  v_MWContent.attr("data-lockWidth")
			                                   ,lockHeight: v_MWContent.attr("data-lockHeight")
			                                   ,url:        v_MWContent.attr("data-url")
			                                   ,openTimer:  v_MWContent.attr("data-openTimer")
			                                   ,timer:      v_MWContent.attr("data-timer")
			                                   ,classed:    ""};
	});
	d3.select("#MWindowBody").selectAll(".MWindowNull").each(function(d ,i)
	{
		var v_MWContent = d3.select(this);
		
		v_CacheDatas[v_MWContent.attr("id")] = {width:      v_MWContent.style("width") 
			                                   ,height:     v_MWContent.style("height")
			                                   ,lockWidth:  v_MWContent.attr("data-lockWidth")
			                                   ,lockHeight: v_MWContent.attr("data-lockHeight")
			                                   ,url:        ""
			                                   ,openTimer:  v_MWContent.attr("data-openTimer")
				                               ,timer:      v_MWContent.attr("data-timer")
			                                   ,classed:    "MWindowNull"};
	});
	
	var v_MWindowBodyControl = d3.select("#MWindowBody_Control").style("z-index" ,99999902);
	d3.select("#MWindowBody").selectAll(".MWindowContent")
	.style("opacity" ,1)
	.transition().duration(300)
	.style("opacity" ,0.5);
	
	
	v_HVSize = 5;
	calcAllComponentSize();
	showAllContentSizeInfo();
});



/**
 * 打开全屏显示控制模式
 *
 * ZhengWei(HY) Add 2019-08-22
 */
function openFullControlBar()
{
	v_IsFullMW = true;
	hideMWindowControlBar();
	
	
	var v_MWindowBodyControl = d3.select("#MWindowBody_Control").style("z-index" ,99999902);
	d3.select("#MWindowBody").selectAll(".MWindowContent")
	.style("opacity" ,1)
	.transition().duration(300)
	.style("opacity" ,0.5);
	
	d3.select("#MWindowBody_Control").selectAll(".MWindowContent").html("");
	
	setAllMWindowHV(0);
}



/**
 * 全屏显示按钮的点击事件
 *
 * ZhengWei(HY) Add 2019-08-22
 */
d3.select("#fullMWindowsBtn").on("click" ,function()
{
	openFullControlBar();
});



/**
 * 取消多屏窗口按钮的点击事件
 *
 * ZhengWei(HY) Add 2019-07-11
 */
d3.select("#cancelMWindowsBtn").on("click" ,function()
{
	v_IsSettingMW = false;
	hideMWindowControlBar();
	d3.selectAll(".MWindowMenusBtns")  .attr("aria-disabled" ,"false").style("opacity" ,1).style("display" ,"inline");
	d3.selectAll(".MWindowSettingBtns").attr("aria-disabled" ,"true") .style("display" ,"none");
	
	var v_MWindowBodyControl = d3.select("#MWindowBody_Control").style("z-index" ,99999900);
	
	if ( v_CacheDatas != null && v_CacheDatas.length >= 1 )
	{
		/* 恢复设计前的数据 */
		d3.select("#MWindowBody").selectAll(".MWindowComponent")
		.filter(function()
		{
			var v_ID    = d3.select(this).attr("id")
			var v_Class = d3.select(this).attr("class");
			
			return (v_Class.indexOf("MWindowH") < 0 && v_Class.indexOf("MWindowV") < 0 && v_ID.indexOf("MWindowBody") < 0 );
		})
		.each(function(d ,i)
		{
			var v_MWContent = d3.select(this);
			var v_MWControl = d3.select("#" + v_MWContent.attr("id") + "_Control");
			var v_Data      = v_CacheDatas[v_MWContent.attr("id")];
			
			/* 不用在此清除临时设定的定时器，清除动作将在loadMWindowAll()方法中执行 */
			
			v_MWContent
			.style("width"          ,v_Data.width)
			.style("height"         ,v_Data.height)
			.attr("data-lockWidth"  ,v_Data.lockWidth)
			.attr("data-lockHeight" ,v_Data.lockHeight)
			.attr("data-openTimer"  ,v_Data.openTimer)
			.attr("data-timer"      ,v_Data.timer)
			.attr("data-url"        ,v_Data.url);
			
			v_MWControl
			.style("width"          ,v_Data.width)
			.style("height"         ,v_Data.height)
			.attr("data-lockWidth"  ,v_Data.lockWidth)
			.attr("data-lockHeight" ,v_Data.lockHeight)
			.attr("data-openTimer"  ,v_Data.openTimer)
			.attr("data-timer"      ,v_Data.timer)
			.attr("data-url"        ,v_Data.url);
			
			/*
			if ( v_Data.classed != null && v_Data.classed != "" )
			{
				v_MWContent.attr("class" ,v_Data.classed).attr("data-url" ,"");
				v_MWControl.attr("class" ,v_Data.classed).attr("data-url" ,"");
				removeMWControlEvents(v_MWControl);
				
				v_MWContent.select("iframe").remove();
			}
			*/
		});
		d3.select("#MWindowBody").selectAll(".MWindowNull").each(function(d ,i)
		{
			var v_MWContent = d3.select(this);
			var v_MWControl = d3.select("#" + v_MWContent.attr("id") + "_Control");
			var v_Data      = v_CacheDatas[v_MWContent.attr("id")];
			
			if ( v_Data != null )
			{
				v_MWContent
				.style("width"  ,v_Data.width)
				.style("height" ,v_Data.height);
				
				v_MWControl
				.style("width"  ,v_Data.width)
				.style("height" ,v_Data.height);
			}
			
			v_MWContent.style("background-color" ,"white");
			v_MWControl.style("background-color" ,"white");
		});
	}
	
	setAllMWindowHV(0);
	loadMWindowAll();
	
	d3.select("#MWindowBody").selectAll(".MWindowContent")
	.style("opacity" ,0.5)
	.transition().duration(500)
	.style("opacity" ,1);
});



/**
 * 自动布局按钮的点击事件
 *
 * ZhengWei(HY) Add 2019-07-13
 */
d3.select("#reSizeMWindowsBtn").on("click" ,function()
{
	d3.selectAll(".MWindowNull")
	.each(function()
	{
		var v_MWComponent = d3.select(this);
		
		if ( "100%" != v_MWComponent.style("width") )
		{
			v_MWComponent.style("width"  ,"0px");
		}
		
		if ( "100%" != v_MWComponent.style("height") )
		{
			v_MWComponent.style("height" ,"0px");
		}
	});
	
	autoLayoutTo0Value();
	calcAllComponentSize();
	showAllContentSizeInfo();
	clearnMWindowsMenus(1);
});



/**
 * 自动布局全部0值
 *
 * ZhengWei(HY) Add 2019-07-16
 */
function autoLayoutTo0Value()
{
	d3.selectAll(".MWindowComponent")
	.filter(function()
	{
		var v_ID    = d3.select(this).attr("id")
		var v_Class = d3.select(this).attr("class");
		
		return (v_Class.indexOf("MWindowH") < 0 && v_Class.indexOf("MWindowV") < 0 && v_ID.indexOf("MWindowBody") < 0 );
	})
	.each(function()
	{
		var v_MWComponent  = d3.select(this);
		var v_IsLockWidth  = (v_MWComponent.attr("data-lockWidth")  != null && v_MWComponent.attr("data-lockWidth")  == "1");
		var v_IsLockHeight = (v_MWComponent.attr("data-lockHeight") != null && v_MWComponent.attr("data-lockHeight") == "1");
		
		if ( !v_IsLockWidth )
		{
			v_MWComponent.style("width"  ,"0px");
		}
		
		if ( !v_IsLockHeight )
		{
			v_MWComponent.style("height" ,"0px");
		}
	});
}



/**
 * 保存多屏窗口按钮的点击事件
 *
 * ZhengWei(HY) Add 2019-07-11
 */
d3.select("#saveMWindowsBtn").on("click" ,function()
{
	v_IsSettingMW = false;
	hideMWindowControlBar();
	d3.selectAll(".MWindowMenusBtns")  .attr("aria-disabled" ,"false").style("opacity" ,1).style("display" ,"inline");
	d3.selectAll(".MWindowSettingBtns").attr("aria-disabled" ,"true") .style("display" ,"none");
	

	var v_MWindowBodyControl = d3.select("#MWindowBody_Control").style("z-index" ,99999900);
	
	v_HVSize = 0;
	calcAllComponentSize();
	loadMWindowAll();
	
	d3.select("#MWindowBody").selectAll(".MWindowContent")
	.style("opacity" ,0.5)
	.transition().duration(500)
	.style("opacity" ,1);
	
	commitMultiWindowsSave();
});



/**
 * 重新整体布局
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function redoMWindowsLayout()
{
	v_CacheDatas = {};   /* 不允许恢复 */
	initMWindowsConfig();
	
	initAllComponents();
	initAllComponentControls();
	registerAllMWindowNullEvents();
	autoLayoutTo0Value();
	calcAllComponentSize();
	
	d3.select("#MWindowBody_Control").style("z-index" ,99999902);
	d3.select("#MWindowBody").selectAll(".MWindowContent").style("opacity" ,0.5);
	showAllContentSizeInfo();
	loadMWindowAll();
	
	clearnMWindowsMenus(1);
}



/**
 * 等宽布局与等高布局的转换
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#layoutTypeBtn").on("click" ,function()
{
	if ( "COL" == v_MWindowsType)
	{
		v_MWindowsType = "ROW";
		d3.select("#layoutTypeBtn").html("翻转布局-等宽");
	}
	else
	{
		v_MWindowsType = "COL";
		d3.select("#layoutTypeBtn").html("翻转布局-等高");
	}
	
	v_MWindowsConfig = getMWindowsConfig();
	
	redoMWindowsLayout();
});



/**
 * 一屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout1").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,1 ,1);
	redoMWindowsLayout();
});



/**
 * 二分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout2").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,1 ,2);
	redoMWindowsLayout();
});



/**
 * 三分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout3").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,1 ,3);
	redoMWindowsLayout();
});



/**
 * 三分屏(两窄中宽)
 *
 * ZhengWei(HY) Add 2019-07-16
d3.select("#defaultLayout3_21").on("click" ,function()
{
	console.log(v_MWindowsConfig);
	
	if ( "COL" == v_MWindowsType)
	{
		makeMWindowsConfig(v_MWindowsType ,1 ,3);
		
		v_MWindowsConfig[0].height               = "100px";
		v_MWindowsConfig[0].lockHeight           = "1";
		v_MWindowsConfig[0].childs[0].height     = "100px";
		v_MWindowsConfig[0].childs[0].lockHeight = "1";
		
		v_MWindowsConfig[2].height               = "100px";
		v_MWindowsConfig[2].lockHeight           = "1";
		v_MWindowsConfig[2].childs[0].height     = "100px";
		v_MWindowsConfig[2].childs[0].lockHeight = "1";
	}
	else
	{
		makeMWindowsConfig(v_MWindowsType ,3 ,1);
		
		v_MWindowsConfig[0].width               = "100px";
		v_MWindowsConfig[0].lockWidth           = "1";
		v_MWindowsConfig[0].childs[0].width     = "100px";
		v_MWindowsConfig[0].childs[0].lockWidth = "1";
		
		v_MWindowsConfig[2].width               = "100px";
		v_MWindowsConfig[2].lockWidth           = "1";
		v_MWindowsConfig[2].childs[0].width     = "100px";
		v_MWindowsConfig[2].childs[0].lockWidth = "1";
	}
	
	redoMWindowsLayout();
});
 */



/**
 * 四分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout4").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,2 ,2);
	redoMWindowsLayout();
});



/**
 * 六分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout6").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,2 ,3);
	redoMWindowsLayout();
});



/**
 * 八分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout8").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,2 ,4);
	redoMWindowsLayout();
});



/**
 * 九分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout9").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,3 ,3);
	redoMWindowsLayout();
});



/**
 * 十分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout10").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,2 ,5);
	redoMWindowsLayout();
});



/**
 * 十二分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout12").on("click" ,function()
{
	makeMWindowsConfig(v_MWindowsType ,3 ,4);
	redoMWindowsLayout();
});



/**
 * 十四分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout14").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,2 ,7);
	redoMWindowsLayout();
});



/**
 * 十五分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout15").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,3 ,5);
	redoMWindowsLayout();
});



/**
 * 十六分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout16").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,4 ,4);
	redoMWindowsLayout();
});



/**
 * 十六分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout18").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,3 ,6);
	redoMWindowsLayout();
});



/**
 * 二十分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout20").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,4 ,5);
	redoMWindowsLayout();
});



/**
 * 二十一分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout21").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,3 ,7);
	redoMWindowsLayout();
});



/**
 * 二十四分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayout24").on("click" ,function() 
{
	makeMWindowsConfig(v_MWindowsType ,4 ,6);
	redoMWindowsLayout();
});



/**
 * 自定义分屏
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#defaultLayoutX").on("click" ,function() 
{
	showLayoutXMWdinwoDialog();
});