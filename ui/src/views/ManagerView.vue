<template>
  <div class="container">
    <!-- 左侧菜单 -->
    <el-menu
      :default-active="activeMenu"
      class="el-menu-vertical-demo"
      :collapse="isCollapse"
      @select="handleSelect"
      unique-opened="true"
    >
      <!-- 手动展开/收起 -->
      <el-menu-item index="0" @click="toggleMenu">
        <i class="el-icon-menu"></i>
        <span slot="title">{{ isCollapse ? "展开" : "收起" }}</span>
      </el-menu-item>

      <el-menu-item index="1">
        <i class="el-icon-menu"></i>
        <span slot="title">概况</span>
      </el-menu-item>

      <el-menu-item index="2">
        <i class="el-icon-menu"></i>
        <span slot="title">用户</span>
      </el-menu-item>

      <el-menu-item index="3">
        <i class="el-icon-location"></i>
          <span slot="title">设置</span>
      </el-menu-item>

      <el-menu-item index="4">
        <i class="el-icon-location"></i>
          <span slot="title">上传与下载</span>
      </el-menu-item>

      <el-menu-item index="5">
        <i class="el-icon-location"></i>
          <span slot="title">插件</span>
      </el-menu-item>

      <el-menu-item index="6" @click="($router.push('/'))">
        <i class="el-icon-menu"></i>
        <span slot="title">订阅列表</span>
      </el-menu-item>

      <el-menu-item index="7">
        <i class="el-icon-menu"></i>
        <span slot="title">关于</span>
      </el-menu-item>
    </el-menu>

    <!-- 右侧内容区 -->
    <div class="main">
      <div v-show="activeMenu == '1'">
        <h2>概况</h2>
        <GeneralOverview/>
      </div>

      <!-- 用户 -->
      <div v-show="activeMenu == '2'">
        <el-form :model="form" label-position="top">
          <h4>用户名密码</h4>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="用户名">
                <el-input v-model="form.username" placeholder="请输入用户名" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="密码">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" round size="mini">更新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 上传与下载 -->
      <div v-show="activeMenu == 4">
        <DownloadUpload/>
      </div>

      <div v-show="activeMenu == '5'">
        <h2>插件</h2>
        <PluginManager/>
      </div>

      <div v-show="activeMenu == '7'">
        <ReadmeReader url="https://raw.githubusercontent.com/yajuhua/podcast2/refs/heads/v2/README.md"/>
      </div>
    </div>
  </div>
</template>

<script>
import GeneralOverview from '../components/manager/GeneralOverview.vue'
import ReadmeReader from '../components/manager/ReadmeReader.vue'
import PluginManager from '../components/manager/PluginManager.vue'
import DownloadUpload from '../components/manager/DownloadUpload.vue'
export default {
components: {GeneralOverview, ReadmeReader, PluginManager, DownloadUpload},
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
      this.activeMenu = key;
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
