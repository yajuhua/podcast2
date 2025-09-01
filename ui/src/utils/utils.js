// 工具库写法
export async function copy(content) {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    try {
      await navigator.clipboard.writeText(content);
      return true;
    } catch (e) {
      console.error("Clipboard API failed", e);
      return fallbackCopy(content);
    }
  } else {
    return fallbackCopy(content);
  }
}

function fallbackCopy(content) {
  const input = document.createElement('textarea');
  input.value = content;
  input.setAttribute('readonly', '');
  input.style.position = 'absolute';
  input.style.left = '-9999px';
  document.body.appendChild(input);
  input.select();
  const result = document.execCommand('copy');
  document.body.removeChild(input);
  return result;
}
