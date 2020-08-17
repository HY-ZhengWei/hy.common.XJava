var v_DayInfoColors = d3.scaleOrdinal(d3.schemeCategory10);
var v_DayInfos      = new Object();



d3.selectAll(".dayInfoBarCloseBtn").on("click" ,function()
{
    d3.select("#dayInfoBar").style("visibility" ,"hidden").style("height" ,"0px");
});



function showTaskInfo()
{
}




/**
 * 日期点击事件 ZhengWei(HY) Add 2020-04-30
 */
function showDayInfo_OnClick(i_Year ,i_Month ,i_Day ,i_DayInfo)
{
    return;
  
    d3.select("#dayInfoBar")
    .style("opacity" ,"0")
    .style("height" ,"auto")
    .style("visibility" ,"visible")
    .transition().duration(400)
    .style("opacity" ,"1");
    
    d3.select("#dayInfoBar").selectAll(".dayInfoTaskBar").remove();
  
    
    var v_DayInfo = v_DayInfos["y" + i_Year + "m" + i_Month];
    if ( v_DayInfo == null || v_DayInfo.length <= 0 )
    {
        loadingDayInfos(i_Year ,i_Month ,false);
        v_DayInfo = v_DayInfos["y" + i_Year + "m" + i_Month];
    }
    if ( v_DayInfo == null || v_DayInfo.length <= 0 )
    {
        d3.selectAll(".dayInfoTitle").html(i_Year + "-" + i_Month + "-" + i_Day + " 暂无安排");
        return;
    }
    else
    {
        v_DayInfo = v_DayInfo[i_Day - 1];
        if ( v_DayInfo.tasks == null || v_DayInfo.tasks.length <= 0 )
        {
            d3.selectAll(".dayInfoTitle").html(v_DayInfo.taskDate + " 暂无安排");
        }
        else
        {
            d3.selectAll(".dayInfoTitle").html(v_DayInfo.taskDate + " 工作任务");
        }
    }
    
    
    var v_RColor  = Math.round(Math.random() * 10);
    d3.select("#dayInfoBar").selectAll(".dayInfoTaskBar").data(v_DayInfo.tasks).enter()
    .append("div")
    .attr("class" ,"dayInfoTaskBar")
    .style("background-color" ,function(d ,i)
    {
        return v_DayInfoColors(v_RColor + i);
    })
    .on("click" ,function(d ,i)
    {
        if ( d.url != null && d.url != "" )
        {
            window.parent.openWindowRightChildBar(d);
        }
    })
    .style("opacity" ,"0")
    .transition().delay(function(d ,i){ return (i + 1) * 120; }).duration(400)
    .style("opacity" ,"1");
    
    d3.selectAll(".dayInfoTaskBar").data(v_DayInfo.tasks).each(function(d ,i)
    {
        d3.select(this)
        .append("div")
        .attr("class" ,"dayInfoTaskIsTimeout");
        
        d3.select(this)
        .append("div")
        .attr("class" ,"dayInfoTaskMessage")
        .html(d.message);
    });
}



/**
 * 加载一个月份的工作任务  ZhengWei(HY) Add 2020-05-06
 */
function loadingDayInfos(i_Year ,i_Month ,i_IsAsync)
{
    /*
    $.ajaxSettings.async = i_IsAsync;
    
    $.post("../workRest/dayInfos.page"
      ,{
           userNo: window.parent.v_UserNo
          ,yearMonth: i_Year + '-' + i_Month
       }
      ,function(data)
      {
          if ( data == null || data[0] == null || data[0].data == null || data[0].user == null )
          {
              return;
          }
          v_DayInfos["y" + i_Year + "m" + i_Month] = data[0].data;
          app.reLoadRefreshUser(data[0].user.userName ,data[0].user.modelID);
      });
    
    $.ajaxSettings.async = true;
    */
}



function loadingDayInfosDefault()
{
    var v_Now = new Date();
    loadingDayInfos(v_Now.getFullYear() ,v_Now.getMonth() + 1 ,true);
}



loadingDayInfosDefault();
