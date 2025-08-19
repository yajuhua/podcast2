<!-- SubDetail.vue -->
<template>
  <el-dialog
    title="详细"
    :width="adaptWidth()"
    :visible="visible"
    @close="handleClose"
  >
    <el-form ref="form" :model="data" label-width="auto" label-position="top">
      <el-form-item
        v-for="field in fields"
        :key="field.key"
        :label="field.label"
      >
        <div @click="handleCopy(field.key, data[field.key])" @touchstart="handleCopy(field.key, data[field.key])">
          <el-input :value="data[field.key] || ''" readonly></el-input>
        </div>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
import { copy } from '@/utils/utils';
export default {
  name: 'SubDetail',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    data: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      fields: [
        { key: 'uuid', label: 'UUID' },
        { key: 'equal', label: '比对' },
        { key: 'title', label: '名称' },
        { key: 'link', label: '链接' },
        { key: 'status', label: '状态码' },
        { key: 'description', label: '描述' },
        { key: 'image', label: '封面链接' },
        { key: 'createTime', label: '创建时间' },
        { key: 'checkTime', label: '上次检查更新时间' },
        { key: 'updateTime', label: '上次更新时间' },
        { key: 'type', label: '类型' },
        { key: 'survivalTime', label: '存活时间' },
        { key: 'cron', label: '更新频率' },
        { key: 'plugin', label: '插件名称' },
        { key: 'episodes', label: 'episodes' },
        { key: 'customEpisodes', label: 'customEpisodes' },
        { key: 'isUpdate', label: '继续更新' },
        { key: 'isFilter', label: '是否开启过滤' },
        { key: 'minDuration', label: '过滤最小时长' },
        { key: 'maxDuration', label: '过滤最大时长' },
        { key: 'titleKeywords', label: '过滤标题' },
        { key: 'descKeywords', label: '过滤描述' },
        { key: 'isExtend', label: '是否扩展' },
        { key: 'survivalWay', label: '存活方式' },
        { key: 'keepLast', label: '保留最近' },
      ],
    };
  },
  methods: {
    handleCopy(key, value) {
      if (value !== undefined && value !== null) {
        if(copy(value)){
          this.$message.success('复制成功！');
          console.log('复制成功');
        }else{
          this.$message.error('复制失败！');
          console.log('复制失败');
        }
      } else {
        this.$message.warning(`字段 ${key} 无数据`);
      }
    },
    handleClose() {
    this.$emit('update:visible', false); // 通知父组件关闭弹窗
  },
  //适配宽度
  adaptWidth(){
      let type = this.$deviceType;
      console.log("deviceType: " + type);
      if(type == 'mobile'){
        return '80%';
      }else if (type == 'tablet'){
        return '50%';
      }else {
        return '40%';
      }
    }
  }
};
</script>

<style scoped>
.el-input {
  cursor: pointer;
}
</style>