var v_NewSystemBysCodes = "";    /* 所属系统的已选择系统编码 */



/**
 * 设置及更新所属系统的显示标题
 *
 * ZhengWei(HY) Add 2020-08-14
 */
function setNewSystemBysTitle()
{
    var v_NewTitle = "";
    var v_NewCodes = "";
    
    d3.select("#newSystemBysItemBar").selectAll(".newSystemBysItem").each(function()
    {
        var v_My = d3.select(this);
        
        if ( v_My.property("checked") )
        {
            if ( v_NewCodes != null && v_NewCodes != "" )
            {
                v_NewCodes += ",";
                v_NewTitle += " | ";
            }
            
            v_NewCodes += v_My.attr("data-code");
            v_NewTitle += v_My.attr("data-name");
        }
    });
    
    if ( v_NewCodes == "" )
    {
        v_NewTitle = "请选择";
    }
    
    v_NewSystemBysCodes = v_NewCodes;
    d3.select("#newSystemBysTitle").html(v_NewTitle);
}



/**
 * 设置所属系统的选择性
 *
 * ZhengWei(HY) Add 2020-08-14
 */
function setNewSystemBysCheckeds(i_Codes)
{
    if ( i_Codes == null || i_Codes == "" )
    {
        d3.select("#newSystemBysItemBar").selectAll(".newSystemBysItem").property("checked" ,false);
        return;
    }
  
    d3.select("#newSystemBysItemBar").selectAll(".newSystemBysItem").each(function()
    {
        var v_My = d3.select(this);
        
        v_My.property("checked" ,(i_Codes.indexOf(v_My.attr("data-code")) >= 0));
    });
}



/**
 * 初始化编辑App的对话窗口
 * 
   <div class="dropdown-item">
       <div class="form-check">
           <input class="form-check-input newSystemBysItem" type="checkbox" value="" id="defaultCheck1" data-name="计算系统">
           <label class="form-check-label" for="defaultCheck1">计算系统<\label>
       <\div>
   <\div>
 *
 * ZhengWei(HY) Add 2020-08-14
 */
function initNewAppDialog()
{
    if ( v_SystemDatas.length <= 0 )
    {
        d3.select("#newSystemBysBar").remove();
        return;
    }
    
    d3.select("#newSystemBysItemBar").selectAll(".dropdown-item").data(v_SystemDatas).enter()
    .append("div")
    .attr("class" ,"dropdown-item")
    .append("div")
    .attr("class" ,"form-check");
    
    d3.select("#newSystemBysItemBar").selectAll(".dropdown-item").data(v_SystemDatas).each(function(d ,i)
    {
        var v_My = d3.select(this);
        
        v_My.append("input")
        .attr("id"        ,"newSystemBysItem_" + d.code)
        .attr("class"     ,"form-check-input newSystemBysItem")
        .attr("type"      ,"checkbox")
        .attr("data-code" ,d.code)
        .attr("data-name" ,d.name)
        .on("change" ,function()
        {
            setNewSystemBysTitle();
        });
        
        v_My.append("label")
        .attr("class" ,"form-check-label")
        .attr("for"   ,"newSystemBysItem_" + d.code)
        .html(d.name);
    });    
}

initNewAppDialog();



/**
 * 显示新建App的对话窗口
 * 
 * i_MWTID  多屏同显的惟一ID
 *
 * ZhengWei(HY) Add 2019-06-21
 */
function showNewAppDialog(i_MWTID)
{
    d3.select("#newAppName").attr("data-x" ,d3.event.x).attr("data-y" ,d3.event.y);
    $('#newAppName')   .val("");
    $('#newActionType').val("open");
    if ( i_MWTID != null && i_MWTID != "" )
    {
        $('#newAppUrl').val("multiWindows.page?mwtid=" + i_MWTID);
    }
    else
    {
        $('#newAppUrl').val("http://");
    }
    $('#newAppDownloadUrl').val("");
    $('#newAppConfirm').val("");
    d3.select("#newAppIcon").attr("data-icon"   ,"");
    d3.select("#newAppIcon").attr("data-iconID" ,"");
    
    d3.select("#newBackgroundColor")
    .attr("data-color"        ,"#4395FF")
    .style("background-color" ,"#4395FF")
    .html("海军蓝");
    
    d3.select("#newSizeType")
    .attr("data-sizeType" ,"middle")
    .html(v_Sizes["middle"].comment);
    
    d3.select("#newNameToLMR")
    .attr("data-nameToLMR" ,"toLeft")
    .html(v_NameToLMR["toLeft"].comment);
    
    delete v_FileConfigs.initialPreviewAsData;
    delete v_FileConfigs.initialPreview;
    delete v_FileConfigs.initialPreviewConfig;
    $("#newAppIcon")
    .fileinput('clear')
    .fileinput('destroy')
    .fileinput(v_FileConfigs)
    .on("fileuploaded", function(e, data, previewiId, index) 
    { 
        if ( data.response != null && data.response.value)
        {
            d3.select("#newAppIcon").attr("data-icon"   ,data.response.paramObj.icon);
            d3.select("#newAppIcon").attr("data-iconID" ,data.response.paramObj.iconID);
        }
        else
        {
            d3.select("#newAppIcon").attr("data-icon"   ,"");
            d3.select("#newAppIcon").attr("data-iconID" ,"");
        }
    })
    .on('filedeleted', function(event, key, jqXHR, data) 
    {
        d3.select("#newAppIcon").attr("data-icon"   ,"");
        d3.select("#newAppIcon").attr("data-iconID" ,"");
    })
    .on('fileremoved' ,function(event, id, index) 
    {
        d3.select("#newAppIcon").attr("data-icon"   ,"");
        d3.select("#newAppIcon").attr("data-iconID" ,"");
    })
    .on('filesuccessremove', function(event, id) 
    {
        d3.select("#newAppIcon").attr("data-icon"   ,"");
        d3.select("#newAppIcon").attr("data-iconID" ,"");
    });
    
    setNewSystemBysCheckeds(null);
    setNewSystemBysTitle();
    
    $('#newAppDialog').modal('show');
}



/**
 * 新建App图标的确定按钮的事件
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#newAppBtn").on("click" ,function()
{
    var v_NewAppName    = $('#newAppName')   .val();
    var v_NewActionType = $('#newActionType').val();
    var v_NewAppUrl     = $('#newAppUrl')    .val();
    var v_IsError       = false;
    
    if ( v_NewAppName == null || v_NewAppName == "" )
    {
        $('#newAppName').popover('show');
        v_IsError = true;
    }
    if ( v_NewAppUrl == null || v_NewAppUrl == "" )
    {
        $('#newAppUrl').popover('show');
        v_IsError = true;
    }
    if ( v_IsError ) { return; }
    
    $('#newAppDialog').modal('hide');
    
    
    var v_NewData = {};
    v_NewData.userID          = v_UserID;
    v_NewData.appID           = "APP" + v_UserID + "_" + (new Date()).getTime();
    v_NewData.appName         = v_NewAppName;
    v_NewData.actionType      = v_NewActionType;
    v_NewData.url             = v_NewAppUrl;
    v_NewData.downloadUrl     = $('#newAppDownloadUrl').val();
    v_NewData.confirm         = $('#newAppConfirm').val();
    v_NewData.icon            = d3.select('#newAppIcon')        .attr("data-icon");
    v_NewData.iconID          = d3.select('#newAppIcon')        .attr("data-iconID");
    v_NewData.backgroundColor = d3.select("#newBackgroundColor").attr("data-color");
    v_NewData.sizeType        = d3.select("#newSizeType")       .attr("data-sizeType");
    v_NewData.nameToLMR       = d3.select("#newNameToLMR")      .attr("data-nameToLMR");
    v_NewData.x               = d3.select("#newAppName")        .attr("data-x");
    v_NewData.y               = d3.select("#newAppName")        .attr("data-y");
    v_NewData.systemBysCodes  = v_NewSystemBysCodes;
    
    if ( v_NewData.sizeType == null || v_NewData.sizeType == "" )
    {
        v_NewData.sizeType = "middle";
    }
    
    v_Apps.push(v_NewData);
    var v_MyG = createApp(v_NewData ,null);
    v_MyG
    .attr("opacity" ,0)
    .transition()
    .duration(2000)
    .attr("opacity" ,1);
    
    commitWindowAppCreate(v_NewData);
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#newAppName").on("keyup" ,function()
{
    var v_Text = $('#newAppName').val();
    if ( v_Text == null || v_Text == "" )
    {
        $('#newAppName').popover('show');
    }
    else
    {
        $('#newAppName').popover('h de');
    }
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#newAppUrl").on("keyup" ,function()
{
    var v_Text = $('#newAppUrl').val();
    if ( v_Text == null || v_Text == "" )
    {
        $('#newAppUrl').popover('show');
    }
    else
    {
        $('#newAppUrl').popover('hide');
    }
});



/**
 * 输入变化时的提示信息
 *
 * ZhengWei(HY) Add 2020-12-16
 */
d3.select("#newAppDownloadUrl").on("keyup" ,function()
{
    var v_Text = $('#newAppDownloadUrl').val();
    if ( v_Text == null || v_Text == "" )
    {
        $('#newAppDownloadUrl').popover('show');
    }
    else
    {
        $('#newAppDownloadUrl').popover('hide');
    }
});



/**
 * 访问方式的改变事件
 *
 * ZhengWei(HY) Add 2019-06-21
 */
$("#newActionType").change(function()
{
    var v_Value = $('#newActionType').val();
    if ( "javaScript" == v_Value )
    {
        if  ( $('#newAppUrl').val() == "" )
        {
            $('#newAppUrl').val("javascript:");
        }
    }
    else
    {
        if  ( $('#newAppUrl').val() == "" )
        {
            $('#newAppUrl').val("http://");
        }
    }
});



/**
 * App背景颜色的选择改变事件、鼠标移动事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.selectAll(".newBackgroundColorItem")
.on("click" ,function()
{
    var v_NewBGColor = d3.select(this).attr("data-color");
    d3.select("#newBackgroundColor")
    .attr("data-color"        ,v_NewBGColor)
    .style("background-color" ,v_NewBGColor)
    .html(d3.select(this).html());
})
.on("mouseover" ,function()
{
    try
    {
        var v_My    = d3.select(this);
        var v_FSize = Number(v_My.style("font-size").replace("px" ,""));
        
        v_My
        .style("font-size" ,(v_FSize + 3) + "px")
        .style("font-weight" ,"bold");
    }
    catch (error)
    {
        /* Nothing. */
    }
})
.on("mouseout" ,function()
{
    try
    {
        var v_My    = d3.select(this);
        var v_FSize = Number(v_My.style("font-size").replace("px" ,""));
        
        v_My
        .style("font-size" ,(v_FSize - 3) + "px")
        .style("font-weight" ,"normal");
    }
    catch (error)
    {
        /* Nothing. */
    }
});



/**
 * App图标大小的选择改变事件
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.selectAll(".newSizeTypeItem").on("click" ,function()
{
    var v_NewSizeType = d3.select(this).attr("data-sizeType");
    d3.select("#newSizeType")
    .attr("data-sizeType" ,v_NewSizeType)
    .html(d3.select(this).html());
});



/**
 * App图标名称对齐方式的选择改变事件
 *
 * ZhengWei(HY) Add 2020-10-22
 */
d3.selectAll(".newNameToLMRItem").on("click" ,function()
{
    var v_EditNameToLMR = d3.select(this).attr("data-nameToLMR");
    d3.select("#newNameToLMR")
    .attr("data-nameToLMR" ,v_EditNameToLMR)
    .html(d3.select(this).html());
});