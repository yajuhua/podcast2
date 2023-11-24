new Vue({
    el:"#app",
    data(){
        return{
            url:null,
            type:"audio",
            frequency:"1200",
            survival:"604800",
            customInput:null,
            episodes:"0",
            finalEpisodes:null,
            selectArgs:"0",
            args:null,
            loading: false
        }
    },methods:{
        submit(){
            //正则表达式：检查URL链接格式是否正确
            const pattern = /(http|https):\/\/([\w.]+\/?)\S*/;
            //正则表达式：检查自定义剧集格式是否正确，格式1,2,3,4
            const patternCustomEpisodes = /^([1-9])(?!.*\b\1\b)(?:[0-9]{0,2}|[1-9])(?!,$)/;

            if (this.url == null || this.episodes == "1" && this.customInput == null ){
                //数据不全不提交
                this.$message.error('请填写完整参数！');
            } else if (!pattern.test(this.url)){
                this.$message.error('请输入正确主页链接')
            }else if (this.customInput!=null){
                if (!patternCustomEpisodes.test(this.customInput)){
                    this.$message.error('请输入正确自定义剧集数')
                }else {

                //自定义剧集
                    this.finalEpisodes = this.customInput;

                    var _this = this;

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

                            _this.loading = true;
                            console.log("args:"+_this.args);
                            //提交表单
                            axios({
                                method: "post",
                                url: "./user/xmlFactoryServlet",
                                data: "url=" + _this.url + "&type=" + _this.type + "&frequency=" + _this.frequency + "&survival=" + _this.survival + "&episodes=" + _this.finalEpisodes+"&args="+encodeURIComponent(_this.args)
                            }).then(function (response) {
                                _this.loading = false;
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
            } else {

                //默认值
                this.finalEpisodes = this.episodes;
                var _this = this;

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

                            _this.loading = true;
                            //提交表单
                            axios({
                                method: "post",
                                url: "./user/xmlFactoryServlet",
                                data: "url=" + _this.url + "&type=" + _this.type + "&frequency=" + _this.frequency + "&survival=" + _this.survival + "&episodes=" + _this.finalEpisodes+"&args="+encodeURIComponent(_this.args)

                            }).then(function (response) {
                                _this.loading = false;
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
