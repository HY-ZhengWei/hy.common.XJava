/**
 * 删除App图标的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#delAppBtn").on("click" ,function()
{
	$('#delAppDialog').modal('hide');
	
	v_ContextData.del = 1;
	v_Recoverys.push(v_ContextData.appIndex);
	
	v_ContextG
	.attr("opacity" ,1)
	.transition()
	.duration(1200)
	.attr("opacity" ,0)
	.remove();
	
	commitWindowAppDel(v_ContextData);
});