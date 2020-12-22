var v_Drag = d3.drag()
.on("start" ,function()
{
    var v_XY = getGXY(d3.select(this));
    v_XOffset = d3.event.x - v_XY[0];
    v_YOffset = d3.event.y - v_XY[1];
    v_IsDrag  = true;
})
.on("drag" ,function()
{
    appDragMoving(d3.select(this));
})
.on("end" ,function()
{
    appDragEnd(d3.select(this));
});



/**
 * 拖动App图标的拖动中动作
 *
 * i_G      哪个App图标
 *
 * ZhengWei(HY) Add 2019-07-04
 */
function appDragMoving(i_G)
{
    var v_X       = d3.event.x - v_XOffset;
    var v_Y       = d3.event.y - v_YOffset;
    var v_VtoX    = (v_X - v_SpaceX) % (v_Sizes.min.width  + v_SpaceX);
    var v_HtoY    = (v_Y - v_SpaceY) % (v_Sizes.min.height + v_SpaceY);
    var v_ChangeX = v_X;
    var v_ChangeY = v_Y;
    var v_IsChage = false;
    
    if ( v_VtoX <= v_Sizes.min.width )
    {
        v_ChangeX = v_X - v_VtoX;
        v_IsChage = true;
    }
    if ( v_HtoY <= v_Sizes.min.height )
    {
        v_ChangeY = v_Y - v_HtoY;
        v_IsChage = true;
    }
    
    if ( v_IsChage )
    {
        i_G.attr("transform" ,"translate(" + v_ChangeX + "," + v_ChangeY + ")");
    }
    else
    {
        i_G.attr("transform" ,"translate(" + v_X + "," + v_Y + ")");
    }
    
    return 
}



/**
 * 拖动App图标的完成动作
 *
 * i_G      哪个App图标
 *
 * ZhengWei(HY) Add 2019-07-04
 */
function appDragEnd(i_G)
{
    var v_XY   = getGXY(i_G);
    var v_Data = v_Apps[i_G.attr("data-appIndex")];
    
    v_Data.x = parseInt(v_XY[0]);
    v_Data.y = parseInt(v_XY[1]);
    v_IsDrag = false;
    
    appOnMouseout(i_G ,v_Data);
    
    commitWindowAppXXColorSize(v_Data);
}



/**
 * 点击App图标
 *
 * i_G      哪个App图标
 * i_Data   App图标的数据信息
 *
 * ZhengWei(HY) Add 2019-06-19
 */
function appOnClick(i_G ,i_Data)
{
    if ( i_Data == null || i_Data.url == null || i_Data.url == "" )
    {
        showMessage("访问地址为空");
        return;
    }
    
    clearnWindowStartMenus(1);
    
    if ( i_Data.confirm != null && i_Data.confirm != "" )
    {
        $('#confirmAppTextDiv').html(i_Data.confirm);
        $('#confirmAppDialog').modal('show');
        
        d3.select("#confirmAppBtn").on("click" ,function()
        {
            $('#confirmAppDialog').modal('hide');
            
            openAppToWindowPage(i_Data);
        });
    }
    else
    {
        openAppToWindowPage(i_Data);
    }
}



/**
 * 打开窗口或执行脚本
 * 
 * i_Data    某一App数据
 *
 * ZhengWei(HY) Add 2019-07-04
 */
function openAppToWindowPage(i_Data)
{
    i_Data.userID = v_UserID;
    if ( i_Data.url.toLowerCase().indexOf("javascript:") >= 0 )
    {
        var v_Func = eval(i_Data.url.split(":")[1]);
        new v_Func();
        commitWindowAppOpenCount(i_Data);
    }
    else
    {
        if ( i_Data.actionType == "new" )
        {
            window.open(i_Data.url.replace(':USERNO' ,v_UserNo)); 
            
            if ( i_Data.downloadUrl != undefined && i_Data.downloadUrl != '' )
            {
                window.open(i_Data.downloadUrl.replace(':USERNO' ,v_UserNo)); 
            }
            
            commitWindowAppOpenCount(i_Data);
        }
        else
        {
            createWindowPage(i_Data);
        }
    }
}



/**
 * 创建所有App图标
 *
 * ZhengWei(HY) Add 2019-06-21
 */
function createApps()
{
    v_SVG.selectAll(".app").data(v_Apps).enter()
    .append("g")
    .attr("id" ,function(d ,i)
    {
        return "App_" + d.appID;
    })
    .attr("class" ,"app")
    .attr("transform", function(d ,i) 
    {
        return "translate(" + d.x + "," + d.y + ")";
    })
    .attr("data-appIndex", function(d ,i) 
    {
        d.appIndex = i;
        return i;
    })
    .call(v_Drag);
    
    
    v_SVG.selectAll(".app").data(v_Apps).each(function(d ,i)
    {
        var v_MyG = d3.select(this);
        createApp(d ,v_MyG);
    });
}



/**
 * 创建App图标
 *
 * i_Data       App图标数据
 * i_G          哪个App图标。可为空。为空时自动创建
 *
 * ZhengWei(HY) Add 2019-06-21
 */
function createApp(i_Data ,i_G)
{
    if ( i_Data.sizeType == null || i_Data.sizeType == "" )
    {
        i_Data.sizeType = "middle";
    }
    
    var v_MySize = v_Sizes[i_Data.sizeType];
    var v_MyG    = i_G; 
    
    if ( v_MyG == null )
    {
        v_MyG = v_SVG.append("g")
        .attr("id" ,"App_" + i_Data.appID)
        .attr("class" ,"app")
        .attr("transform", function() 
        {
            return "translate(" + i_Data.x + "," + i_Data.y + ")";
        })
        .attr("data-appIndex", function() 
        {
            if ( i_Data.appIndex == null || i_Data.appIndex < 0 )
            {
                i_Data.appIndex = ++v_AppIndex;
            }
            else 
            {
                if ( i_Data.appIndex > v_AppIndex )
                {
                    v_AppIndex = i_Data.appIndex;
                }
            }
            return i_Data.appIndex;
        })
        .call(v_Drag);
    }
    
    /* 图标块 */
    v_MyG.append("rect")
    .attr("fill" ,i_Data.backgroundColor)
    .attr("width"  ,v_MySize.width)
    .attr("height" ,v_MySize.height)
    .attr("stroke" ,v_Colors.borderColor)
    .attr("stroke-width" ,0)
    .style("cursor" ,"pointer")
    .on("mouseover" ,function()
    {
        appOnMouseover(v_MyG ,i_Data);
    })
    .on("mouseout" ,function()
    {
        appOnMouseout(v_MyG ,i_Data);
    })
    .on("click" ,function()
    {
        appOnClick(v_MyG ,i_Data);  
    })
    .on("contextmenu" ,function()
    {
        appOnContextmenu(v_MyG ,i_Data);
    });
    
    
    if ( i_Data.icon != null && i_Data.icon != "" )
    {
        /* 图标 */
        v_MyG.append("image")
        .attr("xlink:href" ,i_Data.icon)
        .attr("x" ,(v_MySize.width  - v_MySize.iconSize) / 2)
        .attr("y" ,(v_MySize.height - v_MySize.iconSize) / 2)
        .attr("width"  ,v_MySize.iconSize)
        .attr("height" ,v_MySize.iconSize)
        .style("cursor" ,"pointer")
        .on("mouseover" ,function()
        {
            appOnMouseover(v_MyG ,i_Data);
        })
        .on("mouseout" ,function()
        {
            appOnMouseout(v_MyG ,i_Data);
        })
        .on("click" ,function()
        {
            appOnClick(v_MyG ,i_Data);  
        })
        .on("contextmenu" ,function()
        {
            appOnContextmenu(v_MyG ,i_Data);
        });
    }
    
    
    if ( i_Data.sizeType != "min" )
    {
        var v_X   = v_MySize.fontSize;
        var v_LMR = "start";
        if ( i_Data.nameToLMR == "toLeft" )
        {
            v_X   = v_MySize.fontSize;
            v_LMR = "start"; 
        }
        else if ( i_Data.nameToLMR == "toMiddle" )
        {
            v_X   = v_MySize.width / 2;
            v_LMR = "middle";
        }
        else if ( i_Data.nameToLMR == "toRight" )
        {
            v_X   = v_MySize.width - v_MySize.fontSize / 4;
            v_LMR = "end";
        }
        
        /* 图标文字 */
        v_MyG.append("text")
        .attr("x"           ,v_X)
        .attr("y"           ,v_MySize.height - v_MySize.fontSize)
        .attr("dx"          ,v_MySize.fontSize / 3 * -1)
        .attr("dy"          ,v_MySize.fontSize / 3)
        .attr("font-size"   ,v_MySize.fontSize)
        .attr("fill"        ,v_Colors.textColor)
        .attr("text-anchor" ,v_LMR)
        .style("cursor"     ,"pointer")
        .text(i_Data.appName)
        .on("mouseover" ,function()
        {
            appOnMouseover(v_MyG ,i_Data);
        })
        .on("mouseout" ,function()
        {
            appOnMouseout(v_MyG ,i_Data);
        })
        .on("click" ,function()
        {
            appOnClick(v_MyG ,i_Data);  
        })
        .on("contextmenu" ,function()
        {
            appOnContextmenu(v_MyG ,i_Data);
        });
    }
    
    return v_MyG;
}
        
        
        
/**
 * 编辑App图标
 *
 * i_Data         App图标数据
 * i_G            哪个App图标。
 * i_ActionTime   动画时间。单位：微秒
 *
 * ZhengWei(HY) Add 2019-06-24
 */
function editApp(i_Data ,i_G ,i_ActionTime)
{
    var v_MySize = v_Sizes[i_Data.sizeType];
    var v_MyG    = i_G; 
    
    /* 图标块 */
    v_MyG.select("rect")
    .transition().duration(i_ActionTime)
    .attr("fill" ,i_Data.backgroundColor)
    .attr("width"  ,v_MySize.width)
    .attr("height" ,v_MySize.height);
    
    
    /* 图标 */
    if ( i_Data.icon != null && i_Data.icon != "" )
    {
        if ( v_MyG.select("image").empty() )
        {
            v_MyG.append("image")
            .attr("xlink:href" ,i_Data.icon)
            .attr("x" ,(v_MySize.width  - v_MySize.iconSize) / 2)
            .attr("y" ,(v_MySize.height - v_MySize.iconSize) / 2)
            .attr("width"  ,v_MySize.iconSize)
            .attr("height" ,v_MySize.iconSize)
            .style("cursor" ,"pointer")
            .on("mouseover" ,function()
            {
                appOnMouseover(v_MyG ,i_Data);
            })
            .on("mouseout" ,function()
            {
                appOnMouseout(v_MyG ,i_Data);
            })
            .on("click" ,function()
            {
                appOnClick(v_MyG ,i_Data);  
            })
            .on("contextmenu" ,function()
            {
                appOnContextmenu(v_MyG ,i_Data);
            });
        }
        else 
        {
            v_MyG.select("image")
            .attr("xlink:href" ,i_Data.icon)
            .transition().duration(i_ActionTime)
            .attr("x" ,(v_MySize.width  - v_MySize.iconSize) / 2)
            .attr("y" ,(v_MySize.height - v_MySize.iconSize) / 2)
            .attr("width"  ,v_MySize.iconSize)
            .attr("height" ,v_MySize.iconSize);
        }
    }
    else
    {
        if ( !v_MyG.select("image").empty() )
        {
            v_MyG.select("image").remove();
        }
    }
    
    
    /* 图标文字 */
    if ( i_Data.sizeType != "min" )
    {
        var v_X   = v_MySize.fontSize;
        var v_LMR = "start";
        if ( i_Data.nameToLMR == "toLeft" )
        {
            v_X   = v_MySize.fontSize;
            v_LMR = "start"; 
        }
        else if ( i_Data.nameToLMR == "toMiddle" )
        {
            v_X   = v_MySize.width / 2;
            v_LMR = "middle";
        }
        else if ( i_Data.nameToLMR == "toRight" )
        {
            v_X   = v_MySize.width - v_MySize.fontSize / 4;
            v_LMR = "end";
        }
      
        if ( v_MyG.select("text").empty() )
        {
            v_MyG.append("text")
            .attr("x"           ,v_X)
            .attr("y"           ,v_MySize.height - v_MySize.fontSize)
            .attr("dx"          ,v_MySize.fontSize / 3 * -1)
            .attr("dy"          ,v_MySize.fontSize / 3)
            .attr("font-size"   ,v_MySize.fontSize)
            .attr("fill"        ,v_Colors.textColor)
            .attr("text-anchor" ,v_LMR)
            .style("cursor"     ,"pointer")
            .text(i_Data.appName)
            .on("mouseover" ,function()
            {
                appOnMouseover(v_MyG ,i_Data);
            })
            .on("mouseout" ,function()
            {
                appOnMouseout(v_MyG ,i_Data);
            })
            .on("click" ,function()
            {
                appOnClick(v_MyG ,i_Data);  
            })
            .on("contextmenu" ,function()
            {
                appOnContextmenu(v_MyG ,i_Data);
            });
        }
        else
        {
            v_MyG.select("text")
            .transition().duration(i_ActionTime)
            .attr("x"           ,v_X)
            .attr("y"           ,v_MySize.height - v_MySize.fontSize)
            .attr("dx"          ,v_MySize.fontSize / 3 * -1)
            .attr("dy"          ,v_MySize.fontSize / 3)
            .attr("font-size"   ,v_MySize.fontSize)
            .attr("text-anchor" ,v_LMR)
            .text(i_Data.appName);
        }
    }
    else
    {
        if ( !v_MyG.select("text").empty() )
        {
            v_MyG.select("text").remove();
        }
    }
    
    return v_MyG;
}



/**
 * 获取App图标组件。不存在时，返回空
 *
 * i_AppID   App标识ID
 * 
 * ZhengWei(HY) Add 2019-07-04
 */
function getAppG(i_AppID)
{
    var v_MyG = v_SVG.select("#App_" + i_AppID);
    
    if ( v_MyG.empty() )
    {
        return null;
    }
    else
    {
        return v_MyG;
    }
}



/**
 * 显示App图标的提示
 *
 * i_Tooltip   提示文字
 * i_X         X坐标
 * i_Y         Y坐标
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function showAppTooltip(i_Tooltip ,i_X ,i_Y)
{
    var v_X = i_X;
    var v_Y = i_Y;
    
    $("#appTooltip").html(i_Tooltip);
    $("#appTooltip").css("left" ,(i_X + v_Sizes.min.width) + "px");
    $("#appTooltip").css("top"  ,(i_Y - 30) + "px");
    $("#appTooltip").css("opacity" ,100); 
} 



/**
 * 隐藏App图标的提示
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function hideAppTooltip()
{
    $("#appTooltip").html("");
    $("#appTooltip").css("left" ,"-99999px");     
    $("#appTooltip").css("top"  ,"-99999px");
    $("#appTooltip").css("opacity" ,0);
}



/**
 * 改变App图标的大小
 *
 * i_G     哪个App图标
 * i_Data  App图标数据
 *
 * ZhengWei(HY) Add 2019-06-20
 */
function changeAppSize(i_G ,i_Data ,i_ToSize)
{
    var v_MySize = v_Sizes[i_ToSize];
    i_Data.sizeType = i_ToSize;
    
    /* 图标块 */
    i_G.select("rect")
    .transition()
    .duration(500)
    .attr("width"  ,v_MySize.width)
    .attr("height" ,v_MySize.height)
    .attr("stroke-width" ,0);
    
    
    if ( i_Data.icon != null && i_Data.icon != "" )
    {
        /* 图标 */
        i_G.select("image")
        .transition()
        .duration(500)
        .attr("x" ,(v_MySize.width  - v_MySize.iconSize) / 2)
        .attr("y" ,(v_MySize.height - v_MySize.iconSize) / 2)
        .attr("width"  ,v_MySize.iconSize)
        .attr("height" ,v_MySize.iconSize);
    }
    
    
    if ( i_ToSize != "min" )
    {
        var v_GText = i_G.select("text");
        if ( v_GText.empty() )
        {
            v_GText = i_G.append("text")
            .attr("fill" ,v_Colors.textColor)
            .style("cursor" ,"pointer")
            .text(i_Data.appName)
            .on("mouseover" ,function()
            {
                appOnMouseover(i_G ,i_Data);
            })
            .on("mouseout" ,function()
            {
                appOnMouseout(i_G ,i_Data);
            })
            .on("click" ,function()
            {
                appOnClick(i_G ,i_Data);    
            })
            .on("contextmenu" ,function()
            {
                appOnContextmenu(i_G ,i_Data);
            });
        }
        
        var v_X   = v_MySize.fontSize;
        var v_LMR = "start";
        if ( i_Data.nameToLMR == "toLeft" )
        {
            v_X   = v_MySize.fontSize;
            v_LMR = "start"; 
        }
        else if ( i_Data.nameToLMR == "toMiddle" )
        {
            v_X   = v_MySize.width / 2;
            v_LMR = "middle";
        }
        else if ( i_Data.nameToLMR == "toRight" )
        {
            v_X   = v_MySize.width - v_MySize.fontSize / 4;
            v_LMR = "end";
        }
        
        /* 图标文字 */
        v_GText
        .transition()
        .duration(500)
        .attr("x"           ,v_X)
        .attr("y"           ,v_MySize.height - v_MySize.fontSize)
        .attr("dx"          ,v_MySize.fontSize / 3 * -1)
        .attr("dy"          ,v_MySize.fontSize / 3)
        .attr("font-size"   ,v_MySize.fontSize)
        .attr("text-anchor" ,v_LMR);
    }
    else
    {
        var v_GText = i_G.select("text");
        if ( v_GText != null )
        {
            v_GText.remove();
        }
    }
    
    commitWindowAppXXColorSize(i_Data);
}



/**
 * 鼠标进入App图标
 *
 * i_G     哪个App图标
 * i_Data  App图标数据
 *
 * ZhengWei(HY) Add 2019-06-19
 */
function appOnMouseover(i_G ,i_Data)
{
    if ( !v_IsDrag )
    {
        i_G.select("rect").attr("stroke-width" ,v_BorderSize);
        
        if ( i_Data.sizeType == "min" )
        {
            showAppTooltip(i_Data.appName ,d3.event.x ,d3.event.y);
        }
    }
}



/**
 * 鼠标移出App图标
 *
 * i_G     哪个App图标
 * i_Data  App图标数据
 *
 * ZhengWei(HY) Add 2019-06-19
 */
function appOnMouseout(i_G ,i_Data)
{
    if ( !v_IsDrag )
    {
        i_G.select("rect").attr("stroke-width" ,0);
    }
    
    hideAppTooltip();
}



createApps();