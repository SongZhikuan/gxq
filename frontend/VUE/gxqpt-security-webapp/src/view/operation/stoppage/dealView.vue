<template>
	<div>
		<table class="tab">
			<tr>
				<th width="150">故障类别</th>
				<td>{{dataView.faultType}}</td>
				<th width="150">故障级别</th>
				<td>{{dataView.faultLevel | filtersFaultLevel}}</td>
			</tr>
			<tr>
				<th>故障简述</th>
				<td colspan="3">{{dataView.faultInfo}}</td>
			</tr>
			<tr>
				<th>处理时间</th>
				<td colspan="3">{{dataView.dealTime}}</td>
			</tr>
			<tr>
				<th>处理流程</th>
				<td colspan="3">{{dataView.dealProcess}}</td>
			</tr>
			<tr>
				<th>结果</th>
				<td colspan="3">{{dataView.result}}</td>
			</tr>
			<tr>
				<th>建议</th>
				<td colspan="3">{{dataView.advice}}</td>
			</tr>
			<tr>
				<th>附件</th>
				<td colspan="3"><p v-for="fileItem in dataView.fileList"><a class="downloadMain" href="javascript:" @click="downloadFile(fileItem.fileUrl,fileItem.fileName)">{{fileItem.fileName}} <Icon class="downloadIcon" type="archive"></Icon></a></p></td>
			</tr>
			<tr v-if="isJoinKnowledge">
				<th>是否加入知识库</th>
				<td colspan="3">{{dataView.isJoinKnowledge|filterIsknow}}</td>
			</tr>
		</table>
	</div>
</template>

<script>
	export default {
		props: ['dataView'],
		data() {
			return {
				isJoinKnowledge:true,
			}
		},
		filters:{
			filterIsknow(val){
				if(val==1){
					return "是";
				}else{
					return "否";
				};
			},
			filtersFaultLevel(val){
			  let faultLevelText = ''
			  switch (val){
			  	case '1':
			  	  faultLevelText = '一级'
			  		break;
			  	case '2':
			  	  faultLevelText = '二级'
			  		break;
			  	case '3':
			  	  faultLevelText = '三级'
			  		break;
			  	default:
			  		break;
			  }
			  return faultLevelText;
			}
		},
		methods:{
			downloadFile(url,filename){
				let urlDownload = "/api/file/file/download?url=" + url + "&filename=" + filename;
                window.open(urlDownload);
			},
			isJoinKnowPage(){
				this.isJoinKnowledge=false;
			}
		}
	}
</script>

<style lang="less" type="text/css" scoped>
.downloadMain{
	position: relative;
}
.downloadIcon{
	position: absolute;
    bottom: -1px;
    right: -20px;
}
.tab{
	width: 100%;
	table-layout: fixed;
	border-collapse: collapse;
	border-spacing: 0;
	th{
		background: #f5f5f5;
		padding: 9px;
		border:1px solid #ddd;
	}
	td{
		padding: 9px;
		border:1px solid #ddd;
	}
}
</style>