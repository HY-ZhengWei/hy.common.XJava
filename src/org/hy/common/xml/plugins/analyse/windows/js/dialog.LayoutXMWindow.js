d3.select("#layoutXMWindowDialog").call(v_HtmlDrag);
d3.select("#layoutXMWindowDialog").selectAll("input") .call(d3.drag());
d3.select("#layoutXMWindowDialog").selectAll("button").call(d3.drag());

$('[data-toggle="popover"]').on('shown.bs.popover', function () {
    $('[role="tooltip"]').css("z-index","99999999");
});



/**
 * 显示自定义布局
 * 
 * i_Url  访问地址
 *
 * ZhengWei(HY) Add 2019-07-16
 */
function showLayoutXMWdinwoDialog()
{
	$('#layoutXMWindowRowText') .val("0");
	$('#layoutXMWindowColText').val("0");
	
	$('#layoutXMWindowDialog').modal('show');
}



/**
 * 自定义布局的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-07-16
 */
d3.select("#layoutXMWindowBtn").on("click" ,function()
{
	var v_Row = $('#layoutXMWindowRowText').val();
	var v_Col = $('#layoutXMWindowColText').val();
	
	if ( v_Row == null || v_Row == "" || isNaN(new Number(v_Row)) || new Number(v_Row) <= 0 )
	{
		$('#layoutXMWindowRowText').popover('show');
		return;
	}
	if ( v_Col == null || v_Col == "" || isNaN(new Number(v_Col)) || new Number(v_Col) <= 0 )
	{
		$('#layoutXMWindowColText').popover('show');
		return;
	}
	
	makeMWindowsConfig(v_MWindowsType ,parseInt(v_Row) ,parseInt(v_Col));
	redoMWindowsLayout();
	
	$('#layoutXMWindowDialog').modal('hide');
});