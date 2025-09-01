<template>
        <el-dialog 
        title="二维码" 
        :visible="visible" 
        width="350px"
        @close="handleClose">
      <div>
        <vue-qr :text="url" :size="300">
        </vue-qr>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="handleCopy(url)">复 制 URL</el-button>
      </span>
    </el-dialog>
</template>
<script>
import VueQr from 'vue-qr'
import { copy } from '@/utils/utils';
export default {
    components:{
     VueQr,
  },
  name: 'QuickCode',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    url: {
        type: String,
        default: 'https://bing.com'
    }
  },
  methods: {
    handleClose() {
    this.$emit('update:visible', false); // 通知父组件关闭弹窗
  },
  handleCopy(url){
    if(copy(url)){
        this.$message.success('复制成功！');
        this.handleClose();
    }else{
        this.$message.error('复制失败！');
    }
  }
  }
}
</script>