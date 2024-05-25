<template>
     <div class="container">
        <div class="login-form" id="app">
            <h2>Podcast2</h2>
            <div>
                <div class="form-group">
                    <input type="text" name="username" placeholder="用户名" v-model="login.username">
                </div>
                <div class="form-group">
                    <input type="password" name="password" placeholder="密码" v-model="login.password" @keydown.enter="submit">
                </div>
                <div class="form-group">
                    <button @click="submit"><span>登录</span></button>
                </div>
                <div class="bottom-links">
                    <a  @click="resetAccount" style="cursor: pointer;">重置账号</a>
                    <a  @click="resetPassword" style="cursor: pointer;">重置密码</a>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      login: {
        username: '',
        password: ''
      }
    };
  },
  methods: {
    submit() {
      if (this.login.username === '' || this.login.password === '') {
        this.$message.error('请输入用户名和密码！');
      } else {
        axios
          .post('/user/login', this.login)
          .then(response => {
            console.log(response.data); // 打印响应数据
            if (response.data.code == '1') {
              this.$message({
                message: '登录成功！',
                type: 'success'
              });
              //将token写入LocalStorage
              localStorage.setItem('token',response.data.data.token);
              this.$router.push('/')
            } else if (response.data.code == '0') {
              this.$message.error(response.data.msg);
            }
          })
          .catch(error => {
            console.error(error); // 处理错误情况
          });
      }
    },
    resetPassword() {
      this.$alert(
        '将config/config.json中initUserNameAndPasswor字段改成true',
        '重置密码',
        {
          confirmButtonText: '确定'
        }
      );
    },
    resetAccount() {
      this.$alert(
        '将config/config.json中initUserNameAndPasswor字段改成true',
        '重置用户名和密码',
        {
          confirmButtonText: '确定'
        }
      );
    }
  }
};
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
/* 共有样式 */
body {
  background-color: #ffffff;
  font-family: Arial, sans-serif;
}

.container {
  background-color: #ffffff;
  font-family: Arial, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 95vh;
}

input:focus {
    outline: none; /* 隐藏默认的黑色边框 */
    border: 1px solid #007bff; /* 自定义选中时的边框样式 */
}

.login-form {
  width: 300px;
  height: 300px;
  padding: 20px;
  background-color: #fff;
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.1);
  border-radius: 25px;
  zoom: 1.2;
}

h2 {
  text-align: center;
  margin-bottom: 15px;
  color: #00A1FF;
}

.form-group {
  margin-bottom: 15px;
}

.form-group input {
  width: 100%;
  height: 55px;
  padding: 8px;
  font-size: 15px;
  color: #D7D2D7;
  border: 1px solid #ccc;
  border-radius: 23px;
  box-sizing: border-box;
  margin: 0 auto;
  display: block;
  aspect-ratio: 1/4;
  background-color: #ffffff;
  text-indent: 10px;
}

.form-group input:focus {
  color: #00A1FF;
  border-color: #00A1FF;
}

.form-group input::placeholder {
  color: #00A1FF;
}

.form-group button {
  width: 100%;
  height: 55px;
  padding-top: calc(100% * 1/9);
  font-size: 14px;
  color: #fff;
  background-color: #00A1FF;
  border: none;
  border-radius: 23px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.form-group button span {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  white-space: nowrap;
  font-size: 20px;
}

.bottom-links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  text-align: center;
}

button:active {
  background-color: #0087C0;
}

.bottom-links a {
  color: #00A1FF;
  text-decoration: none;
  font-size: 14px;
  flex: 1;
  text-align: left;
}

.bottom-links a:last-child {
  text-align: right;
}

/* 媒体查询 */
@media only screen and (max-width: 480px) {

  /* 适配小屏幕手机，宽度小于等于480px */
  .login-form {
    width: 90%;
    height: 250px;
    zoom: 1;
  }

  .form-group input {
    aspect-ratio: 3/4;
  }
}

@media only screen and (min-width: 481px) and (max-width: 768px) {

  /* 适配中等屏幕手机，宽度大于480px小于等于768px */
  .login-form {
    width: 70%;
    height: 300px;
    zoom: 1.2;
  }
}

@media only screen and (min-width: 769px) and (max-width: 1024px) {

  /* 适配大屏幕手机，宽度大于768px小于等于1024px */
  .login-form {
    width: 50%;
    height: 350px;
    zoom: 1.4;
  }
}


/* 添加更多媒体查询以适配其他屏幕尺寸 */
</style>
