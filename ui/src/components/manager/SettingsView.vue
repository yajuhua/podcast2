<template>
  <div>
    <!-- 自定义域名 -->
    <div>
      <el-form :model="domain" :rules="rules" ref="formRef" label-position="top">
        <h4>自定义域名</h4>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="域名" prop="domain">
              <el-input v-model="domain.value" placeholder="请输入自定义域名" />
              <el-tooltip class="item" effect="dark" content="如https://yajuhua.github.io:8088" placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" round size="mini" @click="changeDomain">更新</el-button>
          <el-button type="danger" round size="mini" @click="deleteDomain">删除</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 开启https -->
    <div>
      <h4>开启HTTPS</h4>
      <div v-if="cert.list.length > 0" @click="switchSsl()">
        <el-switch v-model="cert.switchSsl" active-text="开启"></el-switch>
      </div>
      <br>
      <el-upload class="upload-demo" ref="uploadCertFile" action="/user/cert" :on-change="certFilehandleChange"
        :on-remove="certFilehandleRemove" :file-list="cert.fileList" :auto-upload="false" :multiple="true" name="files"
        v-if="cert.list.length == 0">
        <el-button slot="trigger" size="mini" type="primary" round>选取文件</el-button>
        <el-button style="margin-left: 10px;" size="mini" type="success" round @click="uploadCert">上传
        </el-button>
        <div slot="tip" class="el-upload__tip">只能上传crt和key文件</div>
      </el-upload>
      <el-table ref="multipleTable" :data="cert.list" tooltip-effect="dark" style="width: 100%">
        <el-table-column>
          <el-row>
            <el-tag style="margin-right: 8px;">crt</el-tag>
            <el-tag type="success">key</el-tag>
          </el-row>
        </el-table-column>

        <el-table-column label="操作" width="180">
          <el-button type="danger" size="mini" round @click="deleteCert()">删除</el-button>
        </el-table-column>
      </el-table>

    </div>
    <!-- 访问路径 -->
    <div>
      <el-form ref="formRef" label-position="top">
        <h4>访问路径</h4>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="路径" prop="path">
              <el-input v-model="path.value" placeholder="请输入访问路径" />
              <el-tooltip class="item" effect="dark" content="如果设置为podcast2,那么面板访问路径/p/podcast2" placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" round size="mini" @click="updatePath()">更新</el-button>
          <el-button type="danger" round size="mini" @click="deletePath()">删除</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import axios from 'axios';
export default {
  mounted() {
    this.getEnclosureDomain();
    this.getSslList();
    this.getSslStatus()
    this.getPath();
  },
  data() {
    return {
      domain: {
        rules: [
          { required: true, message: '请输入自定义域名', trigger: 'blur' },
          { type: 'url', message: '请输入有效的域名', trigger: ['blur', 'change'] }
        ],
        value: ''
      },
      cert: {
        fileList: [],
        fileFormList: [],
        list: [],
        switchSsl: false
      },
      path: {
        value: ''
      }
    }
  },
  methods: {
    //修改附件域名
    changeDomain() {
      const domainRegex = /(http|https):\/\/([\w.]+\/?)\S*/;
      const domain = this.domain.value;
      if (domainRegex.test(domain)) {
        this.$confirm('此操作将修改附件域名, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/api/user/enclosureDomain?domain=' + domain)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('修改成功！');
                this.getEnclosureDomain();
              } else {
                this.$message.error(res.data.msg)
              }
            })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入正确域名！')
      }
    },

    //删除自定义域名
    deleteDomain() {
      this.$confirm('此操作将删除附件自定义域名, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/user/enclosureDomain')
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('删除成功！');
              this.getEnclosureDomain();
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
            console.log(err)
            this.$message.error('删除失败！')
          })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },
    //获取附件自定义域名
    getEnclosureDomain() {
      axios.get('/api/user/enclosureDomain')
        .then(res => {
          this.domain.value = res.data.data;
        }).catch(err => {
          console.log(err)
          this.$message.error('获取附件自定义域名失败！')
        })
    },
    //设置ssl
    switchSsl() {
      axios.post('/api/user/switchSsl?status=' + this.cert.switchSsl)
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success('设置成功，重启后生效！');
          } else {
            this.$message({
              showClose: true,
              message: res.data.msg,
              type: 'error'
            });
          }
        }).catch(err => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '设置ssl失败！',
            type: 'error'
          });

        })
    },
    //获取ssl列表
    getSslList() {
      axios.get('/api/user/cert')
        .then(res => {
          if (res.data.code == '1') {
            this.cert.list = res.data.data;
          } else {
            this.$message({
              showClose: true,
              message: res.data.msg,
              type: 'error'
            });
            return false;
          }
        }).catch(err => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '获取ssl列表!',
            type: 'error'
          });

        })
    },
    //删除ssl
    deleteCert() {
      this.$confirm('此操作将删除证书和密钥, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/user/cert')
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('删除成功！')
              this.getSslList();
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
            console.log(err)
            this.$message.error('删除失败！')
          })
      }).catch(() => {
        this.$message({
          showClose: true,
          message: '已取消',
          type: 'info'
        });
      })
    },
    //获取ssl状态
    getSslStatus() {
      axios.get('/api/user/sslStatus')
        .then(res => {
          if (res.data.code == '1') {
            this.cert.switchSsl = res.data.data;
          } else {
            this.$message.error(res.data.msg)
          }
        }).catch(err => {
          console.log(err)
          this.$message.error('获取ssl状态失败！')
        })
    },
    certFilehandleChange(file, fileList) {
      this.cert.fileList = fileList;
    },
    certFilehandleRemove(file, fileList) {
      this.cert.fileList = fileList;
    },
    //上传证书和密钥
    uploadCert() {
      let fileList = this.cert.fileList;
      console.log(fileList)
      if (fileList.length < 2) {
        this.$message.error("crt和key都要上传")
        return;
      }
      if (fileList.length > 2) {
        this.$message.error("只能上传crt和key文件");
        return;
      }

      for (let i = 0; i < fileList.length; i++) {
        let sp = fileList[i].name.split('.');
        let ext = sp[sp.length - 1];
        console.log(ext)
        if (!(ext == 'crt' || ext == 'key')) {
          this.$message.error('只能上传crt和key文件')
          return;
        }
        fileList[i].name = ext;
      }

      //构建一个表单把文件传进去
      let param = new FormData()
      for (let index in this.$refs.uploadCertFile.uploadFiles) {
        param.append("files", this.$refs.uploadCertFile.uploadFiles[index].raw)
      }

      axios.post('/api/user/cert', param, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success('上传成功,重启后生效');
            this.getSslList();
          } else {
            this.$message.error(res.data.msg);
          }

        })
        .catch(error => {
          console.error(error);
        });
    },
    //更新访问路径
    updatePath() {
      const pathRegex = /^[a-zA-Z0-9\-_]{1,30}$/;
      const path = this.path.value;
      if (path.length > 0 && pathRegex.test(path)) {
        this.$confirm('此操作将修改面板访问路径, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/api/user/path?path=' +  path)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('修改成功！')
                this.logout();
              } else {
                this.$message.error(res.data.msg)
              }
            })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入格式正确的路径！')
      }
    },
    //删除路径
    deletePath() {
      this.$confirm('此操作将删除面板访问路径, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/user/path')
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success("删除面板访问路径成功！");
              this.getPath();
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
            this.$message.error("删除面板访问路径错误！")
            console.log(err)
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取访问路径
    getPath() {
      axios.get('/api/user/path')
        .then(res => {
          if (res.data.code == '1') {
            this.path.value = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('获取面板访问路径失败！')
        })
    },
    //登出
    logout() {
      //删除token
      localStorage.removeItem('token');
      this.$router.push('/login')
    },
  }
};
</script>