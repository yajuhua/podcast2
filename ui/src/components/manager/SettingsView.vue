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
    <!-- openlist -->
    <div>
      <el-form class="demo-form-inline" :model="openListSubmit" label-width="auto" label-position="top"
        :rules="openListRules" ref="openListRef">
        <h4>设置OpenList</h4>
        <el-form-item label="链接" prop="url">
          <el-tooltip class="item" effect="dark" content="如：http://192.168.123.3:5244" placement="top-start">
            <el-input v-model="openListSubmit.url" :placeholder="openListInfo.url" clearable></el-input>
          </el-tooltip>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="openListSubmit.username" :placeholder="openListInfo.username" clearable></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="openListSubmit.password" :placeholder="openListInfo.password" clearable></el-input>
        </el-form-item>

        <el-form-item label="目录" prop="path">
          <el-tooltip class="item" effect="dark" content="如：/podcast2是alist根目录下的podcast2文件夹" placement="top-start">
            <el-input v-model="openListSubmit.path" :placeholder="openListInfo.path" clearable></el-input>
          </el-tooltip>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="openAlist()" v-if="openListInfo.open == false" round
            size="mini">开启</el-button>
          <el-button type="danger" @click="closeAlist()" v-if="openListInfo.open == true" round
            size="mini">关闭</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- Github加速站 -->
    <div>
      <el-form label-position="top">
        <h4>Github加速站</h4>
        <el-form-item label="链接" prop="url">
          <el-input v-model="githubProxy.url" placeholder="请输入Github加速站" />
          <el-tooltip class="item" effect="dark" content="国内无法直接通过yt-dlp更新需要设置Github加速站,当然代理除外。" placement="top-start">
            <i class="el-icon-question"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="updateGithubProxyUrl()" size="mini" round>修改</el-button>
          <el-button type="danger" @click="deleteGithubProxyUrl()" size="mini" round>删除</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--api-->
    <div>
      <div>
        <h4>API</h4>
        <el-input v-model="apiToken.apiToken" readonly>
          <template slot="prepend">apiToken</template>
          <el-button slot="append" @click="copy(apiToken.apiToken)">复制</el-button>
        </el-input>

        <el-row style="margin-top: 15px;">
          <el-button type="primary" v-if="!apiToken.hasApiToken" @click="createApiToken()" round
            size="mini">点击生成</el-button>
          <el-button type="danger" v-else @click="removeApiToken()" round size="mini">点击删除</el-button>
        </el-row>
      </div>

      <div style="margin-top: 15px;">
        <el-form label-width="80px" label-position="top">
          <el-form-item label="api文档">
            <a :href="currentHost + '/doc.html'" target="_blank">
              {{ currentHost + '/doc.html' }}
            </a>
          </el-form-item>

          <el-form-item label="开关">
            <el-select v-model="apiDoc.status">
              <el-option label="开启" :value="true"></el-option>
              <el-option label="关闭" :value="false"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="updateApiDocStatus()" round size="mini">修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <!-- 自定义插件仓库 -->
    <div>
      <el-form label-position="top">
        <h4>自定义插件仓库</h4>
        <el-form-item label="链接" prop="url">
          <el-input v-model="plugin.url" placeholder="请输入插件仓库链接" />
          <el-tooltip class="item" effect="dark" content="支持不同插件仓库" placement="top-start">
            <i class="el-icon-question"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="updatePluginUrl()" size="mini" round>修改</el-button>
          <el-button type="danger" @click="deletePluginUrl()" size="mini" round>删除</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 自动更新插件 -->
    <div>
      <div>
        <h4>插件更新</h4>
        <div @click="autoUpdatePlugin()">
          <el-switch v-model="plugin.autoUpdate" active-text="自动更新"></el-switch>
        </div>
      </div>
    </div>

  </div>
</template>
<script>
import axios from 'axios';
export default {
  computed: {
    currentHost() {
      return window.location.origin;
    }
  },
  mounted() {
    this.getEnclosureDomain();
    this.getSslList();
    this.getSslStatus()
    this.getPath();
    this.getOpenListInfo();
    this.getGithubProxyUrl();
    this.getApiTokenInfo();
    this.getApiDocStatus();
    this.getPluginUrl();
    this.getAutoUpdateStatus()
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
      },
      openListInfo: {
        url: '',
        username: '',
        password: '',
        path: '',
        open: false
      },
      openListSubmit: {
        url: '',
        username: '',
        password: '',
        path: '',
        open: false
      },
      openListRules: {
        url: [
          { required: true, message: '请输入链接', trigger: 'blur' },
          { pattern: /^https?:\/\/[^\s]+$/, message: '请输入合法的链接（http/https 开头）', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        path: [
          { required: true, message: '请输入目录', trigger: 'blur' },
          { pattern: /^\/.+/, message: '目录必须以斜杠 "/" 开头', trigger: 'blur' }
        ]
      },
      githubProxy: {
        url: '',
      },
      apiToken: {
        hasApiToken: false,
        apiToken: ''
      },
      apiDoc: {
        status: false
      },
      plugin: {
        url: '',
        autoUpdate: true
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
          axios.post('/api/user/path?path=' + path)
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
    //开启OpenList
    openAlist() {
      this.$refs.openListRef.validate((valid) => {
        if (!valid) {
          this.$message.error('请先填写完整OpenList信息');
          return;
        }

        this.$confirm('此操作将开启OpenList, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.openListSubmit.open = true;

          axios.post('/api/user/alist/update', this.openListSubmit)
            .then(res => {
              if (res.data.code == '1') {
                this.getOpenListInfo();
                this.$message.success("OpenList 开启成功");
              } else {
                this.$message.error(res.data.msg);
              }
            })
            .catch(() => {
              this.$message.error("请求失败，请检查网络");
            });
        }).catch(() => {
          this.$message.info('已取消开启');
        });
      });
    },
    //关闭alist
    closeAlist() {
      this.$confirm('此操作将关闭OpenList, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.openListSubmit.open = false;
        axios.post('/api/user/alist/update', this.openListSubmit)
          .then(res => {
            if (res.data.code == '1') {
              this.getOpenListInfo();
              this.$message.success("OpenList关闭成功")
            } else {
              this.$message.error(res.data.msg)
            }
          })
      }).catch(() => {
        this.$message.info('已取消关闭')
      });
    },
    //获取OpenList配置信息
    getOpenListInfo() {
      axios.get('/api/user/alist/info')
        .then(res => {
          if (res.data.code == '1') {
            this.openListInfo = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('获取OpenList配置信息失败！')
        })
    },
    //更新GithubProxyUrl
    updateGithubProxyUrl() {
      const urlRegex = /^https?:\/\/(?:www\.)?[\w.-]+(?:\.[a-zA-Z]{2,})+(?:\/[\w-./?%&=]*)?$/;
      const githubProxyUrl = this.githubProxy.url;
      if (urlRegex.test(githubProxyUrl)) {
        this.$confirm('此操作将修改Github加速站, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/api/user/github?githubProxyUrl=' + githubProxyUrl)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('修改成功！')
                this.getGithubProxyUrl();
              } else {
                this.$message.error(res.data.msg)
              }
            })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入格式正确的URL！')
      }
    },
    //删除GithubProxyUrl
    deleteGithubProxyUrl() {
      this.$confirm('此操作将删除Github加速站, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/user/github')
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success("删除Github加速站成功！");
              this.getGithubProxyUrl();

            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
            this.$message.error("删除Github加速站错误！")
            console.log(err)
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取GithubProxyUrl
    getGithubProxyUrl() {
      axios.get('/api/user/github')
        .then(res => {
          if (res.data.code == '1') {
            this.githubProxy.url = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('获取Github加速站失败！')
        })
    },
    copy(content) {
      const textarea = document.createElement('textarea');

      console.log('复制到粘贴板')

      textarea.value = content;
      textarea.setAttribute('readonly', '');
      textarea.style.position = 'absolute';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);

      // 延迟显示复制成功提示
      setTimeout(() => {
        this.$message({
          message: '复制成功！',
          type: 'success'
        });
      }, 100);

      console.log('内容已成功复制到剪贴板');
    },
    //获取apiToken数据
    getApiTokenInfo() {
      axios.get('/api/user/apiTokenInfo')
        .then(res => {
          if (res.data.code == '1') {
            this.apiToken = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('获取apiToken信息失败！')
        })
    },
    //创建apiToken
    createApiToken() {
      axios.get('/api/user/createApiToken')
        .then(res => {
          if (res.data.code == '1') {
            this.apiToken.apiToken = res.data.data;
            this.apiToken.hasApiToken = true;
            this.$message.success("创建apiToken成功！")
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('创建apiToken失败！')
        })
    },
    //移除apiToken
    removeApiToken() {
      axios.delete('/api/user/apiToken')
        .then(res => {
          if (res.data.code == '1') {
            this.apiToken.apiToken = '';
            this.apiToken.hasApiToken = false;
            this.$message.success("移除apiToken成功！")
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('移除apiToken失败！')
        })
    },
    //更新api文档状态
    updateApiDocStatus() {
      axios.post('/api/user/apiDocStatus', this.apiDoc)
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success("修改成功！重启后生效")
            this.getApiDocStatus()
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error(err.toString());
        })
    },
    //获取api文档状态
    getApiDocStatus() {
      axios.get('/api/user/apiDocStatus')
        .then(res => {
          if (res.data.code == '1') {
            this.apiDoc.status = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error(err.toString())
        })
    },
    //更新插件仓库链接
    updatePluginUrl() {
      const urlRegex = /^(https?:\/\/[\w.-]+(?:\/[\w\-./?%&=]*)?)$|^([\w.-]+\/[\w.-]+@[\w.\-/]+)$/;
      const pluginUrl = this.plugin.url;
      if (urlRegex.test(pluginUrl)) {
        this.$confirm('此操作将修改插件仓库链接, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/api/user/plugin?pluginUrl=' + this.plugin.url)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('修改成功！')
                this.getPluginUrl();
              } else {
                this.$message.error(res.data.msg)
              }
            })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入格式正确的URL！')
      }
    },
    //删除自定义插件仓库链接
    deletePluginUrl() {
      this.$confirm('此操作将删除自定义插件仓库链接使用默认, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/user/plugin')
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success("删除自定义插件仓库链接成功！");
              this.getPluginUrl();
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
            this.$message.error("删除自定义插件仓库链接错误！")
            console.log(err)
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取自定义插件仓库链接
    getPluginUrl() {
      axios.get('/api/user/plugin')
        .then(res => {
          if (res.data.code == '1') {
            this.plugin.url = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('获取自定义插件仓库链接失败！')
        })
    },
    //设置插件自动更新
    autoUpdatePlugin() {
      let status = 0;
      if (this.plugin.autoUpdate) {
        status = 1;
      }
      axios.post('/api/plugin/autoUpdate?status=' + status)
        .then(res => {
          if (res.data.code == '1') {
            if (status == '1') {
              this.plugin.autoUpdate = true;
            } else {
              this.plugin.autoUpdate = false;
            }
            console.log('修改成功！')
            this.$message.success('修改成功！');
          } else {
            this.$message.error('修改错误！')
          }
        }).catch(err => {
          this.$message.error('修改错误！');
          console.log(err)
        })
    },
    getAutoUpdateStatus() {
      axios.get('/api/plugin/autoUpdate')
        .then(res => {
          if (res.data.code == '1') {
            if (res.data.data == '1') {
              this.plugin.autoUpdate = true;
            } else {
              this.plugin.autoUpdate = false;
            }
          } else {
            this.$message.error('获取插件自动更新状态失败！');
          }
        }).catch(err => {
          this.$message.error('获取插件自动更新状态失败！');
          console.log(err);
        })
    },
  }
};
</script>