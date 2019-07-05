/** Html元素的拖动事件 */
var v_HtmlX    = 0;
var v_HtmlY    = 0;
var v_HtmlDrag = d3.drag()
.on("start" ,function()
{ 
	v_HtmlX = d3.select(this).style("left").replace('px' ,'');
	v_HtmlY = d3.select(this).style("top") .replace('px' ,'');
	
	/* 处理百分比的情况 */
	if ( v_HtmlX.indexOf("%") > 0 )
	{
		v_HtmlX = document.body.clientWidth * v_HtmlX.replace('%' ,'') / 100;
	}
	
	/* 处理百分比的情况 */
	if ( v_HtmlY.indexOf("%") > 0 )
	{
		v_HtmlY = document.body.clientHeight * v_HtmlY.replace('%' ,'') / 100;
	}
	
	v_HtmlX = d3.event.x - v_HtmlX;
	v_HtmlY = d3.event.y - v_HtmlY;
})
.on("drag", function()
{
	d3.select(this)
	.style("left", (d3.event.x - v_HtmlX) + "px")
	.style("top" , (d3.event.y - v_HtmlY) + "px");                    
});





/**
 * 获取G元素的XY坐标位置
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-10-23
 * @version     v1.0
 */
function getGXY(i_G)
{
	var v_Ret = i_G.attr("transform").replace("translate(" ,"").replace(")" ,"");
	
	if ( v_Ret.indexOf(",") >= 0 )
	{
		return v_Ret.split(",");
	}
	else if ( v_Ret.indexOf(" ") >= 0 )
	{
		/* IE浏览器有用空格分割位置XY的情况 */
		return v_Ret.split(" ");
	}
	else
	{
		return (v_Ret + ",0").split(",");
	}
}