<template>
  <div>
    <!-- 用户名密码表单 -->
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <h4>用户名密码</h4>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" round size="mini" @click="handleSubmit">更新</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据迁移部分 -->
    <h4>数据迁移</h4>
    <DataMigration />
  </div>
</template>

<script>
// 引入数据迁移组件
import DataMigration from './DataMigration.vue';
import axios from 'axios';

export default {
  name: "UserForm",
  components: {
    DataMigration,
  },
  data() {
    return {
      // 表单数据
      form: {
        username: "",
        password: "",
      },
      // 表单验证规则
      rules: {
        username: [
          { required: true, message: '用户名不能为空', trigger: 'blur' },
          { min: 1, max: 30, message: '用户名必须1-30位', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
          { min: 6, max: 30, message: '密码必须6-30位', trigger: 'blur' },
        ],
      },
    };
  },
  methods: {
    // 提示函数
    showMessage(message, type = 'error') {
      this.$message({
        showClose: true,
        message,
        type
      });
    },

    // 用户名和密码修改
    userAccountChange() {
      const { username, password } = this.form;
      const passwordRegex = /^.{6,30}$/;
      const usernameRegex = /^.{1,30}$/;

      // 验证用户名和密码不能为空
      if (!username || !password) {
        this.showMessage('请先输入用户名和密码！');
        return;
      }

      // 验证密码
      if (!passwordRegex.test(password)) {
        this.showMessage('密码必须6-30位');
        return;
      }

      // 验证用户名
      if (!usernameRegex.test(username)) {
        this.showMessage('用户名必须1-30位');
        return;
      }

      // 确认操作
      this.$confirm('此操作将修改用户名和密码, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 执行API请求
        axios.post('/api/user/change', { username, password })
          .then(res => {
            if (res.data.code == '1') {
              this.showMessage('修改成功！', 'success');
              // 删除token，跳转到登录页
              localStorage.removeItem('token');
              this.$router.push('/login');
            } else {
              this.showMessage(res.data.msg);
            }
          })
          .catch(err => {
            console.error(err);
            this.showMessage('修改错误！');
          });
      }).catch(() => {
        this.showMessage('已取消', 'info');
      });
    },

    // 表单提交处理
    handleSubmit() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.userAccountChange(); // 触发用户名和密码修改操作
        } else {
          this.showMessage('请检查输入！');
        }
      });
    },
  },
};
</script>

<style scoped>

</style>
