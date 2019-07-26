/**
 * 注册“预留空白”组件的事件
 * 
 * i_MWControl  控制层组件
 *
 * ZhengWei(HY) Add 2019-07-15
 */
function registerMWNullEvents(i_MWControl)
{
	i_MWControl
	.on("click" ,function()
	{
		v_ContentIDNull = d3.select(this).attr("id").replace("_Control" ,"");
		$('#addMWindowDialog').modal('show');
	});
}



/**
 * 注销“预留空白”组件的事件
 * 
 * i_MWControl  控制层组件
 *
 * ZhengWei(HY) Add 2019-07-15
 */
function removeMWNullEvents(i_MWControl)
{
	i_MWControl.on("click" ,null);
}



/**
 * 注册所有预留空白组件的点击事件。
 *
 * ZhengWei(HY) Add 2019-07-15
 */
function registerAllMWindowNullEvents()
{
	d3.selectAll(".MWindowNull")
	.each(function()
	{
		registerMWNullEvents(d3.select(this));
	});
}



/**
 * 添加新的内容组件，将预留空白组件转为内容组件。
 *
 * ZhengWei(HY) Add 2019-07-15
 */
d3.select("#addMWindowBtn")
.on("click" ,function()
{
	$('#addMWindowDialog').modal('hide');
	
	if ( v_ContentIDNull == null || v_ContentIDNull == "" )
	{
		return;
	}
	
	var v_MWNull        = d3.select("#" + v_ContentIDNull);
	var v_MWNullControl = d3.select("#" + v_ContentIDNull + "_Control");
	var v_SuperID       = v_MWNull.attr("data-super");
	var v_SuperLevel    = v_MWNull.attr("data-superLevel");
	var v_SuperLayout   = v_MWNull.attr("data-superLayout");
	var v_SuperContent  = $("#" + v_MWNull.attr("data-super")); 
	var v_SuperControl  = $("#" + v_MWNull.attr("data-super") + "_Control"); 
	var v_IDPrefix      = v_SuperID == "MWindowBody" ? "MW_" : v_SuperID + "_";
	var v_ID            = parseInt(v_MWNull.attr("data-index"));
	var v_Class         = v_MWNull.attr("data-class");
	
	removeMWNullEvents(v_MWNullControl);
	
	if ( v_Class == "MWindowContent" )
	{
		v_MWNull
		.style("background-color" ,null)
		.attr("class"             ,"MWindowComponent MWindowContent")
		.attr("data-previous"     ,v_IDPrefix + (v_ID - 1))
		.attr("data-next"         ,v_IDPrefix + (v_ID + 1));
		
		v_MWNullControl
		.style("background-color" ,null)
		.attr("class"             ,"MWindowComponent MWindowContent")
		.attr("data-previous"     ,v_IDPrefix + (v_ID - 1))
		.attr("data-next"         ,v_IDPrefix + (v_ID + 1));
		
		registerMWControlEvents(v_MWNullControl);
	}
	else
	{
		v_MWNull       .attr("class" ,"MWindowComponent " + v_Class).style("background-color" ,null).html("");
		v_MWNullControl.attr("class" ,"MWindowComponent " + v_Class).style("background-color" ,null).html("");
		
		var v_LayoutType     = v_Class == "MWindowRowBar" ? "ROW" : "COL";
		var v_ChildContentID = (1 + ((v_SuperLevel + 1) * 100));
		var v_ChildBySuperID = v_MWNull.attr("id");
		v_MWNull.append("div")
		.attr("id"               ,v_ChildBySuperID + "_" + v_ChildContentID)
		.attr("class"            ,"MWindowComponent MWindowContent")
		.attr("data-super"       ,v_ChildBySuperID)
		.attr("data-superLevel"  ,v_SuperLevel + 1)
		.attr("data-superLayout" ,v_LayoutType)
		.attr("data-previous"    ,v_ChildBySuperID)
		.attr("data-next"        ,v_ChildBySuperID + "_" + (v_ChildContentID + 1));
		
		var v_ChildMWControl = v_MWNullControl.append("div")
		.attr("id"               ,v_MWNull.attr("id") + "_" + v_ChildContentID + "_Control")
		.attr("class"            ,"MWindowComponent MWindowContent")
		.attr("data-super"       ,v_ChildBySuperID)
		.attr("data-superLevel"  ,v_SuperLevel + 1)
		.attr("data-superLayout" ,v_LayoutType)
		.attr("data-previous"    ,v_ChildBySuperID)
		.attr("data-next"        ,v_ChildBySuperID + "_" + (v_ChildContentID + 1));
		
		registerMWControlEvents(v_ChildMWControl);
		
		if ( "ROW" == v_LayoutType )
		{
			/* 添加子级的垂直分隔条 */
			$("#" + v_ChildBySuperID)             .append(makeMWindowVToHtml(v_ChildBySuperID + "_" + v_ChildContentID ,v_ChildBySuperID + "_" + (v_ChildContentID + 1)));
			$("#" + v_ChildBySuperID + "_Control").append(makeMWindowVToHtml(v_ChildBySuperID + "_" + v_ChildContentID ,v_ChildBySuperID + "_" + (v_ChildContentID + 1)));
		}
		else
		{
			/* 添加子级的垂直分隔条 */
			$("#" + v_ChildBySuperID)             .append(makeMWindowHToHtml(v_ChildBySuperID + "_" + v_ChildContentID ,v_ChildBySuperID + "_" + (v_ChildContentID + 1)));
			$("#" + v_ChildBySuperID + "_Control").append(makeMWindowHToHtml(v_ChildBySuperID + "_" + v_ChildContentID ,v_ChildBySuperID + "_" + (v_ChildContentID + 1)));
		}
		
		/* 添加子级的预留空白组件 */
		$("#" + v_ChildBySuperID)             .append(makeMWindowNull("MWindowContent" ,v_ChildContentID + 1 ,v_ChildBySuperID ,v_SuperLevel + 1 ,v_LayoutType));
		$("#" + v_ChildBySuperID + "_Control").append(makeMWindowNull("MWindowContent" ,v_ChildContentID + 1 ,v_ChildBySuperID ,v_SuperLevel + 1 ,v_LayoutType ,true));
	}
	
	
	/* 添加水平、垂直分隔条 */
	if ( "COL" == v_SuperLayout )
	{
		v_SuperContent.append(makeMWindowHToHtml(v_IDPrefix + v_ID ,v_IDPrefix + (v_ID + 1)));
		v_SuperControl.append(makeMWindowHToHtml(v_IDPrefix + v_ID ,v_IDPrefix + (v_ID + 1)));
	}
	else
	{
		v_SuperContent.append(makeMWindowVToHtml(v_IDPrefix + v_ID ,v_IDPrefix + (v_ID + 1)));
		v_SuperControl.append(makeMWindowVToHtml(v_IDPrefix + v_ID ,v_IDPrefix + (v_ID + 1)));
	}
	
	/* 添加预留空白组件 */
	v_SuperContent.append(makeMWindowNull(v_Class ,v_ID + 1 ,v_SuperID ,v_SuperLevel ,v_SuperLayout));
	v_SuperControl.append(makeMWindowNull(v_Class ,v_ID + 1 ,v_SuperID ,v_SuperLevel ,v_SuperLayout ,true));
	
	
	registerMWindowHEvents();
	registerMWindowVEvents();
	registerAllMWindowNullEvents();
	calcAllComponentSize();
	showAllContentSizeInfo();
});



/**
 * 生成空白组件的Html代码
 * 
 * @param i_Class       空白组件的布局CSS样式名称
 * @param i_ID          空白组件的ID
 * @param i_SuperID     父组件的ID
 * @param i_SuperLevel  父组件的层级
 * @param i_SuperLayout 父组件的布局类型
 * @param i_IsControl   是否为控制层的组件
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-13
 */
function makeMWindowNull(i_Class ,i_ID ,i_SuperID ,i_SuperLevel ,i_SuperLayout ,i_IsControl)
{
	var v_IDPrefix = i_SuperID == "MWindowBody" ? "MW_" : i_SuperID + "_";
	
	return "<div class='MWindowNull" 
	     + "' id='"               + v_IDPrefix + i_ID + (i_IsControl ? "_Control" : "")
	     + "' data-index='"       + i_ID 
	     + "' data-class='"       + i_Class 
	     + "' data-super='"       + i_SuperID 
	     + "' data-superLevel='"  + i_SuperLevel 
	     + "' data-superLayout='" + i_SuperLayout
	     + "'></div>";
}