var v_CacheDatas     = {};                   /* 缓存信息，主要用于取消操作时的恢复 */
var v_ContentIDClick = "";                   /* 当前点击选择的内容组件的ID */
var v_ContentIDNull  = "";                   /* 当前点击选择的预留空白组件的ID */
var v_ContentIDMove  = "";                   /* 鼠标移动选择的内容组件的ID */
var v_IsSettingMW    = false;                /* 是否在设置多窗口模式 */
var v_HVSize         = 0;                    /* 横向、纵向分割栏的高度或宽度 */
var v_MWindowTop     = 0;
var v_MWindowBottom  = 0;
var v_MWindowLeft    = 0;
var v_MWindowRight   = 0;
var v_MWindowH       = 0;
var v_MWindowV       = 0;
var v_MWindowHDrag   = d3.drag()
.on("start" ,function()
{ 
	var v_H      = d3.select(this);
	var v_Top    = d3.select("#" + v_H.attr("data-top"));
	var v_Bottom = d3.select("#" + v_H.attr("data-bottom"));
	
	v_MWindowH   = d3.event.y;
	v_MWindowTop = parseFloat(v_Top.style("height").replace("px" ,""));
	
	if ( !v_Bottom.empty() )  /* 防止下部不存在 */
	{
		v_MWindowBottom = parseFloat(v_Bottom.style("height").replace("px" ,""));
	}
	
	clearnMWindowsMenus(1);
})
.on("drag", function()
{
	var v_H             = d3.select(this);
	var v_Top           = $("#" + v_H.attr("data-top"));
	var v_Bottom        = $("#" + v_H.attr("data-bottom"));
	var v_TopControl    = $("#" + v_H.attr("data-top")    + "_Control");
	var v_BottomControl = $("#" + v_H.attr("data-bottom") + "_Control");
	var v_Class         = "";
	var v_Width         = 0;
	var v_Height        = 0;
	
	v_Height = v_MWindowTop + (d3.event.y - v_MWindowH);
	v_Top       .css("height" ,v_Height + "px");
	v_TopControl.css("height" ,v_Height + "px");
	
	v_Class = v_Top.attr("class");
	if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
	{
		v_Width = parseFloat(v_Top.css("width").replace("px" ,""));
		
		calcAllChildComponentSize(v_Width ,v_Height ,v_Top);
		calcAllChildComponentSize(v_Width ,v_Height ,v_TopControl);
	}
	
	if ( !d3.select("#" + v_H.attr("data-bottom")).empty() )  /* 防止下部不存在 */
	{
		if ( v_BottomControl.attr("class") != "MWindowNull" )
		{
			v_Width  = parseFloat(v_Bottom.css("width").replace("px" ,""));
			v_Height = v_MWindowBottom - (d3.event.y - v_MWindowH);
			
			v_Bottom       .css("height" ,v_Height + "px");
			v_BottomControl.css("height" ,v_Height + "px");
			
			v_Class = v_Bottom.attr("class");
			if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
			{
				calcAllChildComponentSize(v_Width ,v_Height ,v_Bottom);
				calcAllChildComponentSize(v_Width ,v_Height ,v_BottomControl);
			}
		}
	}
	
	showAllContentSizeInfo();
});

var v_MWindowVDrag = d3.drag()
.on("start" ,function()
{ 
	var v_V     = d3.select(this);
	var v_Left  = d3.select("#" + v_V.attr("data-left")); 
	var v_Right = d3.select("#" + v_V.attr("data-right"));
	
	v_MWindowV    = d3.event.x;
	v_MWindowLeft = parseFloat(v_Left .style("width").replace("px" ,""));
	
	if ( !v_Right.empty() )  /* 防止右侧不存在 */
	{
		v_MWindowRight = parseFloat(v_Right.style("width").replace("px" ,""));
	}
	
	clearnMWindowsMenus(1);
})
.on("drag", function()
{
	var v_V            = d3.select(this);
	var v_Left         = $("#" + v_V.attr("data-left"));
	var v_Right        = $("#" + v_V.attr("data-right"));
	var v_LeftControl  = $("#" + v_V.attr("data-left")  + "_Control");
	var v_RightControl = $("#" + v_V.attr("data-right") + "_Control");
	var v_Class        = "";
	var v_Width        = 0;
	var v_Height       = 0;
	
	v_Width = (v_MWindowLeft  + (d3.event.x - v_MWindowV));
	v_Left       .css("width" ,v_Width + "px");
	v_LeftControl.css("width" ,v_Width + "px");
	
	v_Class = v_Left.attr("class");
	if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
	{
		v_Height = parseFloat(v_Left.css("height").replace("px" ,""));
		
		calcAllChildComponentSize(v_Width ,v_Height ,v_Left);
		calcAllChildComponentSize(v_Width ,v_Height ,v_LeftControl);
	}
	
	if ( !d3.select("#" + v_V.attr("data-right")).empty() ) /* 防止右侧不存在 */
	{
		if ( v_RightControl.attr("class") != "MWindowNull" )
		{
			v_Width  = v_MWindowRight - (d3.event.x - v_MWindowV);
			v_Height = parseFloat(v_Right.css("height").replace("px" ,""));
			
			v_Right       .css("width" ,v_Width + "px");
			v_RightControl.css("width" ,v_Width + "px");
			
			var v_Class = v_Right.attr("class");
			if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
			{
				calcAllChildComponentSize(v_Width ,v_Height ,v_Right);
				calcAllChildComponentSize(v_Width ,v_Height ,v_RightControl);
			}
		}
	}
	
	showAllContentSizeInfo();
});



/**
 * 显示组件的大小尺寸信息
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function makeContentSizeInfo(i_MWindowContent)
{
	var v_Auto = "";
	if ( i_MWindowContent.attr("data-auto") != null )
	{
		v_Auto = "(自动伸展)";
	}
	/*
	i_MWindowContent.attr("id").replace("_Control" ,"")
    + v_Auto + "<br/>"
    + 
	*/
	return "宽"    + i_MWindowContent.style("width") .replace("px" ,"") 
	     + " * 高" + i_MWindowContent.style("height").replace("px" ,"");
}



/**
 * 显示所有组件的大小尺寸信息
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function showAllContentSizeInfo()
{
	d3.select("#MWindowBody_Control").selectAll(".MWindowContent")
	.html(function()
	{
		return makeContentSizeInfo(d3.select(this));
	});
	
	d3.select("#MWindowBody_Control").selectAll(".MWindowNull")
	.html(function()
	{
		return "添加多屏";  /*"预留" + makeContentSizeInfo(d3.select(this));*/
	});
}



/**
 * 加载“展示层”组件的内容
 * 
 * i_MWContent   展示层组件
 *
 * ZhengWei(HY) Add 2019-07-12
 */
function loadMWindowContent(i_MWContent)
{
	var v_MWContentIFrame = i_MWContent.select("iframe");
	if ( v_MWContentIFrame.empty() )
	{
		i_MWContent
		.append("iframe")
		.attr("width" ,"100%")
		.attr("height" ,"100%")
		.attr("frameborder" ,"0")
		.attr("border" ,"0")
		.attr("marginwidth" ,"0")
		.attr("marginheight" ,"0")
		.attr("allowtransparency" ,"yes")
		.attr("src" ,i_MWContent.attr("data-url"));
	}
	else
	{
		if ( v_MWContentIFrame.attr("src") != i_MWContent.attr("data-url") )
		{
			v_MWContentIFrame.attr("src" ,i_MWContent.attr("data-url"));
		}
	}
}



/**
 * 加载所有“展示层”组件的内容
 * 
 * i_MWContent   展示层组件
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function loadMWindowAll()
{
	d3.select("#MWindowBody").selectAll(".MWindowContent").each(function()
	{
		loadMWindowContent(d3.select(this));
	});
}



/**
 * 显示、隐藏所有水平、垂直分隔条
 * 
 * i_HVSize  水平、垂直分隔条的高或宽。0表示隐藏
 * 
 * ZhengWei(HY) Add 2019-07-13
 */
function setAllMWindowHV(i_HVSize)
{
	v_HVSize = i_HVSize;
	d3.selectAll(".MWindowH")
	.style("height"              ,v_HVSize + "px")
	.style("border-top-width"    ,(v_HVSize == 0 ? 0 : 1) + "px")
	.style("border-bottom-width" ,(v_HVSize == 0 ? 0 : 1) + "px")
	.style("display"             ,(v_HVSize == 0 ? "none" : "inline"));
	
	d3.selectAll(".MWindowV")
	.style("width"              ,v_HVSize + "px")
	.style("border-left-width"  ,(v_HVSize == 0 ? 0 : 1) + "px")
	.style("border-right-width" ,(v_HVSize == 0 ? 0 : 1) + "px")
	.style("display"            ,(v_HVSize == 0 ? "none" : "inline"));
}



/**
 * 计算子组件的大小，包括子子组件的大小
 * 
 * 不能动态用document.body.clientWidth | document.body.clientHeight的值。原因是设置过程中会有可能出现滚动条，造成可见区域大小的改变
 * 
 * i_SuperWidth      父层级的组件的宽度
 * i_SuperHeight     父层级的组件的高度
 * i_SuperComponent  父层级的组件
 *
 * ZhengWei(HY) Add 2019-07-10
 */
function calcAllChildComponentSize(i_SuperWidth ,i_SuperHeight ,i_SuperComponent)
{
	var v_OffsetSize   = 0;                                                                               /* 明确定义过大小的组件大小合计值 */
	var v_ContentCount = 0;                                                                               /* 内容组件的数量 */
	var v_HVCount      = 0;                                                                               /* 分割条的数量 */
	var v_SuperLayout  = (i_SuperComponent.attr("class").indexOf("MWindowColBar") >= 0) ? "COL" : "ROW";  /* 布局类型 */
	
	i_SuperComponent.children(".MWindowComponent").each(function()
	{
		var v_Class = $(this).attr("class");
		if ( v_Class.indexOf("MWindowH") < 0 && v_Class.indexOf("MWindowV") < 0 )
		{
			if ( "COL" == v_SuperLayout )
			{
				var v_OldHeight = $(this).css("height");
				if ( v_OldHeight == "" || v_OldHeight == "0px" )
				{
					v_ContentCount++;
				}
				else
				{
					v_OffsetSize += parseFloat(v_OldHeight.replace("px" ,""));
				}
			}
			else
			{
				var v_OldWidth = $(this).css("width");
				if ( v_OldWidth == "" || v_OldWidth == "0px" )
				{
					v_ContentCount++;
				}
				else
				{
					v_OffsetSize += parseFloat(v_OldWidth.replace("px" ,""));
				}
			}
		}
		else
		{
			v_HVCount++;
		}
	});
	
	var v_HVBorderSize = v_HVSize == 0 ? 0 : 2;
	var v_Width        = 0;
	var v_Height       = 0;
	var v_AutoWidth    = v_ContentCount == 0 ? i_SuperWidth  - v_OffsetSize : 0;
	var v_AutoHeight   = v_ContentCount == 0 ? i_SuperHeight - v_OffsetSize : 0;
	
	if ( "COL" == v_SuperLayout )
	{
		v_Width  = i_SuperWidth;
		v_Height = v_ContentCount == 0 ? -1 : (i_SuperHeight - v_OffsetSize) / v_ContentCount; 
	}
	else
	{
		v_Width  = v_ContentCount == 0 ? -1 : (i_SuperWidth  - v_OffsetSize) / v_ContentCount;
		v_Height = i_SuperHeight;
	}
	
	i_SuperComponent.children(".MWindowComponent").each(function()
	{
		var v_Class = $(this).attr("class");
		if ( v_Class.indexOf("MWindowH") >= 0 )
		{
			$(this).css("height"              ,v_HVSize + "px");
			$(this).css("border-top-width"    ,(v_HVSize == 0 ? 0 : 1) + "px");
			$(this).css("border-bottom-width" ,(v_HVSize == 0 ? 0 : 1) + "px");
			$(this).css("display" ,(v_HVSize == 0 ? "none" : "inline"));
		}
		else if ( v_Class.indexOf("MWindowV") >= 0 )
		{
			$(this).css("width"              ,v_HVSize + "px");
			$(this).css("border-left-width"  ,(v_HVSize == 0 ? 0 : 1) + "px");
			$(this).css("border-right-width" ,(v_HVSize == 0 ? 0 : 1) + "px");
			$(this).css("display" ,(v_HVSize == 0 ? "none" : "inline"));
		}
		else
		{
			/*
			 * 无任是否显示水平、垂直分隔条，分隔条的大小均不计算在组件内。
			if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
			{
				v_Height += ((v_HVBorderSize + v_HVSize) * v_HVCount);
				v_Width  += ((v_HVBorderSize + v_HVSize) * v_HVCount);
			}
			*/
			
			var v_IsLockWidth  = ($(this).attr("data-lockWidth")  != null && $(this).attr("data-lockWidth")  == "1");
			var v_IsLockHeight = ($(this).attr("data-lockHeight") != null && $(this).attr("data-lockHeight") == "1");
			
			if ( "COL" == v_SuperLayout )
			{
				if ( !v_IsLockHeight )
				{
					if ( v_Height >= 0 )
					{
						var v_OldHeight = $(this).css("height");
						if ( v_OldHeight == "" || v_OldHeight == "0px" )
						{
							$(this).css("height" ,v_Height + "px");
						}
					}
					
					if ( $(this).attr("data-auto") != null )
					{
						$(this).css("height" ,(parseFloat($(this).css("height").replace("px" ,"")) + v_AutoHeight) + "px");
					}
				}
				
				if ( !v_IsLockWidth )
				{
					$(this).css("width" ,v_Width + "px");
				}
			}
			else
			{
				if ( !v_IsLockWidth )
				{
					if ( v_Width >= 0 )
					{
						var v_OldWidth = $(this).css("width");
						if ( v_OldWidth == "" || v_OldWidth == "0px" )
						{
							$(this).css("width"  ,v_Width + "px");
						}
					}
					
					if ( $(this).attr("data-auto") != null )
					{
						$(this).css("width" ,(parseFloat($(this).css("width").replace("px" ,"")) + v_AutoWidth) + "px");
					}
				}
				
				if ( !v_IsLockHeight )
				{
					$(this).css("height" ,v_Height + "px");
				}
			}
			
			if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
			{
				calcAllChildComponentSize(parseFloat($(this).css("width") .replace("px" ,"")) 
										 ,parseFloat($(this).css("height").replace("px" ,"")) 
									 	 ,$(this));
			}
		}
	});
	
	i_SuperComponent.children(".MWindowNull").each(function()
	{
		var v_MWNull = $(this);
		
		if ( "COL" == v_SuperLayout )
		{
			v_MWNull.css("width"  ,i_SuperWidth + "px");
			v_MWNull.css("height" ,150 + "px");
		}
		else
		{
			v_MWNull.css("width"  ,150 + "px");
			v_MWNull.css("height" ,i_SuperHeight + "px");
		}
		
		if ( v_HVSize <= 0 )
		{
			v_MWNull.css("background-color" ,"white");
		}
		else
		{
			v_MWNull.css("background-color" ,"PaleTurquoise");
		}
	});
}



/**
 * 计算所有组件的大小，包括子子组件的大小。
 * 同时，也包含“控制层”组件
 * 
 * 不能动态用document.body.clientWidth | document.body.clientHeight的值。原因是设置过程中会有可能出现滚动条，造成可见区域大小的改变
 *
 * ZhengWei(HY) Add 2019-07-10
 */
function calcAllComponentSize()
{
	var v_ClientWidth  = document.body.clientWidth;
	var v_ClientHeight = document.body.clientHeight;
	
	calcAllChildComponentSize(v_ClientWidth ,v_ClientHeight ,$("#MWindowBody"));
	calcAllChildComponentSize(v_ClientWidth ,v_ClientHeight ,$("#MWindowBody_Control"));
}



/**
 * 浏览器改变大小自动重新计算组件大小
 * 
 * ZhengWei(HY) Add 2019-07-13
 */
d3.select(window).on("resize" ,function()
{
	showAllContentSizeInfo();
});



/**
 * 生成水平分隔条的Html代码
 * 
 * i_TopID     上面展示层组件的ID
 * i_BottomID  下面展示层组件的ID
 *
 * ZhengWei(HY) Add 2019-07-12
 */
function makeMWindowHToHtml(i_TopID ,i_BottomID)
{
	return "<div class='MWindowComponent MWindowH' data-top ='" + i_TopID + "' data-bottom='" + i_BottomID + "'></div>";
}



/**
 * 生成水平分隔条的Html代码
 * 
 * i_LeftID   左侧展示层组件的ID
 * i_RightID  右侧展示层组件的ID
 *
 * ZhengWei(HY) Add 2019-07-12
 */
function makeMWindowVToHtml(i_LeftID ,i_RightID)
{
	return "<div class='MWindowComponent MWindowV' data-left ='" + i_LeftID + "' data-right='" + i_RightID + "'></div>";
}



/**
 * 初始化“展示层”组件
 * 
 * i_SuperComponent  父层级的组件
 * i_SuperLevel      父组件的层级。最顶级传入0值，有效值从1值。
 *
 * ZhengWei(HY) Add 2019-07-12
 * 
 * <div class="MWindowComponent MWindowH" data-top ="W001" data-bottom="W002"></div>
 * <div class="MWindowComponent MWindowV" data-left="W003" data-right ="W004"></div>
 */
function initAllChildComponents(i_SuperComponent ,i_SuperLevel)
{
	var v_SuperID     = i_SuperComponent.attr("id");
	var v_IDPrefix    = v_SuperID == "MWindowBody" ? "MW_" : v_SuperID + "_";
	var v_SuperLayout = (i_SuperComponent.attr("class").indexOf("MWindowColBar") >= 0) ? "COL" : "ROW";  /* 布局类型 */
	var v_IDIndex     = 0;
	var v_ID          = 0 + (i_SuperLevel * 100);
	var v_MWComponent = null; 
	var v_Class       = null;
	
	i_SuperComponent.children("*").each(function()
	{
		v_IDIndex++;
		v_ID++;
		v_MWComponent = $(this);
		
		v_MWComponent
		.attr("id"               ,v_IDPrefix + v_ID)
		.attr("data-super"       ,v_SuperID)
		.attr("data-superLevel"  ,i_SuperLevel)
		.attr("data-superLayout" ,v_SuperLayout)
		.attr("data-previous"    ,(v_IDIndex <= 1 ? v_SuperID : v_IDPrefix + (v_ID - 1)))
		.attr("data-next"        ,v_IDPrefix + (v_ID + 1));
		
		var v_SameLevelByPreviousID = v_ID;
		v_Class = v_MWComponent.attr("class");
		if ( v_Class.indexOf("MWindowRowBar") >= 0 || v_Class.indexOf("MWindowColBar") >= 0 )
		{
			initAllChildComponents(v_MWComponent ,i_SuperLevel + 1);
		}
		
		if ( "COL" == v_SuperLayout )
		{
			v_MWComponent.after(makeMWindowHToHtml(v_IDPrefix + v_SameLevelByPreviousID ,v_IDPrefix + (v_ID + 1)));
		}
		else
		{
			v_MWComponent.after(makeMWindowVToHtml(v_IDPrefix + v_SameLevelByPreviousID ,v_IDPrefix + (v_ID + 1)));
		}
	});

	/*
	 * 不默认开户自动伸展功能 
	 * v_MWComponent.attr("data-auto" ,"true"); 
	 * v_MWComponent.css("flex-shrink" ,1);
	 */
	if ( v_MWComponent != null )
	{
		/* 添加预留的空白组件 */
		var v_NullHtml = "";
		if ( v_Class.indexOf("MWindowColBar") >= 0 )
		{
			v_NullHtml = makeMWindowNull("MWindowColBar"  ,v_ID + 1 ,v_SuperID ,i_SuperLevel ,v_SuperLayout);
		}
		else if ( v_Class.indexOf("MWindowRowBar") >= 0 )
		{
			v_NullHtml = makeMWindowNull("MWindowRowBar"  ,v_ID + 1 ,v_SuperID ,i_SuperLevel ,v_SuperLayout);
		}
		else if ( v_Class.indexOf("MWindowContent") >= 0 )
		{
			v_NullHtml = makeMWindowNull("MWindowContent" ,v_ID + 1 ,v_SuperID ,i_SuperLevel ,v_SuperLayout);
		}
		else
		{
			return;
		}
		
		i_SuperComponent.append(v_NullHtml)
	}
}



/**
 * 初始化所有“展示层”组件
 * 
 * ZhengWei(HY) Add 2019-07-12
 */
function initAllComponents()
{
	initAllChildComponents($("#MWindowBody") ,0);
}



/**
 * 初始化“控制层”组件
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function initAllComponentControls()
{
	var v_MWindowBodyControl = d3.select("#MWindowBody").clone(true);
	
	v_MWindowBodyControl.attr("id" ,"MWindowBody_Control").selectAll("*")
	.filter(function()
	{
		var v_ID = d3.select(this).attr("id");
		
		return v_ID != null && v_ID != "";
	})
	.attr("id" ,function()
	{
		return d3.select(this).attr("id") + "_Control";
	});
	
	registerMWindowHEvents();
	registerMWindowVEvents();
	
	v_MWindowBodyControl.selectAll(".MWindowContent")
	.each(function()
	{
		registerMWControlEvents(d3.select(this));
	});
}



/**
 * 注册水平分隔条组件的事件
 * 
 * ZhengWei(HY) Add 2019-07-15
 */
function registerMWindowHEvents()
{
	d3.select("#MWindowBody_Control").selectAll(".MWindowH")
	.on("mouseover" ,function()
	{
		d3.select(this).style("background-color" ,"#C4E1A4");
	})
	.on("mouseout" ,function()
	{
		d3.select(this).style("background-color" ,"#DDDDDD");
	})
	.call(v_MWindowHDrag);
}



/**
 * 注册垂直分隔条组件的事件
 * 
 * ZhengWei(HY) Add 2019-07-15
 */
function registerMWindowVEvents()
{
	d3.select("#MWindowBody_Control").selectAll(".MWindowV")
	.on("mouseover" ,function()
	{
		d3.select(this).style("background-color" ,"#C4E1A4");
	})
	.on("mouseout" ,function()
	{
		d3.select(this).style("background-color" ,"#DDDDDD");
	})
	.call(v_MWindowVDrag);
}



/**
 * 注册“控制层”组件的事件
 * 
 * i_MWControl  控制层组件
 *
 * ZhengWei(HY) Add 2019-07-15
 */
function registerMWControlEvents(i_MWControl)
{
	i_MWControl
	.on("click" ,function()
	{
		v_ContentIDClick = d3.select(this).attr("id").replace("_Control" ,"");
		showEditMWindowDialog();
		clearnMWindowsMenus(1);
	})
	.on("mouseover" ,function()
	{
		v_ContentIDMove = d3.select(this).attr("id").replace("_Control" ,"");
		
		if ( v_MWindowMenuIsDrag )
		{
			d3.select(this)
			.style("opacity"          ,0.5)
			.style("background-color" ,"red");
		}
	})
	.on("mouseout" ,function()
	{
		v_ContentIDMove = null;
		
		d3.select(this)
		.style("background-color" ,null)
		.style("opacity"          ,1);
	});
}



/**
 * 注销“控制层”组件的事件
 * 
 * i_MWControl  控制层组件
 *
 * ZhengWei(HY) Add 2019-07-15
 */
function removeMWControlEvents(i_MWControl)
{
	i_MWControl
	.on("click"     ,null)
	.on("mouseover" ,null)
	.on("mouseout"  ,null);
}



/**
 * 检查是否配置了任意一个Url
 * 
 * ZhengWei(HY) Add 2019-07-18
 */
function checkHaveUrl()
{
	var v_Ret = false;
	
	d3.selectAll(".MWindowContent").each(function()
	{
		var v_Url = d3.select(this).attr("data-url");
		
		if ( v_Url != null && v_Url != "" )
		{
			v_Ret = true;
			return;
		}
	});
	
	return v_Ret;
}



initAllComponents();
initAllComponentControls();
registerAllMWindowNullEvents();
calcAllComponentSize();

$(function() 
{
	if ( checkHaveUrl() )
	{
		loadMWindowAll();
	}
	else
	{
		v_IsSettingMW = true;
		showMWindowControlBar();
	}
})

$('.dropdown-toggle').dropdown();