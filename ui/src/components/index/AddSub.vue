<template>
  <div>
    <!-- 添加订阅 -->
    <el-dialog v-loading="loading" title="添加订阅" :visible="visible" :width="adaptWidth()" @close="handleClose">
      <el-form ref="form" label-width="auto" label-position="top">

        <!-- 创建订阅类型 -->
        <el-form-item label="创建类型">
          <el-select v-model="addSub.subType" placeholder="请选择创建订阅类型">
            <el-option label="默认" value="plugin"></el-option>
            <el-option label="空订阅" value="empty"></el-option>
          </el-select>
        </el-form-item>

        <!-- 默认添加订阅 -->
        <span v-if="addSub.subType == 'plugin'">
          <el-form-item label="主页链接">
            <el-input v-model="addSub.url" placeholder="请输入主页链接"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="addSub.type" placeholder="请选择类型">
              <el-option label="视频" value="Video"></el-option>
              <el-option label="音频" value="Audio"></el-option>
            </el-select>
          </el-form-item>

          <!-- 订阅同步 -->
          <el-form-item label="同步方式">
            <el-select v-model="addSub.syncWay" placeholder="请选择订阅同步方式">
              <el-option label="最新" value="latest"></el-option>
              <el-option label="最近" value="recent"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="节目存活">
            <el-select v-model="addSub.survivalWay" placeholder="请选择节目存活方式">
              <el-option label="保留时间" value="keepTime"></el-option>
              <el-option label="保留最近" value="keepLast"></el-option>
            </el-select>
          </el-form-item>

          <!--  保留时间   -->
          <span v-if="addSub.survivalWay == 'keepTime'">
            <el-form-item label="保留时间">
              <el-select v-model="addSub.survivalTime" placeholder="请选择存活时间">
                <el-option label="1天" value="86400"></el-option>
                <el-option label="3天" value="259200"></el-option>
                <el-option label="7天" value="604800"></el-option>
                <el-option label="15天" value="1296000"></el-option>
                <el-option label="30天" value="2592000"></el-option>
                <el-option label="永久" value="-1"></el-option>
                <el-option label="自定义" value="-2"></el-option>
              </el-select>
            </el-form-item>

            <!--    自定义保留时间    -->
            <el-form-item label="自定义" v-if="addSub.survivalTime == '-2'">
              <div style="margin-top: 15px">
                <el-input v-model.number="addSub.customSurvivalTime" placeholder="请输入自定义保留时间" :min="1" type="number"
                  size="medium" :style="{ width: '250px' }" class="input-with-select">
                  <el-select v-model="addSub.survivalTimeUnit" placeholder="时间单位" slot="append"
                    :style="{ width: '100px' }">
                    <el-option label="秒钟" value="1"></el-option>
                    <el-option label="分钟" value="60"></el-option>
                    <el-option label="小时" value="3600"></el-option>
                    <el-option label="天" value="86400"></el-option>
                    <el-option label="月" value="2592000"></el-option>
                    <el-option label="年" value="31104000"></el-option>
                  </el-select>
                </el-input>
              </div>
            </el-form-item>
          </span>

          <!--  保留最近 -->
          <span v-if="addSub.survivalWay == 'keepLast'">
            <el-form-item label="保留最近">
              <el-select v-model="addSub.keepLast" placeholder="请选择保留最近">
                <el-option label="最近10期" value="10"></el-option>
                <el-option label="最近15期" value="15"></el-option>
                <el-option label="最近30期" value="30"></el-option>
                <el-option label="自定义" value="-1"></el-option>
              </el-select>
            </el-form-item>

            <!--    自定义保留最近    -->
            <el-form-item label="自定义" v-if="addSub.keepLast == -1">
              <div style="margin-top: 15px">
                <el-input v-model.number="addSub.customKeepLast" placeholder="请输入保留最近" :min="1" type="number"
                  size="medium" :style="{ width: '250px' }" class="input-with-select">
                </el-input> 集
              </div>
            </el-form-item>
          </span>
            <el-form-item label="轮询方式">
            <el-select v-model="addSub.scheduleType" placeholder="请选择轮询方式">
              <el-option label="间隔轮询" value="cron"></el-option>
              <el-option label="Cron表达式" value="cron_expression"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="间隔轮询" v-if="addSub.scheduleType == 'cron'">
            <el-select v-model="addSub.cron" placeholder="请选择更新频率">
              <el-option label="20分钟" value="1200"></el-option>
              <el-option label="30分钟" value="1800"></el-option>
              <el-option label="60分钟" value="3600"></el-option>
              <el-option label="2个小时" value="7200"></el-option>
              <el-option label="6个小时" value="21600"></el-option>
              <el-option label="12个小时" value="43200"></el-option>
              <el-option label="1天" value="86400"></el-option>
              <el-option label="2天" value="172800"></el-option>
              <el-option label="4天" value="345600"></el-option>
              <el-option label="7天" value="604800"></el-option>
              <el-option label="自定义" value="-1"></el-option>
            </el-select>
          </el-form-item>
          <!--    自定义更新频率    -->
          <el-form-item label="自定义" v-if="addSub.cron == '-1'">
            <div style="margin-top: 15px">
              <el-input v-model.number="addSub.customCron" placeholder="请输入更新频率" :min="1" type="number" size="medium"
                :style="{ width: '250px' }" class="input-with-select">
                <el-select v-model="addSub.cronUnit" placeholder="更新频率单位" slot="append" :style="{ width: '100px' }">
                  <el-option label="秒钟" value="1"></el-option>
                  <el-option label="分钟" value="60"></el-option>
                  <el-option label="小时" value="3600"></el-option>
                  <el-option label="天" value="86400"></el-option>
                </el-select>
              </el-input>
            </div>
          </el-form-item>
          <!-- cron表达式 -->
          <el-form-item label="Cron表达式" v-if="addSub.scheduleType == 'cron_expression'">
            <el-popover v-model="cronPopover">
              <cron @change="changeCronExpression" @close="cronPopover=false" i18n="cn"></cron>
              <el-input slot="reference" @click="cronPopover=true" v-model="addSub.cronExpression" placeholder="请输入定时策略"></el-input>
            </el-popover>
          </el-form-item>
          <el-form-item label="过滤器">
            <el-select v-model="addSub.isFilter">
              <el-option label="禁用" value="0"></el-option>
              <el-option label="启用" value="1"></el-option>
            </el-select>
          </el-form-item>
          <div v-show="addSub.isFilter == '1'">
            <el-form-item label="最小时长">
              <el-input-number v-model="addSub.minDuration" :min="-1"> 秒</el-input-number>
            </el-form-item>
            <el-form-item label="最大时长">
              <el-input-number v-model="addSub.maxDuration" :min="-1"> 秒</el-input-number>
            </el-form-item>
            <el-form-item label="标题">
              <el-tag :key="keyword" v-for="keyword in addSub.titleKeywords" closable :disable-transitions="false"
                @close="filterHandleClose('title', keyword)">
                {{ keyword }}
              </el-tag>
              <el-input class="input-new-tag" v-if="addSub.titleInputVisible" v-model="addSub.titleInputValue"
                ref="saveTagInput" size="small" @keyup.enter.native="filterHandleInputConfirm('title')"
                @blur="filterHandleInputConfirm('title')">
              </el-input>
              <el-button v-else class="button-new-tag" size="small" @click="filterShowInput('title')">+ 关键字</el-button>
            </el-form-item>
            <el-form-item label="描述">
              <el-tag :key="keyword" v-for="keyword in addSub.descKeywords" closable :disable-transitions="false"
                @close="filterHandleClose('desc', keyword)">
                {{ keyword }}
              </el-tag>
              <el-input class="input-new-tag" v-if="addSub.descInputVisible" v-model="addSub.descInputValue"
                ref="saveTagInput" size="small" @keyup.enter.native="filterHandleInputConfirm('desc')"
                @blur="filterHandleInputConfirm('desc')">
              </el-input>
              <el-button v-else class="button-new-tag" size="small" @click="filterShowInput('desc')">+ 关键字</el-button>
            </el-form-item>
          </div>

          <el-form-item label="剧集选择">
            <el-select v-model="addSub.episodes" placeholder="请选择剧集">
              <el-option label="最新一集" value="0"></el-option>
              <el-option label="最近30集" value="-1"></el-option>
              <el-option label="自定义剧集" value="1"></el-option>
            </el-select>
          </el-form-item>

          <div v-show="addSub.episodes == '1'">
            <el-form-item label="自定义">
              <el-input v-model="addSub.customEpisodes"></el-input>
              <el-tooltip class="item" effect="dark" content="在英文状态下输入1,2-5将下载列表中序号1到5集的节目" placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
          </div>

          <el-form-item label="存放位置">
            <el-select v-model="addSub.status" placeholder="请选择存放位置">
              <el-option label="本地" value="21"></el-option>
              <el-option label="alist" value="22"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="更多选项">
            <el-button @click="getExtendList">更多选项</el-button>
          </el-form-item>
          <!-- 扩展选项 -->
          <div v-show="addSub.isExtend == '1'">
            <!-- select选择框 -->
            <div v-for="(select, selectIndex) in addSub.extendList.selectList" :key="select.id">
              <el-form-item :label="select.name">
                <el-select v-model="addSub.selectListData[selectIndex].content">
                  <span v-show="false">{{ addSub.selectListData[selectIndex].name = select.name }}</span>
                  <el-option v-for="(option) in select.options" :key="option" :label="option"
                    :value="option"></el-option>
                </el-select>
              </el-form-item>
            </div>
            <!-- 输入框 -->
            <div v-for="(input, inputIndex) in addSub.extendList.inputList" :key="input.id">
              <span v-show="false">{{ addSub.inputListData[inputIndex].name = input.name }}</span>
              <el-form-item :label="input.name">
                <el-input v-model="addSub.inputListData[inputIndex].content"></el-input>
              </el-form-item>
            </div>
          </div>
        </span>

        <!-- 创建空订阅  -->
        <span v-if="addSub.subType == 'empty'">
          <el-form-item label="名称">
            <el-input v-model="addSub.title" placeholder="请输入自定义订阅名称"></el-input>
          </el-form-item>

          <el-form-item label="封面">
            <el-input v-model="addSub.image" placeholder="请输入自定义订阅封面链接"></el-input>
          </el-form-item>

          <el-form-item label="描述">
            <el-input v-model="addSub.description" placeholder="请输入自定义订阅描述"></el-input>
          </el-form-item>

          <el-form-item label="节目存活">
            <el-select v-model="addSub.survivalWay" placeholder="请选择节目存活方式">
              <el-option label="保留时间" value="keepTime"></el-option>
              <el-option label="保留最近" value="keepLast"></el-option>
            </el-select>
          </el-form-item>

          <!--  保留时间   -->
          <span v-if="addSub.survivalWay == 'keepTime'">
            <el-form-item label="保留时间">
              <el-select v-model="addSub.survivalTime" placeholder="请选择存活时间">
                <el-option label="1天" value="86400"></el-option>
                <el-option label="3天" value="259200"></el-option>
                <el-option label="7天" value="604800"></el-option>
                <el-option label="15天" value="1296000"></el-option>
                <el-option label="30天" value="2592000"></el-option>
                <el-option label="永久" value="-1"></el-option>
                <el-option label="自定义" value="-2"></el-option>
              </el-select>
            </el-form-item>

            <!--    自定义保留时间    -->
            <el-form-item label="自定义" v-if="addSub.survivalTime == '-2'">
              <div style="margin-top: 15px">
                <el-input v-model.number="addSub.customSurvivalTime" placeholder="请输入自定义保留时间" :min="1" type="number"
                  size="medium" :style="{ width: '250px' }" class="input-with-select">
                  <el-select v-model="addSub.survivalTimeUnit" placeholder="时间单位" slot="append"
                    :style="{ width: '100px' }">
                    <el-option label="秒钟" value="1"></el-option>
                    <el-option label="分钟" value="60"></el-option>
                    <el-option label="小时" value="3600"></el-option>
                    <el-option label="天" value="86400"></el-option>
                    <el-option label="月" value="2592000"></el-option>
                    <el-option label="年" value="31104000"></el-option>
                  </el-select>
                </el-input>
              </div>
            </el-form-item>
          </span>

          <!--  保留最近 -->
          <span v-if="addSub.survivalWay == 'keepLast'">
            <el-form-item label="保留最近">
              <el-select v-model="addSub.keepLast" placeholder="请选择保留最近">
                <el-option label="最近10期" value="10"></el-option>
                <el-option label="最近15期" value="15"></el-option>
                <el-option label="最近30期" value="30"></el-option>
                <el-option label="自定义" value="-1"></el-option>
              </el-select>
            </el-form-item>

            <!--    自定义保留最近    -->
            <el-form-item label="自定义" v-if="addSub.keepLast == -1">
              <div style="margin-top: 15px">
                <el-input v-model.number="addSub.customKeepLast" placeholder="请输入保留最近" :min="1" type="number"
                  size="medium" :style="{ width: '250px' }" class="input-with-select">
                </el-input> 集
              </div>
            </el-form-item>
          </span>

          <el-form-item label="存放位置">
            <el-select v-model="addSub.status" placeholder="请选择存放位置">
              <el-option label="本地" value="21"></el-option>
              <el-option label="alist" value="22"></el-option>
            </el-select>
          </el-form-item>
        </span>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="addSubCommit" :icon="addSubStatus">添加订阅</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import axios from 'axios'
import {cron} from 'vue-cron'
export default {
  name: 'AddSub',
  components: { cron },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    subData: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      addSub: {
        url: '',
        type: 'Audio',
        survivalTime: '604800',
        cron: '1200',
        plugin: '',
        episodes: '0',
        customEpisodes: '',
        isUpdate: '1',
        isFilter: '0',
        maxDuration: -1,
        minDuration: -1,
        titleKeywords: [],
        descKeywords: [],
        isExtend: '0',
        inputAndSelectDataList: [],
        titleInputValue: '',
        titleInputVisible: false,
        descInputValue: '',
        descInputVisible: false,
        extendList: { inputList: [], selectList: [] },
        inputListData: [],
        selectListData: [],
        status: '21',
        cronUnit: '1',
        customCron: '0',
        survivalWay: 'keepTime',//默认
        keepLast: '',
        customKeepLast: '',//自定义保留最近N期节目
        customSurvivalTime: '',//自定义设置存活时间
        survivalTimeUnit: '1',//存活时间单位
        subType: 'plugin',//创建订阅方式，默认是plugin
        title: '',
        image: '',
        description: '',
        syncWay: 'latest',//同步方式
        scheduleType: 'cron',
        cronExpression: ''

      },
      //初始数据
      initAddSub: {
        url: '',
        type: 'Audio',
        survivalTime: '604800',
        cron: '1200',
        plugin: '',
        episodes: '0',
        customEpisodes: '',
        isUpdate: '1',
        isFilter: '0',
        maxDuration: -1,
        minDuration: -1,
        titleKeywords: [],
        descKeywords: [],
        isExtend: '0',
        inputAndSelectDataList: [],
        titleInputValue: '',
        titleInputVisible: false,
        descInputValue: '',
        descInputVisible: false,
        extendList: { inputList: [], selectList: [] },
        inputListData: [],
        selectListData: [],
        status: '21',
        survivalWay: 'keepTime',//默认
        keepLast: '',
        subType: 'plugin',//创建订阅方式，默认是plugin
        syncWay: '',//同步方式
        scheduleType: 'cron',
        cronExpression: ''
      },
      loading: false,
      addSubStatus: '',
      cronPopover: false
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false); // 通知父组件关闭弹窗
    },
    //添加时tag标签的关闭
    filterHandleClose(name, content) {
      this.addSub[name + 'Keywords'].splice(this.addSub[name + 'Keywords'].indexOf(content), 1);
    },
    //添加时tag标签的
    filterShowInput(name) {
      this.addSub[name + 'InputVisible'] = true;
      this.$nextTick(_ => {
        console.log(_)
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },
    //添加时tag标签的
    filterHandleInputConfirm(name) {
      let inputValue = this.addSub[name + 'InputValue'];
      if (inputValue) {
        this.addSub[name + 'Keywords'].push(inputValue);
      }
      this.addSub[name + 'InputVisible'] = false;
      this.addSub[name + 'InputValue'] = '';

    },
    //添加订阅
    addSubCommit() {
      if (this.addSubStatus == 'el-icon-loading') {
        this.$message.warning('正在添加中...')
        return;
      }
      this.addSubStatus = 'el-icon-loading'
      let tempAddSub = { ...this.addSub }; // 使用展开运算符进行深拷贝

      //默认创建类型
      if (tempAddSub.subType === 'plugin') {
        //将扩展中的input和select合并
        for (let i = 0; i < tempAddSub.inputListData.length; i++) {
          tempAddSub.inputAndSelectDataList.push(this.addSub.inputListData[i])
        }
        for (let i = 0; i < tempAddSub.selectListData.length; i++) {
          tempAddSub.inputAndSelectDataList.push(this.addSub.selectListData[i])
        }

        //校验主页链接
        const patternUrl = /(http|https):\/\/([\w.]+\/?)\S*/;
        if (!patternUrl.test(tempAddSub.url)) {
          this.$message.error("请输入正确的主页链接！")
          this.addSubStatus = ''
          return
        }
        //网站主机名称即为插件名称
        tempAddSub.plugin = new URL(tempAddSub.url).hostname;

        //校验自定义剧集
        if (tempAddSub.episodes == '1') {
          const patternCustomEpisodes = /^([1-9])(?!.*\b\1\b)(?:[0-9]{0,2}|[1-9])(?!,$)/;
          if (tempAddSub.customEpisodes == null || tempAddSub.customEpisodes == '' || !patternCustomEpisodes.test(tempAddSub.customEpisodes)) {
            this.$message.error("请输入正确的自定义剧集")
            this.addSubStatus = ''
            return
          }
        }
        //处理自定义更新频率
        if (tempAddSub.cron == -1) {
          tempAddSub.cron = tempAddSub.cronUnit * tempAddSub.customCron;
          if (tempAddSub.cron < 1200) {
            this.$message.error("更新频率不能小于20分钟");
            this.addSubStatus = ''
            return;
          }
        }
      } else if (tempAddSub.subType === 'empty') {
        //空订阅
        //必须有title
        let title = tempAddSub.title.trim();//去掉空格
        if (title.length === 0) {
          this.$message.error("请输入自定义订阅名称");
          this.addSubStatus = ''
          return;
        }
      } else {
        this.$message.error("添加失败！未选择创建类型");
        this.addSubStatus = ''
        return;
      }

      //处理自定义存活时间
      if (tempAddSub.survivalTime == -2) {
        tempAddSub.survivalTime = tempAddSub.survivalTimeUnit * tempAddSub.customSurvivalTime;
        if (tempAddSub.survivalTime <= 0) {
          this.$message.error("存活时间需要大于0s");
          this.addSubStatus = ''
          return;
        }
      }

      //处理自定义保留最近N期节目
      if (tempAddSub.keepLast == -1) {
        tempAddSub.keepLast = tempAddSub.customKeepLast;
        if (tempAddSub.keepLast <= 0) {
          this.$message.error("保留最近节目需要大于0");
          this.addSubStatus = ''
          return;
        }
      }

      //将数据发送
      axios.post('/api/sub/add', tempAddSub)
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success('添加成功')
            this.handleClose();
            this.addSub = this.initAddSub
            this.addSubStatus = ''
            this.getSubList();//更新

          } else if (res.data.code == '0') {
            this.$message.error(res.data.msg)
            this.addSubStatus = ''
          }
        }).catch(error => {
          this.addSubStatus = ''
          this.$message.error('未知错误')
          console.log(error)
        })
    },
    //获取订阅列表
    getSubList() {
      var _this = this;
      axios({
        method: "get",
        url: "/api/sub/list"
      }).then(function (resp) {
        _this.$emit('update:subData', resp.data.data);
      })
    },
    //获取插件扩展
    getExtendList() {
      if (this.addSub.isExtend == '0') {
        this.loading = true;
        let url = this.addSub.url;
        let type = this.addSub.type;
        const patternUrl = /(http|https):\/\/([\w.]+\/?)\S*/;
        if (url == null || url == '' || !patternUrl.test(url)) {
          this.$message.error('请输入正确的主页链接！')
          this.loading = false
          return
        }
        let plugin = new URL(url).hostname
        axios.get('/api/sub/extendList', {
          params: {
            plugin: plugin,
            url: url,
            type: type
          }
        })
          .then(res => {
            if (res.data.code == '1') {
              let hasInputExtendList = res.data.data.extendList.inputList.length > 0;
              let hasSelectExtendList = res.data.data.extendList.selectList.length > 0;
              let hasInputListData = res.data.data.inputListData.length > 0;
              let hasSelectListData = res.data.data.selectListData.length > 0;

              if (!hasInputExtendList && !hasInputListData && !hasSelectListData && !hasSelectExtendList) {
                this.$message.info('暂无扩展选项！')
              } else {
                this.addSub.extendList = res.data.data.extendList
                this.addSub.inputListData = res.data.data.inputListData
                this.addSub.selectListData = res.data.data.selectListData
                this.addSub.isExtend = '1'
              }
            } else if (res.data.code == '0') {
              this.$message.error(res.data.msg)
            }
          }).catch(error => {
            console.log(error)
            this.$message.error('未知错误！')
          })
        this.loading = false
      } else {
        this.addSub.isExtend = '0';
      }
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
    },
    changeCronExpression(val){
      this.addSub.cronExpression=val;
    },
  }
}
</script>
<style scoped></style>