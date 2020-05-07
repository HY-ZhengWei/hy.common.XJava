var v_WindowRightBar               = d3.select("#windowRightBar");
var v_WindowRightBarOpenClose      = d3.select("#windowRightBarOpenClose");
var v_WindowRightChildBar          = d3.select("#windowRightChildBar");
var v_WindowRightChildBarOpenClose = d3.select("#windowRightChildBarOpenClose");
var v_WindowRightWidth             = 400;



/**
 * 关闭二级右侧边框小窗口  ZhengWei(HY) Add 2020-05-06
 */
function closeWindowRightChildBar()
{
    d3.select("#windowRightChildURL").attr("src" ,"#");
    d3.select("#windowRightChildBar")
    .style("opacity" ,"1")
    .transition().duration(500)
    .style("opacity" ,"0")
    .transition().delay(500)
    .style("width" ,"0px")
    .style("visibility" ,"hidden");
}



/**
 * 打开二级右侧边框小窗口  ZhengWei(HY) Add 2020-05-06
 */
function openWindowRightChildBar(d)
{
    d3.select("#windowRightChildBar").select(".windowRightChildTitle").html(d.message);
    d3.select("#windowRightChildBar")
    .style("width" ,v_WindowRightWidth + "px")
    .style("visibility" ,"visible")
    .style("opacity" ,"0")
    .transition().duration(400)
    .style("opacity" ,"1");
    
    d3.select("#windowRightChildURL").attr("src" ,d.url);
    v_WindowRightChildBarOpenClose.html("&gt;").attr("class" ,"windowRightBarToClose");
}



v_WindowRightChildBarOpenClose.on("click" ,function()
{
    v_WindowRightBarOpenClose.dispatch("click");
  
    if ( v_WindowRightChildBarOpenClose.html() == "&gt;" )
    {
        v_WindowRightChildBar.transition().duration(400).style("width" ,"0px");
        v_WindowRightChildBarOpenClose.html("&lt;").attr("class" ,"windowRightBarToOpen");
    }
    else
    {
        v_WindowRightChildBar.transition().duration(400).style("width" ,v_WindowRightWidth + "px");
        v_WindowRightChildBarOpenClose.html("&gt;").attr("class" ,"windowRightBarToClose");
    }
});



v_WindowRightBarOpenClose.on("click" ,function()
{
    if ( v_WindowRightBarOpenClose.html() == "&gt;" )
    {
        v_WindowRightBar.transition().duration(400).style("width" ,"0px");
        v_WindowRightBarOpenClose.html("&lt;").attr("class" ,"windowRightBarToOpen");
    }
    else
    {
        v_WindowRightBar.transition().duration(400).style("width" ,v_WindowRightWidth + "px");
        v_WindowRightBarOpenClose.html("&gt;").attr("class" ,"windowRightBarToClose");
    }
});



d3.select(".windowRightChildGoBack").on("click" ,function()
{
    closeWindowRightChildBar();
});
