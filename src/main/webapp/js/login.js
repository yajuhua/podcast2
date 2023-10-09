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
            var _this = this;

            axios({
                method: "post",
                url: "./loginServlet",
                data: "username=" + _this.username + "&password=" + _this.password
            }).then(function(response) {
                console.log(response.data)//打印响应数据
                if (response.data === "ok") {
                    window.location.href = "index.html";//跳转到订阅列表
                } else {
                    window.location.href = "login.html";//跳转到订阅页面
                }
            }).catch(function(error) {
                console.error(error); // 处理错误情况
            });


        }
    }
})