/**
 * 提交保存多屏同显布局
 * 
 * i_MWContent   展示层组件
 *
 * ZhengWei(HY) Add 2019-07-17
 */
function commitMultiWindowsSave()
{
	if ( v_MWTID == null || v_MWTID == "" )
	{
		return;
	}
	
	var v_MWConfig    = getMWindowsConfig();
	var v_MWConfigStr = JSON.stringify(v_MWConfig); 
	
	$.post("saveMultiWindows.page"
		,{
			 mwtid: v_MWTID
			,mwLayout: v_MWindowsType
			,mwConfig: v_MWConfigStr
		 }
		,function(data)
		{
		});
}
