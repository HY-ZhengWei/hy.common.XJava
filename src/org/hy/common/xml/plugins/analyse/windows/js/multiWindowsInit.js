/**
 * 按配置数据生成所有布局组件
 * 
 * ZhengWei(HY) Add 2019-07-16
 */
function initMWindowsConfig()
{
	if ( v_MWindowsConfig == null || v_MWindowsConfig.length <= 0 )
	{
		makeMWindowsConfig(v_MWindowsType ,1 ,2);
	}
	
	var v_Body = d3.select("body");
	v_Body.select("#MWindowBody")        .remove();
	v_Body.select("#MWindowBody_Control").remove();
	
	var v_MWBody = v_Body.append("div")
	.attr("id"    ,"MWindowBody")
	.attr("class" ,"MWindowComponent " + (("COL" == v_MWindowsType) ? "MWindowColBar" : "MWindowRowBar"));
	
	initMWindowsConfigByChilds(v_MWindowsType ,v_MWBody ,v_MWindowsConfig);
}



/**
 * 按配置数据生成布局组件
 * 
 * @param i_SuperLayout     父组件的布局类型（COL:等高类型、ROW:等宽类型）
 * @param i_SuperComponent  父组件
 * @param i_MWConfig        组件配置数据
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-16
 */
function initMWindowsConfigByChilds(i_SuperLayout ,i_SuperComponent ,i_MWConfig)
{
	var v_LayoutType = ("COL" == i_SuperLayout) ? "ROW" : "COL";
	var v_Classed    = ("COL" == v_LayoutType)  ? "MWindowColBar" : "MWindowRowBar";
	
	for (var i=0; i<i_MWConfig.length; i++)
	{
		var v_MWConfig    = i_MWConfig[i];
		var v_MWComponent = i_SuperComponent.append("div")
		.attr("id"              ,v_MWConfig.id)
		.attr("class"           ,"MWindowComponent " + ("MWindowContent" == v_MWConfig.classed ? v_MWConfig.classed : v_Classed))
		.style("width"          ,v_MWConfig.width)
		.style("height"         ,v_MWConfig.height)
		.attr("data-lockWidth"  ,v_MWConfig.lockWidth)
		.attr("data-lockHeight" ,v_MWConfig.lockHeight);
		
		if ( "MWindowContent" == v_MWConfig.classed )
		{
			v_MWComponent.attr("data-url" ,v_MWConfig.url)
		}
		else if ( v_MWConfig.childs != null && v_MWConfig.childs.length >= 1 )
		{
			initMWindowsConfigByChilds(v_LayoutType ,v_MWComponent ,v_MWConfig.childs);
		}
	}
}



/**
 * 获取所有配置数据
 * 
 * ZhengWei(HY) Add 2019-07-16
 */
function getMWindowsConfig()
{
	var v_Temp_MWindowsConfig = [];
	
	getMWindowsConfigByChilds($("#MWindowBody") ,v_Temp_MWindowsConfig);
	
	return v_Temp_MWindowsConfig;
}



/**
 * 获取子节点的配置数据
 * 
 * i_SuperComponent   父组件
 * io_SuperMWConfig   父组件的配置数据
 * 
 * ZhengWei(HY) Add 2019-07-16
 */
function getMWindowsConfigByChilds(i_SuperComponent ,io_SuperMWConfig)
{
	i_SuperComponent.children(".MWindowComponent").each(function()
	{
		var v_MWComponent = $(this);
		var v_Class       = $(this).attr("class");
		if ( v_Class.indexOf("MWindowH") < 0 && v_Class.indexOf("MWindowV") < 0 )
		{
			var v_Classed = "MWindowContent";
			
			if ( v_Class.indexOf("MWindowColBar") >= 0 )
			{
				v_Classed = "MWindowColBar";
			}
			else if ( v_Class.indexOf("MWindowRowBar") >= 0 )
			{
				v_Classed = "MWindowRowBar";
			}
			
			var v_MWConfig = {
					 id:         v_MWComponent.attr("id")
					,classed:    v_Classed
					,width:      v_MWComponent.css("width")
					,height:     v_MWComponent.css("height")
					,lockWidth:  v_MWComponent.attr("data-lockWidth")
					,lockHeight: v_MWComponent.attr("data-lockHeight")
					,openTimer:  v_MWComponent.attr("data-openTimer")
					,timer:      v_MWComponent.attr("data-timer")
			}
			
			if ( v_Classed != "MWindowContent" )
			{
				v_MWConfig.childs = [];
				getMWindowsConfigByChilds(v_MWComponent ,v_MWConfig.childs);
			}
			else
			{
				v_MWConfig.url = v_MWComponent.attr("data-url");
			}
			
			io_SuperMWConfig.push(v_MWConfig);
		}
	});
}



/**
 * 生成几行几列的默认布局配置数据
 * 
 * @param i_MWindowsType  多屏窗口的整体类型（COL:等高类型、ROW:等宽类型）
 * @param i_RowCount      几行
 * @param i_ColCount      几列
 * @returns
 * 
 * ZhengWei(HY) Add 2019-07-16
 */
function makeMWindowsConfig(i_MWindowsType ,i_RowCount ,i_ColCount)
{
	if ( i_RowCount >= 1 && i_ColCount >= 1 )
	{
		v_MWindowsConfig = [];
		var v_MWClass  = ("COL" == i_MWindowsType) ? "MWindowRowBar" : "MWindowColBar";
		var v_ColCount = ("COL" == i_MWindowsType) ? i_ColCount : i_RowCount;
		var v_RowCount = ("COL" == i_MWindowsType) ? i_RowCount : i_ColCount;
		
		for (var v_ColIndex=0; v_ColIndex<v_ColCount; v_ColIndex++)
		{
			v_MWindowsConfig[v_ColIndex] = {classed: v_MWClass ,width: "0px" ,height: "0px" ,lockWidth: "0" ,lockHeight: "0" ,childs: []};
			
			for (var v_RowlIndex=0; v_RowlIndex<v_RowCount; v_RowlIndex++)
			{
				v_MWindowsConfig[v_ColIndex].childs[v_RowlIndex] = {classed: "MWindowContent" ,width: "0px" ,height: "0px" ,lockWidth: "0" ,lockHeight: "0" ,url:""};
			}
		}
	}
}



if ( "COL" == v_MWindowsType)
{
	d3.select("#layoutTypeBtn").html("翻转布局-等高");
}
else
{
	d3.select("#layoutTypeBtn").html("翻转布局-等宽");
	v_MWindowsType = "ROW";
}
initMWindowsConfig();