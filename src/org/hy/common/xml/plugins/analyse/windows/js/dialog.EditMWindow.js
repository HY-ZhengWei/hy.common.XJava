d3.select("#editMWindowDialog").call(v_HtmlDrag);
d3.select("#editMWindowDialog").selectAll("input") .call(d3.drag());
d3.select("#editMWindowDialog").selectAll("button").call(d3.drag());

$('[data-toggle="popover"]').on('shown.bs.popover', function () {
    $('[role="tooltip"]').css("z-index","99999999");
});

$('[data-toggle="tooltip"]').on('shown.bs.tooltip', function () {
    $('[role="tooltip"]').css("z-index","99999999");
});

$("#editMWindowDialog [data-toggle='tooltip']").tooltip();



/**
 * 显示编辑多屏同屏的Url访问地址 
 * 
 * i_Url  访问地址
 *
 * ZhengWei(HY) Add 2019-07-11
 */
function showEditMWindowDialog()
{
	var v_EditMW     = d3.select("#" + v_ContentIDClick);
	var v_Width      = v_EditMW.style("width");
	var v_Height     = v_EditMW.style("height");
	var v_LockWidth  = v_EditMW.attr("data-lockWidth");
	var v_LockHeight = v_EditMW.attr("data-lockHeight");
	
	$('#editMWindowUrlText')   .val(v_EditMW.attr("data-url"));
	$('#editMWindowWidthText') .val(v_Width);
	$('#editMWindowHeightText').val(v_Height);
	$('#editLockWidth') .prop("checked" ,(v_LockWidth  != null && v_LockWidth  == "1"));
	$('#editLockHeight').prop("checked" ,(v_LockHeight != null && v_LockHeight == "1"));
	
	$('#editMWindowDialog').modal('show');
}



/**
 * 编辑多屏同屏的Url访问地址的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-07-11
 */
d3.select("#editMWindowBtn").on("click" ,function()
{
	var v_NewUrl = $('#editMWindowUrlText').val();
	if ( v_NewUrl == null || v_NewUrl == "" )
	{
		v_NewUrl = "";
	}
	
	var v_ClientWidth  = document.body.clientWidth;
	var v_ClientHeight = document.body.clientHeight;
	var v_MWContent    = d3.select("#" + v_ContentIDClick);
	var v_MWControl    = d3.select("#" + v_ContentIDClick + "_Control");
	var v_Width        = $('#editMWindowWidthText') .val().replace("px" ,"");
	var v_Height       = $('#editMWindowHeightText').val().replace("px" ,"");
	var v_Diff         = 0;
	var v_New          = 0;
	
	if ( v_Width == "" || isNaN(new Number(v_Width.replace("%" ,""))) )
	{
		$('#editMWindowWidthText').popover('show');
		return;
	}
	if ( v_Height == "" || isNaN(new Number(v_Height.replace("%" ,""))) )
	{
		$('#editMWindowHeightText').popover('show');
		return;
	}
	
	if ( v_Width.indexOf("%") >= 0 )
	{
		v_Width  = v_ClientWidth  * parseFloat(v_Width.replace("%" ,"")) / 100;
	}
	
	if ( v_Height.indexOf("%") >= 0 )
	{
		v_Height = v_ClientHeight * parseFloat(v_Height.replace("%" ,"")) / 100;
	}
	
	/* 自己改变时，也计算影响的组件大小 */
	var v_MWContentRef = d3.select("#" + v_MWContent.attr("data-next"));
	if ( v_MWContentRef.empty() )
	{
		v_MWContentRef = d3.select("#" + v_MWContent.attr("data-previous"));
	}
	var v_MWControlRef    = d3.select("#" + v_MWContentRef.attr("id") + "_Control");
	var v_ClassRef        = v_MWContentRef.attr("class");
	var v_SuperLayoutType = v_MWContent   .attr("data-superLayout");
	var v_SuperLevel      = v_MWContent   .attr("data-superLevel");
	var v_SuperLevelRef   = v_MWContentRef.attr("data-superLevel");
	var v_SuperMWContent  = $("#" + v_MWContent.attr("data-super"));
	var v_SuperMWControl  = $("#" + v_MWContent.attr("data-super") + "_Control");
	
	if ( "ROW" == v_SuperLayoutType )
	{
		/* 横向布局的同级改变 */
		if ( v_SuperLevel == v_SuperLevelRef )
		{
			v_Diff = parseFloat(v_MWContent   .style("width").replace("px" ,"")) - v_Width;
			v_New  = parseFloat(v_MWContentRef.style("width").replace("px" ,"")) + v_Diff;
			
			v_MWContent   .style("width" ,v_Width + "px");
			v_MWControl   .style("width" ,v_Width + "px");
			
			v_MWContentRef.style("width" ,v_New + "px");
			v_MWControlRef.style("width" ,v_New + "px");
			
			if ( v_ClassRef.indexOf("MWindowRowBar") || v_ClassRef.indexOf("MWindowColBar") )
			{
				calcAllChildComponentSize(v_New ,parseFloat(v_MWContentRef.style("height").replace("px" ,"")) ,$("#" + v_MWContentRef.attr("id")));
				calcAllChildComponentSize(v_New ,parseFloat(v_MWContentRef.style("height").replace("px" ,"")) ,$("#" + v_MWControlRef.attr("id")));
			}
		}
		else
		{
			v_MWContent.style("width" ,v_Width + "px");
			v_MWControl.style("width" ,v_Width + "px");
		}
		
		v_SuperMWContent.css("height" ,v_Height + "px");
		v_SuperMWControl.css("height" ,v_Height + "px");
		
		/* 
		 * 1. 影响同级相邻组件的宽度，但不影响跨级相邻组件的宽度；
		 * 2. 影响同级所有组件的高度，但不影响跨级相邻组件的高度;
		 */
		calcAllChildComponentSize(parseFloat(v_SuperMWContent.css("width").replace("px" ,"")) ,v_Height ,v_SuperMWContent);
		calcAllChildComponentSize(parseFloat(v_SuperMWContent.css("width").replace("px" ,"")) ,v_Height ,v_SuperMWControl);
	}
	else
	{
		/* 纵向布局的同级改变 */
		if ( v_SuperLevel == v_SuperLevelRef )
		{
			v_Diff = parseFloat(v_MWContent   .style("height").replace("px" ,"")) - v_Height;
			v_New  = parseFloat(v_MWContentRef.style("height").replace("px" ,"")) + v_Diff;
			
			v_MWContent   .style("height" ,v_Height + "px");
			v_MWControl   .style("height" ,v_Height + "px");
			
			v_MWContentRef.style("height" ,v_New + "px");
			v_MWControlRef.style("height" ,v_New + "px");
			
			if ( v_ClassRef.indexOf("MWindowRowBar") || v_ClassRef.indexOf("MWindowColBar") )
			{
				calcAllChildComponentSize(parseFloat(v_MWContentRef.style("width").replace("px" ,"")) ,v_New ,$("#" + v_MWContentRef.attr("id")));
				calcAllChildComponentSize(parseFloat(v_MWContentRef.style("width").replace("px" ,"")) ,v_New ,$("#" + v_MWControlRef.attr("id")));
			}
		}
		else
		{
			v_MWContent.style("height" ,v_Height + "px");
			v_MWControl.style("height" ,v_Height + "px");
		}
		
		v_SuperMWContent.css("width" ,v_Width + "px");
		v_SuperMWControl.css("width" ,v_Width + "px");
		
		/* 
		 * 1. 影响同级相邻组件的高度，但不影响跨级相邻组件的高度；
		 * 2. 影响同级所有组件的宽度，但不影响跨级相邻组件的宽度;
		 */
		calcAllChildComponentSize(v_Width ,parseFloat(v_SuperMWContent.css("height").replace("px" ,"")) ,v_SuperMWContent);
		calcAllChildComponentSize(v_Width ,parseFloat(v_SuperMWContent.css("height").replace("px" ,"")) ,v_SuperMWControl);
	}
	
	var v_IsLockWidth  = $("#editLockWidth") .prop('checked');
	var v_IsLockHeight = $("#editLockHeight").prop('checked');
	
	v_MWContent
	.attr("data-url"        ,v_NewUrl)
	.attr("data-lockWidth"  ,v_IsLockWidth  ? "1" : "0")
	.attr("data-lockHeight" ,v_IsLockHeight ? "1" : "0");
	
	v_MWControl
	.attr("data-url"        ,v_NewUrl)
	.attr("data-lockWidth"  ,v_IsLockWidth  ? "1" : "0")
	.attr("data-lockHeight" ,v_IsLockHeight ? "1" : "0");
	
	if ( "ROW" == v_SuperLayoutType )
	{
		if ( v_IsLockHeight )
		{
			v_SuperMWContent.attr("data-lockHeight" ,"1");
			v_SuperMWControl.attr("data-lockHeight" ,"1");
		}
	}
	else
	{
		if ( v_IsLockWidth )
		{
			v_SuperMWContent.attr("data-lockWidth" ,"1");
			v_SuperMWControl.attr("data-lockWidth" ,"1");
		}
	}
	
	loadMWindowContent(v_MWContent);
	$('#editMWindowDialog').modal('hide');
});