"use strict";(self["webpackChunkPodcast2"]=self["webpackChunkPodcast2"]||[]).push([[445],{3445:function(t,e,i){i.r(e),i.d(e,{default:function(){return b}});var a=function(){var t=this,e=t._self._c;return e("div",[e("el-input",{staticStyle:{width:"20%"},attrs:{placeholder:"搜索订阅"},model:{value:t.searchKeyword,callback:function(e){t.searchKeyword=e},expression:"searchKeyword"}}),t._v("  "),e("el-button",{attrs:{type:"primary",icon:t.searchIng},on:{click:function(e){return t.subSearch()}}},[t._v("搜索")]),e("el-button",{attrs:{type:"primary"},on:{click:function(e){return t.getSubList()}}},[t._v("全部")]),e("br"),e("br"),e("el-dialog",{attrs:{title:"详细",visible:t.subDetail.visible,width:"30%"},on:{"update:visible":function(e){return t.$set(t.subDetail,"visible",e)}}},[e("el-form",{ref:"form",attrs:{model:t.subDetail.detail,"label-width":"auto"}},[e("el-form-item",{attrs:{label:"UUID"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.uuid)}}},[e("el-input",{model:{value:t.subDetail.detail.uuid,callback:function(e){t.$set(t.subDetail.detail,"uuid",e)},expression:"subDetail.detail.uuid"}})],1)]),e("el-form-item",{attrs:{label:"比对"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.equal)}}},[e("el-input",{model:{value:t.subDetail.detail.equal,callback:function(e){t.$set(t.subDetail.detail,"equal",e)},expression:"subDetail.detail.equal"}})],1)]),e("el-form-item",{attrs:{label:"名称"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.title)}}},[e("el-input",{model:{value:t.subDetail.detail.title,callback:function(e){t.$set(t.subDetail.detail,"title",e)},expression:"subDetail.detail.title"}})],1)]),e("el-form-item",{attrs:{label:"链接"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.link)}}},[e("el-input",{model:{value:t.subDetail.detail.link,callback:function(e){t.$set(t.subDetail.detail,"link",e)},expression:"subDetail.detail.link"}})],1)]),e("el-form-item",{attrs:{label:"状态码"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.status)}}},[e("el-input",{model:{value:t.subDetail.detail.status,callback:function(e){t.$set(t.subDetail.detail,"status",e)},expression:"subDetail.detail.status"}})],1)]),e("el-form-item",{attrs:{label:"描述"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.description)}}},[e("el-input",{model:{value:t.subDetail.detail.description,callback:function(e){t.$set(t.subDetail.detail,"description",e)},expression:"subDetail.detail.description"}})],1)]),e("el-form-item",{attrs:{label:"封面链接"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.image)}}},[e("el-input",{model:{value:t.subDetail.detail.image,callback:function(e){t.$set(t.subDetail.detail,"image",e)},expression:"subDetail.detail.image"}})],1)]),e("el-form-item",{attrs:{label:"创建时间"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.createTime)}}},[e("el-input",{model:{value:t.subDetail.detail.createTime,callback:function(e){t.$set(t.subDetail.detail,"createTime",e)},expression:"subDetail.detail.createTime"}})],1)]),e("el-form-item",{attrs:{label:"上次检查更新时间"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.checkTime)}}},[e("el-input",{model:{value:t.subDetail.detail.checkTime,callback:function(e){t.$set(t.subDetail.detail,"checkTime",e)},expression:"subDetail.detail.checkTime"}})],1)]),e("el-form-item",{attrs:{label:"上次更新时间"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.updateTime)}}},[e("el-input",{model:{value:t.subDetail.detail.updateTime,callback:function(e){t.$set(t.subDetail.detail,"updateTime",e)},expression:"subDetail.detail.updateTime"}})],1)]),e("el-form-item",{attrs:{label:"类型"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.type)}}},[e("el-input",{model:{value:t.subDetail.detail.type,callback:function(e){t.$set(t.subDetail.detail,"type",e)},expression:"subDetail.detail.type"}})],1)]),e("el-form-item",{attrs:{label:"存活时间"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.survivalTime)}}},[e("el-input",{model:{value:t.subDetail.detail.survivalTime,callback:function(e){t.$set(t.subDetail.detail,"survivalTime",e)},expression:"subDetail.detail.survivalTime"}})],1)]),e("el-form-item",{attrs:{label:"更新频率"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.cron)}}},[e("el-input",{model:{value:t.subDetail.detail.cron,callback:function(e){t.$set(t.subDetail.detail,"cron",e)},expression:"subDetail.detail.cron"}})],1)]),e("el-form-item",{attrs:{label:"插件名称"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.plugin)}}},[e("el-input",{model:{value:t.subDetail.detail.plugin,callback:function(e){t.$set(t.subDetail.detail,"plugin",e)},expression:"subDetail.detail.plugin"}})],1)]),e("el-form-item",{attrs:{label:"episodes"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.episodes)}}},[e("el-input",{model:{value:t.subDetail.detail.episodes,callback:function(e){t.$set(t.subDetail.detail,"episodes",e)},expression:"subDetail.detail.episodes"}})],1)]),e("el-form-item",{attrs:{label:"customEpisodes"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.customEpisodes)}}},[e("el-input",{model:{value:t.subDetail.detail.customEpisodes,callback:function(e){t.$set(t.subDetail.detail,"customEpisodes",e)},expression:"subDetail.detail.customEpisodes"}})],1)]),e("el-form-item",{attrs:{label:"继续更新"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.isUpdate)}}},[e("el-input",{model:{value:t.subDetail.detail.isUpdate,callback:function(e){t.$set(t.subDetail.detail,"isUpdate",e)},expression:"subDetail.detail.isUpdate"}})],1)]),e("el-form-item",{attrs:{label:"是否开启过滤"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.isFilter)}}},[e("el-input",{model:{value:t.subDetail.detail.isFilter,callback:function(e){t.$set(t.subDetail.detail,"isFilter",e)},expression:"subDetail.detail.isFilter"}})],1)]),e("el-form-item",{attrs:{label:"过滤最小时长"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.minDuration)}}},[e("el-input",{model:{value:t.subDetail.detail.minDuration,callback:function(e){t.$set(t.subDetail.detail,"minDuration",e)},expression:"subDetail.detail.minDuration"}})],1)]),e("el-form-item",{attrs:{label:"过滤最大时长"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.maxDuration)}}},[e("el-input",{model:{value:t.subDetail.detail.maxDuration,callback:function(e){t.$set(t.subDetail.detail,"maxDuration",e)},expression:"subDetail.detail.maxDuration"}})],1)]),e("el-form-item",{attrs:{label:"过滤标题"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.titleKeywords)}}},[e("el-input",{model:{value:t.subDetail.detail.titleKeywords,callback:function(e){t.$set(t.subDetail.detail,"titleKeywords",e)},expression:"subDetail.detail.titleKeywords"}})],1)]),e("el-form-item",{attrs:{label:"过滤描述"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.descKeywords)}}},[e("el-input",{model:{value:t.subDetail.detail.descKeywords,callback:function(e){t.$set(t.subDetail.detail,"descKeywords",e)},expression:"subDetail.detail.descKeywords"}})],1)]),e("el-form-item",{attrs:{label:"是否扩展"}},[e("div",{on:{click:function(e){return t.copy(t.subDetail.detail.isExtend)}}},[e("el-input",{model:{value:t.subDetail.detail.isExtend,callback:function(e){t.$set(t.subDetail.detail,"isExtend",e)},expression:"subDetail.detail.isExtend"}})],1)])],1)],1),e("el-table",{ref:"multipleTable",staticStyle:{width:"100%"},attrs:{data:t.subData,"tooltip-effect":"dark"},on:{"selection-change":t.handleSelectionChange}},[e("el-table-column",{attrs:{type:"selection",width:"55"}}),e("el-table-column",{attrs:{type:"index",width:"50"}}),e("el-table-column",{attrs:{label:"更新时间",width:"120",prop:"updateTime"}}),e("el-table-column",{attrs:{label:"频道名称",prop:"title","show-overflow-tooltip":""}}),e("el-table-column",{attrs:{label:"操作"},scopedSlots:t._u([{key:"default",fn:function(i){return[e("el-button",{attrs:{size:"mini",type:"success",plain:""},on:{click:function(e){return t.copyUrl(i.row.uuid)}}},[t._v("复制URL")]),e("el-button",{attrs:{size:"mini",type:"success",plain:""},on:{click:function(e){return t.qrcode(i.row.uuid)}}},[t._v("二维码")]),e("el-button",{attrs:{size:"mini",type:"danger",plain:""},on:{click:function(e){return t.batchDelete(i.row.uuid)}}},[t._v("删除")]),e("el-button",{attrs:{size:"mini",type:"primary",plain:""},on:{click:function(e){return t.getEditSubInfo(i.row.uuid)}}},[t._v("编辑")]),e("el-button",{attrs:{size:"mini",type:"primary",plain:""},on:{click:function(e){return t.subDetailShow(i.row.uuid)}}},[t._v("详细")])]}}])})],1),e("el-dialog",{attrs:{title:"二维码",visible:t.qrcodeVisible,width:"350px"},on:{"update:visible":function(e){t.qrcodeVisible=e}}},[e("div",[e("vue-qr",{attrs:{text:t.url,size:300}})],1),e("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[e("el-button",{on:{click:function(e){t.qrcodeVisible=!1}}},[t._v("取 消")]),e("el-button",{attrs:{type:"primary"},on:{click:function(e){return t.copyUrl("")}}},[t._v("复 制 URL")])],1)]),e("el-dialog",{attrs:{title:"二维码",visible:t.subGroupData.qrcodeVisible,width:"350px"},on:{"update:visible":function(e){return t.$set(t.subGroupData,"qrcodeVisible",e)}}},[e("div",[e("vue-qr",{attrs:{text:t.subGroupData.url+t.subGroupData.group,size:300}})],1),e("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[e("el-input",{attrs:{placeholder:"请输入组名"},model:{value:t.subGroupData.group,callback:function(e){t.$set(t.subGroupData,"group",e)},expression:"subGroupData.group"}}),e("br"),e("br"),e("el-button",{on:{click:function(e){t.subGroupData.qrcodeVisible=!1}}},[t._v("取 消")]),e("el-button",{attrs:{type:"primary"},on:{click:function(e){return t.copy(t.subGroupData.url+t.subGroupData.group)}}},[t._v("复 制 URL")])],1)]),e("div",{staticClass:"footer"},[e("el-button",{attrs:{type:"primary",size:"mini"},on:{click:function(e){t.addSubVisible=!0}}},[t._v("添加订阅")]),e("el-button",{attrs:{type:"primary",plain:"",size:"mini"},on:{click:t.toManager}},[t._v("管理")]),e("span",[t._v(t._s("\t"))]),e("el-dropdown",[e("el-button",{attrs:{type:"primary",size:"mini",plain:""}},[t._v(" 更多"),e("i",{staticClass:"el-icon-arrow-down el-icon--right"})]),e("el-dropdown-menu",{attrs:{slot:"dropdown"},slot:"dropdown"},[e("el-dropdown-item",{nativeOn:{click:function(e){return t.batchDelete("")}}},[t._v("批量删除")]),e("el-dropdown-item",{nativeOn:{click:function(e){return t.downloadOPML.apply(null,arguments)}}},[t._v("生成OPML")]),e("el-dropdown-item",{nativeOn:{click:function(e){return t.subGroup()}}},[t._v("订阅组")])],1)],1)],1),e("el-dialog",{directives:[{name:"loading",rawName:"v-loading",value:t.loading,expression:"loading"}],attrs:{title:"添加订阅",visible:t.addSubVisible,width:"audo"},on:{"update:visible":function(e){t.addSubVisible=e}}},[e("el-form",{ref:"form",attrs:{model:t.addSub,"label-width":"80px"}},[e("el-form-item",{attrs:{label:"主页链接"}},[e("el-input",{attrs:{placeholder:"请输入主页链接"},model:{value:t.addSub.url,callback:function(e){t.$set(t.addSub,"url",e)},expression:"addSub.url"}})],1),e("el-form-item",{attrs:{label:"类型"}},[e("el-select",{attrs:{placeholder:"请选择类型"},model:{value:t.addSub.type,callback:function(e){t.$set(t.addSub,"type",e)},expression:"addSub.type"}},[e("el-option",{attrs:{label:"视频",value:"Video"}}),e("el-option",{attrs:{label:"音频",value:"Audio"}})],1)],1),e("el-form-item",{attrs:{label:"存活时间"}},[e("el-select",{attrs:{placeholder:"请选择存活时间"},model:{value:t.addSub.survivalTime,callback:function(e){t.$set(t.addSub,"survivalTime",e)},expression:"addSub.survivalTime"}},[e("el-option",{attrs:{label:"1天",value:"1"}}),e("el-option",{attrs:{label:"3天",value:"3"}}),e("el-option",{attrs:{label:"7天",value:"7"}}),e("el-option",{attrs:{label:"15天",value:"15"}}),e("el-option",{attrs:{label:"30天",value:"30"}}),e("el-option",{attrs:{label:"永久",value:"-1"}})],1)],1),e("el-form-item",{attrs:{label:"更新频率"}},[e("el-select",{attrs:{placeholder:"请选择存活时间"},model:{value:t.addSub.cron,callback:function(e){t.$set(t.addSub,"cron",e)},expression:"addSub.cron"}},[e("el-option",{attrs:{label:"20分钟",value:"1200"}}),e("el-option",{attrs:{label:"30分钟",value:"1800"}}),e("el-option",{attrs:{label:"60分钟",value:"3600"}}),e("el-option",{attrs:{label:"2个小时",value:"7200"}}),e("el-option",{attrs:{label:"6个小时",value:"21600"}})],1)],1),e("el-form-item",{attrs:{label:"过滤器"}},[e("el-select",{model:{value:t.addSub.isFilter,callback:function(e){t.$set(t.addSub,"isFilter",e)},expression:"addSub.isFilter"}},[e("el-option",{attrs:{label:"禁用",value:"0"}}),e("el-option",{attrs:{label:"启用",value:"1"}})],1)],1),e("div",{directives:[{name:"show",rawName:"v-show",value:"1"==t.addSub.isFilter,expression:"addSub.isFilter == '1'"}]},[e("el-form-item",{attrs:{label:"最小时长"}},[e("el-input-number",{attrs:{min:-1},model:{value:t.addSub.minDuration,callback:function(e){t.$set(t.addSub,"minDuration",e)},expression:"addSub.minDuration"}},[t._v(" 秒")])],1),e("el-form-item",{attrs:{label:"最大时长"}},[e("el-input-number",{attrs:{min:-1},model:{value:t.addSub.maxDuration,callback:function(e){t.$set(t.addSub,"maxDuration",e)},expression:"addSub.maxDuration"}},[t._v(" 秒")])],1),e("el-form-item",{attrs:{label:"标题"}},[t._l(t.addSub.titleKeywords,(function(i){return e("el-tag",{key:i,attrs:{closable:"","disable-transitions":!1},on:{close:function(e){return t.filterHandleClose("title",i)}}},[t._v(" "+t._s(i)+" ")])})),t.addSub.titleInputVisible?e("el-input",{ref:"saveTagInput",staticClass:"input-new-tag",attrs:{size:"small"},on:{blur:function(e){return t.filterHandleInputConfirm("title")}},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.filterHandleInputConfirm("title")}},model:{value:t.addSub.titleInputValue,callback:function(e){t.$set(t.addSub,"titleInputValue",e)},expression:"addSub.titleInputValue"}}):e("el-button",{staticClass:"button-new-tag",attrs:{size:"small"},on:{click:function(e){return t.filterShowInput("title")}}},[t._v("+ 关键字")])],2),e("el-form-item",{attrs:{label:"描述"}},[t._l(t.addSub.descKeywords,(function(i){return e("el-tag",{key:i,attrs:{closable:"","disable-transitions":!1},on:{close:function(e){return t.filterHandleClose("desc",i)}}},[t._v(" "+t._s(i)+" ")])})),t.addSub.descInputVisible?e("el-input",{ref:"saveTagInput",staticClass:"input-new-tag",attrs:{size:"small"},on:{blur:function(e){return t.filterHandleInputConfirm("desc")}},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.filterHandleInputConfirm("desc")}},model:{value:t.addSub.descInputValue,callback:function(e){t.$set(t.addSub,"descInputValue",e)},expression:"addSub.descInputValue"}}):e("el-button",{staticClass:"button-new-tag",attrs:{size:"small"},on:{click:function(e){return t.filterShowInput("desc")}}},[t._v("+ 关键字")])],2)],1),e("el-form-item",{attrs:{label:"剧集选择"}},[e("el-select",{attrs:{placeholder:"请选择剧集"},model:{value:t.addSub.episodes,callback:function(e){t.$set(t.addSub,"episodes",e)},expression:"addSub.episodes"}},[e("el-option",{attrs:{label:"最新一集",value:"0"}}),e("el-option",{attrs:{label:"最近30集",value:"-1"}}),e("el-option",{attrs:{label:"自定义剧集",value:"1"}})],1)],1),e("div",{directives:[{name:"show",rawName:"v-show",value:"1"==t.addSub.episodes,expression:"addSub.episodes == '1'"}]},[e("el-form-item",{attrs:{label:"自定义"}},[e("el-input",{model:{value:t.addSub.customEpisodes,callback:function(e){t.$set(t.addSub,"customEpisodes",e)},expression:"addSub.customEpisodes"}})],1)],1),e("el-form-item",{attrs:{label:"更多选项"}},[e("el-button",{on:{click:t.getExtendList}},[t._v("更多选项")])],1),e("div",{directives:[{name:"show",rawName:"v-show",value:"1"==t.addSub.isExtend,expression:"addSub.isExtend == '1'"}]},[t._l(t.addSub.extendList.selectList,(function(i,a){return e("div",{key:i.id},[e("el-form-item",{attrs:{label:i.name}},[e("el-select",{model:{value:t.addSub.selectListData[a].content,callback:function(e){t.$set(t.addSub.selectListData[a],"content",e)},expression:"addSub.selectListData[selectIndex].content"}},[e("span",{directives:[{name:"show",rawName:"v-show",value:!1,expression:"false"}]},[t._v(t._s(t.addSub.selectListData[a].name=i.name))]),t._l(i.options,(function(t){return e("el-option",{key:t,attrs:{label:t,value:t}})}))],2)],1)],1)})),t._l(t.addSub.extendList.inputList,(function(i,a){return e("div",{key:i.id},[e("span",{directives:[{name:"show",rawName:"v-show",value:!1,expression:"false"}]},[t._v(t._s(t.addSub.inputListData[a].name=i.name))]),e("el-form-item",{attrs:{label:i.name}},[e("el-input",{model:{value:t.addSub.inputListData[a].content,callback:function(e){t.$set(t.addSub.inputListData[a],"content",e)},expression:"addSub.inputListData[inputIndex].content"}})],1)],1)}))],2)],1),e("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[e("el-button",{on:{click:function(e){t.addSubVisible=!1}}},[t._v("取 消")]),e("el-button",{attrs:{type:"primary",icon:t.addSubStatus},on:{click:t.addSubCommit}},[t._v("添加订阅")])],1)],1),e("el-dialog",{attrs:{title:t.editSubData.title,visible:t.editSubVisible,width:"audo"},on:{"update:visible":function(e){t.editSubVisible=e}}},[e("el-form",{ref:"form",attrs:{model:t.editSubData,"label-width":"80px"}},[e("el-form-item",{attrs:{label:"类型"}},[e("el-select",{attrs:{placeholder:"请选择类型"},model:{value:t.editSubData.type,callback:function(e){t.$set(t.editSubData,"type",e)},expression:"editSubData.type"}},[e("el-option",{attrs:{label:"视频",value:"Video"}}),e("el-option",{attrs:{label:"音频",value:"Audio"}})],1)],1),e("el-form-item",{attrs:{label:"存活时间"}},[e("el-select",{attrs:{placeholder:"请选择存活时间"},model:{value:t.editSubData.survivalTime,callback:function(e){t.$set(t.editSubData,"survivalTime",e)},expression:"editSubData.survivalTime"}},[e("el-option",{attrs:{label:"1天",value:"86400"}}),e("el-option",{attrs:{label:"3天",value:"259200"}}),e("el-option",{attrs:{label:"7天",value:"604800"}}),e("el-option",{attrs:{label:"15天",value:"1296000"}}),e("el-option",{attrs:{label:"30天",value:"2592000"}}),e("el-option",{attrs:{label:"永久",value:"-1"}})],1)],1),e("el-form-item",{attrs:{label:"更新频率"}},[e("el-select",{attrs:{placeholder:"请选择存活时间"},model:{value:t.editSubData.cron,callback:function(e){t.$set(t.editSubData,"cron",e)},expression:"editSubData.cron"}},[e("el-option",{attrs:{label:"20分钟",value:"1200"}}),e("el-option",{attrs:{label:"30分钟",value:"1800"}}),e("el-option",{attrs:{label:"60分钟",value:"3600"}}),e("el-option",{attrs:{label:"2个小时",value:"7200"}}),e("el-option",{attrs:{label:"6个小时",value:"21600"}})],1)],1),e("el-form-item",{attrs:{label:"继续更新"}},[e("el-select",{model:{value:t.editSubData.isUpdate,callback:function(e){t.$set(t.editSubData,"isUpdate",e)},expression:"editSubData.isUpdate"}},[e("el-option",{attrs:{label:"是",value:"1"}}),e("el-option",{attrs:{label:"否",value:"0"}})],1)],1),e("el-form-item",{attrs:{label:"过滤器"}},[e("el-select",{model:{value:t.editSubData.isFilter,callback:function(e){t.$set(t.editSubData,"isFilter",e)},expression:"editSubData.isFilter"}},[e("el-option",{attrs:{label:"禁用",value:"0"}}),e("el-option",{attrs:{label:"启用",value:"1"}})],1)],1),e("div",{directives:[{name:"show",rawName:"v-show",value:"1"==t.editSubData.isFilter,expression:"editSubData.isFilter == '1'"}]},[e("el-form-item",{attrs:{label:"最小时长"}},[e("el-input-number",{attrs:{min:-1},model:{value:t.editSubData.minDuration,callback:function(e){t.$set(t.editSubData,"minDuration",e)},expression:"editSubData.minDuration"}},[t._v(" 秒")])],1),e("el-form-item",{attrs:{label:"最大时长"}},[e("el-input-number",{attrs:{min:-1},model:{value:t.editSubData.maxDuration,callback:function(e){t.$set(t.editSubData,"maxDuration",e)},expression:"editSubData.maxDuration"}},[t._v(" 秒")])],1),e("el-form-item",{attrs:{label:"标题"}},[t._l(t.editSubData.titleKeywords,(function(i){return e("el-tag",{key:i,attrs:{closable:"","disable-transitions":!1},on:{close:function(e){return t.editFilterHandleClose("title",i)}}},[t._v(" "+t._s(i)+" ")])})),t.editSubData.titleInputVisible?e("el-input",{ref:"saveTagInput",staticClass:"input-new-tag",attrs:{size:"small"},on:{blur:function(e){return t.editFilterHandleInputConfirm("title")}},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.editFilterHandleInputConfirm("title")}},model:{value:t.editSubData.titleInputValue,callback:function(e){t.$set(t.editSubData,"titleInputValue",e)},expression:"editSubData.titleInputValue"}}):e("el-button",{staticClass:"button-new-tag",attrs:{size:"small"},on:{click:function(e){return t.editFilterShowInput("title")}}},[t._v("+ 关键字")])],2),e("el-form-item",{attrs:{label:"描述"}},[t._l(t.editSubData.descKeywords,(function(i){return e("el-tag",{key:i,attrs:{closable:"","disable-transitions":!1},on:{close:function(e){return t.editFilterHandleClose("desc",i)}}},[t._v(" "+t._s(i)+" ")])})),t.editSubData.descInputVisible?e("el-input",{ref:"saveTagInput",staticClass:"input-new-tag",attrs:{size:"small"},on:{blur:function(e){return t.editFilterHandleInputConfirm("desc")}},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.editFilterHandleInputConfirm("desc")}},model:{value:t.editSubData.descInputValue,callback:function(e){t.$set(t.editSubData,"descInputValue",e)},expression:"editSubData.descInputValue"}}):e("el-button",{staticClass:"button-new-tag",attrs:{size:"small"},on:{click:function(e){return t.editFilterShowInput("desc")}}},[t._v("+ 关键字")])],2)],1),"1"==t.editSubData.isExtend?e("div",[t._l(t.editSubData.extendList.selectList,(function(i,a){return e("div",{key:i.id},[e("el-form-item",{attrs:{label:i.name}},[e("el-select",{model:{value:t.editSubData.selectListData[a].content,callback:function(e){t.$set(t.editSubData.selectListData[a],"content",e)},expression:"editSubData.selectListData[selectIndex].content"}},[e("span",{directives:[{name:"show",rawName:"v-show",value:!1,expression:"false"}]},[t._v(t._s(t.editSubData.selectListData[a].name=i.name))]),t._l(i.options,(function(t){return e("el-option",{key:t,attrs:{label:t,value:t}})}))],2)],1)],1)})),t._l(t.editSubData.extendList.inputList,(function(i,a){return e("div",{key:i.id},[e("span",{directives:[{name:"show",rawName:"v-show",value:!1,expression:"false"}]},[t._v(t._s(t.editSubData.inputListData[a].name=i.name))]),e("el-form-item",{attrs:{label:i.name}},[e("el-input",{model:{value:t.editSubData.inputListData[a].content,callback:function(e){t.$set(t.editSubData.inputListData[a],"content",e)},expression:"editSubData.inputListData[inputIndex].content"}})],1)],1)}))],2):t._e()],1),e("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[e("el-button",{on:{click:function(e){t.editSubVisible=!1}}},[t._v("取 消")]),e("el-button",{attrs:{type:"primary"},on:{click:t.editSubCommit}},[t._v("修改")])],1)],1)],1)},l=[],s=(i(4114),i(4603),i(7566),i(8721),i(8355)),n=i(9323),u=i.n(n),o={components:{VueQr:u()},data(){return{searchKeyword:"",searchIng:"",subData:[],qrcodeVisible:!1,addSubVisible:!1,editSubVisible:!1,url:"",multipleSelection:[],delele:[],addSub:{url:"",type:"Audio",survivalTime:"7",cron:"1200",plugin:"",episodes:"0",customEpisodes:"",isUpdate:"1",isFilter:"0",maxDuration:-1,minDuration:-1,titleKeywords:[],descKeywords:[],isExtend:"0",inputAndSelectDataList:[],titleInputValue:"",titleInputVisible:!1,descInputValue:"",descInputVisible:!1,extendList:{inputList:[],selectList:[]},inputListData:[],selectListData:[]},initAddSub:{url:"",type:"Audio",survivalTime:"7",cron:"1200",plugin:"",episodes:"0",customEpisodes:"",isUpdate:"1",isFilter:"0",maxDuration:-1,minDuration:-1,titleKeywords:[],descKeywords:[],isExtend:"0",inputAndSelectDataList:[],titleInputValue:"",titleInputVisible:!1,descInputValue:"",descInputVisible:!1,extendList:{inputList:[],selectList:[]},inputListData:[],selectListData:[]},editSubData:{title:"",type:"",survivalTime:"",cron:"",isUpdate:"",isFilter:"",maxDuration:"",minDuration:"",titleKeywords:[],descKeywords:[],isExtend:"",extendList:"",inputListData:[],selectListData:[],titleInputValue:"",titleInputVisible:"",descInputValue:"",descInputVisible:""},initEditSubData:{title:"",type:"",survivalTime:"",cron:"",isUpdate:"",isFilter:"",maxDuration:"",minDuration:"",titleKeywords:[],descKeywords:[],isExtend:"",extendList:"",inputListData:[],selectListData:[],titleInputValue:"",titleInputVisible:"",descInputValue:"",descInputVisible:""},loading:!1,addSubStatus:"",subDetail:{detail:"",visible:!1},subGroupData:{url:"",group:"",qrcodeVisible:!1,uuids:[]}}},mounted(){this.getSubList()},methods:{getSubList(){var t=this;(0,s.A)({method:"get",url:"/sub/list"}).then((function(e){t.subData=e.data.data}))},toggleSelection(t){t?t.forEach((t=>{this.$refs.multipleTable.toggleRowSelection(t)})):this.$refs.multipleTable.clearSelection()},handleSelectionChange(t){this.multipleSelection=t},copyUrl(t){const e=document.createElement("textarea");null==t||""==t?(e.value=this.url,this.qrcodeVisible=!1):e.value=window.location.protocol+"//"+window.location.host+"/sub/xml/"+t,e.setAttribute("readonly",""),e.style.position="absolute",e.style.left="-9999px",document.body.appendChild(e),e.select(),document.execCommand("copy"),document.body.removeChild(e),setTimeout((()=>{this.$message({message:"复制成功！",type:"success"})}),100),console.log("内容已成功复制到剪贴板")},qrcode(t){this.url=window.location.protocol+"//"+window.location.host+"/sub/xml/"+t,this.qrcodeVisible=!0},batchDelete(t){if(null==t||""==t){if(0==this.multipleSelection.length)return void this.$message({message:"请先选择！",type:"warning"});this.delele=[];for(var e=0;e<this.multipleSelection.length;e++)this.delele.push(this.multipleSelection[e].uuid)}else this.delele=[],this.delele.push(t);this.$confirm("此操作将永久删除选择的订阅, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{s.A.delete("/sub?uuids="+this.delele).then((t=>{"1"==t.data.code?(this.$message.success("删除成功"),this.getSubList()):"0"==t.data.code&&this.$message.error("删除失败")})).catch((t=>{this.$message.error("未知错误"),console.log(t)}))})).catch((()=>{this.$message.info("已取消删除")}))},addSubCommit(){if("el-icon-loading"==this.addSubStatus)return void this.$message.warning("正在添加中...");this.addSubStatus="el-icon-loading";for(let e=0;e<this.addSub.inputListData.length;e++)this.addSub.inputAndSelectDataList.push(this.addSub.inputListData[e]);for(let e=0;e<this.addSub.selectListData.length;e++)this.addSub.inputAndSelectDataList.push(this.addSub.selectListData[e]);const t=/(http|https):\/\/([\w.]+\/?)\S*/;if(!t.test(this.addSub.url))return this.$message.error("请输入正确的主页链接！"),void(this.addSubStatus="");if(this.addSub.plugin=new URL(this.addSub.url).hostname,"1"==this.addSub.episodes){const t=/^([1-9])(?!.*\b\1\b)(?:[0-9]{0,2}|[1-9])(?!,$)/;if(null==this.addSub.customEpisodes||""==this.addSub.customEpisodes||!t.test(this.addSub.customEpisodes))return this.$message.error("请输入正确的自定义剧集"),void(this.addSubStatus="")}s.A.post("/sub/add",this.addSub).then((t=>{"1"==t.data.code?(this.$message.success("添加成功"),this.addSubVisible=!1,this.addSub=this.initAddSub,this.addSubStatus="",this.getSubList()):"0"==t.data.code&&(this.$message.error(t.data.msg),this.addSubStatus="")})).catch((t=>{this.addSubStatus="",this.$message.error("未知错误"),console.log(t)}))},toManager(){this.$router.push("/manager")},getExtendList(){if("0"==this.addSub.isExtend){this.loading=!0;let t=this.addSub.url,e=this.addSub.type;const i=/(http|https):\/\/([\w.]+\/?)\S*/;if(null==t||""==t||!i.test(t))return void this.$message.error("请输入正确的主页链接！");let a=new URL(t).hostname;s.A.get("/sub/extendList",{params:{plugin:a,url:t,type:e}}).then((t=>{if("1"==t.data.code){let e=t.data.data.extendList.inputList.length>0,i=t.data.data.extendList.selectList.length>0,a=t.data.data.inputListData.length>0,l=t.data.data.selectListData.length>0;e||a||l||i?(this.addSub.extendList=t.data.data.extendList,this.addSub.inputListData=t.data.data.inputListData,this.addSub.selectListData=t.data.data.selectListData,this.addSub.isExtend="1"):this.$message.info("暂无扩展选项！")}else"0"==t.data.code&&this.$message.error(t.data.msg)})).catch((t=>{console.log(t),this.$message.error("未知错误！")})),this.loading=!1}else this.addSub.isExtend="0"},downloadOPML(){if(0!=this.multipleSelection.length){let t='<?xml version="1.0" encoding="UTF-8"?>\n';t+='<opml version="1.0">\n',t+="  <head>\n",t+="    <title>OPML</title>\n",t+="  </head>\n",t+="  <body>\n";for(let l=0;l<this.multipleSelection.length;l++)t+='    <outline type="rss"  xmlUrl="'+window.location.protocol+"//"+window.location.host+"/sub/xml/"+this.multipleSelection[l].uuid+'" />\n';t+="  </body>\n",t+="</opml>\n";const e=new Blob([t],{type:"text/plain"}),i=URL.createObjectURL(e),a=document.createElement("a");a.href=i,a.setAttribute("download","opml.opml"),document.body.appendChild(a),a.click()}else this.$message.error("不能为空！请先选择！")},getEditSubInfo(t){this.editSubData=this.initEditSubData,console.log(t),this.editSubVisible=!0,s.A.get("/sub/edit/"+t).then((t=>{"1"==t.data.code?(this.editSubData=t.data.data,console.log(this.editSubData),this.editSubData.survivalTime=""+t.data.data.survivalTime,this.editSubData.cron=""+t.data.data.cron,this.editSubData.isUpdate=""+t.data.data.isUpdate,this.editSubData.isFilter=""+t.data.data.isFilter,this.editSubData.isExtend=""+t.data.data.isExtend):"0"==t.data.code&&this.$message.error("编辑错误！")})).catch((t=>{console.log(t),this.$message.error("未知错误！")}))},editSubCommit(){s.A.put("/sub",this.editSubData).then((t=>{"1"==t.data.code?(this.$message.success("编辑成功！"),this.getSubList(),this.editSubVisible=!1,this.editSubData=this.initEditSubData):"0"==t.data.code&&this.$message.error(t.data.msg)})).catch((t=>{console.log(t),this.$message.error("未知错误！")}))},filterHandleClose(t,e){this.addSub[t+"Keywords"].splice(this.addSub[t+"Keywords"].indexOf(e),1)},filterShowInput(t){this.addSub[t+"InputVisible"]=!0,this.$nextTick((t=>{console.log(t),this.$refs.saveTagInput.$refs.input.focus()}))},filterHandleInputConfirm(t){let e=this.addSub[t+"InputValue"];e&&this.addSub[t+"Keywords"].push(e),this.addSub[t+"InputVisible"]=!1,this.addSub[t+"InputValue"]=""},editFilterHandleClose(t,e){this.editSubData[t+"Keywords"].splice(this.addSub[t+"Keywords"].indexOf(e),1)},editFilterShowInput(t){this.editSubData[t+"InputVisible"]=!0,this.$nextTick((t=>{console.log(t),this.$refs.saveTagInput.$refs.input.focus()}))},editFilterHandleInputConfirm(t){let e=this.editSubData[t+"InputValue"];e&&this.editSubData[t+"Keywords"].push(e),this.editSubData[t+"InputVisible"]=!1,this.editSubData[t+"InputValue"]=""},subSearch(){this.searchIng="el-icon-loading",s.A.get("/sub/search?keywords="+this.searchKeyword).then((t=>{"1"==t.data.code?(this.subData=t.data.data,this.searchIng=""):(this.$message.error(t.data.msg),this.searchIng="")})).catch((t=>{this.$message.error(t),this.searchIng=""}))},subDetailShow(t){this.subDetail.detail={},this.subDetail.visible=!0,s.A.get("/sub/detail/"+t).then((t=>{"1"==t.data.code?this.subDetail.detail=t.data.data:this.$message.error(t.data.msg)})).catch((t=>{this.$message.error("获取订阅详细信息失败！"),console.log(t)}))},copy(t){const e=document.createElement("textarea");console.log("复制到粘贴板"),e.value=t,e.setAttribute("readonly",""),e.style.position="absolute",e.style.left="-9999px",document.body.appendChild(e),e.select(),document.execCommand("copy"),document.body.removeChild(e),setTimeout((()=>{this.$message({message:"复制成功！",type:"success"})}),100),console.log("内容已成功复制到剪贴板")},subGroup(){if(0!=this.multipleSelection.length){for(var t=0;t<this.multipleSelection.length;t++)this.subGroupData.uuids.push(this.multipleSelection[t].uuid);this.subGroupData.url=window.location.protocol+"//"+window.location.host+"/sub/xml?uuids="+this.subGroupData.uuids+"&group=",this.subGroupData.qrcodeVisible=!0}else this.$message({message:"请先选择！",type:"warning"})}}},d=o,r=i(1656),c=(0,r.A)(d,a,l,!1,null,"4b8d8cc0",null),b=c.exports}}]);
//# sourceMappingURL=445.84354355.js.map