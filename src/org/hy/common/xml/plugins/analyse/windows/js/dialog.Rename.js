/**
 * App图标重命名的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#renameBtn").on("click" ,function()
{
	var v_NewName = $('#renameText').val();
	if ( v_NewName == null || v_NewName == "" )
	{
		$('#renameText').popover('show');
		return;
	}
	
	$('#renameDialog').modal('hide');
	
	v_ContextData.appName = $.trim(v_NewName);
	var v_GText = v_ContextG.select("text");
	if ( !v_GText.empty() )
	{
		/* 图标文字 */
		v_GText
		.text(v_ContextData.appName)
		.attr("fill" ,v_ContextG.select("rect").attr("fill"))
		.transition()
		.duration(1500)
		.attr("fill" ,v_Colors.textColor);
	}
	
	commitWindowAppName(v_ContextData);
});



/**
 * App图标重命名的输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#renameText").on("keyup" ,function()
{
	var v_Text = $('#renameText').val();
	if ( v_Text == null || v_Text == "" )
	{
		$('#renameText').popover('show');
	}
	else
	{
		$('#renameText').popover('hide');
	}
});