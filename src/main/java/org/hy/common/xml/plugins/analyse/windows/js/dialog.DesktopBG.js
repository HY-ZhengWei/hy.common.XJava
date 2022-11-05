var v_OldDesktopImg        = null;
var v_OldDesktopImgOpacity = null;



/**
 * 显示桌面背景的对话窗口
 * 
 * i_MWTID  多屏同显的惟一ID
 *
 * ZhengWei(HY) Add 2019-09-06
 */
function showDesktopBGDialog()
{
	v_OldDesktopImg        = $('#desktopBGBar').css('opacity');
	v_OldDesktopImgOpacity = $('#desktopBGBar').css('background-image');
	
	$('#desktopImgOpacity').slider("value" ,v_OldDesktopImg * 100);
	$('#desktopImgOpacity-handle').text(v_OldDesktopImg * 100);
	
	delete v_FileConfigs.initialPreviewAsData;
	delete v_FileConfigs.initialPreview;
	delete v_FileConfigs.initialPreviewConfig;
	$("#desktopImg")
	.fileinput('clear')
	.fileinput('destroy')
	.fileinput(v_FileConfigs)
	.on("fileuploaded", function(e, data, previewiId, index) 
	{ 
		if ( data.response != null && data.response.value)
		{
			d3.select("#desktopImg").attr("data-icon"   ,data.response.paramObj.icon);
			d3.select("#desktopImg").attr("data-iconID" ,data.response.paramObj.iconID);
			
			$('#desktopBGBar').css('background-image' ,'url("' + data.response.paramObj.icon + '")');
		}
		else
		{
			d3.select("#desktopImg").attr("data-icon"   ,"");
			d3.select("#desktopImg").attr("data-iconID" ,"");
		}
	})
	.on('filedeleted', function(event, key, jqXHR, data) 
	{
		d3.select("#desktopImg").attr("data-icon"   ,"");
		d3.select("#desktopImg").attr("data-iconID" ,"");
	})
	.on('fileremoved' ,function(event, id, index) 
	{
		d3.select("#desktopImg").attr("data-icon"   ,"");
		d3.select("#desktopImg").attr("data-iconID" ,"");
	})
	.on('filesuccessremove', function(event, id) 
	{
		d3.select("#desktopImg").attr("data-icon"   ,"");
		d3.select("#desktopImg").attr("data-iconID" ,"");
	});
	
	$('#desktopBGDialog').modal('show');
}



/**
 * 桌面背景的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-09-06
 */
d3.select("#desktopBGBtn").on("click" ,function()
{
	var v_Desktop = {};
	v_Desktop.desktopImgID      = d3.select('#desktopImg')  .attr('data-iconID');
	v_Desktop.desktopImgOpacity = d3.select('#desktopBGBar').style('opacity');
	
	commitDesktopBG(v_Desktop);
	
	$('#desktopBGDialog').modal('hide');
});



/**
 * 桌面背景的取消按钮的事件
 *
 * ZhengWei(HY) Add 2019-09-06
 */
d3.select("#desktopBGCancelBtn").on("click" ,function()
{
	$('#desktopBGBar').css('opacity'          ,v_OldDesktopImg);
	$('#desktopBGBar').css('background-image' ,v_OldDesktopImgOpacity);
});



var v_DesktopImgOpacityHandle = $("#desktopImgOpacity-handle");
$("#desktopImgOpacity").slider(
{
	orientation: "horizontal",
    range: "min",
    max: 100,
    min: 0,
	create: function() 
	{
		v_DesktopImgOpacityHandle.text($(this).slider("value"));
	},
	slide: function( event, ui ) 
	{
		v_DesktopImgOpacityHandle.text(ui.value);
		$('#desktopBGBar').css("opacity" ,ui.value / 100);
	}
});
