new Vue({
    el: "#app",
    data() {
        return {
            activeMenu: 'info',
            formInline: {
                user: '',
                password: ''
            },
            logs: [], // 存储日志信息的数组
            downloadInfo:"",
            systemAllData: [],
            systemAllData1: "",
            infoData: [
                { key: "已运行", value: "" },
                { key: "版本", value: "" },
                { key: "更新时间", value: "" },
                { key: "代号", value: "" }
            ],
            fileList: [],
            multipleSelection: [],
            preparingForUpdates:false,
            downloadData:[],
            downloadDone:[],
            multipleDoneSelection:[],
            DownloaderInfo:[],
            multipleSelectChannelData: [],
            channelData:null,
            importData:null,
            multipleSelectImportData:null,
            importDataDialog:false
        };
    }, mounted: function () {
        // 页面加载完成后，发送异步请求，查询数据
        var _this = this;
        axios({
            method: "get",
            url: "./system/systemInfoServlet"
        }).then(function (resp) {
            //系统信息
            _this.systemAllData1 = resp.data;
            _this.infoData[0].value = resp.data.systemRuntime;
            _this.infoData[1].value = resp.data.systemVersion;
            _this.infoData[2].value = resp.data.systemUpdate;
            _this.infoData[3].value = resp.data.systemCode;

        })

        //获取频道所有信息
        this.getAllChannelData();

        //获取下载器信息
        this.getDownloaderInfo();

        //获取下载完成数据
        this.getDownloadDone();

        //开启WS
        this.setupLogSocket();
        this.setupDownloadSocket();


    },
    created() {
    },
    beforeUnmount() {
        // 组件卸载前关闭 WebSocket 连接
        this.logSocket.close();
        this.downloadSocket.close();
    },
    beforeDestroy() {
        // 执行关闭资源的操作
        this.logSocket.close();
        this.downloadSocket.close();
    },
    methods: {
        //获取下载器信息
        getDownloaderInfo(){
            _this = this;
            axios({
                method:"get",
                url:"./system/downloaderInfoServlet"
            }).then(function (resp) {
                console.log(resp.data);
                _this.DownloaderInfo = resp.data;
            })
        },
        //获取下载已完成数据
        getDownloadDone(){
            _this = this;
            axios({
                method:"get",
                url:"./user/selectCompleteDownloadServlet"
            }).then(function (resp) {
                console.log(resp.data);
                _this.downloadDone = resp.data;
            })
        },
        //根据id删除记录
        deleteDownloadRecord(id){
            _this = this;

            this.$confirm('此操作将永久删除该记录, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {

                axios({
                    method:"get",
                    url:"./user/deleteDownloadRecordServlet?id="+id
                }).then(function (resp) {
                    if (resp.data == 'ok'){
                        //删除成功
                        _this.$message({
                            showClose: true,
                            message: '删除成功！',
                            type: 'success'
                        });
                        //重新获取
                        _this.getDownloadDone();
                    }else {
                        //删除失败
                        _this.$message({
                            showClose: true,
                            message: '删除失败',
                            type: 'error'
                        });
                    }
                })

            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        //日志展示
        setupLogSocket() {
            this.logSocket = new WebSocket(
                "ws://" + window.location.host + "/podcast2/websocket/logs"
            );

            this.logSocket.onopen = () => {
                console.log("logSocket 连接成功");
            };

            this.logSocket.onmessage = (event) => {
                const message = event.data;
                this.logs.push(message);
                this.scrollToBottom();
            };

            this.logSocket.onclose = () => {
                console.log("logSocket 连接关闭");
            };
        },
        //下载进度展示
        setupDownloadSocket() {
            _this = this;
            this.downloadSocket = new WebSocket(
                "ws://" + window.location.host + "/podcast2/websocket/download"
            );

            this.downloadSocket.onopen = () => {
                console.log("WebSocket 连接成功");
            };

            this.downloadSocket.onmessage = (event) => {
                const message = event.data;
                console.log("下载器日志信息：" + message);
                console.log("下载器日志信息对象：" + JSON.parse(message));
                var json = JSON.parse(message);
                this.scrollToBottom();

                const index = this.downloadData.findIndex((item) => item.id === json.id);
                if (index !== -1) {
                    console.log("id存在");
                    this.$set(this.downloadData, index, json);
                    console.log("更新完成");
                    if (this.downloadData[index].percentage === 100.0){
                        console.log("达到100%，删除该元素");
                        this.downloadData.splice(index, 1);
                        //刷新页面
                        this.getDownloadDone();
                    }

                } else {
                    if (json.percentage != 100.0){
                        console.log("id不存在");
                        this.downloadData.push(json);
                        console.log("添加完成");
                    }
                }
            };

            this.downloadSocket.onclose = () => {
                console.log("WebSocket 连接关闭");
            };
        },
        handleMenuSelect(index) {
            this.activeMenu = index;
        },
        clearLogs() {
            this.logs = []; // 清空日志数组
        },
        scrollToBottom() {
            this.$nextTick(() => {
                const logsContainer = this.$el.querySelector(".logs");
                logsContainer.scrollTop = logsContainer.scrollHeight;
            });
        },
        toSubscriptionList() {
            window.location.href = "index.html"
        },
        exit() {
            axios({
                method: "post",
                url: "./user/exitServlet"
            }).then(function (resp) {
                if (resp.data === "ok") {
                    window.location.href = "login.html";
                }
            })
        },
        //修改用户名和密码
        changeUserPasswd() {
            _this = this;
            if (this.formInline.user != '' || this.formInline.password != ''){
                //避免全部传入空值
               if (this.formInline.user != '' && this.formInline.password == ''){
                   //修改用户名
                   axios({
                       method: "post",
                       url: "./user/changeServlet",
                       data: "username=" + this.formInline.user
                   }).then(function (resp) {
                       if (resp.data == "ok") {
                           //提示
                           _this.$message({
                               message: '修改用户名成功！',
                               type: 'success'
                           });
                           window.location.href = "login.html";
                       }
                   })
               }else if (this.formInline.user == '' && this.formInline.password != ''){
                   //修改密码
                   axios({
                       method: "post",
                       url: "./user/changeServlet",
                       data: "password=" + this.formInline.password
                   }).then(function (resp) {
                       if (resp.data == "ok") {
                           //提示
                           _this.$message({
                               message: '修改密码成功！',
                               type: 'success'
                           });
                           window.location.href = "login.html";
                       }
                   })
               }else if (this.formInline.user != '' && this.formInline.password != ''){
                   //修改用户名和密码
                   axios({
                       method: "post",
                       url: "./user/changeServlet",
                       data: "username=" + this.formInline.user + "&password=" + this.formInline.password
                   }).then(function (resp) {
                       if (resp.data == "ok") {
                           //提示
                           _this.$message({
                               message: '用户名和密码修改成功！',
                               type: 'success'
                           });
                           window.location.href = "login.html";
                       }
                   })
               }
            }else {
                //提示错误
                _this.$message.error('请输入要修改的用户名或密码');
            }
        },
        tableRowClassName({ row, rowIndex }) {
            if (rowIndex === 1) {
                return 'warning-row';
            } else if (rowIndex === 3) {
                return 'success-row';
            }
            return '';
        },
        //上传插件
        submitUpload() {
            var rs = this.$refs.upload1.submit().data;
            alert(rs)
        },
        //上传war包
        submitUpload2() {
            var rs = this.$refs.upload2.submit().data;
            alert(rs);
        },
        //更新系统
        updateSystem(){

        },
        handleRemove(file, fileList) {
            console.log(file, fileList);
        },
        handlePreview(file) {
            console.log(file);
        },
        handleResponse(response, file, fileList) {
            if (response == "uploadok") {
                this.$message({
                    message: '添加成功！',
                    type: 'success'
                });
                window.location.reload();
            } else {
                this.$message.error('添加失败！');
            }
        },
        handleRemove2(file, fileList) {
            console.log(file, fileList);
        },
        handlePreview2(file) {
            console.log(file);
        },
        handleResponse2(response, file, fileList) {
            if (response == "uploadok") {
                this.preparingForUpdates = !this.preparingForUpdates;
                this.$message({
                    message: '添加成功！',
                    type: 'success'
                });
            } else {
                this.$message.error('添加失败！');
            }
        },
        //重启系统
        restart() {
            this.$confirm('此操作将重启系统且该页面不会自动刷新, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios({
                    method: "post",
                    url: "./system/controlSystemServlet",
                    data: "controlCode=1"
                })
                this.$message({
                    type: 'success',
                    message: '正在重启中...该页面不会自动刷新!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消重启'
                });
            });
        },
        //更新系统
        updateSystem() {
            this.$confirm('此操作将更新系统且该页面不会自动刷新, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios({
                    method: "post",
                    url: "./system/controlSystemServlet",
                    data: "controlCode=2"
                })
                this.$message({
                    type: 'success',
                    message: '正在更新系统中...该页面不会自动刷新!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消重启'
                });
            });
        },
        toggleSelection(rows) {
            if (rows) {
                rows.forEach(row => {
                    this.$refs.multipleTable.toggleRowSelection(row);
                });
            } else {
                this.$refs.multipleTable.clearSelection();
            }
        },
        handleSelectionChange(val) {
            for (let i = 0; i < val.length; i++) {
                var name = val[i].name;
                var version = val[i].version;
                var info = "name=" + name + "&version=" + version;
                this.multipleSelection[i] = info;
            }
        },
        //批量删除
        deletePlugins() {
            this.$confirm('此操作将永久删除选择的插件且系统会重启, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                for (let i = 0; i < this.multipleSelection.length; i++) {
                    axios({
                        method: "post",
                        url: "./system/deletePluginServlet",
                        data: this.multipleSelection[i]
                    })
                }
                this.$message({
                    type: 'success',
                    message: '插件正在删除中...该页面不会自动刷新'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        //单个删除
        deletePlugin(row) {
            this.$confirm('此操作将永久删除该插件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios({
                    method: "post",
                    url: "./system/deletePluginServlet",
                    data: "name=" + row.name + "&version=" + row.version
                })
                this.$message({
                    type: 'success',
                    message: '插件正在删除中...该页面不会自动刷新!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        //选择删除要的下载记录
        handleSelectionDoneChange(val) {
            this.multipleDoneSelection = val;
            console.log(this.multipleDoneSelection);
        },
        //批量删除下载完成记录
        deleteDownloadRecords(){

            this.$confirm('此操作将永久删除选择的下载记录, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {

                for (let i = 0; i < this.multipleDoneSelection.length; i++) {
                    id = this.multipleDoneSelection[i].id;
                    axios({
                        method:"get",
                        url:"./user/deleteDownloadRecordServlet?id="+id
                    })
                }

                //重新获取下载完成数据
                this.getDownloadDone();

                this.$message({
                    type: 'success',
                    message: '删除成功！'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        //选择频道数据
        selectionChannelData(val) {
            this.multipleSelectChannelData = val;
        },
        //选择频道数据
        selectionImportData(val) {
            this.multipleSelectImportData = val;
        },
        //获取所有频道数据
        getAllChannelData(){
            _this = this;
            axios({
                method:"get",
                url:"./user/channelDataServlet"
            }).then(function (resp) {
                var json = resp.data;
                _this.channelData = json;
                console.log(_this.channelData)
            })
        },
        //数据导出
        dataExport(){
            //选择不能为空
            if (this.multipleSelectChannelData.length!=0) {
                //生成
                const blob = new Blob([JSON.stringify(this.multipleSelectChannelData)], {type: 'text/plain'});
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', 'dataExport.json');
                document.body.appendChild(link);
                link.click();
            }
        },
        //数据导入
        dataImport(){
            this.$refs.fileInput.value = '';
            this.triggerFileSelect();
        },
        //读取并解析本地json文件
        handleFileSelect(event) {
            const file = event.target.files[0];
            const reader = new FileReader();


            // 解除之前的事件处理函数绑定
            reader.onload = null;

            // 注册新的事件处理函数
            reader.onload = () => {
                const content = reader.result;
                this.importData = JSON.parse(content);
                this.importDataDialog = true
            };

            reader.readAsText(file);
        },
        triggerFileSelect() {
            document.querySelector('.custom-file-input').click();
        },
        //发往服务器
        importChannelDataToService(){
            _this = this;
            axios({
                method: "post",
                url: "./user/importChannelDataServlet",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"  // 指定请求字符编码为 UTF-8
                },
                data: "importData=" + encodeURIComponent(JSON.stringify(_this.multipleSelectImportData))  // 对数据进行 URL 编码
            }).then(function (resp) {
                var rs = resp.data;
                if (rs == 'importOk') {
                    console.log('importOk');

                    //重新获取频道数据
                    _this.channelData = _this.getAllChannelData();

                    //提示成功
                    _this.$message({
                        message: '导入成功！',
                        type: 'success'
                    });
                }else {
                    _this.$message.error('导入失败，请检查文件！');
                }
            })

            this.importDataDialog = false;
        }
    },
});