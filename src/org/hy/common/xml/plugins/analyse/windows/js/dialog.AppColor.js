/**
 * 显示颜色选择器
 *
 * i_Color     初始化颜色
 * i_X         X坐标
 * i_Y         Y坐标
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function showColorPicker(i_Color ,i_X ,i_Y ,i_OKFun)
{
	$("#colorPicker").colpick({
	    flat: true,
	    layout: 'full',
	    colorScheme: 'light',
	    submit: true,
	    onSubmit: function(hsb,hex,rgb,el,bySetColor)
	    {
	    	hideColorPicker();
	    	i_OKFun('#' + hex);
	    },
		onChange: function (hsb,hex,rgb,el,bySetColor) 
		{
			/* Nothing. */
        }
	});
	
	$("#colorPicker").colpickSetColor(i_Color ,true);
	$("#colorPicker").css("left" ,(i_X + v_Sizes.min.width) + "px");
	$("#colorPicker").css("top"  ,(i_Y - 20) + "px");
	$("#colorPicker").css("opacity" ,100); 
}



/**
 * 隐藏颜色选择器
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-11-17
 * @version     v1.0
 */
function hideColorPicker()
{
	$("#colorPicker").css("opacity" ,0);
	$("#colorPicker").css("left" ,"-99999px");
	$("#colorPicker").css("top"  ,"-99999px");
}