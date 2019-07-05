/**
 * 显示新建App的对话窗口
 *
 * ZhengWei(HY) Add 2019-06-21
 */
function showNewAppDialog()
{
	d3.select("#newAppName").attr("data-x" ,d3.event.x).attr("data-y" ,d3.event.y);
	$('#newAppName')   .val("");
	$('#newActionType').val("open");
	$('#newAppUrl')    .val("http://");
	$('#newAppConfirm').val("");
	d3.select("#newAppIcon").attr("data-icon"   ,"");
	d3.select("#newAppIcon").attr("data-iconID" ,"");
	
	d3.select("#newBackgroundColor")
	.attr("data-color"        ,"#4395FF")
	.style("background-color" ,"#4395FF")
	.html("海军蓝");
	
	d3.select("#newSizeType")
	.attr("data-sizeType" ,"middle")
	.html("中");
	
	delete v_FileConfigs.initialPreviewAsData;
	delete v_FileConfigs.initialPreview;
	delete v_FileConfigs.initialPreviewConfig;
	$("#newAppIcon")
	.fileinput('clear')
	.fileinput('destroy')
	.fileinput(v_FileConfigs)
	.on("fileuploaded", function(e, data, previewiId, index) 
	{ 
		if ( data.response != null && data.response.value)
		{
			d3.select("#newAppIcon").attr("data-icon"   ,data.response.paramObj.icon);
			d3.select("#newAppIcon").attr("data-iconID" ,data.response.paramObj.iconID);
		}
		else
		{
			d3.select("#newAppIcon").attr("data-icon"   ,"");
			d3.select("#newAppIcon").attr("data-iconID" ,"");
		}
	})
	.on('filedeleted', function(event, key, jqXHR, data) 
	{
		d3.select("#newAppIcon").attr("data-icon"   ,"");
		d3.select("#newAppIcon").attr("data-iconID" ,"");
	})
	.on('fileremoved' ,function(event, id, index) 
	{
		d3.select("#newAppIcon").attr("data-icon"   ,"");
		d3.select("#newAppIcon").attr("data-iconID" ,"");
	})
	.on('filesuccessremove', function(event, id) 
	{
		d3.select("#newAppIcon").attr("data-icon"   ,"");
		d3.select("#newAppIcon").attr("data-iconID" ,"");
	});
	
	$('#newAppDialog').modal('show');
}



/**
 * 新建App图标的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#newAppBtn").on("click" ,function()
{
	var v_NewAppName    = $('#newAppName')   .val();
	var v_NewActionType = $('#newActionType').val();
	var v_NewAppUrl     = $('#newAppUrl')    .val();
	var v_IsError       = false;
	
	if ( v_NewAppName == null || v_NewAppName == "" )
	{
		$('#newAppName').popover('show');
		v_IsError = true;
	}
	if ( v_NewAppUrl == null || v_NewAppUrl == "" )
	{
		$('#newAppUrl').popover('show');
		v_IsError = true;
	}
	if ( v_IsError ) { return; }
	
	$('#newAppDialog').modal('hide');
	
	
	var v_NewData = {};
	v_NewData.appName         = v_NewAppName;
	v_NewData.actionType      = v_NewActionType;
	v_NewData.url             = v_NewAppUrl;
	v_NewData.confirm         = $('#newAppConfirm').val();
	v_NewData.icon            = d3.select('#newAppIcon')        .attr("data-icon");
	v_NewData.iconID          = d3.select('#newAppIcon')        .attr("data-iconID");
	v_NewData.backgroundColor = d3.select("#newBackgroundColor").attr("data-color");
	v_NewData.sizeType        = d3.select("#newSizeType")       .attr("data-sizeType");
	v_NewData.x               = d3.select("#newAppName")        .attr("data-x");
	v_NewData.y               = d3.select("#newAppName")        .attr("data-y");
	
	v_Apps.push(v_NewData);
	var v_MyG = createApp(v_NewData ,null);
	v_MyG
	.attr("opacity" ,0)
	.transition()
	.duration(2000)
	.attr("opacity" ,1);
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#newAppName").on("keyup" ,function()
{
	var v_Text = $('#newAppName').val();
	if ( v_Text == null || v_Text == "" )
	{
		$('#newAppName').popover('show');
	}
	else
	{
		$('#newAppName').popover('hide');
	}
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#newAppUrl").on("keyup" ,function()
{
	var v_Text = $('#newAppUrl').val();
	if ( v_Text == null || v_Text == "" )
	{
		$('#newAppUrl').popover('show');
	}
	else
	{
		$('#newAppUrl').popover('hide');
	}
});



/**
 * 访问方式的改变事件
 *
 * ZhengWei(HY) Add 2019-06-21
 */
$("#newActionType").change(function()
{
	var v_Value = $('#newActionType').val();
	if ( "javaScript" == v_Value )
	{
		if  ( $('#newAppUrl').val() == "" )
		{
			$('#newAppUrl').val("javascript:");
		}
	}
	else
	{
		if  ( $('#newAppUrl').val() == "" )
		{
			$('#newAppUrl').val("http://");
		}
	}
});



/**
 * App背景颜色的选择改变事件、鼠标移动事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.selectAll(".newBackgroundColorItem")
.on("click" ,function()
{
	var v_NewBGColor = d3.select(this).attr("data-color");
	d3.select("#newBackgroundColor")
	.attr("data-color"        ,v_NewBGColor)
	.style("background-color" ,v_NewBGColor)
	.html(d3.select(this).html());
})
.on("mouseover" ,function()
{
	try
	{
		var v_My    = d3.select(this);
		var v_FSize = Number(v_My.style("font-size").replace("px" ,""));
		
		v_My
		.style("font-size" ,(v_FSize + 3) + "px")
		.style("font-weight" ,"bold");
	}
	catch (error)
	{
		/* Nothing. */
	}
})
.on("mouseout" ,function()
{
	try
	{
		var v_My    = d3.select(this);
		var v_FSize = Number(v_My.style("font-size").replace("px" ,""));
		
		v_My
		.style("font-size" ,(v_FSize - 3) + "px")
		.style("font-weight" ,"normal");
	}
	catch (error)
	{
		/* Nothing. */
	}
});



/**
 * App图标大小的选择改变事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.selectAll(".newSizeTypeItem").on("click" ,function()
{
	var v_NewBGColor = d3.select(this).attr("data-sizeType");
	d3.select("#newSizeType")
	.attr("data-sizeType" ,v_NewBGColor)
	.html(d3.select(this).html());
});
