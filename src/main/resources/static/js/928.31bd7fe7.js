"use strict";(self["webpackChunkPodcast2"]=self["webpackChunkPodcast2"]||[]).push([[928],{3928:function(e,t,s){s.r(t),s.d(t,{default:function(){return d}});var a=function(){var e=this,t=e._self._c;return t("div",{attrs:{id:"app"}},[t("div",{staticClass:"sidebar"},[t("el-menu",{staticClass:"el-menu-vertical-demo",attrs:{"default-active":e.activeMenu},on:{select:e.handleMenuSelect}},[t("el-submenu",{attrs:{index:"system"}},[t("template",{slot:"title"},[e._v("项目")]),t("el-menu-item",{attrs:{index:"info"}},[e._v("概况")]),t("el-menu-item",{attrs:{index:"logs"}},[e._v("日志")])],2),t("el-submenu",{attrs:{index:"user"}},[t("template",{slot:"title"},[e._v("用户")]),t("el-menu-item",{attrs:{index:"changePassword"}},[e._v("修改")]),t("el-menu-item",{attrs:{index:"dataMigration"}},[e._v("数据迁移")]),t("el-menu-item",{attrs:{index:"customDomainName"}},[e._v("自定义域名")]),t("el-menu-item",{attrs:{index:"certificates"}},[e._v("开启https")])],2),t("el-submenu",{attrs:{index:"download"}},[t("template",{slot:"title"},[e._v("下载")]),t("el-menu-item",{attrs:{index:"Downloading"}},[e._v("正在下载 "),e.download.progress.length>0?t("span",[e._v(e._s(e.download.progress.length))]):e._e()]),t("el-menu-item",{attrs:{index:"Done"}},[e._v("已完成 "),e.download.done.length>0?t("span",[e._v(e._s(e.download.done.length))]):e._e()]),t("el-menu-item",{attrs:{index:"DownloaderInfo"}},[e._v("下载器信息")])],2),t("el-submenu",{attrs:{index:"plugin"}},[t("template",{slot:"title"},[e._v("插件")]),t("el-menu-item",{attrs:{index:"pluginSettings"}},[e._v("插件设置")]),t("el-menu-item",{attrs:{index:"pluginList"}},[e._v("插件列表")]),t("el-menu-item",{attrs:{index:"pluginUpdate"}},[e._v("插件更新")])],2),t("el-menu-item",{on:{click:e.toSubList}},[e._v("订阅列表")]),t("el-menu-item",{attrs:{index:"about"}},[e._v("关于")]),t("el-menu-item",{attrs:{index:"logout"},on:{click:function(t){return e.logout()}}},[e._v("退出")])],1),t("el-dialog",{attrs:{title:"详细",visible:e.download.downloadDetailVisible,width:"30%"},on:{"update:visible":function(t){return e.$set(e.download,"downloadDetailVisible",t)}}},[t("el-form",{ref:"form",attrs:{model:e.download.detail,"label-width":"auto"}},[t("el-form-item",{attrs:{label:"下载时间"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.createTime)}}},[t("el-input",{model:{value:e.download.detail.createTime,callback:function(t){e.$set(e.download.detail,"createTime",t)},expression:"download.detail.createTime"}})],1)]),t("el-form-item",{attrs:{label:"下载状态"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.status)}}},[t("el-input",{model:{value:e.download.detail.status,callback:function(t){e.$set(e.download.detail,"status",t)},expression:"download.detail.status"}})],1)]),t("el-form-item",{attrs:{label:"下载器"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.downloaderName)}}},[t("el-input",{model:{value:e.download.detail.downloaderName,callback:function(t){e.$set(e.download.detail,"downloaderName",t)},expression:"download.detail.downloaderName"}})],1)]),t("el-form-item",{attrs:{label:"下载器版本"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.downloaderVersion)}}},[t("el-input",{model:{value:e.download.detail.downloaderVersion,callback:function(t){e.$set(e.download.detail,"downloaderVersion",t)},expression:"download.detail.downloaderVersion"}})],1)]),t("el-form-item",{attrs:{label:"文件名称"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.fileName)}}},[t("el-input",{model:{value:e.download.detail.fileName,callback:function(t){e.$set(e.download.detail,"fileName",t)},expression:"download.detail.fileName"}})],1)]),t("el-form-item",{attrs:{label:"频道名称"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.channelName)}}},[t("el-input",{model:{value:e.download.detail.channelName,callback:function(t){e.$set(e.download.detail,"channelName",t)},expression:"download.detail.channelName"}})],1)]),t("el-form-item",{attrs:{label:"节目标题"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.itemTitle)}}},[t("el-input",{model:{value:e.download.detail.itemTitle,callback:function(t){e.$set(e.download.detail,"itemTitle",t)},expression:"download.detail.itemTitle"}})],1)]),t("el-form-item",{attrs:{label:"节目链接"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.itemLink)}}},[t("el-input",{model:{value:e.download.detail.itemLink,callback:function(t){e.$set(e.download.detail,"itemLink",t)},expression:"download.detail.itemLink"}})],1)]),t("el-form-item",{attrs:{label:"订阅链接"}},[t("div",{on:{click:function(t){return e.copy(e.download.detail.subLink)}}},[t("el-input",{model:{value:e.download.detail.subLink,callback:function(t){e.$set(e.download.detail,"subLink",t)},expression:"download.detail.subLink"}})],1)])],1)],1),t("el-dialog",{attrs:{title:"设置",visible:e.plugin.settingsVisible,width:"30%"},on:{"update:visible":function(t){return e.$set(e.plugin,"settingsVisible",t)}}},[e.plugin.settings.length>0?t("div",[t("el-form",{ref:"form",attrs:{"label-width":"auto"}},e._l(e.plugin.settings,(function(s,a){return t("div",{key:a},[t("el-form-item",{attrs:{label:s.name}},[t("el-input",{model:{value:s.content,callback:function(t){e.$set(s,"content",t)},expression:"item.content"}}),t("el-tooltip",{staticClass:"item",attrs:{effect:"dark",content:s.tip,placement:"top-start"}},[t("i",{staticClass:"el-icon-question"})])],1)],1)})),0)],1):e._e(),0==e.plugin.settings.length?t("span",[e._v("暂无设置")]):e._e(),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{on:{click:function(t){e.plugin.settingsVisible=!1}}},[e._v("取 消")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.updatePluginSettings()}}},[e._v("确 定")])],1)])],1),t("div",{staticClass:"main"},[t("div",{directives:[{name:"show",rawName:"v-show",value:"logs"===e.activeMenu,expression:"activeMenu === 'logs'"}]},[t("title",[e._v("实时日志页面")]),t("div",{attrs:{id:"logspage"}},[t("el-card",[t("div",{staticClass:"clearfix",attrs:{slot:"header"},slot:"header"},[t("span",[e._v("实时日志")]),t("el-button",{staticStyle:{float:"right"},attrs:{type:"danger"},on:{click:e.clearLogs}},[e._v("清除日志")])],1),t("div",{staticClass:"logs"},e._l(e.system.logs,(function(s){return t("div",{key:s.id},[t("p",[e._v(e._s(s))])])})),0)])],1)]),t("div",{directives:[{name:"show",rawName:"v-show",value:"about"===e.activeMenu,expression:"activeMenu === 'about'"}]},[t("el-menu",[t("el-menu-item",[t("iframe",{staticStyle:{height:"95vh"},attrs:{src:"https://yajuhua.github.io",width:"100%",frameborder:"0"}})])],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"changePassword"===e.activeMenu,expression:"activeMenu === 'changePassword'"}]},[t("el-form",{staticClass:"demo-form-inline",attrs:{inline:!0}},[t("el-form-item",{attrs:{label:"用户名"}},[t("el-input",{attrs:{placeholder:"请输入用户名"},model:{value:e.user.account.username,callback:function(t){e.$set(e.user.account,"username",t)},expression:"user.account.username"}})],1),t("el-form-item",{attrs:{label:"密码"}},[t("el-input",{attrs:{placeholder:"请输入密码"},model:{value:e.user.account.password,callback:function(t){e.$set(e.user.account,"password",t)},expression:"user.account.password"}})],1),t("el-form-item",[t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.userAccountChange()}}},[e._v("修改")])],1)],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"customDomainName"===e.activeMenu,expression:"activeMenu === 'customDomainName'"}]},[t("el-form",{staticClass:"demo-form-inline",attrs:{inline:!0}},[t("el-form-item",{attrs:{label:"选择"}},[t("el-select",{model:{value:e.user.domain.select,callback:function(t){e.$set(e.user.domain,"select",t)},expression:"user.domain.select"}},[t("el-option",{attrs:{label:"默认",value:"0"}}),t("el-option",{attrs:{label:"修改",value:"1"}})],1)],1),"1"==e.user.domain.select?t("div",[t("el-form-item",{attrs:{label:"附件域名"}},[t("el-input",{attrs:{placeholder:"请输入"},model:{value:e.user.domain.value,callback:function(t){e.$set(e.user.domain,"value",t)},expression:"user.domain.value"}}),t("el-tooltip",{staticClass:"item",attrs:{effect:"dark",content:"如https://yajuhua.github.io:8088",placement:"top-start"}},[t("i",{staticClass:"el-icon-question"})])],1),t("el-form-item",[t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.changeDomain()}}},[e._v("修改")])],1)],1):e._e()],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"certificates"===e.activeMenu,expression:"activeMenu === 'certificates'"}]},[e.user.cert.list.length>0?t("div",{on:{click:function(t){return e.switchSsl()}}},[t("el-switch",{attrs:{"active-text":"开启"},model:{value:e.user.cert.switchSsl,callback:function(t){e.$set(e.user.cert,"switchSsl",t)},expression:"user.cert.switchSsl"}})],1):e._e(),t("br"),0==e.user.cert.list.length?t("el-upload",{ref:"uploadCertFile",staticClass:"upload-demo",attrs:{action:"/user/cert","on-change":e.certFilehandleChange,"on-remove":e.certFilehandleRemove,"file-list":e.user.cert.fileList,"auto-upload":!1,multiple:!0,name:"files"}},[t("el-button",{attrs:{slot:"trigger",size:"small",type:"primary"},slot:"trigger"},[e._v("选取文件")]),t("el-button",{staticStyle:{"margin-left":"10px"},attrs:{size:"small",type:"success"},on:{click:e.uploadCert}},[e._v("上传到服务器")]),t("div",{staticClass:"el-upload__tip",attrs:{slot:"tip"},slot:"tip"},[e._v("只能上传crt和key文件")])],1):e._e(),t("el-table",{ref:"multipleTable",staticStyle:{width:"100%"},attrs:{data:e.user.cert.list,"tooltip-effect":"dark"}},[t("el-table-column",[t("el-tag",[e._v("crt")]),t("el-tag",{attrs:{type:"success"}},[e._v("key")])],1),t("el-table-column",{attrs:{label:"操作",width:"180"}},[t("el-button",{attrs:{type:"danger"},on:{click:function(t){return e.deleteCert()}}},[e._v("删除")])],1)],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"dataMigration"===e.activeMenu,expression:"activeMenu === 'dataMigration'"}]},[t("el-button",{attrs:{type:"success",round:""},on:{click:function(t){return e.dataExport()}}},[e._v("导出")]),t("el-button",{attrs:{type:"primary",round:""},on:{click:function(t){return e.dataImport()}}},[e._v(" 导入 "),t("input",{ref:"fileInput",staticClass:"custom-file-input",attrs:{type:"file"},on:{change:e.handleFileSelect}})]),t("el-table",{ref:"multipleTable",staticStyle:{width:"100%"},attrs:{data:e.user.dataMigration.subData,"tooltip-effect":"dark"},on:{"selection-change":e.dataExportHandleSelectionChange}},[t("el-table-column",{attrs:{type:"selection",width:"55"}}),t("el-table-column",{attrs:{type:"index",width:"50"}}),t("el-table-column",{attrs:{label:"更新时间",width:"120",prop:"updateTime"}}),t("el-table-column",{attrs:{label:"频道名称",prop:"title","show-overflow-tooltip":""}})],1),t("el-dialog",{attrs:{title:"导入",visible:e.user.dataMigration.dataImportVisible,width:"40%"},on:{"update:visible":function(t){return e.$set(e.user.dataMigration,"dataImportVisible",t)}}},[t("div",[t("el-table",{ref:"multipleTable",staticStyle:{width:"100%"},attrs:{data:e.user.dataMigration.import,"tooltip-effect":"dark"},on:{"selection-change":e.dataImportHandleSelectionChange}},[t("el-table-column",{attrs:{type:"selection",width:"55"}}),t("el-table-column",{attrs:{type:"index",width:"50"}}),t("el-table-column",{attrs:{label:"频道名称",prop:"sub.title","show-overflow-tooltip":""}})],1)],1),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{on:{click:function(t){e.user.dataMigration.dataImportVisible=!1}}},[e._v("取 消")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.dataImportToServer()}}},[e._v("导 入")])],1)])],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"Downloading"===e.activeMenu,expression:"activeMenu === 'Downloading'"}]},[t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.download.progress,stripe:""}},[t("el-table-column",{attrs:{prop:"downloader",label:"下载器",width:"180"}}),t("el-table-column",{attrs:{prop:"channelName",label:"频道名称",width:"180"}}),t("el-table-column",{attrs:{label:"进度"},scopedSlots:e._u([{key:"default",fn:function(e){return t("el-progress",{attrs:{"text-inside":!0,"stroke-width":26,percentage:e.row.downloadProgress}})}}])}),t("el-table-column",{attrs:{prop:"downloadSpeed",label:"速度"}}),t("el-table-column",{attrs:{prop:"downloadTimeLeft",label:"剩余时间"}}),t("el-table-column",{attrs:{label:"操作"},scopedSlots:e._u([{key:"default",fn:function(s){return t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.downloadDetail(s.row.uuid)}}},[e._v("详细")])}}])})],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"Done"===e.activeMenu,expression:"activeMenu === 'Done'"}]},[t("div",{staticStyle:{height:"90vh","overflow-y":"scroll"}},[t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.download.done,stripe:""}},[t("el-table-column",{attrs:{label:"状态",width:"180"},scopedSlots:e._u([{key:"default",fn:function(e){return t("i",{class:{"el-icon-success":"5"==e.row.status,"el-icon-error":"5"!==e.row.status},style:{fontSize:"45px",color:"5"==e.row.status?"#54AC1C":"#F95C61"}})}}])}),t("el-table-column",{attrs:{prop:"channelName",label:"频道名称",width:"180"}}),t("el-table-column",{attrs:{prop:"itemName",label:"节目标题"}}),t("el-table-column",{attrs:{label:"进度"},scopedSlots:e._u([{key:"default",fn:function(e){return t("el-progress",{attrs:{status:"5"!=e.row.status?"exception":"success","text-inside":!0,"stroke-width":26,percentage:e.row.downloadProgress}})}}])}),t("el-table-column",{attrs:{label:"操作"},scopedSlots:e._u([{key:"default",fn:function(s){return t("div",{},[t("el-button",{attrs:{type:"success"},on:{click:function(t){return e.reDownload(s.row.uuid)}}},[e._v("重新")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.downloadDetail(s.row.uuid)}}},[e._v("详细")]),t("el-button",{attrs:{type:"danger"},on:{click:function(t){return e.downloadDelete(s.row.uuid)}}},[e._v("删除")])],1)}}])})],1)],1)]),t("div",{directives:[{name:"show",rawName:"v-show",value:"DownloaderInfo"===e.activeMenu,expression:"activeMenu === 'DownloaderInfo'"}]},[t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.download.info,stripe:""}},[t("el-table-column",{attrs:{prop:"name",label:"名称"}}),t("el-table-column",{attrs:{prop:"version",label:"版本"}}),t("el-table-column",{attrs:{prop:"updateTime",label:"更新时间"}})],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"pluginSettings"===e.activeMenu,expression:"activeMenu === 'pluginSettings'"}]},[t("div",{on:{click:function(t){return e.autoUpdatePlugin()}}},[t("el-switch",{attrs:{"active-text":"自动更新"},model:{value:e.plugin.autoUpdate,callback:function(t){e.$set(e.plugin,"autoUpdate",t)},expression:"plugin.autoUpdate"}})],1)]),t("div",{directives:[{name:"show",rawName:"v-show",value:"pluginList"===e.activeMenu,expression:"activeMenu === 'pluginList'"}]},[t("el-dialog",{attrs:{title:"详细信息",visible:e.plugin.detailVisible,width:"30%"},on:{"update:visible":function(t){return e.$set(e.plugin,"detailVisible",t)}}},[t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.plugin.detail,stripe:""}},[t("el-table-column",{attrs:{prop:"name"}}),t("el-table-column",{attrs:{prop:"content"}})],1),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{attrs:{type:"primary"},on:{click:function(t){e.plugin.detailVisible=!1}}},[e._v("确 定")])],1)],1),t("el-input",{staticStyle:{width:"20%"},attrs:{placeholder:"搜索插件"},model:{value:e.plugin.search,callback:function(t){e.$set(e.plugin,"search",t)},expression:"plugin.search"}}),e._v("  "),t("el-button",{attrs:{type:"primary",icon:e.plugin.searchIng},on:{click:function(t){return e.pluginSearch()}}},[e._v("搜索")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.getPluginList()}}},[e._v("全部")]),t("br"),t("br"),t("el-upload",{ref:"upload",staticClass:"upload-demo",attrs:{"auto-upload":!1,limit:1}},[t("el-button",{attrs:{slot:"trigger",size:"small",type:"primary"},slot:"trigger"},[e._v("上传本地插件")]),t("el-button",{staticStyle:{"margin-left":"10px"},attrs:{size:"small",type:"success"},on:{click:e.submitUpload}},[e._v("上传到服务器")]),t("div",{staticClass:"el-upload__tip",attrs:{slot:"tip"},slot:"tip"},[e._v("只能上传jar文件")])],1),t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.plugin.list,stripe:""}},[t("el-table-column",{attrs:{prop:"name",label:"名称",width:"180"}}),t("el-table-column",{attrs:{prop:"version",label:"版本",width:"180"}}),t("el-table-column",{attrs:{prop:"update",label:"更新时间"}}),t("el-table-column",{attrs:{fixed:"right",label:"操作"},scopedSlots:e._u([{key:"default",fn:function(s){return t("div",{},[!0!==s.row.install||s.row.hasUpdate?e._e():t("div",[t("el-button",{attrs:{type:"danger"},on:{click:function(t){return e.pluginDelete(s.row.uuid)}}},[e._v("卸载")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.pluginDetail(s.row.uuid)}}},[e._v("详细")]),t("el-button",{attrs:{type:"info"},on:{click:function(t){return e.getPluginSettings(s.row.name)}}},[e._v("设置")])],1),!1===s.row.install?t("div",[t("el-button",{ref:s.row.uuid,attrs:{type:"success"},on:{click:function(t){return e.pluginInstall(s.row.uuid)}}},[e._v("安装")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.pluginDetail(s.row.uuid)}}},[e._v("详细")])],1):e._e(),!0===s.row.hasUpdate&&!0===s.row.install?t("div",[t("el-button",{attrs:{type:"danger"},on:{click:function(t){return e.pluginDelete(s.row.uuid)}}},[e._v("卸载")]),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.pluginDetail(s.row.uuid)}}},[e._v("详细")]),t("el-button",{attrs:{type:"info"},on:{click:function(t){return e.getPluginSettings(s.row.name)}}},[e._v("设置")]),t("el-button",{ref:s.row.name,attrs:{type:"warning"},on:{click:function(t){return e.pluginUpdate(s.row.name)}}},[e._v("更新")])],1):e._e()])}}])})],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"pluginUpdate"===e.activeMenu,expression:"activeMenu === 'pluginUpdate'"}]},[t("el-table",{ref:"multipleTable",staticStyle:{width:"100%"},attrs:{data:e.plugin.updateList,stripe:""},on:{"selection-change":e.handleSelectionChange}},[t("el-table-column",{attrs:{type:"selection",width:"55"}}),t("el-table-column",{attrs:{prop:"name",label:"名称",width:"180"}}),t("el-table-column",{attrs:{prop:"version",label:"版本",width:"180"}}),t("el-table-column",{attrs:{prop:"update",label:"更新时间"}}),t("el-table-column",{attrs:{fixed:"right",label:"操作"},scopedSlots:e._u([{key:"default",fn:function(s){return t("el-button",{ref:s.row.name,attrs:{type:"warning"},on:{click:function(t){return e.pluginUpdate(s.row.name)}}},[e._v("更新")])}}])})],1),t("div",{staticClass:"footer"},[e.plugin.updateNames.length>1?t("el-button",{attrs:{type:"warning"},on:{click:e.pluginBatchUpdate}},[e._v("批量更新")]):e._e()],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"info"===e.activeMenu,expression:"activeMenu === 'info'"}]},[t("h2",[e._v("项目概况")]),t("el-button",{attrs:{type:"danger"},on:{click:function(t){return e.restart()}}},[e._v("重启")]),e._v(" "),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.checkForUpdate()}}},[e._v("检查更新")]),t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.system1.info,stripe:""}},[t("el-table-column",{attrs:{prop:"name"}}),t("el-table-column",{attrs:{prop:"content"}})],1)],1)])])},o=[],l=(s(4114),s(4603),s(7566),s(8721),s(8355)),i={data(){return{activeMenu:"info",plugin:{list:[],installStatus:"",search:"",searchIng:"",autoUpdate:!0,uploadFileList:[],detail:[],detailVisible:!1,updateList:[],updateNames:[],settings:[],settingsVisible:!1},download:{info:[],progress:[],done:[],detail:{},downloadDetailVisible:!1},system:{logs:[]},user:{domain:{value:"",select:"0"},account:{username:"",password:""},dataMigration:{import:[],export:[],subData:[],dataImportVisible:!1,selectImport:[]},cert:{fileList:[],fileFormList:[],list:[],switchSsl:!1}},system1:{info:[]}}},mounted(){this.getPluginList(),this.getAutoUpdateStatus(),this.pluginUpdateList(),this.getDownloaderInfo(),this.setupDownloadSocket(),this.getDownloadDone(),this.setupLogsSocket(),this.getSubList(),this.getSystemInfo(),this.getSslList(),this.getSslStatus()},methods:{toSubList(){this.$router.push("/")},handleMenuSelect(e){this.activeMenu=e},pluginDelete(e){this.$confirm("此操作将永久删除该插件, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.delete("/plugin?uuids="+e).then((e=>{"1"==e.data.code?(this.$message.success("插件删除成功！"),this.getPluginList()):this.$message.error(e.data.msg)})).catch((e=>{this.$message.error("插件删除错误！"),console.log(e)}))})).catch((()=>{this.$message({type:"info",message:"已取消删除"})}))},getPluginList(){l.A.get("/plugin/list").then((e=>{"1"==e.data.code?this.plugin.list=e.data.data:this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("获取插件列表失败")}))},pluginInstall(e){const t=this.$refs[e];"el-icon-loading"!=t.icon?this.$confirm("此操作将安装该插件, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{t.icon="el-icon-loading",l.A.get("/plugin/install?uuids="+e).then((e=>{"1"==e.data.code?(this.$message.success("插件安装成功！"),this.getPluginList()):this.$message.error(e.data.msg),t.icon=""})).catch((e=>{this.$message.error("插件安装失败！"),console.log(e),t.icon=""}));const s=setInterval((()=>{l.A.get("/plugin/install/status/"+e).then((e=>{!0===e.data.data.install&&(this.plugin.installStatus="",clearInterval(s),t.icon="")})).catch((e=>{t.icon="",console.error("查询安装状态失败：",e),clearInterval(s)}))}),2e3)})).catch((()=>{this.$message({type:"info",message:"已取消删除"})})):this.$message.warning("正在安装中请稍等片刻...")},pluginSearch(){""!=this.plugin.search?(this.plugin.searchIng="el-icon-loading",l.A.get("/plugin/search?keyword="+this.plugin.search).then((e=>{"1"==e.data.code?this.plugin.list=e.data.data:this.$message.error("搜索异常!"),this.plugin.searchIng=""})).catch((e=>{this.$message.error("搜索错误！"),console.log(e),this.plugin.searchIng=""}))):this.$message.error("请输入")},autoUpdatePlugin(){let e=0;this.plugin.autoUpdate&&(e=1),l.A.post("/plugin/autoUpdate?status="+e).then((t=>{"1"==t.data.code?(this.plugin.autoUpdate="1"==e,console.log("修改成功！")):this.$message.error("修改错误！")})).catch((e=>{this.$message.error("修改错误！"),console.log(e)}))},getAutoUpdateStatus(){l.A.get("/plugin/autoUpdate").then((e=>{"1"==e.data.code?"1"==e.data.data?this.plugin.autoUpdate=!0:this.plugin.autoUpdate=!1:this.$message.error("获取插件自动更新状态失败！")})).catch((e=>{this.$message.error("获取插件自动更新状态失败！"),console.log(e)}))},submitUpload(){let e=this.$refs.upload.uploadFiles[0].name,t=e.split("."),s=t[t.length-1];if(console.log("文件格式"+s),"jar"==s){let e=new FormData;e.append("files",this.$refs.upload.uploadFiles[0].raw),l.A.post("/common/upload/plugin",e,{headers:{"Content-Type":"multipart/form-data"}}).then((e=>{"1"==e.data.code?(this.$message.success("上传插件成功"),this.getPluginList()):this.$message.error(e.data.msg)})).catch((e=>{console.error(e)}))}else this.$message.error("只能上传插件jar包！")},pluginDetail(e){l.A.get("/plugin/detail/"+e).then((e=>{"1"==e.data.code?(this.plugin.detail=e.data.data,this.plugin.detailVisible=!0):this.$message.warning("请先安装插件！")})).catch((e=>{console.log("获取插件详细信息失败!"),console.log(e)}))},pluginUpdate(e){const t=this.$refs[e];"el-icon-loading"!=t.icon?(this.$confirm("此操作将更新该插件, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{t.icon="el-icon-loading",l.A.post("/plugin/update","names="+e).then((e=>{"1"==e.data.code?(this.$message.success("更新成功！"),this.pluginUpdateList(),this.getPluginList()):this.$message.error("更新插件错误！"),t.icon=""})).catch((e=>{t.icon="",this.$message.error("更新插件错误！"),console.log(e)}));const s=setInterval((()=>{l.A.get("/plugin/update/status/"+e).then((e=>{!1===e.data.data.hasUpdate&&(clearInterval(s),t.icon="")})).catch((e=>{t.icon="",console.error("查询更新状态失败：",e),clearInterval(s)}))}),2e3)})).catch((()=>{this.$message({type:"info",message:"已取消更新"})})),console.log(e)):this.$message.warning("正在更新中...")},pluginBatchUpdate(){this.$confirm("此操作将批量更新插件, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{let e=[];for(let t=0;t<this.plugin.updateNames.length;t++)e.push(this.plugin.updateNames[t].name);l.A.post("/plugin/update","names="+e).then((e=>{"1"==e.data.code?(this.$message.success("更新成功！"),this.pluginUpdateList(),this.getPluginList()):this.$message.error("批量更新插件错误！")})).catch((e=>{this.$message.error("批量更新插件错误！"),console.log(e)}))})).catch((()=>{this.$message({type:"info",message:"已取消批量更新"})}))},pluginUpdateList(){l.A.get("/plugin/updateList").then((e=>{"1"==e.data.code?(this.plugin.updateList=e.data.data,console.log(this.plugin.updateList)):this.$message.error("获取插件更新列表错误！")})).catch((e=>{this.$message.error("获取插件更新列表错误1"),console.log(e)}))},toggleSelection(e){e?e.forEach((e=>{this.$refs.multipleTable.toggleRowSelection(e)})):this.$refs.multipleTable.clearSelection()},handleSelectionChange(e){this.plugin.updateNames=e},getDownloaderInfo(){l.A.get("/download/info").then((e=>{"1"==e.data.code?this.download.info=e.data.data:this.$message.error("获取下载器信息失败！")})).catch((e=>{this.$message.error("获取下载器信息失败！"),console.log(e)}))},setupDownloadSocket(){let e=Math.random().toString(36).substr(2),t=window.location.protocol.includes("https")?"wss":"ws",s=window.location.hostname,a=window.location.port?`:${window.location.port}`:"",o=`${t}://${s}${a}/ws/download/${e}`,l=null;"WebSocket"in window?l=new WebSocket(o):alert("Not support websocket"),l.onerror=function(){console.log("下载ws连接错误")},l.onopen=function(){console.log("下载ws连接成功")};var i=this;l.onmessage=function(e){let t=e.data,s=JSON.parse(t);console.log(s),i.download.progress.length!=s.length&&i.getDownloadDone(),i.download.progress=s},l.onclose=function(){console.log("下载ws关闭")},window.onbeforeunload=function(){l.close()}},setupLogsSocket(){let e=Math.random().toString(36).substr(2),t=window.location.protocol.includes("https")?"wss":"ws",s=window.location.hostname,a=window.location.port?`:${window.location.port}`:"",o=`${t}://${s}${a}/ws/logs/${e}`,l=null;"WebSocket"in window?l=new WebSocket(o):alert("Not support websocket"),l.onerror=function(){console.log("日志ws连接错误")},l.onopen=function(){console.log("日志ws连接成功")};var i=this;l.onmessage=function(e){let t=e.data;i.system.logs.push(t)},l.onclose=function(){console.log("日志ws关闭")},window.onbeforeunload=function(){l.close()}},getDownloadDone(){l.A.get("/download/completed").then((e=>{"1"==e.data.code?this.download.done=e.data.data:this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("获取下载完成的信息失败！")}))},downloadDetail(e){this.download.detail={},this.download.downloadDetailVisible=!0,l.A.get("/download/detail/"+e).then((e=>{"1"==e.data.code?this.download.detail=e.data.data:this.$message.error(e.data.msg)})).catch((e=>{this.$message.error("获取下载详细信息失败！"),console.log(e)}))},copy(e){const t=document.createElement("textarea");console.log("复制到粘贴板"),t.value=e,t.setAttribute("readonly",""),t.style.position="absolute",t.style.left="-9999px",document.body.appendChild(t),t.select(),document.execCommand("copy"),document.body.removeChild(t),setTimeout((()=>{this.$message({message:"复制成功！",type:"success"})}),100),console.log("内容已成功复制到剪贴板")},reDownload(e){this.$confirm("此操作将重新下载, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.get("/download/reDownload/"+e).then((e=>{"1"==e.data.code?(this.$message.success("正在重新下载中"),this.getDownloadDone()):this.$message.error(e.data.msg)})).catch((e=>{this.$message.error("重新下载失败！"),console.log(e)}))})).catch((()=>{this.$message({type:"info",message:"已取消重新下载"})}))},clearLogs(){this.system.logs=[]},downloadDelete(e){this.$confirm("此操作将删除该下载, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.delete("/download?uuids="+e).then((e=>{"1"==e.data.code?(this.$message.success("删除成功！"),this.getDownloadDone()):this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("删除失败！")}))})).catch((()=>{this.$message.info("已取消删除")}))},changeDomain(){const e=/(http|https):\/\/([\w.]+\/?)\S*/,t=this.user.domain.value;e.test(t)?this.$confirm("此操作将修改附件域名, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.post("/user/enclosureDomain?domain="+t).then((e=>{"1"==e.data.code?this.$message.success("修改成功！"):this.$message.error(e.data.msg)}))})).catch((()=>{this.$message.info("已取消")})):this.$message.error("请输入正确域名！")},userAccountChange(){let e=this.user.account.username,t=this.user.account.password;const s=/^.{6,12}$/,a=/^.{1,30}$/;""!=e&&""!=t.length?s.test(t)?(a.test(e)||this.$message({showClose:!0,message:"用户名必须1-30位",type:"error"}),this.$confirm("此操作将修改用户名和密码, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.post("/user/change?username="+e+"&password="+t).then((e=>{"1"==e.data.code?(this.$message({showClose:!0,message:"修改成功！",type:"success"}),localStorage.removeItem("token"),this.$router.push("/login")):this.$message({showClose:!0,message:e.data.msg,type:"error"})})).catch((e=>{console.log(e),this.$message({showClose:!0,message:"修改错误！",type:"error"})}))})).catch((()=>{this.$message({showClose:!0,message:"已取消",type:"info"})}))):this.$message({showClose:!0,message:"密码必须6-12位",type:"error"}):this.$message({showClose:!0,message:"请先输入用户名和密码！",type:"error"})},dataExport(){let e=this.user.dataMigration.export;0!==e.length?this.$confirm("此操作将选择订阅导出, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{let t=[];for(let s=0;s<e.length;s++)t.push(e[s].uuid);l.A.get("/user/dataExport?uuids="+t.join(",")).then((e=>{if("1"==e.data.code){const t=new Blob([JSON.stringify(e.data.data)],{type:"text/plain"}),s=URL.createObjectURL(t),a=document.createElement("a");a.href=s,a.setAttribute("download","dataExport.json"),document.body.appendChild(a),a.click(),this.$message.success("导出成功！")}else this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("导出失败！")}))})).catch((()=>{this.$message.info("已取消导出")})):this.$message.error("请先选择订阅！")},dataImport(){this.$refs.fileInput.value="",document.querySelector(".custom-file-input").click()},dataImportToServer(){l.A.post("/user/dataImport",this.user.dataMigration.selectImport).then((e=>{"1"==e.data.code?(this.$message.success("导入成功！"),this.getSubList(),this.user.dataMigration.dataImportVisible=!1):this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("导入失败")}))},dataExportHandleSelectionChange(e){this.user.dataMigration.export=e,console.log(e)},dataImportHandleSelectionChange(e){this.user.dataMigration.selectImport=e,console.log(e)},handleFileSelect(e){const t=e.target.files[0],s=new FileReader;s.onload=null,s.onload=()=>{try{const e=s.result;this.user.dataMigration.import=JSON.parse(e),this.user.dataMigration.dataImportVisible=!0}catch(e){this.$message.error("请正确上传文件！")}},s.readAsText(t)},getSubList(){l.A.get("/sub/list").then((e=>{this.user.dataMigration.subData=e.data.data})).catch((e=>{console.log(e),this.$message.error("获取订阅列表数据失败！")}))},uploadCert(){let e=this.user.cert.fileList;if(console.log(e),e.length<2)return void this.$message.error("crt和key都要上传");if(e.length>2)return void this.$message.error("只能上传crt和key文件");for(let s=0;s<e.length;s++){let t=e[s].name.split("."),a=t[t.length-1];if(console.log(a),"crt"!=a&&"key"!=a)return void this.$message.error("只能上传crt和key文件");e[s].name=a}let t=new FormData;for(let s in this.$refs.uploadCertFile.uploadFiles)t.append("files",this.$refs.uploadCertFile.uploadFiles[s].raw);l.A.post("/user/cert",t,{headers:{"Content-Type":"multipart/form-data"}}).then((e=>{"1"==e.data.code?(this.$message.success("上传成功,重启后生效"),this.getSslList()):this.$message.error(e.data.msg)})).catch((e=>{console.error(e)}))},certFilehandleChange(e,t){this.user.cert.fileList=t},certFilehandleRemove(e,t){this.user.cert.fileList=t},getSystemInfo(){l.A.get("/system/info").then((e=>{"1"==e.data.code?this.system1.info=e.data.data:this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("获取系统信息失败！")}))},restart(){this.$confirm("此操作将重启项目, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.get("/system/restart").then((e=>{"1"==e.data.code?this.$message({showClose:!0,message:"正在重启中...",type:"success"}):this.$message({showClose:!0,message:e.data.msg,type:"error"})})).catch((e=>{console.log(e),this.$message({showClose:!0,message:"重启失败!",type:"error"})}))})).catch((()=>{this.$message({showClose:!0,message:"已取消",type:"info"})}))},checkForUpdate(){l.A.get("/system/hasUpdate").then((e=>{"1"==e.data.code?this.$alert(e.data.data,"检查更新",{confirmButtonText:"确定"}):this.$message({showClose:!0,message:e.data.msg,type:"error"})})).catch((e=>{console.log(e),this.$message({showClose:!0,message:"检查更新失败！",type:"error"})}))},switchSsl(){l.A.post("/user/switchSsl?status="+this.user.cert.switchSsl).then((e=>{"1"==e.data.code?this.$message.success("设置成功，重启后生效！"):this.$message({showClose:!0,message:e.data.msg,type:"error"})})).catch((e=>{console.log(e),this.$message({showClose:!0,message:"设置ssl失败！",type:"error"})}))},getSslList(){l.A.get("/user/cert").then((e=>{if("1"!=e.data.code)return this.$message({showClose:!0,message:e.data.msg,type:"error"}),!1;this.user.cert.list=e.data.data})).catch((e=>{console.log(e),this.$message({showClose:!0,message:"获取ssl列表!",type:"error"})}))},deleteCert(){this.$confirm("此操作将删除证书和密钥, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{l.A.delete("/user/cert").then((e=>{"1"==e.data.code?(this.$message.success("删除成功！"),this.getSslList()):this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("删除失败！")}))})).catch((()=>{this.$message({showClose:!0,message:"已取消",type:"info"})}))},getSslStatus(){l.A.get("/user/sslStatus").then((e=>{"1"==e.data.code?this.user.cert.switchSsl=e.data.data:this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("获取ssl状态失败！")}))},getPluginSettings(e){this.plugin.settings=[],this.plugin.settingsVisible=!0,l.A.get("/plugin/settings/"+e).then((e=>{"1"==e.data.code?this.plugin.settings=e.data.data:this.$message.error(e.data.msg)})).catch((e=>{console.log(e),this.$message.error("获取插件设置失败！")}))},updatePluginSettings(){this.plugin.settings.length>0?l.A.put("/plugin/settings",this.plugin.settings).then((e=>{"1"==e.data.code?this.$message.success("更新插件设置成功！"):this.$message.error(e.data.mgs),this.plugin.settingsVisible=!1})).catch((e=>{console.log(e),this.$message.error("更新插件设置错误！")})):this.plugin.settingsVisible=!1},logout(){localStorage.removeItem("token"),this.$router.push("/login")}}},n=i,r=s(1656),c=(0,r.A)(n,a,o,!1,null,"62c9c639",null),d=c.exports}}]);
//# sourceMappingURL=928.31bd7fe7.js.map