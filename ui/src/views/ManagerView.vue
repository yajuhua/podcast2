<template>
  <div class="container">
    <!-- 左侧菜单 -->
    <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" :collapse="isCollapse" @select="handleSelect"
      unique-opened="true">
      <!-- 手动展开/收起 -->
      <el-menu-item index="0" @click="toggleMenu">
        <i class="el-icon-s-unfold"></i>
        <span slot="title">{{ isCollapse ? "展开" : "收起" }}</span>
      </el-menu-item>

      <el-menu-item index="1">
        <i class="el-icon-menu"></i>
        <span slot="title">概况</span>
      </el-menu-item>

      <el-menu-item index="2">
        <i class="el-icon-user"></i>
        <span slot="title">用户</span>
      </el-menu-item>

      <el-menu-item index="3">
        <i class="el-icon-setting"></i>
        <span slot="title">设置</span>
      </el-menu-item>

      <el-menu-item index="4">
        <i class="el-icon-download"></i>
        <span slot="title">上传与下载</span>
      </el-menu-item>

      <el-menu-item index="5">
        <i class="el-icon-s-grid"></i>
        <span slot="title"> 插件</span>
      </el-menu-item>

      <el-menu-item index="6" @click="($router.push('/'))">
        <i class="el-icon-s-home"></i>
        <span slot="title">订阅列表</span>
      </el-menu-item>

      <el-menu-item index="7">
        <i class="el-icon-s-order"></i>
        <span slot="title">后台任务</span>
      </el-menu-item>

      <el-menu-item index="8">
        <i class="el-icon-info"></i>
        <span slot="title">关于</span>
      </el-menu-item>
    </el-menu>

    <!-- 右侧内容区 -->
    <div class="main">
      <div v-show="activeMenu == '1'">
        <h2>概况</h2>
        <GeneralOverview />
      </div>

      <!-- 用户 -->
      <div v-show="activeMenu == '2'">
        <h2>用户</h2>
        <User />
      </div>

      <!-- 设置 -->
      <div v-show="activeMenu == '3'">
        <h2>设置</h2>
        <SettingsView></SettingsView>
      </div>

      <!-- 上传与下载 -->
      <div v-show="activeMenu == 4">
        <h2>上传与下载</h2>
        <DownloadUpload />
      </div>

      <div v-show="activeMenu == '5'">
        <h2>插件</h2>
        <PluginManager />
      </div>

      <div v-show="activeMenu == '7'">
        <h2>后台任务</h2>
        <BackgroundTasks/>
      </div>

      <div v-show="activeMenu == '8'">
        <ReadmeReader url="https://raw.githubusercontent.com/yajuhua/podcast2/refs/heads/v2/README.md" />
      </div>
    </div>
  </div>
</template>

<script>
import GeneralOverview from '../components/manager/GeneralOverview.vue'
import ReadmeReader from '../components/manager/ReadmeReader.vue'
import PluginManager from '../components/manager/PluginManager.vue'
import DownloadUpload from '../components/manager/DownloadUpload.vue'
import User from '@/components/manager/User.vue';
import SettingsView from '@/components/manager/SettingsView.vue';
import BackgroundTasks from "@/components/manager/BackgroundTasks.vue";
export default {
  components: {BackgroundTasks, GeneralOverview, ReadmeReader, PluginManager, DownloadUpload, User, SettingsView },
  data() {
    return {
      isCollapse: false, // 是否收起
      activeMenu: "1", // 当前选中菜单
      form: {
        username: "",
        password: "",
      },
    };
  },
  mounted() {
    this.checkScreenSize();
    window.addEventListener("resize", this.checkScreenSize);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.checkScreenSize);
  },
  methods: {
    handleSelect(key) {
      if(key != '0'){
        this.activeMenu = key;
      }
    },
    toggleMenu() {
      this.isCollapse = !this.isCollapse; // 手动切换
    },
    checkScreenSize() {
      // 自动模式：小屏收起，大屏展开
      this.isCollapse = window.innerWidth < 768;
    },
  },
};
</script>

<style>
.container {
  display: flex;
  height: 100vh;
}

.el-menu-vertical-demo {
  min-height: 100vh;
  transition: width 0.3s;
}

.main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
