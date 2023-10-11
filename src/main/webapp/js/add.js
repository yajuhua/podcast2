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
                if (this.episodes == "1"){
                    this.episodes = this.customInput;
                }

                var _this = this;
                //提交
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
            }
        }
    }
})
