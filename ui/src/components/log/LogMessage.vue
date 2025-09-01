<template>
  <div v-html="highlightKeywords(message)" class="log-message"></div>
</template>

<script>
export default {
  props: {
    message: {
      type: String,
      required: true
    }
  },
  methods: {
    highlightKeywords(message) {
      // 将关键字替换为带有高亮样式的 HTML
      const highlighted = message
          .replace(/(INFO|ERROR|DEBUG|WARN)\s+([\w.$]+)/g, '<span class="log-class">$1 $2</span>')
          .replace(/INFO/g, '<span class="log-info">INFO</span>')
          .replace(/ERROR/g, '<span class="log-error">ERROR</span>')
          .replace(/DEBUG/g, '<span class="log-debug">DEBUG</span>')
          .replace(/WARN/g, '<span class="log-warn">WARN</span>');

      return highlighted;
    }
  }
};
</script>

<style>
.log-message {
  padding: 5px;
  font-family: Menlo;
}
.log-info {
  color: green; /* INFO 关键字的高亮样式 */
  font-weight: bold; /* 可选：加粗文本 */
}
.log-error {
  color: red; /* ERROR 关键字的高亮样式 */
  font-weight: bold; /* 可选：加粗文本 */
}
.log-class {
  color: #0159a1; /* ERROR 关键字的高亮样式 */
  font-weight: bold; /* 可选：加粗文本 */
}
</style>
