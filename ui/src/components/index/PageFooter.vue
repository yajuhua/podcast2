<template>
    <!-- 页面底部的控制按钮 -->
    <div class="footer">
      <span style="cursor: pointer" @click="openGithub()">由Podcast2驱动</span>
      <span>{{ '\t' }}</span>
      <span>|</span>
      <span>{{ '\t' }}</span>
      <span style="cursor: pointer" @click="$router.push('/manager')">管理</span>
    </div>
</template>
<script>
export default {
  name: 'PageFooter',
  methods: {
    openGithub() {
    window.open('https://github.com/yajuhua/podcast2', '_blank');
  }
  }
}
</script>
<style scoped>
.footer {
  text-align: center;
  font-size: 12px;
  color: 235, 238, 245;
  background: #ffffff;
  padding: 10px;
}
</style>