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
            systemAllData: [],
            systemAllData1: "",
            infoData: [
                { key: "已运行", value: "" },
                { key: "版本", value: "" },
                { key: "更新时间", value: "" },
                { key: "代号", value: "" }
            ],
            fileList: [],
            multipleSelection: []
        };
    }, mounted: function () {

        // 页面加载完成后，发送异步请求，查询数据
        var _this = this;
        axios({
            method: "get",
            url: "./systemInfoServlet"
        }).then(function (resp) {
            //系统信息
            _this.systemAllData1 = resp.data;
            _this.infoData[0].value = resp.data.systemRuntime;
            _this.infoData[1].value = resp.data.systemVersion;
            _this.infoData[2].value = resp.data.systemUpdate;
            _this.infoData[3].value = resp.data.systemCode;

        })
    },
    created() {
        // 建立 WebSocket 连接
        const socket = new WebSocket("ws://192.168.123.3:8088/podcast2/websocket/logs");

        // 监听 WebSocket 连接事件
        socket.onopen = () => {
            console.log("WebSocket 连接成功");
        };

        // 监听 WebSocket 接收消息事件
        socket.onmessage = (event) => {
            const message = event.data;
            this.logs.push(message); // 将接收到的日志信息添加到数组中
            this.scrollToBottom(); // 滚动到底部
        };

        // 监听 WebSocket 关闭事件
        socket.onclose = () => {
            console.log("WebSocket 连接关闭");
        };
    },
    methods: {
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
                url: "./exitServlet"
            }).then(function (resp) {
                if (resp.data === "ok") {
                    window.location.href = "login.html";
                }
            })
        },
        //修改用户名和密码
        changeUserPasswd() {
            axios({
                method: "post",
                url: "./changeServlet",
                data: "username=" + this.formInline.user + "&password=" + this.formInline.password
            }).then(function (resp) {
                if (resp.data == "ok") {
                    window.location.href = "login.html";
                }
            })
        },
        tableRowClassName({ row, rowIndex }) {
            if (rowIndex === 1) {
                return 'warning-row';
            } else if (rowIndex === 3) {
                return 'success-row';
            }
            return '';
        }, submitUpload() {
            var rs = this.$refs.upload.submit().data;
            alert(rs)
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
        }, restart() {
            //重启系统
            axios({
                method: "post",
                url: "./controlSystemServlet",
                data: "controlCode=1"
            })
        }, toggleSelection(rows) {
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
        deletePlugins() {
            //批量删除
            for (let i = 0; i < this.multipleSelection.length; i++) {
                axios({
                    method: "post",
                    url: "./deletePluginServlet",
                    data: this.multipleSelection[i]
                })
            }
        },
        deletePlugin(row) {
            //单个删除
            axios({
                method: "post",
                url: "./deletePluginServlet",
                data: "name=" + row.name + "&version=" + row.version
            })
        }
    },
});