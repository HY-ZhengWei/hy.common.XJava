var v_Timer       = null;
var v_SizeToBig   = 0;
var v_SizeToSmall = 100;



/**
 * 全屏显示的过程
 * 
 * ZhengWei(HY) Add 2019-08-22
 */
function fullShowing()
{
	v_SizeToBig += 10;
	
	d3.select("#fullMWindowDialog")
	.style("width"  ,v_SizeToBig + "%")
	.style("height" ,v_SizeToBig + "%");
	
	if ( v_SizeToBig >= 100 )
	{
		v_SizeToBig = 0;
		
		d3.select("#fullMWindowDialog").select("iframe").attr("src" ,d3.select("#" + v_ContentIDClick).attr("data-url"));
		d3.select(".fullMWindowDialogClose").style("display" ,"inline");
		v_Timer.stop();
		
		disallowGoBack();
	}
}



/**
 * 隐藏全屏的过程
 * 
 * ZhengWei(HY) Add 2019-08-22
 */
function fullHiding()
{
	v_SizeToSmall -= 10;
	
	d3.select("#fullMWindowDialog")
	.style("width"  ,v_SizeToSmall + "%")
	.style("height" ,v_SizeToSmall + "%");
	
	if ( v_SizeToSmall <= 0 )
	{
		d3.select("#fullMWindowDialog")
		.style("display" ,"none")
		.select("iframe").attr("src" ,"");
		
		v_SizeToSmall = 100;
		v_Timer.stop(); 
		
		enableGoBack();
	}
}



/**
 * 全屏显示
 * 
 * ZhengWei(HY) Add 2019-08-22
 */
function showFullMWindowDialog()
{
	d3.select("#fullMWindowDialog")
	.style("display" ,"inline")
	.style("width" ,"0%")
	.style("height" ,"0%")
	.select("iframe").attr("src" ,d3.select("#" + v_ContentIDClick).attr("data-url"));
	
	v_Timer = d3.interval(fullShowing ,100);
}



/**
 * 隐藏全屏
 * 
 * ZhengWei(HY) Add 2019-08-22
 */
function hideFullMWindowDiaglog()
{
	d3.select(".fullMWindowDialogClose")
	.style("display" ,"none")
	.classed("fullMWindowDialogCloseSelected" ,false);
	
	v_Timer = d3.interval(fullHiding ,100);
}



/**
 * 隐藏全屏的点击事件及鼠标事件
 * 
 * ZhengWei(HY) Add 2019-08-22
 */
d3.select(".fullMWindowDialogClose")
.on("click" ,function()
{
	hideFullMWindowDiaglog();
})
.on("mouseover" ,function()
{
	d3.select(this).classed("fullMWindowDialogCloseSelected" ,true);
})
.on("mouseout" ,function()
{
	d3.select(this).classed("fullMWindowDialogCloseSelected" ,false);
});




/**
 * 启用浏览器的后退
 * 
 * ZhengWei(HY) Add 2019-08-23
 */
function enableGoBack() 
{
    window.removeEventListener('popstate' ,noGoBack);
}



/**
 * 停用后退
 * 
 * ZhengWei(HY) Add 2019-08-23
 */
function noGoBack() 
{
    history.pushState(null, null, document.URL);
}



/**
 * 禁用浏览器的后退
 * 
 * ZhengWei(HY) Add 2019-08-23
 */
function disallowGoBack() 
{
	noGoBack();
	window.addEventListener('popstate' ,noGoBack);
}





