<template>
    <div>
        <el-dialog :title="editSubData.title" :visible="visible" :width="adaptWidth" v-loading="editSubData.loading"
            element-loading-text="正在获取数据中..." @close="handleClose">
            <el-form ref="form" :model="editSubData" label-width="80px">
                <!-- 默认方式 -->
                <span v-if="editSubData.subType == 'plugin'">
                    <el-form-item label="名称">
                        <el-input v-model="editSubData.title" placeholder="请输入名称"></el-input>
                    </el-form-item>
                    <el-form-item label="封面">
                        <el-input v-model="editSubData.image" placeholder="请输入封面链接"></el-input>
                    </el-form-item>
                    <el-form-item label="比对">
                        <el-input v-model="editSubData.equal" placeholder="请输入比对字符串"></el-input>
                    </el-form-item>
                    <el-form-item label="描述">
                        <el-input type="textarea" v-model="editSubData.description" placeholder="请输入描述字符串"></el-input>
                    </el-form-item>
                    <el-form-item label="类型">
                        <el-select v-model="editSubData.type" placeholder="请选择类型">
                            <el-option label="视频" value="Video"></el-option>
                            <el-option label="音频" value="Audio"></el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 订阅同步 -->
                    <el-form-item label="同步方式">
                        <el-select v-model="editSubData.syncWay" placeholder="请选择订阅同步方式">
                            <el-option label="最新" value="latest"></el-option>
                            <el-option label="最近" value="recent"></el-option>
                        </el-select>
                    </el-form-item>

                    <el-form-item label="节目存活">
                        <!--  解决无法选中: https://blog.csdn.net/weixin_40538702/article/details/115093732  -->
                        <el-select v-model="editSubData.survivalWay" placeholder="请选择节目存活方式"
                            @change="execForceUpdate()">
                            <el-option label="保留时间" value="keepTime"></el-option>
                            <el-option label="保留最近" value="keepLast"></el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 存活时间 -->
                    <span v-if="editSubData.survivalWay === 'keepTime'">
                        <el-form-item label="保留时间">
                            <el-select v-model="editSubData.survivalTime" placeholder="请选择存活时间">
                                <el-option label="1天" value="86400"></el-option>
                                <el-option label="3天" value="259200"></el-option>
                                <el-option label="7天" value="604800"></el-option>
                                <el-option label="15天" value="1296000"></el-option>
                                <el-option label="30天" value="2592000"></el-option>
                                <el-option label="永久" value="-1"></el-option>
                                <el-option label="自定义" value="-2"></el-option>
                            </el-select>
                        </el-form-item>

                        <!-- 自定义保留时间 -->
                        <el-form-item label="自定义" v-if="editSubData.survivalTime == '-2'">
                            <div style="margin-top: 15px">
                                <el-input v-model.number="editSubData.customSurvivalTime" placeholder="请输入自定义保留时间"
                                    :min="1" type="number" size="medium" :style="{ width: '250px' }"
                                    class="input-with-select">
                                    <el-select v-model="editSubData.survivalTimeUnit" placeholder="时间单位" slot="append"
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

                    <!--  保留最近  -->
                    <span v-if="editSubData.survivalWay === 'keepLast'">
                        <el-form-item label="保留最近">
                            <el-select v-model="editSubData.keepLast" placeholder="请选择保留最近">
                                <el-option label="最近10期" value="10"></el-option>
                                <el-option label="最近15期" value="15"></el-option>
                                <el-option label="最近30期" value="30"></el-option>
                                <el-option label="自定义" value="-1"></el-option>
                            </el-select>
                        </el-form-item>

                        <!--    自定义保留最近    -->
                        <el-form-item label="自定义" v-if="editSubData.keepLast == -1">
                            <div style="margin-top: 15px">
                                <el-input v-model.number="editSubData.customKeepLast" placeholder="请输入保留最近" :min="1"
                                    type="number" size="medium" :style="{ width: '250px' }" class="input-with-select">
                                </el-input> 集
                            </div>
                        </el-form-item>
                    </span>

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
                            <el-input v-model.number="editSubData.customCron" placeholder="请输入更新频率" :min="1"
                                type="number" size="medium" :style="{ width: '250px' }" class="input-with-select">
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
                            <el-tag :key="keyword" v-for="keyword in editSubData.titleKeywords" closable
                                :disable-transitions="false" @close="editFilterHandleClose('title', keyword)">
                                {{ keyword }}
                            </el-tag>
                            <el-input class="input-new-tag" v-if="editSubData.titleInputVisible"
                                v-model="editSubData.titleInputValue" ref="saveTagInput" size="small"
                                @keyup.enter.native="editFilterHandleInputConfirm('title')"
                                @blur="editFilterHandleInputConfirm('title')">
                            </el-input>
                            <el-button v-else class="button-new-tag" size="small"
                                @click="editFilterShowInput('title')">+
                                关键字</el-button>
                        </el-form-item>
                        <el-form-item label="描述">
                            <el-tag :key="keyword" v-for="keyword in editSubData.descKeywords" closable
                                :disable-transitions="false" @close="editFilterHandleClose('desc', keyword)">
                                {{ keyword }}
                            </el-tag>
                            <el-input class="input-new-tag" v-if="editSubData.descInputVisible"
                                v-model="editSubData.descInputValue" ref="saveTagInput" size="small"
                                @keyup.enter.native="editFilterHandleInputConfirm('desc')"
                                @blur="editFilterHandleInputConfirm('desc')">
                            </el-input>
                            <el-button v-else class="button-new-tag" size="small" @click="editFilterShowInput('desc')">+
                                关键字</el-button>
                        </el-form-item>
                    </div>
                    <!-- 扩展选项 -->
                    <div v-if="editSubData.isExtend == '1'">
                        <!-- select选择框 -->
                        <div v-for="(select, selectIndex) in editSubData.extendList.selectList" :key="select.id">
                            <el-form-item :label="select.name">
                                <el-select v-model="editSubData.selectListData[selectIndex].content">
                                    <span v-show="false">{{ editSubData.selectListData[selectIndex].name = select.name
                                        }}</span>
                                    <el-option v-for="(option) in select.options" :key="option" :label="option"
                                        :value="option"></el-option>
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
                </span>
                <!-- 空订阅 -->
                <span v-if="editSubData.subType == 'empty'">
                    <el-form-item label="名称">
                        <el-input v-model="editSubData.title" placeholder="请输入名称"></el-input>
                    </el-form-item>
                    <el-form-item label="封面">
                        <el-input v-model="editSubData.image" placeholder="请输入封面链接"></el-input>
                    </el-form-item>
                    <el-form-item label="描述">
                        <el-input type="textarea" v-model="editSubData.description" placeholder="请输入描述字符串"></el-input>
                    </el-form-item>

                    <el-form-item label="节目存活">
                        <!--  解决无法选中: https://blog.csdn.net/weixin_40538702/article/details/115093732  -->
                        <el-select v-model="editSubData.survivalWay" placeholder="请选择节目存活方式"
                            @change="execForceUpdate()">
                            <el-option label="保留时间" value="keepTime"></el-option>
                            <el-option label="保留最近" value="keepLast"></el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 存活时间 -->
                    <span v-if="editSubData.survivalWay === 'keepTime'">
                        <el-form-item label="保留时间">
                            <el-select v-model="editSubData.survivalTime" placeholder="请选择存活时间">
                                <el-option label="1天" value="86400"></el-option>
                                <el-option label="3天" value="259200"></el-option>
                                <el-option label="7天" value="604800"></el-option>
                                <el-option label="15天" value="1296000"></el-option>
                                <el-option label="30天" value="2592000"></el-option>
                                <el-option label="永久" value="-1"></el-option>
                                <el-option label="自定义" value="-2"></el-option>
                            </el-select>
                        </el-form-item>

                        <!-- 自定义保留时间 -->
                        <el-form-item label="自定义" v-if="editSubData.survivalTime == '-2'">
                            <div style="margin-top: 15px">
                                <el-input v-model.number="editSubData.customSurvivalTime" placeholder="请输入自定义保留时间"
                                    :min="1" type="number" size="medium" :style="{ width: '250px' }"
                                    class="input-with-select">
                                    <el-select v-model="editSubData.survivalTimeUnit" placeholder="时间单位" slot="append"
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

                    <!--  保留最近  -->
                    <span v-if="editSubData.survivalWay === 'keepLast'">
                        <el-form-item label="保留最近">
                            <el-select v-model="editSubData.keepLast" placeholder="请选择保留最近">
                                <el-option label="最近10期" value="10"></el-option>
                                <el-option label="最近15期" value="15"></el-option>
                                <el-option label="最近30期" value="30"></el-option>
                                <el-option label="自定义" value="-1"></el-option>
                            </el-select>
                        </el-form-item>

                        <!--    自定义保留最近    -->
                        <el-form-item label="自定义" v-if="editSubData.keepLast == -1">
                            <div style="margin-top: 15px">
                                <el-input v-model.number="editSubData.customKeepLast" placeholder="请输入保留最近" :min="1"
                                    type="number" size="medium" :style="{ width: '250px' }" class="input-with-select">
                                </el-input> 集
                            </div>
                        </el-form-item>
                    </span>

                    <el-form-item label="存放位置">
                        <el-select v-model="editSubData.status">
                            <el-option label="本地" value="21"></el-option>
                            <el-option label="alist" value="22"></el-option>
                            <span v-show="false">{{ editSubData.status += '' }}</span>
                        </el-select>
                    </el-form-item>
                </span>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="handleClose()">取 消</el-button>
                <el-button type="primary" @click="editSubCommit" v-if="!editSubData.loading">修改</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import axios from 'axios'
import { adaptWidth } from '@/utils/utils';
export default {
    components: {
        
    },
    computed: {
    adaptWidth() {
      return adaptWidth(); 
    }
},
    name: 'EditSub',
    props: {
        visible: {
            type: Boolean,
            default: false,
        },
        uuid: {
            type: String
        }
    },
    watch: {
        uuid: {
            handler(uuid) {
                this.getEditSubInfo(uuid);
            },
            deep: true // ✅ 深度监听对象内部变化
        }
    },
    data() {
        return {
            //编辑订阅
            editSubData: {
                title: '',
                type: '',
                equal: '',
                description: '',
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
                loading: false,
                survivalWay: '',//默认
                keepLast: '',
                customKeepLast: '',//自定义保留最近N期节目
                customSurvivalTime: '',//自定义设置存活时间
                survivalTimeUnit: '1',//存活时间单位
                subType: '',//创建订阅方式
                syncWay: '',//同步方式
            },
            //初始数据结构
            initEditSubData: {
                title: '',
                type: '',
                equal: '',
                description: '',
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
                customCron: '',
                survivalWay: '',//默认
                keepLast: '',
                customKeepLast: '',//自定义保留最近N期节目
                customSurvivalTime: '',//自定义设置存活时间
                survivalTimeUnit: '1',//存活时间单位
                subType: '',//创建订阅方式
                syncWay: '',//同步方式
            },
        }
    },
    methods: {
        handleClose() {
            this.$emit('update:visible', false); // 通知父组件关闭弹窗
        },
        //获取编辑订阅信息
        getEditSubInfo(uuid) {
            //向清空之前的
            this.editSubData = this.initEditSubData;
            this.editSubData.loading = true;
            console.log(uuid)
            this.$emit('update:visible', true); 
            axios.get('/api/sub/edit/' + uuid)
                .then(res => {
                    if (res.data.code == '1') {
                        this.editSubData = res.data.data
                        console.log(this.editSubData)
                        //无法识别数字,得转成字符串
                        this.editSubData.survivalTime = '' + res.data.data.survivalTime;
                        if (this.editSubData.keepLast == null) {
                            this.editSubData.keepLast = '';
                        } else {
                            this.editSubData.keepLast = '' + res.data.data.keepLast;
                        }
                        this.editSubData.survivalWay = res.data.data.survivalWay;
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
                })
        },
        //编辑订阅提交
        editSubCommit() {
            let tempEditSubData = { ...this.editSubData };
            if (tempEditSubData.cron == -1) {
                tempEditSubData.cron = tempEditSubData.cronUnit * tempEditSubData.customCron;
                if (tempEditSubData.cron < 1200) {
                    this.$message.error("更新频率不能小于20分钟");
                    return;
                }
            }

            //处理自定义存活时间
            if (tempEditSubData.survivalTime == -2) {
                tempEditSubData.survivalTime = tempEditSubData.survivalTimeUnit * tempEditSubData.customSurvivalTime;
                if (tempEditSubData.survivalTime <= 0) {
                    this.$message.error("存活时间需要大于0s");
                    return;
                }
            }

            //处理自定义保留最近N期节目
            if (tempEditSubData.keepLast == -1) {
                tempEditSubData.keepLast = tempEditSubData.customKeepLast;
                if (tempEditSubData.keepLast <= 0) {
                    this.$message.error("保留最近节目需要大于0");
                    return;
                }
            }

            axios.put('/api/sub', tempEditSubData)
                .then(res => {
                    if (res.data.code == '1') {
                        this.$message.success('编辑成功！')
                        this.$emit('getSubList')//更新
                        this.$emit('update:visible', false); 
                        this.editSubData = this.initEditSubData
                    } else if (res.data.code == '0') {
                        this.$message.error(res.data.msg)
                    }
                }).catch(error => {
                    console.log(error)
                    this.$message.error("未知错误！")
                })
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
        execForceUpdate() {
            this.$forceUpdate();
        },
    }
}
</script>

<style></style>