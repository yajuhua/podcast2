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
/**
 * 获取设备类型
 * @returns {'mobile' | 'tablet' | 'desktop'}
 */
export function getDeviceType() {
  const width = window.innerWidth;

  // 优先根据屏幕宽度判断
  if (width <= 768) {
    return 'mobile';
  } else if (width <= 1024) {
    return 'tablet';
  }
  
  return 'desktop';
}

/**
 * 根据设备类型返回适配宽度
 * @returns {string} CSS 宽度，比如 '80%' | '50%' | '40%'
 */
export function adaptWidth() {
  const type = getDeviceType();
  console.log("deviceType:", type);

  switch (type) {
    case 'mobile':
      return '80%';
    case 'tablet':
      return '50%';
    default:
      return '40%';
  }
}

