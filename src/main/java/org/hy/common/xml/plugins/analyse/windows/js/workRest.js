var app = new Vue({
  el: '#app',
  data: {
      weekNoToNames: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'],
      weekNoToShortNames: ['日', '一', '二', '三', '四', '五', '六'],
      bigNumbers: ['一', '二', '三', '四', '五', '六', '七', '八', '九'],
      timerNo: 0,                                    /* 定时器的编号 */
      holidayIndex: 0,                               /* 一天中有多个节假日、纪念日时，当前显示第几个。此为一直累计递增的值 */
      holidays: {
          '1-1': '元旦',
          '2-14': '情人节',
          '3-8': '妇女节',
          '3-12': '植树节',
          '3-14': '白色情人',
          '3-15': '消权日',
          '3-21': '睡眠日',
          '4-1': '愚人节',
          '4-22': '地球日',
          '4-23': '读书日',
          '4-26': '知识产权',
          '5-1': '劳动节',
          '5-4': '青年节',
          '5-8': '微笑日',
          '5-12': '护士节',
          '5-17': '电信节',
          '5-20': '我爱你',
          '5-31': '无烟日',
          '6-1': '儿童节',
          '6-6': '爱眼日',
          '6-23': '奥林匹克',
          '7-1': '建党节',
          '8-1': '建军节',
          '8-6': '电影节',
          '8-8': '爸爸节',
          '9-10': '教师节',
          '9-18': '国耻日',
          '9-20': '爱牙日',
          '10-1': '国庆节',
          '10-8': '高血压日',
          '10-20': '厨师日',
          '10-24': '程序员节',
          '10-31': '万圣节',
          '11-8': '记者节',
          '11-9': '消防日',
          '11-11': '光棍节',
          '11-14': '糖尿病日',
          '11-17': '大学生节',
          '12-2': '交通安全',
          '12-4': '国家宪法',
          '12-9': '足球日',
          '12-13': '国家公祭',
          '12-21': '篮球日',
          '12-24': '平安夜',
          '12-25': '圣诞节'
      },
      holidayWeeks: {                                 /* 某月-第几周-星期几（0周日、1周一...） */
          
      },
      holidayWhatWeeks: {                             /* 某月-第几个-星期几（0周日、1周一...） */
          '5-2-0': '母亲节',
          '6-3-0': '父亲节',
          '11-4-4': '感恩节'
      },
      holidayLunars: {                                /* 农历纪念日 */
          '正月初一': '春节',
          '正月十五': '元宵节',
          '五月初五': '端午节',
          '七月初七': '七夕',
          '八月十五': '中秋节',
          '九月初九': '重阳节',
          '腊月初八': '腊八'
      },
      holidayLunarsLow: {                             /* 显示级别低的农历纪念日 */
          '二月初二': '龙抬头',
          '五月十三': '泼水节',
          '七月十五': '中元节',
          '腊月廿三': '北方小年',
          '腊月廿四': '南方小年'
      },
      myHolidays: {
      
      },
      menus: {
          title: '+',
          childIsShow: false,
          mainSize: 90,
          x: 50,
          y: 50,
          opacity: 0.8
      },
      skin: {                                         /* 皮肤 */
          colors: [
              { color: '#4395FF', name: '海军蓝' },
              { color: '#3CB371', name: '春天绿' }
          ],
          colorIndex: 0                               /* 选择的颜色下标 */
      },
      people: {                                       /* 主人的生日 */
          year: 0,
          month: 0,
          day: 0,
          maxAge: 0                                   /* 倒计时最大年龄（逐步被countDown取代） */
      },
      setting: {
          loadMonthCount: 12,                         /* 加载显示多少个月份 */
          isShowRest: false                           /* 是否显示休息日的标记 */
      },
      cycle: {                                        /* 周期循环对象 */
          startYear: 2019,                            /* 周期循环的开始计算日期 */
          startMonth: 3,
          startDay: 25,                               
          workRestDays: []                            /* 工、休天的情况。1为工作；0为休息。如：[1,1,1,1,0,0,0,0] */
      },
      disableDays: {                                  /* 置灰日期，不可点击 */
          '2019-1-1': '置灰描述'
      },
      selectedDate: null,                             /* 当前选择日期，决定显示首个月份的日期 */
      todayYearName: '',                              /* 今天所在年份的生肖 */
      todayYearGanZhi: '',                            /* 今天所在年份的天干地支 */
      todayLunar: '',                                 /* 今天所在的农历日期 */
      todayYear: 0,                                   /* 今天是哪一年 */
      todayMonth: 0,                                  /* 今天是几月 */
      todayWeek: 0,                                   /* 今天是周几 */
      todayWeekNoOfYear: 0,                           /* 今天是本年度第几周 */ 
      todayNoOfYear: 0,                               /* 今天是本年度第几天 */
      todayConstellation: '',                         /* 今天的星座 */
      today: 0,                                       /* 今天是几号 */
      nowYear: 0,                                     /* 当前时间的是哪一年 */
      nowMonth: 0,                                    /* 当前时间的是几月 */
      nowDay: 0,                                      /* 当前时间的是几号 */
      haveDayCount: 0,                                /* 主人还剩余多少天 */
      countDown: {                                    /* 倒计时类型 */
          types: [
              { type: '0' , isDate: false, name: '请选择' },
              { type: '10', isDate: true , name: '小升初' },
              { type: '11', isDate: true , name: '中考' },
              { type: '12', isDate: true , name: '高考' },
              { type: '13', isDate: true , name: '大学毕业' },
              { type: '97', isDate: false, name: '退休' },
              { type: '98', isDate: false, name: '预估寿命' },
              { type: '99', isDate: true , name: '自定义' }
          ],
          typeIndex: 0,                               /* 选择的倒计时下标 */
          maxDate: ''                                 /* 倒计时最大的日期 */
      },
      clickDay: {                                     /* 点击选中的日期下标索引。-1表示无效 */
          monthIndex: -1,
          weekIndex: -1,
          dayIndex: -1
      },
      days: [{
          year: 0,
          month: 0,
          days: [[{
              id: '',
              day: 0,
              info: '',        /* 显示在日期下面（最低级显示级别）：如，假日、今天、纪念日 */
              isLunar: 0,      /* 显示信息为农历。即 info 为农历信息。 */
              dynamic: '',     /* 显示在日期下面（最优级显示级别）：动态信息 */
              style: '',       /* 日期的整体背景样式 */
              isWork: false,
              isRest: true,
              disable: false,  
              disInfo: ''      /* 显示在日期下面（次优级显示级别）：置灰时的描述 */
              }]]
          }]
    },
    
    
    
    methods: {
      
        /**
         * 是否启用标记 ‘班休’ 功能  ZhengWei(HY) Add 2019-04-16
         */
        isCycle() {
  
            if ( this.cycle )
            {
                if ( this.cycle.workRestDays )
                {
                    if ( this.cycle.workRestDays.length >= 2 ) 
                    {
                        return true;
                    }
                }
            }
  
            return false;
        },
        
        
        
        /**
         * 计算某日期与周期循环开始日期间的偏移量 ZhengWei(HY) Add 2019-04-08
         */
        calcCycleToDate: function (i_Year, i_Month, i_Day) {

            var v_StartDate     = new Date(this.cycle.startYear, this.cycle.startMonth - 1, this.cycle.startDay);
            var v_CompaDate     = new Date(i_Year ,i_Month - 1 ,i_Day);
            var v_CalcDirection = DateHelp.compareYMD(v_StartDate, v_CompaDate);

            if ( v_CalcDirection == 0 )
            {
                return 0;
            }
            else
            {
                var v_DayCount = 0;
                do 
                {
                    v_DayCount = v_DayCount + v_CalcDirection;
                    v_StartDate.setDate(v_StartDate.getDate() + v_CalcDirection);
                }
                while ( DateHelp.compareYMD(v_StartDate, v_CompaDate) != 0 ); 

                v_DayCount = v_DayCount % this.cycle.workRestDays.length;
                if ( v_DayCount < 0 )
                {
                    v_DayCount = this.cycle.workRestDays.length + v_DayCount;
                }

                return v_DayCount;
            }
        },
        
        
        
        /**
         * 计算某天的日期说明 ZhengWei(HY) Add 2019-04-08
         * 
         * @param i_Year        年份
         * @param i_Month       月份
         * @param i_Day         几号
         * @param i_Week        星期几（0周日、1周一...）
         * @param i_WhatWeek    某天为本月出现的第个星期几
         * @param i_24ST        一年的24节气信息
         * @param i_1_X         数九中的第几天。冬至为0，二九为9。。。九九为81
         * @param i_Easter      复活节为几月几号。
         * @param i_GoodFriday  耶稣受难日为几月几号。
         */
        calcDayInfo (i_Year, i_Month, i_Day, i_Week, i_WhatWeek, i_24ST, i_1_X, i_Easter, i_GoodFriday) {

            var v_Lunar   = DateHelp.calcLunar(i_Year, i_Month, i_Day);
            var v_IsLunar = v_Lunar && v_Lunar != null && v_Lunar.monthName != '';
            var v_History = null;
            var v_HIndex  = 0;
            var v_Ret     = {
                info: [''],
                isLunar: 0,
                isToday: 0
            };

            /* 自定义的纪念日 */
            v_History = this.myHolidays[i_Month + '-' + i_Day];
            if ( v_History ) 
            {
                v_Ret.info[v_HIndex++] = v_History.title;
            }

            /* 农历的自定义的纪念日 */
            if ( v_IsLunar )
            {
                v_History = this.myHolidays['L-' + v_Lunar.monthName + '-' + v_Lunar.dayName];
                if ( v_History )
                {
                    v_Ret.info[v_HIndex++] = v_History.title;
                }
            }

            /* 我的生日 */
            if ( i_Month == this.people.month && i_Day == this.people.day ) 
            {
                v_Ret.info[v_HIndex++] = '我的生日';
            }

            /* 阳历纪念日 -- 当月第几周 - 星期几（0周日、1周一...） 
            var v_WeekNoOfMonth = DateHelp.getWeekNoOfMonth(i_Year, i_Month, i_Day);
            v_History = this.data.holidayWeeks[i_Month + '-' + v_WeekNoOfMonth + '-' + i_Week];
            if ( v_History )
            {
                return {
                    info: v_History,
                    isLunar: 0
                };
            } 
            */

            /* 阳历纪念日 -- 当月-第几个-星期几（0周日、1周一...） */
            v_History = this.holidayWhatWeeks[i_Month + '-' + i_WhatWeek + '-' + i_Week];
            if ( v_History )
            {
                v_Ret.info[v_HIndex++] = v_History;
            }

            /* 阳历纪念日 */
            v_History = this.holidays[i_Month + '-' + i_Day];
            if ( v_History )
            {
                v_Ret.info[v_HIndex++] = v_History;
            }

            /* 农历纪念日 */
            if ( v_IsLunar )
            {
                v_History = this.holidayLunars[v_Lunar.monthName + v_Lunar.dayName];
                if ( v_History )
                {
                    v_Ret.info[v_HIndex++] = v_History;
                }

                /* 农历的最后一天是动态的，除夕也是动态，所有要在此特殊处理一下 */
                if ( v_Lunar.monthName == '腊月' && v_Lunar.isMaxDayOfMonth == 1 )
                {
                    v_Ret.info[v_HIndex++] = '除夕';
                }
            }

            /* 复活节、受难日 */
            v_History = i_Month + '-' + i_Day;
            if ( i_Easter == v_History )
            {
                v_Ret.info[v_HIndex++] = '复活节';
            }
            else if ( i_GoodFriday == v_History )
            {
                v_Ret.info[v_HIndex++] = '受难日';
            }

            /* 24节气 */
            for (var i=0; i<2; i++)
            {
                if ( i_24ST[i].day == i_Day )
                {
                    v_Ret.info[v_HIndex++] = i_24ST[i].name;
                }
            }

            /* 显示级别低的农历纪念日 */
            if ( v_IsLunar )
            {
                v_History = this.holidayLunarsLow[v_Lunar.monthName + v_Lunar.dayName];
                if ( v_History )
                {
                    v_Ret.info[v_HIndex++] = v_History;
                }
            }

            /* 星座
            var v_ConstellationFirst = DateHelp.calcConstellationFirst(i_Year, i_Month, i_Day);
            if ( v_ConstellationFirst != '' )
            {
                return {
                    info: v_ConstellationFirst + '座',
                    isLunar: 0            
                    };
            }
            */

            /* 数九天 */
            if ( i_1_X > 0 )
            {
                if ( i_1_X % 9 == 1 )
                { 
                    v_Ret.info[v_HIndex++] = this.bigNumbers[Math.floor(i_1_X / 9)] + '九';
                }
            }

            if ( v_HIndex == 0 )
            {
                /* 今天 */
                if ( i_Year == this.todayYear && i_Month == this.todayMonth && i_Day == this.today )
                {
                    v_Ret.info[v_HIndex++] = '今天';
                    v_Ret.isToday          = 1;
                }
                /* 农历 */
                else if ( v_IsLunar )
                {
                    var v_LunarDayName = v_Lunar.dayName;
                    if ( '初一' == v_LunarDayName )
                    {
                        v_LunarDayName = v_Lunar.monthName;
                        if ( v_Lunar.isLunar == 1 )
                        {
                            v_LunarDayName = '闰' + v_LunarDayName;
                        }
                    }

                    v_Ret.info[v_HIndex++] = v_LunarDayName;
                    v_Ret.isLunar          = 1;
                }
            }
            
            return v_Ret;
        },
        
        
      
        /**
         * 设置选择的日期 ZhengWei(HY) Add 2019-04-07
         */
        setSelectDay(i_Day) {
    
            this.days         = [];
            var v_IsCycle     = this.isCycle();
            var v_Year        = i_Day.getFullYear() - (i_Day.getMonth() == 0 ? 1 : 0);
            var v_Month       = i_Day.getMonth() + 1 - 1;
            var v_WROffset    = v_IsCycle ? this.calcCycleToDate(i_Day.getFullYear(), i_Day.getMonth() + 1, 1) : 0;
            var v_ScrollMonth = 0;     /* 今天在什么位置上：偏移的月数。 */
            var v_ScrollRow   = 0;     /* 今天在什么位置上：偏移的行数。 */
            var v_1_9         = 0;     /* 冬至是哪天，用于计算三九天。零值，表示无效值。 */ 
            var v_1_X         = 0;     /* 数到第几个九天。零值，表示无效值。 */
    
            for (var v_MonthIndex = 1; v_MonthIndex <= this.setting.loadMonthCount; v_MonthIndex++)
            {
                var v_MonthObj = new Object();
                var v_WhatWeek = [0 ,0 ,0 ,0 ,0 ,0 ,0];   /* 统计某天为本月出现的第个星期几。 */
                                                          /* 元素0：为星期日出现的几次。 */
                                                          /* 元素1：为星期一出现的几次。 */
                                                          /* ... */
                                                          /* 元素6：为星期六出现的几次。 */
                
                v_Month          = v_Month >= 12 ? 1 : v_Month + 1;
                v_Year           = (v_Month == 1 ? 1 : 0) + v_Year;
                v_MonthObj.year  = v_Year;
                v_MonthObj.month = v_Month;
                v_MonthObj.days  = [];
    
                var v_Easter     = DateHelp.calcEaster(v_Year); 
                var v_GoodFriday = DateHelp.calcGoodFriday(v_Year);
                var v_24ST       = DateHelp.calcSolarTerm(v_Year, v_Month);
                if ( v_Month == 12 )
                {
                    v_1_9 = v_24ST[1].day;
                    v_1_X = 0;
                }
                
                /* 本月的1号 */
                var v_FirstDayOfMonth = new Date();
                v_FirstDayOfMonth.setFullYear(v_Year);
                v_FirstDayOfMonth.setMonth(v_Month - 1);
                v_FirstDayOfMonth.setDate(1);
                
                /* 本月的最后一天 */
                /* 2019-10-31 发现微信在计算月份的最后一天时，有误。误计算为30号 */
                var v_LastDayOfMonth = new Date();
                v_LastDayOfMonth.setFullYear(v_Year);
                v_LastDayOfMonth.setMonth(v_Month);
                v_LastDayOfMonth.setDate(0);
    
                var v_Index      = 0;
                var v_Day        = 1;
                var v_DayMax     = v_LastDayOfMonth.getDate();
                if ( v_Month != 2 )
                {
                    v_DayMax = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][v_Month - 1];
                }
                /* console.log(v_Year + '-' + v_Month + '月的最后一天' + v_DayMax); */
                var v_WeekIndex  = 0;
                var v_DayWeekArr = new Array();
    
                /* 1号之前的空白日期的计算 */
                for (var v_X = 0; v_Index <= 6 && v_Index < v_FirstDayOfMonth.getDay(); v_Index++ ,v_X++)
                {
                    v_DayWeekArr[v_X]      = new Object();
                    v_DayWeekArr[v_X].day  = ' ';
                    v_DayWeekArr[v_X].info = '';
                }
    
                /* 1号及之后的首周日期的计算 */
                for (var v_X = v_FirstDayOfMonth.getDay(); v_Index <= 6; v_Index++ , v_X++)
                {
                    v_WhatWeek[v_X]           = v_WhatWeek[v_X] + 1;
                    v_DayWeekArr[v_X]         = new Object();
                    v_DayWeekArr[v_X].day     = v_Day++;
                    v_DayWeekArr[v_X].disable = this.disableDays[v_Year + '-' + v_Month + '-' + v_DayWeekArr[v_X].day] ? true : false;
                    v_DayWeekArr[v_X].disInfo = this.disableDays[v_Year + '-' + v_Month + '-' + v_DayWeekArr[v_Index].day];
                    v_DayWeekArr[v_X].id      = v_Year + '-' + v_Month + '-' + v_DayWeekArr[v_X].day;
    
                    var v_NormalWR = DateHelp.calcNormalWorkRest(v_Year, v_Month, v_DayWeekArr[v_X].day);
                    if ( v_NormalWR )
                    {
                        v_DayWeekArr[v_X].style = (v_NormalWR == 'W' ? 'daysColumnWork' : 'daysColumnRest');
                    }
                    else
                    {
                        v_DayWeekArr[v_X].style = '';
                    }
    
                    /* 数九天 */
                    if ( v_1_9 > 0 )
                    {
                        if ( v_1_X <= 0 )
                        {
                            if ( v_1_9 == v_DayWeekArr[v_X].day )
                            {
                                v_1_X = 1;
                            }
                        }
                        else if ( 1 <= v_1_X && v_1_X < 81 )
                        {
                            v_1_X++;
                        }
                        else 
                        {
                            v_1_9 = 0;
                            v_1_X = 0;
                        }
                    }
                    var v_Infos = this.calcDayInfo(v_Year, v_Month, v_DayWeekArr[v_X].day, v_X, v_WhatWeek[v_X], v_24ST, v_1_X, v_Easter, v_GoodFriday);
                    v_DayWeekArr[v_X].info    = v_Infos.info;
                    v_DayWeekArr[v_X].isLunar = v_Infos.isLunar;
                    v_DayWeekArr[v_X].style  += (v_Infos.isToday == 1 ? ' daysColumnToday' : '');
                    if ( v_IsCycle )
                    {
                        v_DayWeekArr[v_X].isWork  = this.cycle.workRestDays[v_WROffset % this.cycle.workRestDays.length] == 1;
                        if ( this.setting.isShowRest )
                        {
                            v_DayWeekArr[v_X].isRest = this.cycle.workRestDays[v_WROffset % this.cycle.workRestDays.length] == 0;
                        }
                        v_WROffset++;
                    }
                }
                v_MonthObj.days[0] = v_DayWeekArr;
    
                /* 本月第二周之后的日期计算 */
                for (v_WeekIndex = 1; v_WeekIndex < 6; v_WeekIndex++) 
                {
                    v_DayWeekArr = new Array();
                    for (v_Index = 0; v_Index <= 6 && v_Day <= v_DayMax; v_Index++)
                    {
                        v_WhatWeek[v_Index]           = v_WhatWeek[v_Index] + 1;
                        v_DayWeekArr[v_Index]         = new Object();
                        v_DayWeekArr[v_Index].day     = v_Day++;
                        v_DayWeekArr[v_Index].disable = this.disableDays[v_Year + '-' + v_Month + '-' + v_DayWeekArr[v_Index].day] ? true : false;
                        v_DayWeekArr[v_Index].disInfo = this.disableDays[v_Year + '-' + v_Month + '-' + v_DayWeekArr[v_Index].day];
                        v_DayWeekArr[v_Index].id      = v_Year + '-' + v_Month + '-' + v_DayWeekArr[v_Index].day;
    
                        var v_NormalWR = DateHelp.calcNormalWorkRest(v_Year, v_Month, v_DayWeekArr[v_Index].day);
                        if ( v_NormalWR )
                        {
                            v_DayWeekArr[v_Index].style = (v_NormalWR == 'W' ? 'daysColumnWork' : 'daysColumnRest');
                        }
                        else
                        {
                            v_DayWeekArr[v_Index].style = '';
                        }
                        
                        /* 数九天 */
                        if ( v_1_9 > 0 )
                        {
                            if ( v_1_X <= 0 )
                            {
                                if ( v_1_9 == v_DayWeekArr[v_Index].day )
                                {
                                    v_1_X = 1;
                                }
                            }
                            else if ( 1 <= v_1_X && v_1_X < 81 )
                            {
                                v_1_X++;
                            }
                            else 
                            {
                                v_1_9 = 0;
                                v_1_X = 0;
                            }
                        }
                        var v_Infos = this.calcDayInfo(v_Year, v_Month, v_DayWeekArr[v_Index].day, v_Index, v_WhatWeek[v_Index], v_24ST, v_1_X, v_Easter, v_GoodFriday);
                        v_DayWeekArr[v_Index].info    = v_Infos.info;
                        v_DayWeekArr[v_Index].isLunar = v_Infos.isLunar;
                        v_DayWeekArr[v_Index].style  += (v_Infos.isToday == 1 ? ' daysColumnToday' : '');
                        if ( v_IsCycle )
                        {
                            v_DayWeekArr[v_Index].isWork  = this.cycle.workRestDays[v_WROffset % this.data.cycle.workRestDays.length] == 1;
                            if ( this.data.setting.isShowRest )
                            {
                                v_DayWeekArr[v_Index].isRest = this.cycle.workRestDays[v_WROffset % this.data.cycle.workRestDays.length] == 0;
                            }
                            v_WROffset++;
                        }
                    }
                    v_MonthObj.days[v_WeekIndex] = v_DayWeekArr;
    
                    if ( v_Day > v_DayMax )
                    {
                        break;
                    }
                }
    
                /* 本周最后一周的空白日期的计算 */
                if ( v_Index <= 6 )
                {
                    for (; v_Index <= 6; v_Index++)
                    {
                        v_DayWeekArr[v_Index]      = new Object();
                        v_DayWeekArr[v_Index].day  = '';
                        v_DayWeekArr[v_Index].info = '';
                    }
                    v_MonthObj.days[v_WeekIndex] = v_DayWeekArr;
                }
    
                this.days.push(v_MonthObj);
            }
        },
        
        
        
        /**
         * 日期点击事件 ZhengWei(HY) Add 2019-04-08
         */
        dayOnClick(e) {
          
            if ( e.currentTarget.dataset.day == '' 
              || e.currentTarget.dataset.day == ' ' 
              || e.currentTarget.dataset.day == '　' )
            {
                return;
            }
            
            var v_Year       = e.currentTarget.dataset.year;
            var v_Month      = e.currentTarget.dataset.month;
            var v_Day        = e.currentTarget.dataset.day;
            var v_MonthIndex = e.currentTarget.dataset.monthindex;
            var v_WeekIndex  = e.currentTarget.dataset.weekindex;
            var v_DayIndex   = e.currentTarget.dataset.dayindex;
            var v_DayInfo    = null;
            var v_Now        = new Date();
            var v_ClickDay   = new Date(v_Year + '-' + v_Month + '-' + v_Day);
            
            showDayInfo_OnClick(v_Year ,v_Month ,v_Day ,v_DayInfo);
            
            /* 先还原上次点击的设定 */
            if ( this.clickDay.monthIndex >= 0 )
            {
                v_DayInfo = this.days[this.clickDay.monthIndex].days[this.clickDay.weekIndex][this.clickDay.dayIndex];
                v_DayInfo.style = v_DayInfo.style.replace('daysColumnClick' ,'');
            }
            
            this.reLoadRefreshTitle(v_ClickDay);
            
            /* 如果是“今天”不设定新样式  */
            if ( DateHelp.compareYMD(v_Now ,v_ClickDay) == 0 )
            {
                this.clickDay.monthIndex = -1;
                this.clickDay.weekIndex  = -1;
                this.clickDay.dayIndex   = -1;
                return;
            }
            
            this.clickDay.monthIndex = v_MonthIndex;
            this.clickDay.weekIndex  = v_WeekIndex;
            this.clickDay.dayIndex   = v_DayIndex;
            v_DayInfo = this.days[this.clickDay.monthIndex].days[this.clickDay.weekIndex][this.clickDay.dayIndex];
            v_DayInfo.style += ' daysColumnClick';
        },
       
        
        
        timerChangeHoliday() {

          this.holidayIndex = this.holidayIndex + 1

        },
        
        
        
        /**
         * 刷新标题（如点击某一日期） ZhengWei(HY) Add 2020-04-28
         */
        reLoadRefreshTitle(i_Date) {
          
          var v_SelectDate = new Date(i_Date.getFullYear(), i_Date.getMonth(), i_Date.getDate()); 
          var v_Now        = new Date();
          
          if ( this.countDown.maxDate != '' )
          {
              var v_MaxDate = DateHelp.newDate(this.data.countDown.maxDate);
              v_MaxDate = new Date(v_MaxDate.getFullYear(), v_MaxDate.getMonth(), v_MaxDate.getDate());
              this.data.haveDayCount = (v_MaxDate - v_SelectDate) / 24 / 60 / 60 / 1000;
          }
  
          var v_FirstDayOfYear = new Date(v_SelectDate.getFullYear() ,0 ,1);
          this.todayNoOfYear = (v_SelectDate - v_FirstDayOfYear) / 24 / 60 / 60 /1000 + 1;
          this.todayWeekNoOfYear = DateHelp.getWeekNoOfYear(v_SelectDate.getFullYear(), v_SelectDate.getMonth() + 1, v_SelectDate.getDate());
          var v_Lunar = DateHelp.calcLunar(v_SelectDate.getFullYear(), v_SelectDate.getMonth() + 1, v_SelectDate.getDate());
          if ( v_Lunar )
          {
              this.todayLunar = (v_Lunar.leapMonth == 1 ? '闰' : '') + v_Lunar.monthName + v_Lunar.dayName;
          }
          else
          {
              this.todayLunar = '';
          }
          
          this.todayYearName      = DateHelp.getYearName(v_SelectDate.getFullYear());
          this.todayYearGanZhi    = DateHelp.calcYearGan(v_SelectDate.getFullYear()) + DateHelp.calcYearZhi(v_SelectDate.getFullYear());
          this.todayLunar         = this.todayLunar;
          this.todayYear          = v_SelectDate.getFullYear();
          this.todayMonth         = v_SelectDate.getMonth() + 1;
          this.todayWeek          = this.weekNoToNames[v_SelectDate.getDay()];
          this.todayWeekNoOfYear  = this.todayWeekNoOfYear;
          this.todayNoOfYear      = this.todayNoOfYear;
          this.todayConstellation = DateHelp.calcConstellation(v_SelectDate.getFullYear(), v_SelectDate.getMonth() + 1, v_SelectDate.getDate());
          this.today              = v_SelectDate.getDate();
          this.nowYear            = v_Now.getFullYear();
          this.nowMonth           = v_Now.getMonth() + 1;
          this.nowDay             = v_Now.getDate();
          this.haveDayCount       = this.haveDayCount;
          
        },
        
        
        
        reLoadRefresh() 
        {
            this.reLoadRefreshTitle(new Date());
            
            
            if ( !this.selectedDate )
            {
                this.selectedDate = new Date();
            }
            this.setSelectDay(this.selectedDate);
    
            clearInterval(this.timerNo);
            this.timerNo = setInterval(this.timerChangeHoliday, 3500);
            this.holidayIndex = 0;
        },
        
        
        
        onLoad()
        {
            
        },



        onShow()
        {
            this.reLoadRefresh();
        }
    }
});





app.onLoad();
app.onShow();