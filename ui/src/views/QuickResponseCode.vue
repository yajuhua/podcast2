<template>
  <div>
     <vue-qr
        :text="imgUrl"
        :size="250"
        :logoSrc="logo"
        :logoScale="0.2">
     </vue-qr>
  </div>
</template>

<script>
import VueQr from 'vue-qr'
export default {
  components:{
     VueQr,
  },
  data() {
     return {
        imgUrl: 'https://baidu.com',
     }
  },
}
</script>
