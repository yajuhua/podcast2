<template>
  <div>

    <!-- 搜索订阅 -->
    <el-input v-model="searchKeyword" style="width: 20%" placeholder="搜索订阅"></el-input>&nbsp;
    <el-button type="primary" @click="subSearch()" :icon="searchIng">搜索</el-button>
    <el-button type="primary" @click="getSubList()">全部</el-button>
    <br><br>

    <!-- 订阅详细信息 -->

    <el-dialog title="详细" :visible.sync="subDetail.visible" width="30%">
      <el-form ref="form" :model="subDetail.detail" label-width="auto">
        <el-form-item label="UUID">
          <div @click="copy(subDetail.detail.uuid)">
            <el-input v-model="subDetail.detail.uuid"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="比对">
          <div @click="copy(subDetail.detail.equal)">
            <el-input v-model="subDetail.detail.equal"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="名称">
          <div @click="copy(subDetail.detail.title)">
            <el-input v-model="subDetail.detail.title"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="链接">
          <div @click="copy(subDetail.detail.link)">
            <el-input v-model="subDetail.detail.link"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="状态码">
          <div @click="copy(subDetail.detail.status)">
            <el-input v-model="subDetail.detail.status"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <div @click="copy(subDetail.detail.description)">
            <el-input v-model="subDetail.detail.description"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="封面链接">
          <div @click="copy(subDetail.detail.image)">
            <el-input v-model="subDetail.detail.image"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="创建时间">
          <div @click="copy(subDetail.detail.createTime)">
            <el-input v-model="subDetail.detail.createTime"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="上次检查更新时间">
          <div @click="copy(subDetail.detail.checkTime)">
            <el-input v-model="subDetail.detail.checkTime"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="上次更新时间">
          <div @click="copy(subDetail.detail.updateTime)">
            <el-input v-model="subDetail.detail.updateTime"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="类型">
          <div @click="copy(subDetail.detail.type)">
            <el-input v-model="subDetail.detail.type"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="存活时间">
          <div @click="copy(subDetail.detail.survivalTime)">
            <el-input v-model="subDetail.detail.survivalTime"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="更新频率">
          <div @click="copy(subDetail.detail.cron)">
            <el-input v-model="subDetail.detail.cron"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="插件名称">
          <div @click="copy(subDetail.detail.plugin)">
            <el-input v-model="subDetail.detail.plugin"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="episodes">
          <div @click="copy(subDetail.detail.episodes)">
            <el-input v-model="subDetail.detail.episodes"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="customEpisodes">
          <div @click="copy(subDetail.detail.customEpisodes)">
            <el-input v-model="subDetail.detail.customEpisodes"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="继续更新">
          <div @click="copy(subDetail.detail.isUpdate)">
            <el-input v-model="subDetail.detail.isUpdate"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="是否开启过滤">
          <div @click="copy(subDetail.detail.isFilter)">
            <el-input v-model="subDetail.detail.isFilter"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="过滤最小时长">
          <div @click="copy(subDetail.detail.minDuration)">
            <el-input v-model="subDetail.detail.minDuration"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="过滤最大时长">
          <div @click="copy(subDetail.detail.maxDuration)">
            <el-input v-model="subDetail.detail.maxDuration"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="过滤标题">
          <div @click="copy(subDetail.detail.titleKeywords)">
            <el-input v-model="subDetail.detail.titleKeywords"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="过滤描述">
          <div @click="copy(subDetail.detail.descKeywords)">
            <el-input v-model="subDetail.detail.descKeywords"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="是否扩展">
          <div @click="copy(subDetail.detail.isExtend)">
            <el-input v-model="subDetail.detail.isExtend"></el-input>
          </div>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-table ref="multipleTable" :data="subData" tooltip-effect="dark" style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55">
      </el-table-column>
      <el-table-column type="index" width="50">
      </el-table-column>
      <el-table-column label="更新时间" width="120" prop="updateTime">
      </el-table-column>
      <el-table-column label="频道名称" prop="title" show-overflow-tooltip>
      </el-table-column>

      <!-- 相关操作 -->
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button size="mini" type="success" plain @click="copyUrl(scope.row.uuid)">复制URL</el-button>
          <el-button size="mini" type="success" plain @click="qrcode(scope.row.uuid)">二维码</el-button>
          <el-button size="mini" type="danger" plain @click="batchDelete(scope.row.uuid)">删除</el-button>
          <el-button size="mini" type="primary" plain @click="getEditSubInfo(scope.row.uuid)">编辑</el-button>
          <el-button size="mini" type="primary" plain @click="subDetailShow(scope.row.uuid)">详细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 展示二维码 -->
    <el-dialog title="二维码" :visible.sync="qrcodeVisible" width="350px">
      <div>
        <vue-qr :text="url" :size="300">
        </vue-qr>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="qrcodeVisible = false">取 消</el-button>
        <el-button type="primary" @click="copyUrl('')">复 制 URL</el-button>
      </span>
    </el-dialog>

    <!-- 展示组订阅二维码 -->
    <el-dialog title="二维码" :visible.sync="subGroupData.qrcodeVisible" width="350px">
      <div>
        <vue-qr :text="subGroupData.url + encodeURI(subGroupData.group)" :size="300">
        </vue-qr>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-input v-model="subGroupData.group" placeholder="请输入组名"></el-input><br><br>
        <el-button @click="subGroupData.qrcodeVisible = false">取 消</el-button>
        <el-button type="primary" @click="copy(subGroupData.url + encodeURI(subGroupData.group))">复 制 URL</el-button>
      </span>
    </el-dialog>

    <!-- 页面底部的控制按钮 -->
    <div class="footer">
      <el-button type="primary" size="mini" @click="addSubVisible = true">添加订阅</el-button>
      <el-button type="primary" plain size="mini" @click="toManager">管理</el-button><span>{{ '\t' }}</span>
      <el-dropdown>
        <el-button type="primary" size="mini" plain>
          更多<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="batchDelete('')">批量删除</el-dropdown-item>
          <el-dropdown-item @click.native="downloadOPML">生成OPML</el-dropdown-item>
          <el-dropdown-item @click.native="subGroup()">订阅组</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!-- 添加订阅 -->
    <el-dialog v-loading="loading" title="添加订阅" :visible.sync="addSubVisible" width="audo">
      <el-form ref="form" :model="addSub" label-width="80px">
        <el-form-item label="主页链接">
          <el-input v-model="addSub.url" placeholder="请输入主页链接"></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="addSub.type" placeholder="请选择类型">
            <el-option label="视频" value="Video"></el-option>
            <el-option label="音频" value="Audio"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存活时间">
          <el-select v-model="addSub.survivalTime" placeholder="请选择存活时间">
            <el-option label="1天" value="1"></el-option>
            <el-option label="3天" value="3"></el-option>
            <el-option label="7天" value="7"></el-option>
            <el-option label="15天" value="15"></el-option>
            <el-option label="30天" value="30"></el-option>
            <el-option label="永久" value="-1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="更新频率">
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
            <el-input v-model.number="addSub.customCron" placeholder="请输入更新频率"
                      :min="1" type="number" size="medium"
                      :style="{ width: '250px' }" class="input-with-select">
            <el-select v-model="addSub.cronUnit" placeholder="更新频率单位" slot="append"
                       :style="{ width: '100px' }">
              <el-option label="秒钟" value="1"></el-option>
              <el-option label="分钟" value="60"></el-option>
              <el-option label="小时" value="3600"></el-option>
              <el-option label="天" value="86400"></el-option>
            </el-select>
            </el-input>
          </div>
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
            <el-tooltip class="item" effect="dark" content="在英文状态下输入1,2,3,4,5将下载列表中序号1-5的节目"
                        placement="top-start">
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
                <el-option v-for="(option) in select.options" :key="option" :label="option" :value="option"></el-option>
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

      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addSubVisible = false">取 消</el-button>
        <el-button type="primary" @click="addSubCommit" :icon="addSubStatus">添加订阅</el-button>
      </span>
    </el-dialog>

    <!-- 编辑订阅 -->
    <el-dialog :title="editSubData.title" :visible.sync="editSubVisible" width="audo" v-loading="editSubData.loading" element-loading-text="正在获取数据中...">
        <el-form ref="form" :model="editSubData" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="editSubData.title" placeholder="请输入名称"></el-input>
          </el-form-item>
          <el-form-item label="封面">
            <el-input v-model="editSubData.image" placeholder="请输入封面链接"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="editSubData.type" placeholder="请选择类型">
              <el-option label="视频" value="Video"></el-option>
              <el-option label="音频" value="Audio"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="存活时间">
            <el-select v-model="editSubData.survivalTime" placeholder="请选择存活时间">
              <el-option label="1天" value="86400"></el-option>
              <el-option label="3天" value="259200"></el-option>
              <el-option label="7天" value="604800"></el-option>
              <el-option label="15天" value="1296000"></el-option>
              <el-option label="30天" value="2592000"></el-option>
              <el-option label="永久" value="-1"></el-option>
              <span v-show="false">{{ editSubData.survivalTime += '' }}</span>
            </el-select>
          </el-form-item>
          <el-form-item label="更新频率">
            <el-select v-model="editSubData.cron" placeholder="请选择更新频率">
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
          <el-form-item label="自定义" v-if="editSubData.cron == '-1'">
            <div style="margin-top: 15px">
              <el-input v-model.number="editSubData.customCron" placeholder="请输入更新频率"
                        :min="1" type="number" size="medium"
                        :style="{ width: '250px' }" class="input-with-select">
                <el-select v-model="editSubData.cronUnit" placeholder="更新频率单位" slot="append"
                           :style="{ width: '100px' }">
                  <el-option label="秒钟" value="1"></el-option>
                  <el-option label="分钟" value="60"></el-option>
                  <el-option label="小时" value="3600"></el-option>
                  <el-option label="天" value="86400"></el-option>
                </el-select>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item label="继续更新">
            <el-select v-model="editSubData.isUpdate">
              <el-option label="是" value="1"></el-option>
              <el-option label="否" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="过滤器">
            <el-select v-model="editSubData.isFilter">
              <el-option label="禁用" value="0"></el-option>
              <el-option label="启用" value="1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="存放位置">
            <el-select v-model="editSubData.status">
              <el-option label="本地" value="21"></el-option>
              <el-option label="alist" value="22"></el-option>
              <span v-show="false">{{ editSubData.status += '' }}</span>
            </el-select>
          </el-form-item>
          <div v-show="editSubData.isFilter == '1'">
            <el-form-item label="最小时长">
              <el-input-number v-model="editSubData.minDuration" :min="-1"> 秒</el-input-number>
            </el-form-item>
            <el-form-item label="最大时长">
              <el-input-number v-model="editSubData.maxDuration" :min="-1"> 秒</el-input-number>
            </el-form-item>
            <el-form-item label="标题">
              <el-tag :key="keyword" v-for="keyword in editSubData.titleKeywords" closable :disable-transitions="false"
                      @close="editFilterHandleClose('title', keyword)">
                {{ keyword }}
              </el-tag>
              <el-input class="input-new-tag" v-if="editSubData.titleInputVisible" v-model="editSubData.titleInputValue"
                        ref="saveTagInput" size="small" @keyup.enter.native="editFilterHandleInputConfirm('title')"
                        @blur="editFilterHandleInputConfirm('title')">
              </el-input>
              <el-button v-else class="button-new-tag" size="small" @click="editFilterShowInput('title')">+
                关键字</el-button>
            </el-form-item>
            <el-form-item label="描述">
              <el-tag :key="keyword" v-for="keyword in editSubData.descKeywords" closable :disable-transitions="false"
                      @close="editFilterHandleClose('desc', keyword)">
                {{ keyword }}
              </el-tag>
              <el-input class="input-new-tag" v-if="editSubData.descInputVisible" v-model="editSubData.descInputValue"
                        ref="saveTagInput" size="small" @keyup.enter.native="editFilterHandleInputConfirm('desc')"
                        @blur="editFilterHandleInputConfirm('desc')">
              </el-input>
              <el-button v-else class="button-new-tag" size="small" @click="editFilterShowInput('desc')">+ 关键字</el-button>
            </el-form-item>
          </div>
          <!-- 扩展选项 -->
          <div v-if="editSubData.isExtend == '1'">
            <!-- select选择框 -->
            <div v-for="(select, selectIndex) in editSubData.extendList.selectList" :key="select.id">
              <el-form-item :label="select.name">
                <el-select v-model="editSubData.selectListData[selectIndex].content">
                  <span v-show="false">{{ editSubData.selectListData[selectIndex].name = select.name }}</span>
                  <el-option v-for="(option) in select.options" :key="option" :label="option" :value="option"></el-option>
                </el-select>
              </el-form-item>
            </div>
            <!-- 输入框 -->
            <div v-for="(input, inputIndex) in editSubData.extendList.inputList" :key="input.id">
              <span v-show="false">{{ editSubData.inputListData[inputIndex].name = input.name }}</span>
              <el-form-item :label="input.name">
                <el-input v-model="editSubData.inputListData[inputIndex].content"></el-input>
              </el-form-item>
            </div>
          </div>

        </el-form>
        <span slot="footer" class="dialog-footer">
        <el-button @click="editSubVisible = false">取 消</el-button>
        <el-button type="primary" @click="editSubCommit" v-if="!editSubData.loading">修改</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import axios from 'axios'
import VueQr from 'vue-qr'
export default {
  components: {
    VueQr,
  },
  data() {
    return {
      searchKeyword: '',
      searchIng: '',
      subData: [],
      qrcodeVisible: false,
      addSubVisible: false,
      editSubVisible: false,
      url: '',
      multipleSelection: [],
      delele: [],
      addSub: {
        url: '',
        type: 'Audio',
        survivalTime: '7',
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
        customCron: '0'
      },
      //初始数据
      initAddSub: {
        url: '',
        type: 'Audio',
        survivalTime: '7',
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
      },
      //编辑订阅
      editSubData: {
        title: '',
        type: '',
        survivalTime: '',
        cron: '',
        isUpdate: '',
        isFilter: '',
        maxDuration: '',
        minDuration: '',
        titleKeywords: [],
        descKeywords: [],
        isExtend: '',
        extendList: '',
        inputListData: [],
        selectListData: [],
        titleInputValue: '',
        titleInputVisible: '',
        descInputValue: '',
        descInputVisible: '',
        status: null,
        image: '',
        cronUnit: '1',
        customCron: '0',
        loading: false
      },
      //初始数据结构
      initEditSubData: {
        title: '',
        type: '',
        survivalTime: '',
        cron: '',
        isUpdate: '',
        isFilter: '',
        maxDuration: '',
        minDuration: '',
        titleKeywords: [],
        descKeywords: [],
        isExtend: '',
        extendList: '',
        inputListData: [],
        selectListData: [],
        titleInputValue: '',
        titleInputVisible: '',
        descInputValue: '',
        descInputVisible: '',
        status: '',
        image: '',
        cronUnit: '',
        customCron: ''
      },
      loading: false,
      addSubStatus: '',
      subDetail: {
        detail: '',
        visible: false
      },
      subGroupData:{
        url:'',
        group:'',
        qrcodeVisible: false,
        uuids:[]
      }
    }
  },
  mounted() {
    // 页面加载完成后，发送异步请求，查询数据
    this.getSubList();
  },
  methods: {
    getSubList() {
      var _this = this;
      axios({
        method: "get",
        url: "/sub/list"
      }).then(function (resp) {
        _this.subData = resp.data.data;
      })
    },
    toggleSelection(rows) {
      if (rows) {
        rows.forEach(row => {
          this.$refs.multipleTable.toggleRowSelection(row);
        });
      } else {
        this.$refs.multipleTable.clearSelection();
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    //复制URL
    copyUrl(uuid) {
      const textarea = document.createElement('textarea');

      if (uuid == null || uuid == '') {
        // 如果没有传入 uuid 参数，则复制 this.url 的值
        textarea.value = this.url;
        this.qrcodeVisible = false;
      } else {
        // 如果传入了 uuid 参数，则拼接新的 URL 进行复制
        textarea.value = window.location.protocol + '//' + window.location.host + '/sub/xml/' + uuid;
      }

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
    //生成二维码
    qrcode(uuid) {
      this.url = window.location.protocol + "//" + window.location.host + "/sub/xml/" + uuid
      this.qrcodeVisible = true;
    },
    batchDelete(uuid) {
      if (uuid == null || uuid == '') {
        //批量删除
        if (this.multipleSelection.length == 0) {
          this.$message({
            message: '请先选择！',
            type: 'warning'
          });
          return;
        } else {
          this.delele = [];
          for (var i = 0; i < this.multipleSelection.length; i++) {
            this.delele.push(this.multipleSelection[i].uuid)
          }
        }
      } else {
        //单个删除
        this.delele = []
        this.delele.push(uuid)
      }

      this.$confirm('此操作将永久删除选择的订阅, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/sub?uuids=' + this.delele).then(res => {
          if (res.data.code == '1') {
            this.$message.success("删除成功")
            this.getSubList();
          } else if (res.data.code == '0') {
            this.$message.error("删除失败");
          }
        }).catch(error => {
          this.$message.error("未知错误");
          console.log(error)
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      });

    },
    //添加订阅
    addSubCommit() {
      if (this.addSubStatus == 'el-icon-loading') {
        this.$message.warning('正在添加中...')
        return;
      }
      this.addSubStatus = 'el-icon-loading'
      //将扩展中的input和select合并
      for (let i = 0; i < this.addSub.inputListData.length; i++) {
        this.addSub.inputAndSelectDataList.push(this.addSub.inputListData[i])
      }
      for (let i = 0; i < this.addSub.selectListData.length; i++) {
        this.addSub.inputAndSelectDataList.push(this.addSub.selectListData[i])
      }

      //校验主页链接
      const patternUrl = /(http|https):\/\/([\w.]+\/?)\S*/;
      if (!patternUrl.test(this.addSub.url)) {
        this.$message.error("请输入正确的主页链接！")
        this.addSubStatus = ''
        return
      }
      //网站主机名称即为插件名称
      this.addSub.plugin = new URL(this.addSub.url).hostname;

      //校验自定义剧集
      if (this.addSub.episodes == '1') {
        const patternCustomEpisodes = /^([1-9])(?!.*\b\1\b)(?:[0-9]{0,2}|[1-9])(?!,$)/;
        if (this.addSub.customEpisodes == null || this.addSub.customEpisodes == '' || !patternCustomEpisodes.test(this.addSub.customEpisodes)) {
          this.$message.error("请输入正确的自定义剧集")
          this.addSubStatus = ''
          return
        }
      }
      //处理自定义更新频率
      let tempAddSub = { ...this.addSub }; // 使用展开运算符进行深拷贝
      if (tempAddSub.cron == -1){
        tempAddSub.cron = tempAddSub.cronUnit*tempAddSub.customCron;
        if (tempAddSub.cron < 1200){
          this.$message.error("更新频率不能小于20分钟");
          this.addSubStatus = ''
          return;
        }
      }
      //将数据发送
      axios.post('/sub/add', tempAddSub)
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success('添加成功')
            this.addSubVisible = false
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
    toManager() {
      this.$router.push('/manager')
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
          return
        }
        let plugin = new URL(url).hostname
        axios.get('/sub/extendList', {
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
    //生成OPML文件
    downloadOPML() {
      //选择不能为空
      if (this.multipleSelection.length != 0) {
        //生成
        let text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        text += "<opml version=\"1.0\">\n";
        text += "  <head>\n";
        text += "    <title>OPML</title>\n";
        text += "  </head>\n";
        text += "  <body>\n";
        for (let i = 0; i < this.multipleSelection.length; i++) {
          text += "    <outline type=\"rss\"  xmlUrl=\"" + window.location.protocol + "//" + window.location.host + "/sub/xml/" + this.multipleSelection[i].uuid + "\" />\n"
        }
        text += "  </body>\n";
        text += "</opml>\n";

        const blob = new Blob([text], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'opml.opml');
        document.body.appendChild(link);
        link.click();
      } else {
        this.$message.error('不能为空！请先选择！');
      }
    },
    //获取编辑订阅信息
    getEditSubInfo(uuid) {
      //向清空之前的
      this.editSubData = this.initEditSubData;
      this.editSubData.loading = true;
      console.log(uuid)
      this.editSubVisible = true
      axios.get('/sub/edit/' + uuid)
        .then(res => {
          if (res.data.code == '1') {
            this.editSubData = res.data.data
            console.log(this.editSubData)
            //无法识别数字,得转成字符串
            this.editSubData.survivalTime = '' + res.data.data.survivalTime
            this.editSubData.cron = '' + res.data.data.cron
            this.editSubData.isUpdate = '' + res.data.data.isUpdate
            this.editSubData.isFilter = '' + res.data.data.isFilter
            this.editSubData.isExtend = '' + res.data.data.isExtend

          } else if (res.data.code == '0') {
            this.$message.error('编辑错误！')
          }

        }).catch(error => {
          console.log(error)
          this.$message.error('未知错误！')
        }).finally(()=>{
        this.editSubData.loading = false;
      })
    },
    //编辑订阅提交
    editSubCommit() {
      let tempEditSubData = {...this.editSubData};
      if (tempEditSubData.cron == -1){
        tempEditSubData.cron = tempEditSubData.cronUnit*tempEditSubData.customCron;
        if (tempEditSubData.cron < 1200){
          this.$message.error("更新频率不能小于20分钟");
          return;
        }
      }
      axios.put('/sub', tempEditSubData)
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success('编辑成功！')
            this.getSubList();//更新
            this.editSubVisible = false
            this.editSubData = this.initEditSubData
          } else if (res.data.code == '0') {
            this.$message.error(res.data.msg)
          }
        }).catch(error => {
          console.log(error)
          this.$message.error("未知错误！")
        })
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

    //编辑时tag标签的关闭
    editFilterHandleClose(name, content) {
      this.editSubData[name + 'Keywords'].splice(this.addSub[name + 'Keywords'].indexOf(content), 1);
    },
    //编辑时tag标签的
    editFilterShowInput(name) {
      this.editSubData[name + 'InputVisible'] = true;
      this.$nextTick(_ => {
        console.log(_)
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },
    //编辑时tag标签的
    editFilterHandleInputConfirm(name) {
      let inputValue = this.editSubData[name + 'InputValue'];
      if (inputValue) {
        this.editSubData[name + 'Keywords'].push(inputValue);
      }
      this.editSubData[name + 'InputVisible'] = false;
      this.editSubData[name + 'InputValue'] = '';
    },
    //搜索订阅
    subSearch() {
      this.searchIng = 'el-icon-loading';
      axios.get('/sub/search?keywords=' + this.searchKeyword)
        .then(res => {
          if (res.data.code == '1') {
            this.subData = res.data.data;
            this.searchIng = '';
          } else {
            this.$message.error(res.data.msg);
            this.searchIng = '';
          }
        })
        .catch(err => {
          this.$message.error(err);
          this.searchIng = '';
        })
    },
    //获取订阅详细信息
    subDetailShow(uuid) {
      this.subDetail.detail = {}
      this.subDetail.visible = true
      axios.get('/sub/detail/' + uuid)
        .then(res => {
          if (res.data.code == '1') {
            this.subDetail.detail = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          this.$message.error('获取订阅详细信息失败！');
          console.log(err);
        })
    },
    //复制内容到粘贴板
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
    //组订阅
    subGroup() {
      if (this.multipleSelection.length == 0) {
        this.$message({
          message: '请先选择！',
          type: 'warning'
        });
        return;
      } else {
        this.subGroupData.uuids = [];
        for (var i = 0; i < this.multipleSelection.length; i++) {
          this.subGroupData.uuids.push(this.multipleSelection[i].uuid)
          }
        this.subGroupData.url = window.location.protocol + '//' + window.location.host + '/sub/xml?uuids=' + this.subGroupData.uuids + '&group=';
        this.subGroupData.qrcodeVisible = true;
      }
    }
  }
}
</script>
<style scoped>
.footer {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}

.footer a {
  /*color: #01A2FF;*/
  margin-right: 10px;
  /* 右边距的设置 */
  text-decoration: none;
  /* 去除下划线 */
  font-size: 20px;
  /* 字体大小设置为25像素 */
  font-weight: bold;
  /* 字体加粗 */
}

.el-tag+.el-tag {
  margin-left: 10px;
}

.button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>