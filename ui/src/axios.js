// axios.js

import axios from 'axios';
import router from '@/router'; // 假设你有router实例

// 设置请求拦截器，在发送请求前将token添加到请求头中
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.token = token;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 设置响应拦截器，处理401状态码
axios.interceptors.response.use(response => {
  return response;
}, error => {
  if (error.response.status === 401) {
    router.replace('/login'); // 替换当前路由为登录页面，根据你的需求修改路由路径
    
    return new Promise(() => {}); // 返回空的Promise以避免axios捕获到异常
  }
  
  return Promise.reject(error);
});

export default axios;
