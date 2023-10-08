new Vue({
    el: "#app",
    data() {
        return {
            tableData: [],
            dialogVisible: false,
            qrurl: "",
            selecteUuid: [],
            _uuid: ""
        }
    },
    mounted() {
        // 页面加载完成后，发送异步请求，查询数据
        var _this = this;

        axios({
            method: "post",
            url: "./selectAllServlet"
        }).then(function (resp) {
            _this.tableData = resp.data;
        })
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
        changeUrl(uuid) {
            this.dialogVisible = true;
            this.qrurl = "https://api.pwmqr.com/qrcode/create/?url=http://" + window.location.host + "/podcast2/xml/" + uuid + ".xml";
            this._uuid = uuid;
        },
        dele(uuid) {
            axios({
                method: "post",
                url: "./deleteServlet",
                data: "uuid=" + uuid
            });
            window.location.reload();
            this.open3()
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
        deletes() {
            // 在这里处理用户点击“批量删除”菜单选项的逻辑
            for (let i = 0; i < this.selecteUuid.length; i++) {
                axios({
                    method: "post",
                    url: "./deleteServlet",
                    data: "uuid=" + this.selecteUuid[i]
                });
            }
            window.location.reload();
        },
        downloadOPML() {
            let text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
            text += "<opml version=\"1.0\">\n";
            text += "  <head>\n";
            text += "    <title>OPML</title>\n";
            text += "  </head>\n";
            text += "  <body>\n";
            for (let i = 0; i < this.selecteUuid.length; i++) {
                text += "    <outline type=\"rss\"  xmlUrl=\"" + "http://" + window.location.host + "/podcast2/xml/" + this.selecteUuid[i] + ".xml\" />\n"
            }
            text += "  </body>\n";
            text += "</opml>\n";

            const blob = new Blob([text], { type: 'text/plain' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'opml.opml');
            document.body.appendChild(link);
            link.click();
        },
        copyToClipboard(uuid) {
            //点击复制URL
            const textarea = document.createElement('textarea');
            textarea.value = "http://" + window.location.host + "/podcast2/xml/" + uuid + ".xml"
            textarea.setAttribute('readonly', '');
            textarea.style.position = 'absolute';
            textarea.style.left = '-9999px';
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            //提示复制成功
            this.open2();
            console.log('内容已成功复制到剪贴板');
        },
        dialogCopyURL() {
            //复制URL
            this.copyToClipboard(this._uuid)
            //关闭对话框
            this.dialogVisible = false;

        }, open2() {
            this.$message({
                message: '复制成功！',
                type: 'success'
            });
        }, open3() {
            this.$message({
                message: '删除成功！',
                type: 'success'
            });
        }, open4() {
            this.$message.error("删除失败！")
        }
    }
})