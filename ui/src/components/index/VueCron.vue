<!-- 修改https://github.com/1615450788/vue-cron -->
<style scoped>
#changeContab .language {
    position: absolute;
    right: 25px;
    z-index: 1;
}

#changeContab .el-tabs {
    box-shadow: none;
}

#changeContab .tabBody .el-row {
    margin: 10px 0;
}

#changeContab .tabBody .el-row .long .el-select {
    width: 350px;
}

#changeContab .tabBody .el-row .el-input-number {
    width: 110px;
}

#changeContab .bottom {
    width: 100%;
    text-align: center;
    margin-top: 5px;
    position: relative;
}

#changeContab .bottom .value {
    font-size: 18px;
    vertical-align: middle;
}
</style>
<template>
    <div id="changeContab">
        <p class="language" >Unix-Cron</p>
        <el-tabs type="border-card">
            <el-tab-pane>
                <span slot="label"><i class="el-icon-date"></i> {{text.Minutes.name}}</span>
                <div class="tabBody">
                    <el-row>
                        <el-radio v-model="minute.cronEvery" label="1">{{text.Minutes.every}}</el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="minute.cronEvery" label="2">{{text.Minutes.interval[0]}}
                            <el-input-number size="small" v-model="minute.incrementIncrement" :min="1" :max="60"></el-input-number>
                            {{text.Minutes.interval[1]}}
                            <el-input-number size="small" v-model="minute.incrementStart" :min="0" :max="59"></el-input-number>
                            {{text.Minutes.interval[2]||''}}
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio class="long" v-model="minute.cronEvery" label="3">{{text.Minutes.specific}}
                            <el-select size="small" multiple v-model="minute.specificSpecific">
                                <el-option v-for="val in 60" :key="val" :value="val-1">{{val-1}}</el-option>
                            </el-select>
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="minute.cronEvery" label="4">{{text.Minutes.cycle[0]}}
                            <el-input-number size="small" v-model="minute.rangeStart" :min="1" :max="60"></el-input-number>
                            {{text.Minutes.cycle[1]}}
                            <el-input-number size="small" v-model="minute.rangeEnd" :min="0" :max="59"></el-input-number>
                            {{text.Minutes.cycle[2]}}
                        </el-radio>
                    </el-row>
                </div>
            </el-tab-pane>
            <el-tab-pane>
                <span slot="label"><i class="el-icon-date"></i> {{text.Hours.name}}</span>
                <div class="tabBody">
                    <el-row>
                        <el-radio v-model="hour.cronEvery" label="1">{{text.Hours.every}}</el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="hour.cronEvery" label="2">{{text.Hours.interval[0]}}
                            <el-input-number size="small" v-model="hour.incrementIncrement" :min="0" :max="23"></el-input-number>
                            {{text.Hours.interval[1]}}
                            <el-input-number size="small" v-model="hour.incrementStart" :min="0" :max="23"></el-input-number>
                            {{text.Hours.interval[2]}}
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio class="long" v-model="hour.cronEvery" label="3">{{text.Hours.specific}}
                            <el-select size="small" multiple v-model="hour.specificSpecific">
                                <el-option v-for="val in 24" :key="val" :value="val-1">{{val-1}}</el-option>
                            </el-select>
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="hour.cronEvery" label="4">{{text.Hours.cycle[0]}}
                            <el-input-number size="small" v-model="hour.rangeStart" :min="0" :max="23"></el-input-number>
                            {{text.Hours.cycle[1]}}
                            <el-input-number size="small" v-model="hour.rangeEnd" :min="0" :max="23"></el-input-number>
                            {{text.Hours.cycle[2]}}
                        </el-radio>
                    </el-row>
                </div>
            </el-tab-pane>
            <el-tab-pane>
                <span slot="label"><i class="el-icon-date"></i> {{text.Day.name}}</span>
                <div class="tabBody">
                    <el-row>
                        <el-radio v-model="day.cronEvery" label="1">{{text.Day.every}}</el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="day.cronEvery" label="2">{{text.Day.intervalWeek[0]}}
                            <el-input-number size="small" v-model="week.incrementIncrement" :min="1" :max="7"></el-input-number>
                            {{text.Day.intervalWeek[1]}}
                            <el-select size="small" v-model="week.incrementStart">
                                <el-option v-for="val in 7" :key="val" :label="text.Week[val-1]" :value="val"></el-option>
                            </el-select>
                            {{text.Day.intervalWeek[2]}}
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="day.cronEvery" label="3">{{text.Day.intervalDay[0]}}
                            <el-input-number size="small" v-model="day.incrementIncrement" :min="1" :max="31"></el-input-number>
                            {{text.Day.intervalDay[1]}}
                            <el-input-number size="small" v-model="day.incrementStart" :min="1" :max="31"></el-input-number>
                            {{text.Day.intervalDay[2]}}
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio class="long" v-model="day.cronEvery" label="4">{{text.Day.specificWeek}}
                            <el-select size="small" multiple v-model="week.specificSpecific">
                                <el-option v-for="val in 7"
                                           :key="val"
                                           :label="text.Week[val-1]"
                                           :value="['SUN','MON','TUE','WED','THU','FRI','SAT'][val-1]"
                                ></el-option>
                            </el-select>
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio class="long" v-model="day.cronEvery" label="5">{{text.Day.specificDay}}
                            <el-select size="small" multiple v-model="day.specificSpecific">
                                <el-option v-for="val in 31" :key="val" :value="val">{{val}}</el-option>
                            </el-select>
                        </el-radio>
                    </el-row>
                </div>
            </el-tab-pane>
            <el-tab-pane>
                <span slot="label"><i class="el-icon-date"></i> {{text.Month.name}}</span>
                <div class="tabBody">
                    <el-row>
                        <el-radio v-model="month.cronEvery" label="1">{{text.Month.every}}</el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="month.cronEvery" label="2">{{text.Month.interval[0]}}
                            <el-input-number size="small" v-model="month.incrementIncrement" :min="0" :max="12"></el-input-number>
                            {{text.Month.interval[1]}}
                            <el-input-number size="small" v-model="month.incrementStart" :min="0" :max="12"></el-input-number>
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio class="long" v-model="month.cronEvery" label="3">{{text.Month.specific}}
                            <el-select size="small" multiple v-model="month.specificSpecific">
                                <el-option v-for="val in 12" :key="val" :label="val" :value="val"></el-option>
                            </el-select>
                        </el-radio>
                    </el-row>
                    <el-row>
                        <el-radio v-model="month.cronEvery" label="4">{{text.Month.cycle[0]}}
                            <el-input-number size="small" v-model="month.rangeStart" :min="1" :max="12"></el-input-number>
                            {{text.Month.cycle[1]}}
                            <el-input-number size="small" v-model="month.rangeEnd" :min="1" :max="12"></el-input-number>
                        </el-radio>
                    </el-row>
                </div>
            </el-tab-pane>
        </el-tabs>
        <div class="bottom">
            <span class="value">{{this.cron}}</span>
            <el-button type="primary" @click="change">{{text.Save}}</el-button>
            <el-button type="primary" @click="close">{{text.Close}}</el-button>
        </div>
    </div>
</template>
<script>
    export default {
    name:'vueCron',
    props:['data','expression'],
    data(){
    return {
        minute: {
            cronEvery: '',
            incrementStart: '3',
            incrementIncrement: '5',
            rangeStart: '',
            rangeEnd: '',
            specificSpecific: [],
        },
        hour: {
            cronEvery: '',
            incrementStart: '3',
            incrementIncrement: '5',
            rangeStart: '',
            rangeEnd: '',
            specificSpecific: [],
        },
        day: {
            cronEvery: '',
            incrementStart: '1',
            incrementIncrement: '1',
            rangeStart: '',
            rangeEnd: '',
            specificSpecific: [],
            cronLastSpecificDomDay: 1,
            cronDaysBeforeEomMinus: '',
            cronDaysNearestWeekday: '',
        },
        week: {
            cronEvery: '',
            incrementStart: '1',
            incrementIncrement: '1',
            specificSpecific: [],
            cronNthDayDay: 1,
            cronNthDayNth: '1',
        },
        month: {
            cronEvery: '',
            incrementStart: '3',
            incrementIncrement: '5',
            rangeStart: '',
            rangeEnd: '',
            specificSpecific: [],
        },
        text: {
            Seconds: {
                name: '秒',
                every: '每一秒钟',
                interval: ['每隔', '秒执行 从', '秒开始'],
                specific: '具体秒数(可多选)',
                cycle: ['周期从', '到', '秒']
            },
            Minutes: {
                name: '分',
                every: '每一分钟',
                interval: ['每隔', '分执行 从', '分开始'],
                specific: '具体分钟数(可多选)',
                cycle: ['周期从', '到', '分']
            },
            Hours: {
                name: '时',
                every: '每一小时',
                interval: ['每隔', '小时执行 从', '小时开始'],
                specific: '具体小时数(可多选)',
                cycle: ['周期从', '到', '小时']
            },
            Day: {
                name: '天',
                every: '每一天',
                intervalWeek: ['每隔', '周执行 从', '开始'],
                intervalDay: ['每隔', '天执行 从', '天开始'],
                specificWeek: '具体星期几(可多选)',
                specificDay: '具体天数(可多选)',
                lastDay: '在这个月的最后一天',
                lastWeekday: '在这个月的最后一个工作日',
                lastWeek: ['在这个月的最后一个'],
                beforeEndMonth: ['在本月底前', '天'],
                nearestWeekday: ['最近的工作日（周一至周五）至本月', '日'],
                someWeekday: ['在这个月的第', '个'],
            },
            Week: ['天', '一', '二', '三', '四', '五', '六'].map(val => '星期' + val),
            Month: {
                name: '月',
                every: '每一月',
                interval: ['每隔', '月执行 从', '月开始'],
                specific: '具体月数(可多选)',
                cycle: ['从', '到', '月之间的每个月']
            },
            Year: {
                name: '年',
                every: '每一年',
                interval: ['每隔', '年执行 从', '年开始'],
                specific: '具体年份(可多选)',
                cycle: ['从', '到', '年之间的每一年']
            },
            Save: '保存',
            Close: '关闭'
        }
    }
},
    watch:{
        data(){
            this.rest(this.$data);
        }
    },
    computed: {
        minutesText() {
            let minutes = '';
            let cronEvery=this.minute.cronEvery;
            switch (cronEvery.toString()){
                case '1':
                    minutes = '*';
                    break;
                case '2':
                    minutes = this.minute.incrementStart+'/'+this.minute.incrementIncrement;
                    break;
                case '3':
                    this.minute.specificSpecific.map(val=> {
                        minutes += val+','
                    });
                    minutes = minutes.slice(0, -1);
                    break;
                case '4':
                    minutes = this.minute.rangeStart+'-'+this.minute.rangeEnd;
                    break;
            }
            return minutes;
        },
        hoursText() {
            let hours = '';
            let cronEvery=this.hour.cronEvery;
            switch (cronEvery.toString()){
                case '1':
                    hours = '*';
                    break;
                case '2':
                    hours = this.hour.incrementStart+'/'+this.hour.incrementIncrement;
                    break;
                case '3':
                    this.hour.specificSpecific.map(val=> {
                        hours += val+','
                    });
                    hours = hours.slice(0, -1);
                    break;
                case '4':
                    hours = this.hour.rangeStart+'-'+this.hour.rangeEnd;
                    break;
            }
            return hours;
        },
        daysText() {
            let days='';
            let cronEvery=this.day.cronEvery;
            switch (cronEvery.toString()){
                case '1':
                    break;
                case '2':
                    break;
                case '4':
                    days = "*";
                    break;
                case '3':
                    days = this.day.incrementStart+'/'+this.day.incrementIncrement;
                    break;
                case '5':
                    this.day.specificSpecific.map(val=> {
                        days += val+','
                    });
                    days = days.slice(0, -1);
                    break;
            }
            return days;
        },
        weeksText() {
            let weeks = '';
            let cronEvery=this.day.cronEvery;
            switch (cronEvery.toString()){
                case '1':
                case '3':
                case '5':
                    weeks = '*';
                    break;
                case '2':
                    weeks = this.week.incrementStart+'/'+this.week.incrementIncrement;
                    break;
                case '4':
                    this.week.specificSpecific.map(val=> {
                        weeks += val+','
                    });
                    weeks = weeks.slice(0, -1);
                    break;
            }
            return weeks;
        },
        monthsText() {
            let months = '';
            let cronEvery=this.month.cronEvery;
            switch (cronEvery.toString()){
                case '1':
                    months = '*';
                    break;
                case '2':
                    months = this.month.incrementStart+'/'+this.month.incrementIncrement;
                    break;
                case '3':
                    this.month.specificSpecific.map(val=> {
                        months += val+','
                    });
                    months = months.slice(0, -1);
                    break;
                case '4':
                    months = this.month.rangeStart+'-'+this.month.rangeEnd;
                    break;
            }
            return months;
        },
        cron(){
            return `${this.minutesText||'*'} ${this.hoursText||'*'} ${this.daysText||'*'} ${this.monthsText||'*'} ${this.weeksText||'*'}`
        },
    },
    methods: {
        getValue(){
            return this.cron;
        },
        change(){
            this.$emit('change',this.cron);
            this.close();
        },
        close(){
            this.$emit('close')
        },
        rest(data){
            for(let i in data){
                if(data[i] instanceof Object){
                    this.rest(data[i])
                }else{
                    switch(typeof data[i]){
                        case 'object':data[i]=[];break;
                        case 'string':data[i]='';break;
                    }
                }
            }
        }
    }
}</script>