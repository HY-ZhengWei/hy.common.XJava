var v_WindowPageMinWidth  = 260;                                /* 窗口改变大小时，允许最小宽度 */
var v_WindowPageMinHeight = 30;                                 /* 窗口改变大小时，允许最小高度 */
var v_DefaultWindowIcon   = "windows/images/windowTask.png";    /* 默认窗口图标 */



/**
 * 窗口的拖动
 *
 * ZhengWei(HY) Add 2019-07-01
 */
var v_WindowPageX    = 0;
var v_WindowPageY    = 0;
var v_WindowPageDrag = d3.drag()
.on("start" ,function()
{ 
	var v_WindowPage = d3.select(this);
	v_WindowPageX    = v_WindowPage.style("left").replace('px' ,'');
	v_WindowPageY    = v_WindowPage.style("top") .replace('px' ,'');
	
	/* 处理百分比的情况 */
	if ( v_WindowPageX.indexOf("%") > 0 )
	{
		v_WindowPageX = document.body.clientWidth * v_WindowPageX.replace('%' ,'') / 100;
	}
	
	/* 处理百分比的情况 */
	if ( v_WindowPageY.indexOf("%") > 0 )
	{
		v_WindowPageY = document.body.clientHeight * v_WindowPageY.replace('%' ,'') / 100;
	}
	
	v_WindowPageX = d3.event.x - v_WindowPageX;
	v_WindowPageY = d3.event.y - v_WindowPageY;
	
	v_WindowPage.select(".windowPageBodyControl").style("display" ,"inline");
})
.on("drag", function()
{
	d3.select(this)
	.style("left", (d3.event.x - v_WindowPageX) + "px")
	.style("top" , (d3.event.y - v_WindowPageY) + "px");                    
})
.on("end" ,function()
{
	d3.select(this).select(".windowPageBodyControl").style("display" ,"none");
});



/**
 * 窗口改变大小（东南两方向）
 *
 * ZhengWei(HY) Add 2019-07-01
 */
var v_WindowReSizeDrag_SE = d3.drag()
.on("start" ,function()
{ 
	d3.select(this).style("width"  ,"100%");
	d3.select(this).style("height" ,"100%");
})
.on("drag", function()
{
	var v_AppID      = d3.select(this).attr("data-appID");
	var v_WindowPage = d3.select("#windowPage_" + v_AppID);
	
	if ( d3.event.x >= v_WindowPageMinWidth )
	{
		v_WindowPage.style("width",  d3.event.x + "px");  
	}
	if ( d3.event.y >= v_WindowPageMinHeight )
	{
		v_WindowPage.style("height", d3.event.y + "px");
	}
})
.on("end" ,function()
{
	d3.select(this).style("width"  ,"5px");
	d3.select(this).style("height" ,"5px");
});



/**
 * 窗口改变大小（南方向）
 *
 * ZhengWei(HY) Add 2019-07-01
 */
var v_WindowReSizeDrag_S = d3.drag()
.on("start" ,function()
{ 
	d3.select(this).style("height" ,"100%");
})
.on("drag", function()
{
	var v_AppID      = d3.select(this).attr("data-appID");
	var v_WindowPage = d3.select("#windowPage_" + v_AppID);
	
	if ( d3.event.y >= v_WindowPageMinHeight )
	{
		v_WindowPage.style("height", d3.event.y + "px");
	}
})
.on("end" ,function()
{
	d3.select(this).style("height" ,"5px");
});



/**
 * 窗口改变大小（东方向）
 *
 * ZhengWei(HY) Add 2019-07-01
 */
var v_WindowReSizeDrag_E = d3.drag()
.on("start" ,function()
{ 
	d3.select(this).style("width" ,"100%");
})
.on("drag", function()
{
	var v_AppID      = d3.select(this).attr("data-appID");
	var v_WindowPage = d3.select("#windowPage_" + v_AppID);
	
	if ( d3.event.x >= v_WindowPageMinWidth )
	{
		v_WindowPage.style("width",  d3.event.x + "px");  
	}
})
.on("end" ,function()
{
	d3.select(this).style("width" ,"5px");
});



/**
 * 创建新的窗口
 * 
 * i_Data  App数据
 *
 * ZhengWei(HY) Add 2019-06-28
 */
function createWindowPage(i_Data)
{
	var v_Create = d3.select("#windowPage_" + i_Data.appID);
	
	/* 已存在，不重复创建 */
	if ( !v_Create.empty() )
	{
		windowPageToShow(v_Create);
		windowPageToTop(v_Create);
		return v_Create;
	}
	
	var v_BGColor = (i_Data.backgroundColor == null || i_Data.backgroundColor == "") ? "#4395FF" : i_Data.backgroundColor;
	
	v_Create = d3.select("#windowPageTemplate").clone(true);
	v_Create
	.attr("id" ,"windowPage_" + i_Data.appID)
	.attr("data-appID" ,i_Data.appID)
	.call(v_WindowPageDrag);
	
	$("#" + v_Create.attr("id") + " [data-toggle='tooltip']").tooltip();
	
	v_Create.select(".windowPageHead")
	.style("background" ,"olive linear-gradient(to right, " + v_BGColor + ", white 90%)")
	.on("click" ,function()
	{
		windowPageToShow(v_Create);
		windowPageToTop(v_Create);
	});
	
	/* 不允许拖动的组件 */
	v_Create.selectAll(".iconfont").call(d3.drag());
	
	/* 改变窗口大小的拖动组件 */
	v_Create.selectAll(".windowPageReSize_SE")
	.attr("data-appID" ,i_Data.appID)
	.call(v_WindowReSizeDrag_SE);
	
	v_Create.selectAll(".windowPageReSize_S")
	.attr("data-appID" ,i_Data.appID)
	.call(v_WindowReSizeDrag_S);
	
	v_Create.selectAll(".windowPageReSize_E")
	.attr("data-appID" ,i_Data.appID)
	.call(v_WindowReSizeDrag_E);
	
	
	/* 在新窗口中打开 */
	v_Create.select(".windowPageControlNew")
	.on("mouseover" ,function()
	{
		d3.select(this).classed("windowPageControlNewFocus" ,true);
	})
	.on("mouseout" ,function()
	{
		d3.select(this).classed("windowPageControlNewFocus" ,false);
	})
	.on("click" ,function()
	{
		window.open(i_Data.url); 
	});
	
	if ( isSameArea(i_Data.url) )
	{
		/* 返回。同域才能用，否则报没有权限 */
		v_Create.select(".windowPageControlBack")
		.style("display" ,"inline")
		.on("mouseover" ,function()
		{
			d3.select(this).classed("windowPageControlBackFocus" ,true);
		})
		.on("mouseout" ,function()
		{
			d3.select(this).classed("windowPageControlBackFocus" ,false);
		})
		.on("click" ,function()
		{
			var v_History = document.getElementById(v_Create.attr("id")).getElementsByTagName("iframe")[0].contentWindow.history;
			console.log(v_History);
			v_History.back();
		});
	}
	
	/* 重新加载窗口中的内容 */
	v_Create.select(".windowPageControlReload")
	.on("mouseover" ,function()
	{
		d3.select(this).classed("windowPageControlReloadFocus" ,true);
	})
	.on("mouseout" ,function()
	{
		d3.select(this).classed("windowPageControlReloadFocus" ,false);
	})
	.on("click" ,function()
	{
		windowPageReload(v_Create);
		windowPageToTop(v_Create);
	});


	/* 最小化Window窗口 */
	v_Create.select(".windowPageControlMin")
	.on("mouseover" ,function()
	{
		d3.select(this).classed("windowPageControlMinFocus" ,true);
	})
	.on("mouseout" ,function()
	{
		d3.select(this).classed("windowPageControlMinFocus" ,false);
	})
	.on("click" ,function()
	{
		windowPageToMin(v_Create);
	});


	/* 最大化Window窗口 */
	v_Create.select(".windowPageControlMax")
	.on("mouseover" ,function()
	{
		d3.select(this).classed("windowPageControlMaxFocus" ,true);
	})
	.on("mouseout" ,function()
	{
		d3.select(this).classed("windowPageControlMaxFocus" ,false);
	})
	.on("click" ,function()
	{
		windowPageToMax(v_Create);
		windowPageToTop(v_Create);
	});

	
	/* 正常化Window窗口 */
	v_Create.select(".windowPageControlNormal")
	.on("mouseover" ,function()
	{
		d3.select(this).classed("windowPageControlNormalFocus" ,true);
	})
	.on("mouseout" ,function()
	{
		d3.select(this).classed("windowPageControlNormalFocus" ,false);
	})
	.on("click" ,function()
	{
		windowPageToNormal(v_Create);
		windowPageToTop(v_Create);
	});


	/* 关闭Window窗口 */
	v_Create.select(".windowPageControlClose")
	.on("mouseover" ,function()
	{
		d3.select(this).classed("windowPageControlCloseFocus" ,true);
	})
	.on("mouseout" ,function()
	{
		d3.select(this).classed("windowPageControlCloseFocus" ,false);
	})
	.on("click" ,function()
	{
		$("#" + v_Create.attr("id") + " [data-toggle='tooltip']").tooltip('hide');
		v_Create.remove();
		removeWindowTask(i_Data);
	});


	/* 双击窗口标题最大化或正常化 */
	v_Create.select(".windowPageHead")
	.on("dblclick" ,function()
	{
		var v_X     = v_Create.style("left");
		var v_Y     = v_Create.style("top");
		var v_Width = v_Create.style("width");
		
		if ( v_X == "0px" && v_Y == "0px" && v_Width == "100%" )
		{
			windowPageToNormal(v_Create);
		}
		else
		{
			windowPageToMax(v_Create);
		}
		windowPageToTop(v_Create);
	})
	.on("click" ,function()
	{
		windowPageToTop(v_Create);
	});
	
	createWindowTask(v_Create ,i_Data);
	
	windowPageToMax(v_Create);
	windowPageToTop(v_Create);
	v_Create.select(".windowPageIcon").attr("src" ,(i_Data.icon != null && i_Data.icon != "") ? i_Data.icon : v_DefaultWindowIcon);
	v_Create.select(".windowPageTitle").html(i_Data.appName);
	v_Create.select("iframe").attr("src" ,i_Data.url);
	
	return v_Create;
}



/**
 * 删除窗口的任务栏
 * 
 * i_Data  App数据
 *
 * ZhengWei(HY) Add 2019-07-01
 */
function removeWindowTask(i_Data)
{
	d3.select("#windowTask_" + i_Data.appID).remove();
}



/**
 * 窗口置顶
 * 
 * i_WindowPage   d3创建的窗口对象
 *
 * ZhengWei(HY) Add 2019-06-28
 */
function windowPageToTop(i_WindowPage)
{
	d3.selectAll(".windowPage").style("z-index" ,100);
	i_WindowPage.style("z-index" ,101);
	
	windowTaskToTop(i_WindowPage.attr("data-appID"));
}



/**
 * 重新加载窗口中的内容
 * 
 * i_WindowPage   d3创建的窗口对象
 *
 * ZhengWei(HY) Add 2019-06-28
 */
function windowPageReload(i_WindowPage)
{
	i_WindowPage.select("iframe").attr("src" ,i_WindowPage.select("iframe").attr("src"));
}



/**
 * 全部Window显示
 * 
 * ZhengWei(HY) Add 2019-07-02
 */
function windowPageAllToShow()
{
	d3.selectAll(".windowPage").each(function()
	{
		var v_WindowPage = d3.select(this);
		
		if ( v_WindowPage.attr("id") != "windowPageTemplate" )
		{
			windowPageToShow(v_WindowPage);
		}
	});
}



/**
 * 全部Window最小化
 * 
 * ZhengWei(HY) Add 2019-07-02
 */
function windowPageAllToMin()
{
	d3.selectAll(".windowPage").each(function()
	{
		var v_WindowPage = d3.select(this);
		
		if ( v_WindowPage.attr("id") != "windowPageTemplate" )
		{
			windowPageToMin(v_WindowPage);
		}
	});
}



/**
 * 判定是否所有窗口都最小化了
 * 
 * ZhengWei(HY) Add 2019-07-02
 */
function windowPageIsAllMin()
{
	var v_IsAllMin = true;
	
	d3.selectAll(".windowPage").each(function()
	{
		var v_WindowPage = d3.select(this);
		if ( v_WindowPage.attr("id") != "windowPageTemplate" )
		{
			var v_IsMin = v_WindowPage.attr("data-isMin");
			if ( v_IsMin == null || v_IsMin == "" || v_IsMin != "Y" )
			{
				v_IsAllMin = false;
			}
		}
	});
	
	return v_IsAllMin;
}



/**
 * 最小Window窗口
 * 
 * i_WindowPage   d3创建的窗口对象
 *
 * ZhengWei(HY) Add 2019-06-28
 */
function windowPageToMin(i_WindowPage)
{
	var v_IsMin = i_WindowPage.attr("data-isMin");
	if ( v_IsMin == null || v_IsMin == "" || v_IsMin != "Y" )
	{
		i_WindowPage.attr("data-isMin"  ,"Y");
		i_WindowPage.attr("data-left"   ,i_WindowPage.style("left"));
		i_WindowPage.attr("data-top"    ,i_WindowPage.style("top"));
		i_WindowPage.attr("data-width"  ,i_WindowPage.style("width"));
		i_WindowPage.attr("data-height" ,i_WindowPage.style("height"));
		
		i_WindowPage.style("left" ,"-99999px");
		i_WindowPage.style("top"  ,"-99999px");
	}
}



/**
 * 显示Window窗口
 * 
 * i_WindowPage   d3创建的窗口对象
 *
 * ZhengWei(HY) Add 2019-07-01
 */
function windowPageToShow(i_WindowPage)
{
	if ( i_WindowPage.attr("data-isMin") == "Y" )
	{
		i_WindowPage.attr("data-isMin" ,"N");
		
		i_WindowPage.style("left" ,i_WindowPage.attr("data-left"));
		i_WindowPage.style("top"  ,i_WindowPage.attr("data-top"));
	}
}



/**
 * 最大化Window窗口
 * 
 * i_WindowPage   d3创建的窗口对象
 *
 * ZhengWei(HY) Add 2019-06-28
 */
function windowPageToMax(i_WindowPage)
{
	i_WindowPage.attr("data-left"   ,i_WindowPage.style("left"));
	i_WindowPage.attr("data-top"    ,i_WindowPage.style("top"));
	i_WindowPage.attr("data-width"  ,i_WindowPage.style("width"));
	i_WindowPage.attr("data-height" ,i_WindowPage.style("height"));
	
	i_WindowPage
	.style("left"    ,"0px")
	.style("top"     ,"0px")
	.style("width"   ,"100%")
	.style("height"  ,(document.body.clientHeight - 40) + "px")
	.style("display" ,"inline");
	
	i_WindowPage.select(".windowPageControlNormal").style("display" ,"inline");
	i_WindowPage.select(".windowPageControlMax")   .style("display" ,"none");
}



/**
 * 正常化Window窗口
 * 
 * i_WindowPage   d3创建的窗口对象
 *
 * ZhengWei(HY) Add 2019-06-28
 */
function windowPageToNormal(i_WindowPage)
{
	var v_Left   = i_WindowPage.attr("data-left");
	var v_Top    = i_WindowPage.attr("data-top");
	var v_Width  = i_WindowPage.attr("data-width");
	var v_Height = i_WindowPage.attr("data-height");
	
	if ( v_Width == "100%" && v_Height == (document.body.clientHeight - 40) + "px" )
	{
		if ( v_Left == "0px" && v_Top == "0px" )
		{
			v_Left = "15%";
			v_Top  = "15%";
		}
		v_Width  = "70%";
		v_Height = "70%";
	}
	else
	{
		if ( v_Left == null || v_Left == "" )
		{
			v_Left = "15%";
		}
		if ( v_Top == null || v_Top == "" )
		{
			v_Top = "15%";
		}
		if ( v_Width == null || v_Width == "" )
		{
			v_Width = "70%";
		}
		if ( v_Height == null || v_Height == "" )
		{
			v_Height = "70%";
		}
	}
	
	i_WindowPage
	.style("left"    ,v_Left)
	.style("top"     ,v_Top)
	.style("width"   ,v_Width)
	.style("height"  ,v_Height)
	.style("display" ,"inline");
	
	i_WindowPage.select(".windowPageControlNormal").style("display" ,"none");
	i_WindowPage.select(".windowPageControlMax")   .style("display" ,"inline");
}



/**
 * 判定是否为同域
 * 
 * i_Url   链接
 *
 * ZhengWei(HY) Add 2019-07-01
 */
function isSameArea(i_Url)
{
	if ( i_Url.toLowerCase().indexOf("https:") < 0 && i_Url.toLowerCase().indexOf("http:") < 0 )
	{
		return true;
	}
	else if ( i_Url.indexOf(window.location.protocol) >= 0 )
	{
		if ( i_Url.indexOf(window.location.host) >= 0 )
		{
			var v_Port = window.location.port;
			if ( v_Port == "" || v_Port == "80" )
			{
				var v_Url = i_Url + "/";
				if ( v_Url.indexOf(window.location.host + "/") >= 0 )
				{
					return true;
				}
				else if ( v_Url.indexOf(window.location.host + ":80/") >= 0 )
				{
					return true;
				}
			}
			else if ( i_Url.indexOf(window.location.host + ":" + v_Port) >= 0 )
			{
				return true;
			}
		}
	}
	
	return false;
}
