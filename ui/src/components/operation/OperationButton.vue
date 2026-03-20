<template>
  <div>
    <div class="operation-button">
      <el-dropdown trigger="click">
        <span class="el-dropdown-link">
          <el-button type="primary" plain icon="el-icon-s-operation" circle></el-button>
        </span>
        <!-- 使用 @click.native 可以强制监听原生 DOM 的 click 事件，绕开 Element UI 某些组件内部事件封装的问题。 -->
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item
            icon="el-icon-chat-dot-square"
            @click.native="$store.operationButton.realTimelogVisible = true"
            >实时日志</el-dropdown-item
          >
          <el-dropdown-item
            icon="el-icon-chat-line-square"
            @click.native="$store.operationButton.historyLogVisible = true"
            >历史日志</el-dropdown-item
          >
          <el-dropdown-item icon="el-icon-circle-close" @click.native="logout()"
            >退出</el-dropdown-item
          >
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { setGlobalStore } from '../../store'
import axios from 'axios';
export default {
  methods: {
    //登出
    logout() {
      axios
          .post('/api/user/logout')
          .then(response => {
            console.log(response.data); 
            //删除token
            localStorage.removeItem("token");
            setGlobalStore("token", null);    
            this.$router.push("/login");
          })
          .catch(error => {
            console.error(error);
          });
    },
  },
};
</script>

<style scoped>
.operation-button {
  position: fixed;
  top: 20px; /* 从顶部开始 20px */
  right: 20px; /* 右侧 20px */
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 999;
}
</style>
