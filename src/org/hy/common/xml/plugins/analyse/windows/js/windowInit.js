$.ajaxSetup({
	 timeout: 30000
	});





var v_SVG = d3.select("body").select("svg")
.on("click" ,function()
{
	if ( v_ContextMenu )
	{
		hideAppMenu();
		hideAppDesktopMenu();
		hideColorPicker();
	}
	
	hideWindowStartMenus(1);
})
.on("contextmenu" ,function()
{
	if ( !v_ContextMenu )
	{
		d3.select("#recovery-LI").classed("ui-state-disabled" ,(v_Recoverys.length <= 0));
		d3.select("#recoveryCount").html(v_Recoverys.length <= 0 ? "" : " (" + v_Recoverys.length + ")");
		
		$("#appDesktopMenuBar").css("left" ,(d3.event.pageX) + "px");  
		$("#appDesktopMenuBar").css("top"  ,(d3.event.pageY) + "px");
		$("#appDesktopMenuBar").css("opacity" ,100);
		
		v_ContextMenu = true;
	}
	
	hideColorPicker();
	hideWindowStartMenus(1);
});



/* 水平、垂直标线 */
/*
v_SVG.selectAll(".HLine").data(d3.range(1 ,100)).enter()
.append("path")
.attr("stroke" ,"black")
.attr("stroke-width" ,"1")
.attr("d" ,function(d ,i)
{
	return "M0," + (i * (v_Sizes.min.height + v_SpaceY) + v_SpaceY) + " H 10000";
});

v_SVG.selectAll(".VLine").data(d3.range(1 ,100)).enter()
.append("path")
.attr("stroke" ,"black")
.attr("stroke-width" ,"1")
.attr("d" ,function(d ,i)
{
	return "M" + (i * (v_Sizes.min.width + v_SpaceX) + v_SpaceX) + ",0 V 10000";
});
*/



/**
 * 显示消息
 *
 * i_Message   消息的内容
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function showMessage(i_Message)
{
	$('#messageTextDiv').html(i_Message);
	$('.toast').toast('show');
}



function bodyPageInit()
{
	$("#appMenu")    .menu();
	$("#desktopMenu").menu();
	
	/* 不设置一下话，可能会出现首次点击无效的问题 */
	$("#newBackgroundColor") .dropdown();
	$("#newSizeType")        .dropdown();
	$("#editBackgroundColor").dropdown();
	$("#editSizeType")       .dropdown();
}