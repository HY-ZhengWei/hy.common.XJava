/**
 * 鼠标右击App图标，显示右击菜单
 *
 * i_G     哪个App图标
 * i_Data  App图标数据
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function appOnContextmenu(i_G ,i_Data)
{
    hideAppDesktopMenu();
    hideColorPicker();
    hideWindowStartMenus(1);
    
    if ( i_Data != null )
    {
        d3.select("#copyApp-LI")         .classed("ui-state-disabled" ,false);
        d3.select("#delApp-LI")          .classed("ui-state-disabled" ,false);
        d3.select("#resizeMin-LI")       .classed("ui-state-disabled" ,(i_Data.sizeType == "min"));
        d3.select("#resizeMiddle-LI")    .classed("ui-state-disabled" ,(i_Data.sizeType == "middle"));
        d3.select("#resizeLongWidth-LI") .classed("ui-state-disabled" ,(i_Data.sizeType == "longWidth"));
        d3.select("#resizeLongHeight-LI").classed("ui-state-disabled" ,(i_Data.sizeType == "longHeight"));
        d3.select("#resizeMax-LI")       .classed("ui-state-disabled" ,(i_Data.sizeType == "max"));
        d3.select("#rename-LI")          .classed("ui-state-disabled" ,false);
        d3.select("#attribute-LI")       .classed("ui-state-disabled" ,false);
    }
    else
    {
        d3.select("#copyApp-LI")         .classed("ui-state-disabled" ,true);
        d3.select("#delApp-LI")          .classed("ui-state-disabled" ,true);
        d3.select("#resizeMin-LI")       .classed("ui-state-disabled" ,true);
        d3.select("#resizeMiddle-LI")    .classed("ui-state-disabled" ,true);
        d3.select("#resizeLongWidth-LI") .classed("ui-state-disabled" ,true);
        d3.select("#resizeLongHeight-LI").classed("ui-state-disabled" ,true);
        d3.select("#resizeMax-LI")       .classed("ui-state-disabled" ,true);
        d3.select("#rename-LI")          .classed("ui-state-disabled" ,true);
        d3.select("#attribute-LI")       .classed("ui-state-disabled" ,true);
    }
    
    $("#appMenuBar").css("left" ,(d3.event.pageX) + "px");  
    $("#appMenuBar").css("top"  ,(d3.event.pageY) + "px");
    $("#appMenuBar").css("opacity" ,100);
    
    v_ContextMenu = true;
    v_ContextG    = i_G;
    v_ContextData = i_Data;
}



/**
 * 隐藏App图标的右击菜单
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function hideAppMenu()
{
    $("#appMenuBar").css("left" ,"-99999px");     
    $("#appMenuBar").css("top"  ,"-99999px");
    $("#appMenuBar").css("opacity" ,0);
    
    v_ContextMenu = false;
}



/**
 * 复制App图标
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#copyApp").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        var v_NewData = $.extend(true, {}, v_ContextData);
        v_NewData.userID   = v_UserID;
        v_NewData.appID    = "APP" + v_UserID + "_" + (new Date()).getTime();
        v_NewData.appName += "-复本"; 
        v_NewData.x        = parseFloat(v_NewData.x) + 20;
        v_NewData.y        = parseFloat(v_NewData.y) + 20;
        v_NewData.appIndex = -1;
        
        /*
        v_NewData.x               = v_ContextData.x + 20;
        v_NewData.y               = v_ContextData.y + 20;
        v_NewData.appName         = v_ContextData.appName + "-复本"; 
        v_NewData.icon            = v_ContextData.icon;
        v_NewData.backgroundColor = v_ContextData.backgroundColor;
        v_NewData.sizeType        = v_ContextData.sizeType;
        v_NewData.actionType      = v_ContextData.actionType;
        v_NewData.url             = v_ContextData.url;
        */
        
        v_Apps.push(v_NewData);
        var v_MyG = createApp(v_NewData ,null);
        v_MyG
        .attr("opacity" ,0)
        .transition()
        .duration(1200)
        .attr("opacity" ,1);
        
        commitWindowAppCreate(v_NewData);
    }
});



/**
 * 删除App图标
 *
 * ZhengWei(HY) Add 2019-06-21
 */
d3.select("#delApp").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        $('#delAppTextDiv').html("确认删除 " + v_ContextData.appName + " ？");
        $('#delAppDialog').modal('show');
    }
});



/**
 * App图标改颜色
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#reColor").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        showColorPicker("colorPickerBG" ,v_ContextData.backgroundColor ,d3.event.pageX ,d3.event.pageY ,function(i_Color)
        {
            v_ContextData.backgroundColor = i_Color;
            v_ContextG.select("rect").attr("fill" ,v_ContextData.backgroundColor);
            
            commitWindowAppXXColorSize(v_ContextData);
        });
    }
});



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#resizeMin").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        changeAppSize(v_ContextG ,v_ContextData ,"min");
    }
});



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#resizeMiddle").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        changeAppSize(v_ContextG ,v_ContextData ,"middle");
    }
});



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#resizeLongWidth").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        changeAppSize(v_ContextG ,v_ContextData ,"longWidth");
    }
});



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#resizeLongHeight").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        changeAppSize(v_ContextG ,v_ContextData ,"longHeight");
    }
});



/**
 * App图标改变大小 
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#resizeMax").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        changeAppSize(v_ContextG ,v_ContextData ,"max");
    }
});



/**
 * App图标重命名 
 *
 * ZhengWei(HY) Add 2019-06-20
 */
d3.select("#rename").on("click" ,function()
{
    hideAppMenu();
    
    if ( v_ContextG != null && v_ContextData != null )
    {
        $('#renameText').val(v_ContextData.appName);
        $('#renameDialog').modal('show');
    }
});



/**
 * App的属性
 *
 * ZhengWei(HY) Add 2019-06-24
 */
d3.select("#attribute").on("click" ,function()
{
    hideAppMenu();
    showEditAppDialog();
});
