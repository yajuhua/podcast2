<template>
    <div>
        <el-dialog 
        v-loading="appendItem.loading" 
        title="追加节目" 
        :visible="visible" 
        width="audo"
        @change="execForceUpdate()" 
        @close="handleClose()">
            <el-form ref="form" label-width="80px" label-position="top">
                <el-form-item label="节目链接">
                    <el-input v-model="appendItem.url" placeholder="请输入节目链接"></el-input>
                </el-form-item>
                <el-form-item label="类型">
                    <el-select v-model="appendItem.type" placeholder="请选择类型">
                        <el-option label="视频" value="Video"></el-option>
                        <el-option label="音频" value="Audio"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="更多选项">
                    <el-button @click="getAppendItemExtendList()">更多选项</el-button>
                </el-form-item>
                <!-- 扩展选项 -->
                <div v-show="appendItem.isExtend">
                    <!-- select选择框 -->
                    <div v-for="(select, selectIndex) in appendItem.extendList.selectList" :key="select.id">
                        <el-form-item :label="select.name">
                            <el-select v-model="appendItem.selectListData[selectIndex].content">
                                <span v-show="false">{{ appendItem.selectListData[selectIndex].name = select.name
                                    }}</span>
                                <el-option v-for="(option) in select.options" :key="option" :label="option"
                                    :value="option"></el-option>
                            </el-select>
                        </el-form-item>
                    </div>
                    <!-- 输入框 -->
                    <div v-for="(input, inputIndex) in appendItem.extendList.inputList" :key="input.id">
                        <span v-show="false">{{ appendItem.inputListData[inputIndex].name = input.name }}</span>
                        <el-form-item :label="input.name">
                            <el-input v-model="appendItem.inputListData[inputIndex].content"></el-input>
                        </el-form-item>
                    </div>
                </div>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="handleClose()">取 消</el-button>
                <el-button type="primary" @click="appendItemCommit()" :icon="appendItem.status">追加节目</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import axios from 'axios'
export default {
    components: {

    },
    name: 'AppendItem',
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        channelUuid: {
            type: String
        }
    },
    data() {
        return {
            //追加节目
            appendItem: {
                url: '',
                type: 'Audio',
                inputAndSelectDataList: [],
                channelUuid: '',
                extendList: { inputList: [], selectList: [] },
                inputListData: [],
                selectListData: [],
                loading: false,
                visible: false,
                status: '',
                isExtend: false
            },
            initAppendItem: {
                url: '',
                type: 'Audio',
                inputAndSelectDataList: [],
                channelUuid: '',
                extendList: { inputList: [], selectList: [] },
                inputListData: [],
                selectListData: [],
                loading: false,
                visible: false,
                status: '',
                isExtend: false
            }
        }
    },
    methods: {
        handleClose() {
            this.$emit('update:visible', false); // 通知父组件关闭弹窗
        },
        execForceUpdate() {
            this.$forceUpdate();
        },
        //追加节目
        appendItemCommit() {
            if (this.appendItem.status == 'el-icon-loading') {
                this.$message.warning('正在添加中...')
                return;
            }
            this.appendItem.status = 'el-icon-loading'
            let tempAppendItem = { ...this.appendItem }; // 使用展开运算符进行深拷贝

            //校验主页链接
            const patternUrl = /(http|https):\/\/([\w.]+\/?)\S*/;
            if (!patternUrl.test(tempAppendItem.url)) {
                this.$message.error("请输入正确链接！")
                this.appendItem.status = ''
                return;
            }

            //将扩展中的input和select合并
            for (let i = 0; i < tempAppendItem.inputListData.length; i++) {
                tempAppendItem.inputAndSelectDataList.push(this.appendItem.inputListData[i])
            }
            for (let i = 0; i < tempAppendItem.selectListData.length; i++) {
                tempAppendItem.inputAndSelectDataList.push(this.appendItem.selectListData[i])
            }
            console.log("appendItemChannelUuid: " + this.channelUuid);
            tempAppendItem.channelUuid = this.channelUuid;

            //将数据发送
            axios.post('/api/sub/appendItem', tempAppendItem)
                .then(res => {
                    if (res.data.code == '1') {
                        this.$message.success('追加成功')
                        this.appendItem.visible = false
                        this.appendItem.status = ''
                        this.appendItem = this.initAppendItem;
                        this.handleClose();

                    } else if (res.data.code == '0') {
                        this.$message.error(res.data.msg)
                        this.appendItem.status = ''
                    }
                }).catch(error => {
                    this.appendItem.status = ''
                    this.$message.error('未知错误')
                    console.log(error)
                })
        },
        //获取追加节目插件扩展
        getAppendItemExtendList() {
            this.appendItem.loading = true;
            let url = this.appendItem.url;
            let type = this.appendItem.type;
            const patternUrl = /(http|https):\/\/([\w.]+\/?)\S*/;
            if (url == null || url == '' || !patternUrl.test(url)) {
                this.$message.error('请输入正确链接！')
                this.appendItem.loading = false;
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
                            this.appendItem.extendList = res.data.data.extendList
                            this.appendItem.inputListData = res.data.data.inputListData
                            this.appendItem.selectListData = res.data.data.selectListData
                            this.appendItem.isExtend = true;
                        }
                    } else if (res.data.code == '0') {
                        this.$message.error(res.data.msg)
                    }
                }).catch(error => {
                    console.log(error)
                    this.$message.error('未知错误！')
                })
            this.appendItem.loading = false;
        },
    }
}
</script>

<style></style>