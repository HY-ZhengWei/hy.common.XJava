/**
 * 显示编辑App的对话窗口
 *
 * ZhengWei(HY) Add 2019-06-24
 */
function showEditAppDialog()
{
	if ( v_ContextG == null || v_ContextData == null )
	{
		return;
	}
	
	$('#editAppName')   .val(v_ContextData.appName);
	$('#editActionType').val((v_ContextData.actionType == null || v_ContextData.actionType == "") ? "open" : v_ContextData.actionType);
	$('#editAppUrl')    .val(v_ContextData.url);
	$('#editAppConfirm').val(v_ContextData.confirm);
	d3.select("#editAppIcon").attr("data-icon"   ,v_ContextData.icon);
	d3.select("#editAppIcon").attr("data-iconID" ,v_ContextData.iconID);
	
	var v_IsFindColor = false;
	d3.selectAll(".editBackgroundColorItem").each(function()
	{
		var v_My      = d3.select(this);
		var v_BGColor = v_My.attr("data-color");
		if ( v_BGColor == v_ContextData.backgroundColor )
		{
			d3.select("#editBackgroundColor")
			.attr("data-color"        ,v_ContextData.backgroundColor)
			.style("background-color" ,v_ContextData.backgroundColor)
			.html(v_My.html());
			
			v_IsFindColor = true;
		}
	});
	
	if ( !v_IsFindColor )
	{
		d3.select("#editBackgroundColor")
		.attr("data-color"        ,v_ContextData.backgroundColor)
		.style("background-color" ,v_ContextData.backgroundColor)
		.html("自选颜色");
	}
	
	d3.select("#editSizeType")
	.attr("data-sizeType" ,v_ContextData.sizeType)
	.html(v_Sizes[v_ContextData.sizeType].comment);
	
	
	if ( v_ContextData.icon != null && v_ContextData.icon != "" )
	{
		v_FileConfigs.initialPreviewAsData = true;
		v_FileConfigs.initialPreview       = [v_ContextData.icon];
		v_FileConfigs.initialPreviewConfig = [{
												key: 1,
												width: v_Sizes.min.width
											  }];
	}
	else
	{
		delete v_FileConfigs.initialPreviewAsData;
		delete v_FileConfigs.initialPreview;
		delete v_FileConfigs.initialPreviewConfig;
	}
	
	$('#editAppIcon')
	.fileinput('clear')
	.fileinput('destroy')
	.fileinput(v_FileConfigs)
	.on("fileuploaded", function(e, data, previewiId, index) 
	{ 
		if ( data.response != null && data.response.value)
		{
			d3.select("#editAppIcon").attr("data-icon"   ,data.response.paramObj.icon);
			d3.select("#editAppIcon").attr("data-iconID" ,data.response.paramObj.iconID);
		}
		else
		{
			d3.select("#editAppIcon").attr("data-icon"   ,"");
			d3.select("#editAppIcon").attr("data-iconID" ,"");
		}
	})
	.on('filedeleted', function(event, key, jqXHR, data) 
	{
		d3.select("#editAppIcon").attr("data-icon"   ,"");
		d3.select("#editAppIcon").attr("data-iconID" ,"");
	})
	.on('fileremoved' ,function(event, id, index) 
	{
		d3.select("#editAppIcon").attr("data-icon"   ,"");
		d3.select("#editAppIcon").attr("data-iconID" ,"");
	})
	.on('filesuccessremove', function(event, id) 
	{
		d3.select("#editAppIcon").attr("data-icon"   ,"");
		d3.select("#editAppIcon").attr("data-iconID" ,"");
	});
	
	$('#editAppDialog').modal('show');
}


	

/**
 * 编辑App图标的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.select("#editAppBtn").on("click" ,function()
{
	var v_EditAppName    = $('#editAppName')   .val();
	var v_EditActionType = $('#editActionType').val();
	var v_EditAppUrl     = $('#editAppUrl')    .val();
	var v_IsError        = false;
	
	if ( v_EditAppName == null || v_EditAppName == "" )
	{
		$('#editAppName').popover('show');
		v_IsError = true;
	}
	if ( v_EditAppUrl == null || v_EditAppUrl == "" )
	{
		$('#editAppUrl').popover('show');
		v_IsError = true;
	}
	if ( v_IsError ) { return; }
	
	$('#editAppDialog').modal('hide');
	
	
	v_ContextData.userID          = v_UserID;
	v_ContextData.appName         = v_EditAppName;
	v_ContextData.actionType      = v_EditActionType;
	v_ContextData.url             = v_EditAppUrl;
	v_ContextData.confirm         = $('#editAppConfirm').val();
	v_ContextData.icon            = d3.select('#editAppIcon')        .attr("data-icon");
	v_ContextData.iconID          = d3.select('#editAppIcon')        .attr("data-iconID");
	v_ContextData.backgroundColor = d3.select("#editBackgroundColor").attr("data-color");
	v_ContextData.sizeType        = d3.select("#editSizeType")       .attr("data-sizeType");
	
	if ( v_ContextData.sizeType == null || v_ContextData.sizeType == "" )
	{
		v_ContextData.sizeType = "middle";
	}
	
	editApp(v_ContextData ,v_ContextG ,1000);
	
	commitWindowAppEdit(v_ContextData);
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.select("#editAppName").on("keyup" ,function()
{
	var v_Text = $('#editAppName').val();
	if ( v_Text == null || v_Text == "" )
	{
		$('#editAppName').popover('show');
	}
	else
	{
		$('#editAppName').popover('hide');
	}
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.select("#editAppUrl").on("keyup" ,function()
{
	var v_Text = $('#editAppUrl').val();
	if ( v_Text == null || v_Text == "" )
	{
		$('#editAppUrl').popover('show');
	}
	else
	{
		$('#editAppUrl').popover('hide');
	}
});



/**
 * 访问方式的改变事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
$("#editActionType").change(function()
{
	var v_Value = $('#editActionType').val();
	if ( "javaScript" == v_Value )
	{
		if ( $('#editAppUrl').val() == "" )
		{
			$('#editAppUrl').val("javascript:");
		}
	}
	else
	{
		if ( $('#editAppUrl').val() == "" )
		{
			$('#editAppUrl').val("http://");
		}
	}
});



/**
 * App背景颜色的选择改变事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.selectAll(".editBackgroundColorItem").on("click" ,function()
{
	var v_EditBGColor = d3.select(this).attr("data-color");
	d3.select("#editBackgroundColor")
	.attr("data-color"        ,v_EditBGColor)
	.style("background-color" ,v_EditBGColor)
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
d3.selectAll(".editSizeTypeItem").on("click" ,function()
{
	var v_EditBGColor = d3.select(this).attr("data-sizeType");
	d3.select("#editSizeType")
	.attr("data-sizeType" ,v_EditBGColor)
	.html(d3.select(this).html());
});



$("#editAppIcon").change(function()
{
	$("#editAppIconTitle").html($("#editAppIcon").val());
});
