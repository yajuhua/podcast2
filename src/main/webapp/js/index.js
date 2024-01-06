
new Vue({
    el: "#app",
    data() {
        return {
            tableData: [],
            dialogVisible: false,
            qrurl: "",
            selecteUuid: [],
            _uuid: "",
            editDialogVisible: false,
            editChannelData:'',
            editChannelDataShow:'',
            frequency:'',
            showButton: false

        }
    },
    mounted() {
        // 页面加载完成后，发送异步请求，查询数据
        var _this = this;
        axios({
            method: "post",
            url: "./user/selectAllServlet"
        }).then(function (resp) {
            _this.tableData = resp.data;
        })
        // 添加滚动事件监听器
        window.addEventListener('scroll', this.handleScroll);
    },
    beforeDestroy() {
        // 在组件销毁前移除滚动事件监听器，以防止内存泄漏
        window.removeEventListener('scroll', this.handleScroll);
    },
    methods: {
        // 跳转到管理页面
        toManage() {
            window.location.href = "manage.html"
        },
        // 跳转到添加订阅页面
        toAdd() {
            window.location.href = "add.html"
        },
        // 关闭二维码弹出框
        handleClose(done) {
            done();
        },
        //根据uuid拼接成新的url，用于二维码生成和链接复制
        changeUrl(uuid) {
            this.dialogVisible = true;
            // this.qrurl = "https://api.pwmqr.com/qrcode/create/?url=http://" + window.location.host + "/podcast2/xml/" + uuid + ".xml";
            this.qrurl = window.location.protocol + "//" + window.location.host + "/podcast2/xml/" + uuid + ".xml";
            this._uuid = uuid;
        },
        //单个删除
        dele(uuid) {
            //确认删除提示框
            this.$confirm('此操作将永久删除该订阅, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                //发送post请求到服务器删除
                axios({
                    method: "post",
                    url: "./user/deleteServlet",
                    data: "uuid=" + uuid
                });
                window.location.reload();
                this.$message({
                    type: 'success',
                    message: '删除成功!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
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
        handleSelectionChange(selection) {
            // 在这里处理选中变化的逻辑
            // console.log(selection); // 输出选中的行数据
            //获取被选中的uuid
            for (let i = 0; i < selection.length; i++) {
                this.selecteUuid[i] = selection[i].uuid
            }
        },
        // 在这里处理用户点击“批量删除”菜单选项的逻辑
        deletes() {
            //不能为空
            if (this.selecteUuid.length!=0){
                this.$confirm('此操作将永久删除选择的订阅, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    //循环批量删除
                    for (let i = 0; i < this.selecteUuid.length; i++) {
                        axios({
                            method: "post",
                            url: "./user/deleteServlet",
                            data: "uuid=" + this.selecteUuid[i]
                        });
                    }
                    //刷新页面
                    window.location.reload();
                    this.$message({
                        type: 'success',
                        message: '删除成功!'
                    });
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消删除'
                    });
                });
            }else {
                this.$message.error('不能为空！请先选择！');
            }
        },
        //生成OPML文件
        downloadOPML() {
            //选择不能为空
            if (this.selecteUuid.length!=0) {
                //生成
                let text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                text += "<opml version=\"1.0\">\n";
                text += "  <head>\n";
                text += "    <title>OPML</title>\n";
                text += "  </head>\n";
                text += "  <body>\n";
                for (let i = 0; i < this.selecteUuid.length; i++) {
                    text += "    <outline type=\"rss\"  xmlUrl=\"" + window.location.protocol + "//" + window.location.host + "/podcast2/xml/" + this.selecteUuid[i] + ".xml\" />\n"
                }
                text += "  </body>\n";
                text += "</opml>\n";

                const blob = new Blob([text], {type: 'text/plain'});
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', 'opml.opml');
                document.body.appendChild(link);
                link.click();
            }else {
                this.$message.error('不能为空！请先选择！');
            }
        },
        //点击复制URL
        copyToClipboard(uuid) {
            const textarea = document.createElement('textarea');
            textarea.value = window.location.protocol + "//" + window.location.host + "/podcast2/xml/" + uuid + ".xml"
            textarea.setAttribute('readonly', '');
            textarea.style.position = 'absolute';
            textarea.style.left = '-9999px';
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            //提示复制成功
            this.$message({
                message: '复制成功！',
                type: 'success'
            });
            console.log('内容已成功复制到剪贴板');
        },
        //复制URL到粘贴板
        dialogCopyURL() {
            //复制URL
            this.copyToClipboard(this._uuid)
            //关闭对话框
            this.dialogVisible = false;
            //清除二维码
            this.qrCodeHandleClose();

        },
        //关闭弹框清除二维码
        qrCodeHandleClose() {
            this.dialogVisible = false;
            this.qrCode = '';
            document.getElementById('qrCode').innerHTML = '';
        },
        //创建二维码
        qrcode(url) {
            this.qrCode = new QRCode('qrCode', {
                text: url,
                width: 260,
                height: 250
            })
        },
        //创建二维码
        qrCodeCreate() {
            this.$nextTick(() => {
                this.qrcode(this.qrurl);
            })
        },
        //二维码显示
        qrCodeOpen(val) {
            this.dialogVisible = true;
        },
        //编辑对话框
        editDialog(uuid){
            _this = this;
            axios({
                method:"post",
                url:"./user/selectChannelByIdServlet",
                data:"uuid="+uuid
            }).then(function (resp) {
                _this.editChannelData=resp.data;
                _this.editChannelData.frequency=resp.data.frequency+""
                _this.editChannelData.survival=resp.data.survival+""
                _this.editChannelData.status=resp.data.status+""
                _this.editDialogVisible=true;
                console.log(_this.editChannelData)
            })
            },
        //提交编辑
        submitEdit(){
            if (this.editChannelData.args=='customize' && this.editChannelData.customArgs==null){
                this.$message.error('自定义下载器选项不能为空！');
            }else {
                _this = this
                let args = null;
                if (this.editChannelData.args=='customize'){
                    args=this.editChannelData.customArgs
                }
                axios({
                    method:"post",
                    url:"./user/updateChannelByIdServlet",
                    data:"uuid="+_this.editChannelData.uuid+"&type="
                        +_this.editChannelData.type+"&survival="+_this.editChannelData.survival
                        +"&status="+_this.editChannelData.status
                        +"&frequency="+_this.editChannelData.frequency
                        +"&args="+args
                }).then(function (resp) {
                    if (resp.data=='ok'){
                        _this.$message({
                            message: '修改成功！',
                            type: 'success'
                        });
                       _this.editDialogVisible=false;
                    }else {
                        _this.$message.error('修改失败,请再试一次！');
                    }
                })
            }
        },
        //反转订阅排序
        reverseSubscribe(){
            this.tableData.reverse();
        },
        scrollToTop() {
            // 使用 smooth 滚动到顶部
            window.scrollTo({ top: 0, behavior: 'smooth' });
        },
        handleScroll() {
            // 根据页面滚动位置决定按钮是否显示
            this.showButton = window.scrollY > window.innerHeight;
        },
    },

})