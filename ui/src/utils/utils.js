// 工具库写法
export function copy(content) {
    return navigator.clipboard.writeText(content)
      .then(() => true)
      .catch(() => false);
  }