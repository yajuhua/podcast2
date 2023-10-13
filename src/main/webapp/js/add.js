new Vue({
    el:"#app",
    data(){
        return{
            url:null,
            type:"audio",
            frequency:"1200",
            survival:"604800",
            episodes:"0",
            customInput:null,
        }
    },methods:{
        submit(){
            //正则表达式：检查URL链接格式是否正确
            const pattern = /(http|https):\/\/([\w.]+\/?)\S*/;

            if (this.url == null || this.episodes == "1" && this.customInput == null ){
                //数据不全不提交
                this.$message.error('请填写完整参数！');
            } else if (!pattern.test(this.url)){
                this.$message.error('请输入正确主页链接')
            }else{
                //判断是否有该插件
                var _this = this;
                var pluginList;
                //提交
                axios({
                    method: "get",
                    url: "./system/systemInfoServlet",
                }).then(function (response) {
                    //获取插件信息
                    pluginList = response.data.pluginList;
                    console.log(pluginList)

                    //判断是否包含插件
                    for (let i = 0; i < pluginList.length; i++) {
                        if (_this.url.includes(pluginList[i].name)){
                            //提交表单
                            axios({
                                method: "post",
                                url: "./user/xmlFactoryServlet",
                                data: "url=" + _this.url + "&type=" + _this.type + "&frequency=" + _this.frequency + "&survival=" + _this.survival + "&episodes=" + _this.episodes
                            }).then(function (response) {
                                console.log(response.data)//打印响应数据
                                if (response.data === "ok") {
                                    _this.$message({
                                        message: '添加成功！',
                                        type: 'success'
                                    });
                                    window.location.href = "index.html";
                                } else {
                                    _this.$message.error('添加失败！');
                                }
                            })
                            //找到插件后结束循环
                            return;
                        }
                    }

                    //找不到插件
                    _this.$message.error('找不到该插件!');
                })
            }
        }
    }
})
