<template>
	<Layout>
		<Content>
			<Breadcrumb>
				<BreadcrumbItem>培训管理</BreadcrumbItem>
				<BreadcrumbItem>培训处理</BreadcrumbItem>
			</Breadcrumb>
			<Card>
				<Form :model="searchCondition" ref="searchCondition" inline :label-width="80">
                    <FormItem label="培训时间" prop="serviceDate">
                        <DatePicker type="daterange" :editable="false" v-model="trainTime" placeholder="请选择培训时间" style="width: 200px"></DatePicker>
                    </FormItem>
                    <FormItem label="培训类别">
                      <Select v-model="currentTypeIdx" style="width:200px">
                        <Option :value="0">全部</Option>
                        <Option :value="i + 1" v-for="(item, i) in typeList" :key="i">{{item.type}}</Option>
                      </Select>
                    </FormItem>
                    <FormItem label="培训名称" prop="name">
                        <Input v-model="searchCondition.name" style="width: 140px;"></Input>
                    </FormItem>
                    <!--<FormItem label="培训内容" prop="context">
                        <Input v-model="searchCondition.context" style="width: 140px;"></Input>
                    </FormItem>-->
                    <FormItem label="提交单位">
                        <!-- <Select v-model="searchCondition.company" style="width: 140px;">
                            <Option :value="item.name" v-for="item in companyList" :key="item.id">{{item.name}}</Option>
                        </Select> -->
                        <Input type="text" v-model="searchCondition.company"></Input>
                    </FormItem>
                    <FormItem label="提交人">
                       <!--  <div class="ivu-form-item-content" @click="openTreeModal(1)">
                            <div class="ivu-input-wrapper ivu-input-type">
                                <i class="ivu-icon ivu-icon-load-c ivu-load-loop ivu-input-icon ivu-input-icon-validate"></i>
                                <div class="ivu-input" style="width: 140px;">{{searchCondition.person}}</div>
                            </div>
                        </div> -->
                        <Input type="text" v-model="searchCondition.person"></Input>
                    </FormItem>
					<FormItem label="状态" prop="status">
						<Select v-model="searchCondition.status" style="width: 140px;">
                            <Option value="">全部</Option>
                            <Option :value="1">申请</Option>
                            <Option :value="2">驳回</Option>
                            <Option :value="3">同意</Option>
                            <Option :value="4">驳回后申请</Option>
                            <Option :value="5">完结</Option>
                        </Select>
					</FormItem>
                    <FormItem label="任务编号" style="width: 200px">
                        <Input v-model="searchCondition.applyCode"></Input>
                    </FormItem>
					<FormItem :label-width="20">
						<Button type="primary" @click="preSearch" v-if="authButton.includes('ops_train_handle_query')">查询</Button>
					</FormItem>
				</Form>
                <hy-table
                    highlight-row
                    border
                    :current="pageInfo.pageNo"
                    :columns="tableList.columns"
                    :data="tableList.data"
                    :total="Number(pageInfo.total)"
                    :pageSize="Number(pageInfo.pageSize)"
                    pageType="small"
                    show-elevator
                    show-sizer
                    ref="thisTable"
                    @on-page-change="pageChange" />
			</Card>
		</Content>
        <uploadModal @on-ok="uploadFile" ref="uploadModal"></uploadModal>
        <!-- 选择培训人的树形弹窗 -->
        <personTreeModal ref="getTrainer" :multiple="false" @on-ok="selectPerson" />
	</Layout>
</template>
<script>
import {mapState} from 'vuex'
import api from '@/api/axiosApi'
import operationApiList from '@/api/operationApiList'
import apiList from '@/api/comApiList'
import uploadModal from "./trainModal/uploadModal.vue"
import personTreeModal from './trainModal/personTreeModal.vue'

function getDateRange(time) {
  if (!time) {
    return ''
  }
  // 结束日期
  const endDate = new Date(time)
  // 当前日期往前推30天
  const startDate = new Date(time - 30 * 24 * 60 * 60 * 1000)
  return {
    start: `${startDate.getFullYear()}-${startDate.getMonth() + 1}-${startDate.getDate()}`,
    end: `${endDate.getFullYear()}-${endDate.getMonth() + 1}-${endDate.getDate()}`
  }
}

export default {
    components: {
        uploadModal,
        personTreeModal
    },
    data() {
        const vm = this
        return {
            // 类型列表
            typeList: [],
            currentTypeIdx: 0,
            companyList: [],
            download: false,
            upload: false,
            searchCondition: {
                type: '',
                name: '',
                context: '',
                company: '',
                person: '',
                status: '',
                applyCode: this.$route.query.code || ''
            },
            tableList: {
                columns: [{
                    title: '序号',
                    type: 'index',
                    width: 80,
                    align: 'center'
                }, {
                    title: '任务编号',
                    key: 'applyCode'
                }, {
                    title: '培训类型',
                    key: 'type'
                }, {
                    title: '培训名称',
                    key: 'name'
                }, {
                    title: '培训时间',
                    key: 'trainTime',
                    width: 160
                }, {
                    title: '提交单位',
                    key: 'company'
                }, {
                    title: '提交人',
                    key: 'person'
                }, {
                    title: '状态',
                    key: 'status',
                    render: (h, params) => {
                        const status = ['--', '申请', '驳回', '同意', '驳回后申请', '完结']
                        return h('span', status[params.row.status])
                    }
                }, {
                    title: '培训评分',
                    key: 'trainSorce',
                    width: 100
                }, {
                    title: '培训反馈文件',
                    key: 'trainBackDoc',
                    minWidth: 80,
                    align: 'center',
                    render: (h, params) => {
                        const {id, files, status, personId} = params.row
                        const {userId, token} = vm.$store.state.userInfo
                        const file = []
                        if (files) {
                            files.map(item => {
                                file.push(item.fileId)
                            })
                        }
                        const upload = h('Button', {
                            props: {
                                type: 'primary',
                                size: 'small',
                                icon:'ios-cloud-upload-outline'
                            },
                            style: {
                                marginRight:"5px"
                            },
                            on: {
                                click: () => {
                                    vm.$refs.uploadModal.open({id, files})
                                }
                            }
                        }, '上传')
                        const download = h('a', {
                            attrs: {
                                href: `/api/file/p/file/downloadpackage?ids[]=${file.join(',')}&userId=${userId}&token=${token}`,
                                target: '_blank'
                            }
                        }, [
                            h('Button', {
                                props: {
                                    type: 'primary',
                                    size: 'small',
                                    icon:'ios-cloud-download-outline'
                                },
                                style: {
                                    marginRight: '5px'
                                }
                            }, '下载'),
                        ])
                        const btns = []
                        // 当前人是培训人
                        if (vm.authButton.includes('ops_train_handle_upload') && status == 3 && vm.userInfo.userId == personId) {
                            btns.push(upload)
                        }
                        if (vm.authButton.includes('ops_train_handle_download') && status == 5) {
                            btns.push(download)
                        }
                        return h('div', btns)
                    }
                }, {
                    title: '操作',
                    type: 'act',
                    width: 200,
                    align: 'center',
                    render: (h, params) => {
                        const {id, status, personId, files} = params.row
                        const deal = h('Button', {
                            props: {
                                type: 'primary',
                                size: 'small'
                            },
                            style: {
                                marginRight: '5px'
                            },
                            on: {
                                click: () => {
                                    vm.endTrain(id)
                                }
                            }
                        }, '完结')
                        const detail = h('Button', {
                            props: {
                                type: 'primary',
                                size: 'small'
                            },
                            style: {
                                marginRight: '5px'
                            },
                            on: {
                                click: () => {
                                    vm.$router.push({
                                        name: 'trainDetail',
                                        params: {
                                            id: id
                                        },
                                        query: {
                                            status: status
                                        }
                                    })
                                }
                            }
                        }, '详情')
                        const btns = []
                        // 不是已完结状态，当前人是培训人，已经上传完文件
                        if (status != 5 && vm.userInfo.userId == personId && files && files.length > 0) {
                            btns.push(deal)
                        }
                        if (vm.authButton.includes('ops_train_handle_detail')) {
                            btns.push(detail)
                        }
                        return h('div', btns)
                    }
                }],
                data: []
            },
            pageInfo: {
                pageNo: 1,
                pageSize: 10,
                total: 0
            },
            // 培训时间的数组
            trainTime: [],
        }
    },
    computed: {
        ...mapState(['authButton', 'userInfo'])
    },
    mounted () {
        this.search()
        this.getTrainTypeList()
        // this.getMyOrgList()
    },
    methods: {
        preSearch() {
            this.pageInfo.pageNo = 1
            this.search()
        },
        // 查询所有的类型和名称的关系列表
        getTrainTypeList() {
            const vm = this
            api(operationApiList.trainTypeList)
            .then(res => {
                if (res.data.errcode === 0) {
                    const typeList = res.data.data
                    typeList.push({
                        type: '自定义培训'
                    })
                    vm.typeList = res.data.data
                }
            }, error => {console.log(error)})
        },
        // 获取所有单位
        getMyOrgList() {
          const vm = this
          api(apiList.getMyOrgList)
          .then(res => {
            if (res.data.errcode === 0) {
              vm.companyList = res.data.data.orgList
            }
          }, error => {console.log(error)})
        },
        // 打开树形结构，选择人员
        openTreeModal(status) {
            this.personStatus = status
            this.$refs.getTrainer.open()
        },
        pageChange(pageNo, pageSize) {
            this.pageInfo.pageNo = pageNo
            this.pageInfo.pageSize = pageSize
            this.search()
        },
        // 搜索列表
        doSearch(pageNo, pageSize) {
            const vm = this
            const endTime = vm.trainTime.length > 0 ? getDateRange(vm.trainTime[1]).end : ''
            const startTime = vm.trainTime.length > 0 ? getDateRange(vm.trainTime[0]).end : ''
            const data = {
                ...vm.searchCondition,
                handle: 1
            }
            data.startTime = startTime ? `${startTime} 00:00:00` : ''
            data.endTime = endTime ? `${endTime} 23:59:59` : ''
            api(operationApiList.trainAuditorPage, {
                pageNo: pageNo || vm.pageInfo.pageNo,
                pageSize: pageSize || vm.pageInfo.pageSize,
                data: data
            }).then(res => {
                if (res.data.errcode === 0) {
                    const result = res.data.data
                    vm.pageInfo.pageNo = result.pageNum
                    vm.pageInfo.total = result.total
                    vm.tableList.data = result.list
                }
            }, error => {console.log(error)})
        },
        search() {
            this.doSearch()
        },
        // 完结
        endTrain(id) {
            const vm = this
            vm.$Modal.confirm({
                title: '完结确认',
                content: '确定完结吗？',
                onOk: () => {
                    api(operationApiList.trainEndUpdate, {
                        id: id
                    }).then(res => {
                        if (res.data.errcode === 0) {
                            vm.$Message.success('完结成功！！！')
                            vm.search()
                        }
                    }, error => {console.log(error)})
                }
            })
        },
        // 文件上传
        uploadFile(files, delFiles) {
            const vm = this
            api(operationApiList.trainFileSave, {
                files,
                fileIds: delFiles
            }).then(res => {
                if (res.data.errcode === 0) {
                    vm.$Message.success('文件上传成功')
                    vm.search()
                }
            }, error => {console.log(error)})
        },
        // 选择人员之后的回调
        selectPerson(list) {
            // 选择人提交人（查询）
            this.searchCondition.person = list.length > 0 ? list[0].title : ''
        }
    },
    watch: {
        currentTypeIdx () {
            if (this.currentTypeIdx === 0) {
                this.searchCondition.type = ''
            } else {
                this.searchCondition.type = this.typeList[this.currentTypeIdx - 1].type
            }
        }
    }
}
</script>
<style scoped="scoped">
	.act-btn-group button{
        margin: 0 12px;
    }
    .ivu-modal-footer{
        display: none;
    }
    .ivu-form-item >>> .ivu-form-item-content{
        height:auto;
    }
    .ivu-input-wrapper >>> textarea{
        resize: none;
    }
</style>