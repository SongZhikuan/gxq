<template>
    <Layout>
      <Content>
        <Breadcrumb>
          <BreadcrumbItem>运维文档管理</BreadcrumbItem>
          <BreadcrumbItem>运维文档{{isManage ? '管理': '查询'}}</BreadcrumbItem>
        </Breadcrumb>
        <Card>
          <Form ref="formValidate" inline :label-width="90">
            <FormItem label="文档类别">
              <div class="ivu-form-item-content" @click="getTypeTree(false)">
                <div class="ivu-input-wrapper ivu-input-type">
                  <i class="ivu-icon ivu-icon-load-c ivu-load-loop ivu-input-icon ivu-input-icon-validate"></i>
                  <div class="ivu-input" style="width: 160px;">{{typeInfo.title}}</div>
                </div>
              </div>
            </FormItem>
            <FormItem label="文档编号">
              <Input type="text" v-model="formData.code"></Input>
            </FormItem>
            <FormItem label="文档名称">
              <Input type="text" v-model="formData.name"></Input>
            </FormItem>
            <FormItem label="上传人">
              <Input type="text" v-model="formData.createUserName"></Input>
            </FormItem>
            <FormItem label="上传时间">
              <DatePicker
                class="queryTime"
                v-model="formData.queryTime"
                format="yyyy-MM-dd"
                type="daterange"
                placement="bottom-start"
                :editable="false"
                placeholder="请选择日期">
              </DatePicker>
            </FormItem>
            <FormItem :label-width="20">
              <Button type="primary" @click="preSearch" v-if="(!isManage && authButton.includes('ops_file_qy_query')) || (isManage && authButton.includes('ops_file_manage_leader_query'))">查询</Button>
              <Button
                type="primary"
                @click="resetCondition"
                v-if="(!isManage && authButton.includes('ops_file_qy_reset')) || (isManage && authButton.includes('ops_file_manage_leader_reset'))">重置</Button>
            </FormItem>
          </Form>
          <Row style="margin-bottom: 20px;">
            <Col>
              <Button
                type="primary"
                @click="downPackage"
                v-if="(!isManage && authButton.includes('ops_file_qy_download')) || (isManage && authButton.includes('ops_file_manage_leader_download'))">下载</Button>
              <Button
                type="primary"
                @click="openCreateModal"
                v-if="isManage && authButton.includes('ops_file_manage_leader_add')">新增</Button>
            </Col>
          </Row>
          <hy-table
            highlight-row
            border
            :current="pageInfo.pageNo"
            :columns="columns"
            :data="docList"
            :total="Number(pageInfo.total)"
            :pageSize="Number(pageInfo.pageSize)"
            pageType="small"
            show-elevator
            show-sizer
            placement="top"
            ref="selection"
            @on-change="pageNoChange"
            @on-page-size-change="pageSizeChange"
            @on-selection-change="doSelect" />
        </Card>
      </Content>
      <Modal
        v-model="createModal"
        :mask-closable="false"
        :title="modalTitle"
        width="40%">
        <Form ref="createForm" :model="createForm" :rules="createRule" :label-width="90">
          <FormItem label="文档类别" prop="classifyName">
            <div class="ivu-form-item-content" @click="getTypeTree(true)">
              <div class="ivu-input-wrapper ivu-input-type">
                <i class="ivu-icon ivu-icon-load-c ivu-load-loop ivu-input-icon ivu-input-icon-validate"></i>
                <div class="ivu-input" style="width: 100%;">{{createForm.classifyName}}</div>
              </div>
            </div>
          </FormItem>
          <FormItem label="文档名称" prop="name">
            <Input type="text" v-model="createForm.name" :maxlength="255" placeholder="限制输入255个字符以内"></Input>
          </FormItem>
          <FormItem label="文档编号" prop="code">
            <Input type="text" v-model="createForm.code":maxlength="64" placeholder="限制输入64个字符以内"></Input>
          </FormItem>
          <FormItem label="单位名称" :required="true" prop="companyId">
            <Select v-model="companyIdx">
              <Option :value="idx" v-for="(item, idx) in companyList" :key="item.id">{{item.name}}</Option>
            </Select>
          </FormItem>
          <FormItem label="文档上传" prop="documentId">
            <hyUpload
              ref="fileUpload"
              action="/api/file/p/file/simple"
              :onSuccess="fileUploadSuccess"
              :before-upload="beforeFileUpload"
              :on-remove="fileRemove"
              :format="['doc','docx','pdf']"
              :default-file-list="defaultFileList"/>
          </FormItem>
          <FormItem label="文档描述" prop="description">
            <Input
              v-model="createForm.description"
              type="textarea"
              :autosize="{minRows: 5,maxRows: 5}"
              :maxlength="2048"
              placeholder="限制输入2048个字符以内">
            </Input>
          </FormItem>
        </Form>
        <div slot="footer">
          <Button class="modalBtn" type="primary" @click="saveFile" size="large">确定</Button>
          <Button class="modalBtn" type="default" @click="createModal = false" size="large">取消</Button>
        </div>
      </Modal>
      <Modal
        v-model="typeModal"
        :mask-closable="false"
        title="文档类别选择"
        width="40%">
        <Tree ref="typeTree" :data="typeTree" empty-text="暂无文档类别"></Tree>
        <div slot="footer">
          <Button class="modalBtn" type="primary" @click="selectType" size="large">确定</Button>
          <Button class="modalBtn" type="default" @click="typeModal = false" size="large">取消</Button>
        </div>
      </Modal>
      <Modal
        v-model="docModal"
        :mask-closable="false"
        title="文件预览"
        width="60%">
        <iframe :src="`http://www.xdocin.com/xdoc?_func=to&amp;_format=html&amp;_cache=1&amp;_xdoc=${docUrl}`" frameborder="0" width="100%" height="400px" v-if="docUrl"></iframe>
      </Modal>
    </Layout>
</template>

<script>
import { mapState } from 'vuex'
import api from '@/api/axiosApi'
import operationApiList from '@/api/operationApiList'
import apiList from '@/api/comApiList'
import hyUpload from '@/components/hengyun/hyUpload'

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

function handleTreeData(data) {
  data.map((value, idx) => {
    if (value.children && value.children.length > 0) {
      data[idx].disableCheckbox = true
      // 默认打开树结构
      // data[idx].expand = true
      data[idx].children = handleTreeData(data[idx].children)
    }
  })
  return data
}

export default {
  components: {
    hyUpload
  },
  data() {
    const vm = this
    return {
      docUrl: '',
      // 是否是编辑或者新增
      isEdit: false,
      // 编辑时文档的id
      fileId: '',
      // 打开文档类别弹窗时是搜索条件还是新增文档
      isCreate: false,
      // 新增窗口
      createModal: false,
      // 新增或者修改弹窗的名称
      modalTitle: '新增文档',
      // 预览窗口
      docModal: false,
      // 已选列表的值
      selections: [],
      // 选择信息类型的弹窗
      typeModal: false,
      modal: false,
      companyList: [],
      // 选择的单位的序号id
      companyIdx: '',
      // 文件上传状态
      uploading: false,
      // 编辑时的默认文件列表
      defaultFileList: [],
      // 分页信息
      pageInfo: {
        pageNo: 1,
        pageSize: 10,
        total: 0
      },
      // 搜索条件的表单
      formData: {
        code: '',
        name: '',
        createUserName: '',
        // 初始化开始时间和结束时间
        queryTime: [],
      },
      // 新增文档的表单
      createForm: {
        // 文件类型
        fileType: '',
        // 单位名称
        companyName: '',
        // 单位id
        companyId: '',
        // 文档名称
        name: '',
        // 文档id
        documentId: '',
        // 文档类别
        classifyName: '',
        // 类别id
        classifyId: '',
        // 文档编号
        code: '',
        // 文档版本
        version: '',
        // 文档描述
        description: ''
      },
      // 文档类别信息
      typeInfo: {
        // 类型名称
        title: '',
        // 类型id
        id: ''
      },
      columns: [{
        type: 'selection',
        width: 60,
        align: 'center'
      },
      {
        title: '文档名称',
        key: 'name',
        render: (h, params) => {
          return h('div', [
            h('Tooltip', {
              props: { placement: 'right' },
              style: {
                maxHeight: '200px',
                overflow: 'auto'
              }
            }, [
              h('span', {
                style: {
                  display: 'inline-block',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap'
                },
                domProps: {
                  innerHTML: params.row.name
                }
              }),
              h('span', {
                slot: 'content',
                style: { whiteSpace: 'normal', wordBreak: 'break-all', maxHeight: '200px' },
                domProps: {
                  innerHTML: params.row.name
                }
              })
            ])
          ])
        }
      },
      {
        title: '文档编号',
        key: 'code',
        render: (h, params) => {
          return h('div', [
            h('span', {
              style: {
                display: 'inline-block',
                width: params.column._width * 0.9 + 'px',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap'
              },
              domProps: {
                title: params.row.code
              }
            }, params.row.code)
          ])
        }
      },
      {
        title: '文档类别',
        key: 'classifyName'
      },
      {
        title: '文档描述',
        key: 'description',
        render: (h, params) => {
          return h('div', [
            h('span', {
            style: {
              display: 'inline-block',
              width: params.column._width * 0.9 + 'px',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap'
            },
            domProps: {
              title: params.row.description
            }
          }, params.row.description)
            // h('Tooltip', {
            //   props: { placement: 'right' },
            //   style: {
            //     maxHeight: '200px',
            //     overflow: 'auto'
            //   }
            // }, [
            //   h('span', {
            //     style: {
            //       display: 'inline-block',
            //       width: params.column._width * 0.9 + 'px',
            //       overflow: 'hidden',
            //       textOverflow: 'ellipsis',
            //       whiteSpace: 'nowrap'
            //     },
            //     domProps: {
            //       innerHTML: params.row.description
            //     }
            //   }),
            //   h('span', {
            //     slot: 'content',
            //     style: { whiteSpace: 'normal', wordBreak: 'break-all', maxHeight: '200px' },
            //     domProps: {
            //       innerHTML: params.row.description
            //     }
            //   })
            // ])
          ])
        }
      },
      {
        title: '上传时间',
        key: 'createTime',
        width: 160
      },
      {
        title: '上传人',
        key: 'createUserName'
      },
      {
        title: '下载次数',
        key: 'downloadCount',
        width: 90
      },
      {
        title: '操作',
        key: 'act',
        align: 'center',
        width: 240,
        render: (h, params) => {
          const {documentId, id, fileType} = params.row
          const {userId, token} = vm.$store.state.userInfo
          const preview = h('Button', {
            props: {
              type: 'primary',
              size: 'small'
            },
            on: {
              click: () => {
                vm.prePreview(documentId)
              }
            }
          }, '预览')
          /* const download = h('a', {
            attrs: {
              href: `/api/file/p/file/down?id=${documentId}&userId=${userId}&token=${token}`,
              target: '_blank'
            }
          }, [h('Button', {
            props: {
              type: 'primary',
              size: 'small'
            }
          }, '下载')]) */
          const download = h('Button', {
            props: {
              type: 'primary',
              size: 'small'
            },
            on: {
              click: () => {
                vm.downPackageOnlyOne(documentId)
              }
            }
          }, '下载')
          const fileDelete = h('Button', {
            props: {
              type: 'primary',
              size: 'small'
            },
            on: {
              click: () => {
                vm.confirmDelete(id)
              }
            }
          }, '删除')
          const fileEdit = h('Button', {
            props: {
              type: 'primary',
              size: 'small'
            },
            on: {
              click: () => {
                vm.$refs.fileUpload.clearFiles()
                vm.createForm = {...params.row}
                vm.isEdit = true
                vm.createModal = true
                vm.modalTitle = '编辑文档'
                vm.getDocDetail(documentId)
              }
            }
          }, '编辑')
          const btns = []
          // 管理
          if (vm.isManage) {
            if (vm.authButton.includes('ops_file_manage_leader_update')) {
              btns.push(fileEdit)
            }
            if (vm.authButton.includes('ops_file_manage_leader_download')) {
              btns.push(download)
            }
            if (vm.authButton.includes('ops_file_manage_leader_delete')) {
              btns.push(fileDelete)
            }
          } else {
            if (vm.authButton.includes('ops_file_qy_preview')) {
              if (fileType == "doc" || fileType == "docx" || fileType == "txt" || fileType == "xls" || fileType == "xlsx" | fileType == "pdf" || fileType == "ppt" || fileType == "pptx") {
                // btns.push(preview)
              }
            }
            if (vm.authButton.includes('ops_file_qy_download')) {
              btns.push(download)
            }
          }
          if (vm.authButton.includes('ops_file_manage_leader_preview')) {
            // 指定可预览的文件类型
            if (fileType == "doc" || fileType == "docx" || fileType == "txt" || fileType == "xls" || fileType == "xlsx" | fileType == "pdf" || fileType == "ppt" || fileType == "pptx") {
              btns.push(preview)
            }
          }
          return h('div', {
            attrs: {
              class: 'act-container'
            }
          }, btns)
        }
      }],
      docList: [],
      // 信息类型list
      typeTree: [],
      // 预览文件的地址
      docUrl: '',
      createRule: {
        classifyName: [{required: true, message: '不可为空', trigger: 'change'}],
        name: [{required: true, message: '不可为空', trigger: 'blur'}],
        code: [{required: true, message: '不可为空', trigger: 'blur'}],
        documentId: [{
          required: true,
          trigger: 'blur',
          validator: (rule, value, cb) => {
            if (!vm.createForm.documentId) {
              cb(new Error('文件不能为空'))
              return
            }
            cb()
          }
        }],
        description: [{required: true, message: '不可为空', trigger: 'blur'}]
      }
    }
  },
  computed: {
    // 判断是管理还是查询，1是管理
    isManage() {
      return this.$route.params.manage === '1'
    },
    // 请求文件列表的地址
    fileQueryUrl() {
      return this.isManage ? 'manageQueryByClassifyId' : 'queryByClassifyId'
    },
    // 按钮权限列表
    ...mapState(['authButton'])
  },
  mounted () {
    this.search()
    this.getMyOrgList()
  },
  methods: {
    prePreview (fileId) {
      const vm = this
      api(apiList.fileFileGet, {
        id: fileId
      }).then(res => {
        if (res && res.data && res.data.errcode == 0 && res.data.data && res.data.data.url) {
          if (res.data.data.size > 4 * 1024 * 1024) {
            vm.$Message.error('文件太大，无法预览，请下载查看！')
          } else {
            vm.docUrl = res.data.data.url
            vm.docModal = true
          }
        } else {
          vm.$Message.error('附件预览失败，请下载查看！')
        }
      }, error => {
        vm.$Message.error('附件预览失败，请下载查看！')
        console.log(error)
      })
    },
    preSearch() {
      this.pageInfo.pageNo = 1
      this.search()
    },
    pageNoChange(pageNo) {
      this.pageInfo.pageNo = pageNo
      this.search()
    },
    pageSizeChange(pageSize) {
      this.pageInfo.pageSize = pageSize
      this.search()
    },
    // 打开新增编辑窗口
    openCreateModal() {
      this.$refs.fileUpload.clearFiles()
      this.modalTitle = '新增文档'
      this.createModal = true
      this.isEdit = false
      this.$refs.createForm.resetFields()
    },
    // 获取所有单位
    getMyOrgList() {
      const vm = this
      api(apiList.getMyOrgList)
      .then(res => {
        if (res.data.errcode === 0) {
          vm.companyList = res.data.data.orgList
          // 默认选中第一个单位
          vm.companyIdx = 0
        }
      }, error => {console.log(error)})
    },
    // 文件上传删除时的钩子
    fileRemove(file, fileList) {
      this.createForm.fileType = ''
      this.createForm.documentId = ''
    },
    // 文件上传前清空列表，因为每次只能传一个文件
    beforeFileUpload() {
      this.$refs.fileUpload.clearFiles()
      this.uploading = true
      return true
    },
    // 新增文件上传成功的回调
    fileUploadSuccess(resp, file, fileList) {
      this.createForm.fileType = resp.data.list[0].ext
      this.createForm.documentId = resp.data.list[0].id
      this.$refs.createForm.validateField('documentId', res => {})
      this.uploading = false
    },
    // 新增/修改文档
    saveFile() {
      const vm = this
      if (vm.uploading) {
        vm.$Message.info('文件上传中，请稍等...')
        return
      }
      vm.$refs.createForm.validate(valid => {
        if (valid) {
          const url = vm.isEdit ? 'fileUpdate' : 'fileSave'
          api(operationApiList[url], {
            ...vm.createForm,
            companyName: vm.companyList[vm.companyIdx].name,
            companyId: vm.companyList[vm.companyIdx].id
          }).then(res => {
            if (res.data.errcode === 0) {
              vm.$Message.success('操作成功！')
              vm.search()
              vm.$refs.createForm.resetFields()
              vm.$refs.fileUpload.clearFiles()
              vm.createForm.classifyId = ''
              vm.createModal = false
              vm.$nextTick(() => {
                // 默认选中第一家单位
                vm.createForm.companyName = vm.companyList[0].name
                vm.createForm.companyId = vm.companyList[0].id
                vm.companyIdx = 0
              })
            } else {
              vm.$Message.error(res.data.errmsg)
            }
          }, error => {vm.$Message.success('新增失败')})
        } else {
          vm.$Message.info('表单验证失败！')
        }
      })
    },
    search () {
      this.getDocList()
    },
    // 获取所有文档的列表
    getDocList (pageNo, pageSize) {
      const vm = this
      const endTime = vm.formData.queryTime.length > 0 ? getDateRange(vm.formData.queryTime[1]).end : ''
      const startTime = vm.formData.queryTime.length > 0 ? getDateRange(vm.formData.queryTime[0]).end : ''
      api(operationApiList[vm.fileQueryUrl], {
        pageNo: pageNo || vm.pageInfo.pageNo,
        pageSize: pageSize || vm.pageInfo.pageSize,
        data: {
          classifyId: vm.typeInfo.id || '751514420097',
          sord: 'asc',
          createTimeEnd: endTime ? `${endTime} 23:59:59` : '',
          createTimeStart: startTime ? `${startTime} 00:00:00` : '',
          ...vm.formData
        }
      }).then(res => {
        if (res.data.errcode === 0) {
          vm.selections = []
          vm.docList = res.data.data.list
          vm.pageInfo.total = Number(res.data.data.total)
        }
      }, error => {console.log(error)})
    },
    // 获取文档类型的树结构
    getTypeTree(isCreate) {
      const vm = this
      vm.isCreate = isCreate
      vm.typeModal = true
      if (vm.typeTree.length > 0) {
        return
      }
      api(operationApiList.getTypeTree, {treeId: '751514420097'})
      .then(res => {
        if (res.data.errcode === 0) {
          const tree = JSON.parse(JSON.stringify(res.data.data).replace(/name/g, 'title'))
          vm.typeTree = handleTreeData(tree)
        }
      }, error => {console.log(error)})
    },
    // 确定选择的文档类型
    selectType() {
      const vm = this
      const obj = vm.$refs.typeTree.getSelectedNodes()
      // 非新增，也就是选择搜索条件
      if (!vm.isCreate) {
        if (obj.length === 0) {
          vm.typeInfo = {
            title: '',
            id: ''
          }
        } else {
          vm.typeInfo = obj[0]
        }
      } else {
        if (obj.length === 0 || (obj[0].children && obj[0].children.length > 0)) {
          vm.createForm.classifyName = ''
          vm.createForm.classifyId = ''
          vm.$Message.info('请选择类别子节点！')
        } else {
          vm.createForm.classifyName = obj[0].title
          vm.createForm.classifyId = obj[0].id
          vm.$refs.createForm.validateField('classifyName', res => {})
        }
      }
      vm.typeModal = false
    },
    resetCondition() {
      Object.assign(this.$data.formData, this.$options.data().formData)
      Object.assign(this.$data.typeInfo, this.$options.data().typeInfo)
    },
    // 勾选列表时触发
    doSelect(selection) {
      this.selections = selection
    },
    // 批量下载
    downPackage() {
      const vm = this
      const {userId, token} = vm.$store.state.userInfo
      const ids = []
      if (vm.selections.length === 0) {
        vm.$Message.info('请先勾选要下载的文件!')
        return
      }
      vm.selections.map(item => {
        ids.push(item.documentId)
      })
      const url = `/api/file/p/file/downloadpackage?ids[]=${ids.join(',')}&userId=${userId}&token=${token}`
      window.open(url)
    },
    // 单个下载
    downPackageOnlyOne(id) {
      const vm = this
      /* const {userId, token} = vm.$store.state.userInfo
      const ids = []
      if (vm.selections.length === 0) {
        vm.$Message.info('请先勾选要下载的文件!')
        return
      }
      vm.selections.map(item => {
        ids.push(item.documentId)
      }) */
      const {userId, token} = vm.$store.state.userInfo
      const url = `/api/file/p/file/down?id=${id}&userId=${userId}&token=${token}`
      window.open(url)
    },
    // 删除文档
    confirmDelete(id) {
      const vm = this
      vm.$Modal.confirm({
        title: '删除确认',
        content: '确认删除此文档吗？',
        onOk: () => {
          api(operationApiList.fileDelete, {
            id: id
          }).then(res => {
            if (res.data.data == true) {
              vm.$Message.success('删除成功！')
              vm.search()
            }
          })
        }
      })
    },
    // 获取文档的详情
    getDocDetail (id) {
      const vm = this
      api(apiList.fileFileGet, {
        id: id
      }).then(res => {
        if (res && res.data && res.data.errcode == 0 && res.data.data && res.data.data.url) {
          vm.defaultFileList = [{
            name: res.data.data.submittedFileName,
            url: res.data.data.url
          }]
          vm.createForm.fileType = res.data.data.ext
          vm.createForm.documentId = id
        } else {
          vm.$Message.error('附件查询失败')
        }
      }, error => {
        vm.$Message.error('附件查询失败')
      })
    }
  },
  watch: {
    '$route' () {
      const vm = this
      const {selections, typeModal, modal, pageInfo, formData, typeInfo, docList, typeTree} = vm.$options.data()
      vm.selections = selections
      vm.typeModal = typeModal
      vm.modal = modal
      vm.pageInfo = pageInfo
      vm.formData = formData
      vm.typeInfo = typeInfo
      vm.docList = docList
      vm.typeTree = typeTree
      vm.$nextTick(() => {
        vm.getDocList()
      })
    }
  }
}
</script>

<style type="text/css" scoped>
.wAuto{
  width: 100%;
}
.queryTime{
  width: 200px;
}
.flow{
  margin-top: 20px ;
}
</style>

<style lang="less">
.act-container button{
  margin: 0 5px !important;
}
.ivu-tooltip-popper{
  max-width: 500px !important;
}
.ivu-tooltip-inner{
  max-width: 500px;
  min-height: 34px;
  max-height: 360px;
  overflow: auto;
}
::-webkit-scrollbar,body::-webkit-scrollbar{
  width: 5px;
  height: 5px;
}
  
/*定义滚动条轨道 内阴影+圆角*/
::-webkit-scrollbar-track,body::-webkit-scrollbar-track{
  -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,0.3);
  border-radius: 10px;
  background-color: #eee;
}
  
/*定义滑块 内阴影+圆角*/
::-webkit-scrollbar-thumb,body::-webkit-scrollbar-thumb {
  border-radius: 10px;
  -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,0.3);
  background-color: #2d8cf0;
}
</style>