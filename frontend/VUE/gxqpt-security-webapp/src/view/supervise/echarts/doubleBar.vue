<!-- 人员预警处理对比分析图 -->
<template>
	<div id="doubleBar"></div>
</template>

<script>
	export default {
		data() {
			return {
				myData:[],
				timeLineData:[],
				databeast:[],
				databeauty:[],
			}
		},
		methods: {
			refresh(data) {
				this.myData = data.myData;
				this.timeLineData = data.myData1;
				this.databeast = data.databeast;
				this.databeauty = data.databeast1;
				this.initLineBar();
			},
			initLineBar() {
				// 基于准备好的dom，初始化echarts实例
				var echarts = this.$echarts;
				var lineBar = echarts.init(document.getElementById('doubleBar'))
				var myData = this.myData;
				var databeast = this.databeast;
				var databeauty = this.databeauty;
				var timeLineData = this.timeLineData;

				const option = {
					baseOption: {
						title: {
							text: '',
							x: 'center',
							top: 30,
							left: 10,
							textStyle: {
								color: '#40CED3',
								fontWeight: 'normal'
							}
						},
						/*  backgroundColor: '#000',*/
						timeline: {
							show: false,
							axisType: 'category',
							data: [],
						},
						tooltip: {
							show: true,
							trigger: 'axis',
							formatter: '{b}<br/>{a}: {c}个',
							axisPointer: {
								type: 'shadow',
							}
						},

						grid: [{
							show: false,
							left: '4%',
							top: 20,
							bottom: 30,
							containLabel: true,
							width: '45%',
						}, {
							show: false,
							left: '54%',
							top: 20,
							bottom: 50,
							width: '30%'
						}, {
							show: false,
							right: '4%',
							top: 20,
							bottom: 30,
							containLabel: true,
							width: '45%',
						}, ],

						xAxis: [{
							type: 'value',
							inverse: true,
							axisLine: {
								show: false,
							},
							axisTick: {
								show: false,
							},
							position: 'bottom',
							axisLabel: {
								show: true,
								textStyle: {
									color: '#B2B2B2',
									fontSize: 12,
								},
							},
							splitLine: {
								show: true,
								lineStyle: {
									color: '#eee',
									width: 1,
									type: 'solid',
								},
							},
						}, {
							gridIndex: 1,
							show: false,
						}, {
							gridIndex: 2,
							type: 'value',
							axisLine: {
								show: false,
							},
							axisTick: {
								show: false,
							},
							position: 'bottom',
							axisLabel: {
								show: true,
								textStyle: {
									color: '#B2B2B2',
									fontSize: 12,
								},
							},
							splitLine: {
								show: true,
								lineStyle: {
									color: '#eee',
									width: 1,
									type: 'solid',
								},
							},
						}, ],
						yAxis: [{
							type: 'category',
							inverse: true,
							position: 'right',
							axisLine: {
								show: false
							},
							axisTick: {
								show: false
							},
							axisLabel: {
								show: true,
								margin: 8,
								textStyle: {
									color: '#333',
									fontSize: 12,
								},

							},
							data: myData,
						}, {
							gridIndex: 1,
							type: 'category',
							inverse: true,
							position: 'left',
							axisLine: {
								show: false
							},
							axisTick: {
								show: false
							},
							axisLabel: {
								show: false,
								textStyle: {
									color: '#333',
									fontSize: 12,
								},

							},
							data: myData.map(function(value) {
								return {
									value: value,
									textStyle: {
										align: 'center',
									}
								}
							}),
							axisPointer: {
							  show: false
							}
						}, {
							gridIndex: 2,
							type: 'category',
							inverse: true,
							position: 'left',
							axisLine: {
								show: false
							},
							axisTick: {
								show: false
							},
							axisLabel: {
								show: true,
								textStyle: {
									color: '#333',
									fontSize: 12,
								},

							},
							data: timeLineData,
						}, ],
						series: [],

					},

					options: [],

				};

				option.baseOption.timeline.data.push(timeLineData[1]);
				option.options.push({
					title: {

					},
					series: [{
							name: '预警数量',
							type: 'bar',
							barGap: 20,
							barWidth: 15,
							label: {
								normal: {
									show: false,
								},
								emphasis: {
									show: true,
									position: 'left',
									offset: [0, 0],
									textStyle: {
										color: '#fff',
										fontSize: 14,
									},
								},
							},
							itemStyle: {
								normal: {
									color: new echarts.graphic.LinearGradient(
										1, 0, 0, 0, [{
												offset: 0,
												color: '#DA9E3E'
											},
											{
												offset: 1,
												color: '#DC432A'
											}
										]
									)
								}

							},

							data: databeast,
						},
						{
							name: '预警数量',
							type: 'bar',
							barGap: 20,
							barWidth: 15,
							xAxisIndex: 2,
							yAxisIndex: 2,
							label: {
								normal: {
									show: false,
								},
								emphasis: {
									show: true,
									position: 'right',
									offset: [0, 0],
									textStyle: {
										color: '#fff',
										fontSize: 14,
									},
								},
							},
							itemStyle: {
								normal: {
									color: new echarts.graphic.LinearGradient(
										1, 0, 0, 0, [{
												offset: 0,
												color: '#0CF2F6'
											},
											{
												offset: 0.5,
												color: '#06BBFA'
											},
											{
												offset: 1,
												color: '#0086FE'
											}
										]
									)
								}

							},
							data: databeauty,
						}
					]
				});
				// 使用刚指定的配置项和数据显示图表。
				lineBar.setOption(option);
			}
		}
	}
</script>

<style lang="less" scoped>
	#doubleBar {
		width: 100%;
		height: 100%;
	}
</style>
