<template>
  <div v-if="html" class="markdown-body" v-html="html"></div>
  <div v-else>加载中...</div>
</template>

<script>
import { marked } from "marked";
import hljs from "highlight.js/lib/core";
import json from "highlight.js/lib/languages/json";
import bash from "highlight.js/lib/languages/bash";
hljs.registerLanguage("json", json);
hljs.registerLanguage("bash", bash);
import "github-markdown-css/github-markdown.css";
import "highlight.js/styles/github.css";

export default {
  name: "ReadmeRenderer",
  props: {
    url: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      html: ""
    };
  },
  created() {
    // 配置 marked
    marked.setOptions({
      highlight(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
          return hljs.highlight(code, { language: lang }).value;
        }
        return hljs.highlightAuto(code).value;
      }
    });

    this.loadReadme();
  },
  methods: {
    async loadReadme() {
      try {
        const res = await fetch(this.url);
        const markdown = await res.text();
        this.html = marked.parse(markdown);
      } catch (err) {
        this.html = `<p style="color:red">加载失败: ${err.message}</p>`;
      }
    }
  }
};
</script>

<style>
.markdown-body {
  box-sizing: border-box;
  min-width: 200px;
  max-width: 980px;
  margin: 20px auto; /* 让上下留点空间 */
  padding: 45px;
  border-radius: 6px;                 /* 圆角 */
  background-color: #fff;             /* 白色背景，保证不是透明 */
  color: #24292e;                     /* GitHub 默认文字颜色 */
  box-shadow: 0 0 10px rgba(0,0,0,0.05); /* 阴影 */
}

</style>