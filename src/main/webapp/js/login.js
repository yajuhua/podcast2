new Vue({
    el:"#app",
    data(){
        return {
            username:"",
            password:""
        }
    },
    methods:{
        submit(){
            if (this.username == "" || this.password == ""){
                //不提交数据
                this.$message.error('请输入用户名和密码！');
            }else {
                var _this = this;
                axios({
                    method: "post",
                    url: "./loginServlet",
                    data: "username=" + _this.username + "&password=" + _this.password
                }).then(function(response) {
                    console.log(response.data)//打印响应数据
                    if (response.data === "ok") {
                        _this.$message({
                            message: '登录成功！',
                            type: 'success'
                        });
                        window.location.href = "index.html";//跳转到订阅列表
                    } else if (response.data === "error"){
                        _this.$message.error('密码或用户名错误！');
                        window.location.href = "login.html";//跳转到订阅列表
                    }
                }).catch(function(error) {
                    console.error(error); // 处理错误情况
                });
            }
        }
    }
})