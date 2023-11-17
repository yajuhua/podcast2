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
                    url: "./user/loginServlet",
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
                        //window.location.href = "login.html";//跳转到订阅列表
                    }
                }).catch(function(error) {
                    console.error(error); // 处理错误情况
                });
            }
        }
        ,
        //重置密码提示
        resetPassword() {
            this.$alert('在init/system.init文件中添加PASSWORD字段，若没有system.init,请创建。', '重置密码', {
                confirmButtonText: '确定',
            });
        },
        //重置账号
        resetAccount(){
            this.$alert('在init/system.init文件中添加ACCOUNT字段，若没有system.init,请创建。', '重置用户名和密码', {
                confirmButtonText: '确定',
            });
        }
    }
})