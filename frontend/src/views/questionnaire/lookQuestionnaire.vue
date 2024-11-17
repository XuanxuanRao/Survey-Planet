<script  setup >
import * as echarts from 'echarts';
import { ref, onMounted,nextTick,reactive,onBeforeUnmount, } from 'vue'
import 'echarts-wordcloud'; // 引入 Word Cloud 插件
import {userGetAnswernaireResult,userGetQuestionnaire,userGetPageResult} from '@/api/questionnaire'
import { useRoute } from 'vue-router'

const isDialogVisible = ref({});
const background = ref(true)
const pageView = reactive({}); //填空题
const pageSize=reactive({});
const currentPages = reactive({});
const route = useRoute()
const sid = route.query.id
const qids = ref([]);
const questions = ref([]); // 定义一个响应式容器
const answers = ref([]); // 用于存储每个 qid 的答案
const mergedData = ref([]);
const tableData = ref([]);
const newArray = ref([]);
const newArray1 = ref([]);
const mode = ref(0);
const mode1 = ref(Array(10000).fill(0)); // 初始化为长度为 n 的数组，所有元素为 0
const transformedWordClouds = ref({}); // 创建一个对象来存储不同的 wordCloud
const surveyType=ref()
const codes = ref({});
const selectedSubmitIds = ref([]);
//初始化
for (let i = 0; i <= 1000; i++) {
  currentPages[i] = 1;
  isDialogVisible[i] = false;  
  pageSize[i] = 5; 
  pageView[i] = [];
}

// 在组件挂载时获取数据
onMounted(async () => {
    //获取问卷数据,包括questions(questionnaires),type
    const res = await userGetQuestionnaire(sid);
    console.log("res",res)
    //questionnaires数据不会再次变,为临时非响应变量
    const questionnaires = res.data.questions; 
    surveyType.value=res.data.type 
    console.log("surveyType.value",surveyType.value)
    questions.value = res.data.questions;
    console.log("questionnaires",questionnaires)
    qids.value = questionnaires
              .filter(questionnaire => questionnaire.type !== 'file') // 过滤掉 type 为 'file' 的项
              .map(questionnaire => questionnaire.qid); // 获取 qid
    // 获取每个 qid 的答案,合成数组results
    const results = await Promise.all(qids.value.map(qid => userGetAnswernaireResult(qid)));
    console.log("results",results)
    //预处理,如果是exam 的填空题和代码题,则额外加上属性
    results.forEach(result => {
        //exam 的填空题和代码题,排除选择题grade,count,index
        if (result.data.answerCount==null  && surveyType.value === 'exam') {          
          codes.value[result.data.qid] = result.data; 
          const item = codes.value[result.data.qid];
          const gradeCountEntries = Object.entries(item.gradeCount);
          const processedCodes = gradeCountEntries.map(([grade, count], index) => ({
            grade: Number(grade), // 将 grade 转换为数字
            count: count,
            index: index + 1 // 添加 index 属性，从 1 开始
          }));
          // 如果需要将处理后的结果存储回 codes
          codes.value[result.data.qid] = processedCodes; // 更新 codes 中对应的 qid
          result.data.codes = processedCodes;         
        }
      });
      
    // 合并所有results的 result.data 到 answers.value
    answers.value = results.map(result => result.data); 
    console.log("answers.value",answers.value)
    //合并questions和answers为mergedData
    mergedData.value = questions.value.map(question => {
        const answer = answers.value.find(ans => ans.qid === question.qid) || {};
        return {
            ...question,
            ...answer // 合并问题和答案
        };
    });
    console.log("mergedData.valueBefore",mergedData.value)
    //提取mergedData中的相关属性到tableData
    tableData.value = mergedData.value.map(item => {
    return {
      options: item.options ? Array.from(item.options) : [], // 将 Proxy(Array) 转换为普通数组
      answerCount: item.answerCount ? Object.fromEntries(Object.entries(item.answerCount)) : {}, // 将 Proxy(Object) 转换为普通对象
        total: item.total
    };
    });
    console.log("tableData.value",tableData.value)
    //将tableData相关属性转换为数组,添加到mergerdData作为其属性
    for (let i = 0; i < tableData.value.length; i++) {
    // 确保 tableData.value[i] 是一个对象而不是数组
    const item = tableData.value[i];
    if (!newArray1.value[i]) {
        newArray1.value[i] = []; // 初始化为数组
    }
    // 使用 map 而不是 flatMap
    newArray.value = item.options.map((option, index) => {
        const answerCount = item.answerCount[option] || 0; // 获取 answerCount，若不存在则为 0
        const rate = item.total > 0 ? ((answerCount / item.total) * 100).toFixed(2)  +'%': '0%';
        return {
            option: String.fromCharCode(65 + index)+" " + option,
            answerCount: String(answerCount), // 转换为字符串
            rate: rate
        };
    });
    // 将 newArray.value 添加到 newArray1
    newArray1.value[i].push(...newArray.value);  // 使用扩展运算符将元素添加到 newArray1
    mergedData.value[i].newArray1Data = newArray1.value[i]; // 作为新属性添加
    newArray.value = null; // 清空 newArray.value   
    }
    //初始化饼图,柱状图和table表格
    mergedData.value.forEach((answernaire, index) => {
    if (answernaire.type === 'single_choice' || answernaire.type === 'multiple_choice'||answernaire.type === 'code'||(surveyType.value=='exam'&&answernaire.type === 'fill_blank')) {
      initCharts(answernaire, index); // 重新初始化图表
    }
    if(answernaire.type === 'fill_blank'||answernaire.type === 'code'||answernaire.type === 'file'){
      console.log("pageSize[answernaire.qid]",pageSize[answernaire.qid])
      handleCurrentChange(1,answernaire.qid,5,answernaire)        
    }
    });
    console.log("mergedData.value",mergedData.value)
    //初始化词云 
    mergedData.value.forEach((answernaire, index) => {
        if (answernaire.type === 'fill_blank') { // 检查 type 属性
            if (Array.isArray(answernaire.wordCloud)) { // 确保 wordCloud 是数组
                const qid = answernaire.qid; // 获取 qid
                const wordCloud = answernaire.wordCloud; // 获取 wordCloud 数组
                console.log("wordCloud",wordCloud)
                // 如果没有该 qid 的数组，则初始化一个空数               
                transformedWordClouds[qid] = ref([]);               
                // 遍历 wordCloud 并压入对应的数组
                answernaire.wordCloud.forEach(item => {
                    transformedWordClouds[qid].value.push({
                        name: item.word,
                        value: item.frequency
                    });
                });       
                console.log("transformedWordClouds[qid].value",transformedWordClouds[qid].value)    
                initChart1(transformedWordClouds[qid],index);               
            }
        }
    });   
})
//文件题的勾选框
const handleSelectionChange = (selectedRows) => {
  // 获取选中行的 submitId 并形成数组
  selectedSubmitIds.value = selectedRows.map(row => row.submitId);
  console.log(selectedSubmitIds.value); // 打印选中的 submitId 数组
};
//分页表格
const handleCurrentChange = async(pageNum,qid,Size,answernaire) => { 
  currentPages[qid]=pageNum
  const t = await userGetPageResult(qid,pageNum,Size);
  // if(answernaire.type=='file'){
  //   console.log("/api/report/detail/",t)
  // }
  
  if(answernaire.type=='code'){
    pageView[qid].value = t.data.records.map((item, index) => {
    const [url, language] = item.content; // 解构 content 数组为 url 和 language
    return {
      ...item, // 保留原有属性
      index: Size * (pageNum - 1) + index + 1, // 添加 index 属性
      url, // 添加 url 属性
      language, // 添加 language 属性
    };
  });
  }else if(answernaire.type=='file'){
    pageView[qid].value = t.data.records.map((item, index) => ({
      ...item, // 保留原有属性
      createTime: item.createTime.replace('T', '  '), // 替换字符 T 为空格
      total:t.data.total,
      index: Size *(pageNum-1) + index + 1 // 添加 index 属性
    }));
  }else{
    pageView[qid].value = t.data.records.map((item, index) => ({
      ...item, // 保留原有属性
      index: Size *(pageNum-1) + index + 1 // 添加 index 属性
    }));
  }
  console.log("pageViewqid.value",qid,pageView[qid].value)
}
//分页表格
const handleSizeChange = async(Size,qid,pageNum,answernaire) => {
  pageSize[qid] = Size;
  // pageView[qid] = ref([]);
  const t = await userGetPageResult(qid,pageNum,Size);
  if(answernaire.type=='code'){
    pageView[qid].value = t.data.records.map((item, index) => {
    const [url, language] = item.content; // 解构 content 数组为 url 和 language
    return {
      ...item, // 保留原有属性
      index: Size * (pageNum - 1) + index + 1, // 添加 index 属性
      url, // 添加 url 属性
      language, // 添加 language 属性
    };
  });
  }else if(answernaire.type=='file'){
    pageView[qid].value = t.data.records.map((item, index) => ({
      ...item, // 保留原有属性
      createTime: item.createTime.replace('T', '  '), // 替换字符 T 为空格
      total:t.data.total,
      index: Size *(pageNum-1) + index + 1 // 添加 index 属性
    }));
  }else{
      pageView[qid].value = t.data.records.map((item, index) => ({
      ...item, // 保留原有属性
      index: Size *(pageNum-1) + index + 1 // 添加 index 属性
      })); 
  }
  console.log("pageView[qid].value",qid,pageView[qid].value)
}
//模式转换,对应按钮统计与分析,查看问卷
const switchMode = (newMode) => {
  mode.value = newMode;
  if (newMode === 0) {
    nextTick(() => {
      mergedData.value.forEach((answernaire, index) => {
        if (answernaire.type === 'single_choice' || answernaire.type === 'multiple_choice'||answernaire.type === 'code'||(surveyType.value=='exam'&&answernaire.type === 'fill_blank')) {
        initCharts(answernaire, index); // 重新初始化图表
        }
      });
    });
  }
};
//模式转换1,对应查看表格
const switchMode1 = async(newMode1,answernaire, index) => {
  console.log("newMode1",newMode1)
    mode1.value[index] = newMode1;
    
    initCharts(answernaire, index); // 重新初始化图表
}
//初始化饼图,柱状图
const initCharts = async(answernaire, index)  => {
      // 图表初始化逻辑
      await nextTick(); // 确保 DOM 更新完成
      const pieChartDom = document.getElementById(`main-${index}`);
      const barChartDom = document.getElementById(`main1-${index}`);
      
      if (mode1.value[index] === 0 && pieChartDom){
      const pieChart = echarts.init(pieChartDom);
      if(answernaire.type === 'single_choice' || answernaire.type === 'multiple_choice'){
        //单选或多选的饼图柱状图
        const pieOption = {
        title: {
          text: '统计情况',
          subtext: '问卷调查',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        series: [
          {
            name: '选项',
            type: 'pie',
            radius: '50%',
            data: answernaire.newArray1Data.map(item => ({
              value: item.answerCount,
              name: item.option
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      pieChart.setOption(pieOption);
      }else{
        //exam 代码题,填空题的饼图,柱状图
        const pieOption = {
        title: {
          text: '得分情况',
          subtext: '问卷调查',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        series: [
          {
            name: '得分',
            type: 'pie',
            radius: '50%',
            data: answernaire.codes.map(item => ({
              value: item.count,
              name: item.grade
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      pieChart.setOption(pieOption);
      }     
      }
      else if(mode1.value[index] ===1  && barChartDom){
      const barChart = echarts.init(barChartDom);
      if(answernaire.newArray1Data.length!=0){
        const barOption = {
        title: {
          text: '选项统计',
          subtext: '问卷调查',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: answernaire.newArray1Data.map(item => item.option),
          name: '选项'
        },
        yAxis: {
          type: 'value',
          name: '数量'
        },
        series: [
          {
            name: '小计',
            type: 'bar',
            data: answernaire.newArray1Data.map(item => item.answerCount),
            emphasis: {
              focus: 'series'
            }
          }
        ]
      };
      barChart.setOption(barOption);
      }else{
        const barChart = echarts.init(barChartDom);
        const barOption = {
        title: {
          text: '得分统计',
          subtext: '问卷调查',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: answernaire.newArray1Data.map(item => item.grade),
          name: '得分'
        },
        yAxis: {
          type: 'value',
          name: '数量'
        },
        series: [
          {
            name: '小计',
            type: 'bar',
            data: answernaire.codes.map(item => item.count),
            emphasis: {
              focus: 'series'
            }
          }
        ]
      };
      barChart.setOption(barOption);      
      }      
    };
    }
  
  //初始化词云
  const initChart1 =async(datalist, index)  => {
    await nextTick(); // 确保 DOM 更新完成
    const chart = echarts.init(document.getElementById(`mywordcloud-${index}`));
    const option = {
      series: [
        {
          type: "wordCloud",
          gridSize: 12,
          shape: "circle",
          sizeRange: [30, 60],
          rotationRange: [0, 0],
          left: "center",
          top: "center",
          width: "200%",
          height: "200%",
          drawOutOfBound: false,
          textStyle: {
            color: function () {
              return (
                "rgb(" +
                Math.round(Math.random() * 256) +
                ", " +
                Math.round(Math.random() * 256) +
                ", " +
                Math.round(Math.random() * 256) +
                ")"
              );
            },
            emphasis: {
              shadowBlur: 10,
              shadowColor: "#2ac",
            },
          },
          data: datalist.value,
        },
      ],
    };
    chart.setOption(option);
  }
</script>

<template>
  <div style="display: flex;justify-content: center; gap: 10px;">
    <el-button class="custom-button" @click="switchMode(0)">
      统计与分析
    </el-button>
    <el-button class="custom-button" @click="switchMode(1)">
      查看问卷
    </el-button>
  </div>
  
  <ul v-if="mode==0" class="infinite-list" style="overflow: auto"> 
      <!--调查问卷  -->
      <li v-if="surveyType=='normal'" v-for="(answernaire, index) in mergedData" :key="index" class="infinite-list-item">
          <!-- 单选题，多选题 -->    
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;
          {{ answernaire.type==='single_choice'? '[单选题]':'[多选题]' }}</p>
          <el-table v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice' "
           :data="answernaire.newArray1Data"
           :default-sort="{ prop: 'answerCount', order: 'descending' }" border stripe   class="tableHead" >
            <el-table-column prop="option"  label="选项"   />
            <el-table-column prop="answerCount" sortable  label="小计" />
            <el-table-column prop="rate" label="比例" />
          </el-table>
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">本题有效填写人数：{{answernaire.total}}</p>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button>
          </div>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
          </div>
          <!-- 填空题 -->
          <p v-if="answernaire.type === 'fill_blank'">
            第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;[填空题]
          </p>
          <div v-if="answernaire.type === 'fill_blank'" class="demo-pagination-block">            
            <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">显示表格</el-button>
            <el-dialog v-model="isDialogVisible[answernaire.qid]" title="表格数据" >
              <el-table :data="pageView[answernaire.qid].value" border stripe class="tableHead">
                <el-table-column prop="index" label="序号"  />
                <el-table-column prop="createTime" label="提交时间"  />
                <el-table-column prop="content" label="内容" />
              </el-table>
              <el-pagination
              v-model:current-page="currentPages[answernaire.qid]"
              v-model:page-size="pageSize[answernaire.qid]"
              :page-sizes="[5, 10, 20]"
              :background="background"
              layout="total, sizes, prev, pager, next, jumper"
              :total="answernaire.total"
              @size-change="(size) =>handleSizeChange(size, answernaire.qid,currentPages[answernaire.qid],answernaire)"
              @current-change="(page) => handleCurrentChange(page, answernaire.qid,pageSize[answernaire.qid],answernaire)"
            />
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="isDialogVisible[answernaire.qid] = false">Cancel</el-button>
                <el-button type="primary" @click="isDialogVisible[answernaire.qid] = false">
                  Confirm
                </el-button>
              </div>
            </template>
            </el-dialog>
                      
          </div>
          <!-- 词云 -->
          <div v-if="answernaire.type === 'fill_blank'"
              :id=" 'mywordcloud-' + index"
              :style="{ width: '100%', height: '300px' }"
              :data="answernaire.wordCloud"
            ></div>
          <p v-if="answernaire.type === 'fill_blank'">本题有效填写人数：{{answernaire.total}}</p>
          <!-- 文件题 -->
          <p v-if="answernaire.type === 'file'">
            第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;[文件题]
          </p>
          <div v-if="answernaire.type === 'file'" class="demo-pagination-block">            
            <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">显示表格</el-button>
            <el-dialog v-model="isDialogVisible[answernaire.qid]" title="表格数据" >
              <el-table @selection-change="handleSelectionChange" :data="pageView[answernaire.qid].value" border stripe class="tableHead">
                <el-table-column type="selection"  width="55" />
                <el-table-column  prop="index" label="序号" width="80" />
                <el-table-column prop="createTime" label="提交时间" width="160" />
                <el-table-column label="内容与链接">
                  <template #default="scope">
                    <el-tooltip content="点击此处下载文件" placement="top">
                      <el-link :href="scope.row.content[0]" target="_blank">{{scope.row.content[0]}}</el-link>
                    </el-tooltip>
                    
                  </template>
                </el-table-column>  
              </el-table>
              <el-pagination
              v-model:current-page="currentPages[answernaire.qid]"
              v-model:page-size="pageSize[answernaire.qid]"
              :page-sizes="[5, 10, 20]"
              :background="background"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pageView[answernaire.qid]?.value[0]?.total||0"
              @size-change="(size) =>handleSizeChange(size, answernaire.qid,currentPages[answernaire.qid],answernaire)"
              @current-change="(page) => handleCurrentChange(page, answernaire.qid,pageSize[answernaire.qid],answernaire)"
              />
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="isDialogVisible[answernaire.qid] = false">Cancel</el-button>
                <el-button type="primary" @click="isDialogVisible[answernaire.qid] = false">
                  Confirm
                </el-button>
              </div>
            </template>
            </el-dialog>
          </div>
      </li>
      <li v-if="surveyType=='exam'" v-for="(answernaire, index) in mergedData" :key="index" class="infinite-list-item">
          <!-- 单选题，多选题 -->    
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;
          {{ answernaire.type==='single_choice'? '[单选题]':'[多选题]' }}</p>

          <el-table v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice' "
           :data="answernaire.newArray1Data"
           :default-sort="{ prop: 'answerCount', order: 'descending' }" border stripe   class="tableHead" >
            <el-table-column prop="option"  label="选项"   />
            <el-table-column prop="answerCount" sortable  label="小计" />
            <el-table-column prop="rate"  label="比例" />
          </el-table>
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">本题有效填写人数：{{answernaire.total}}</p>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button>
          </div>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
          </div>
          <!-- 填空题 -->
          <p v-if="answernaire.type === 'fill_blank'">
            第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;[填空题]
          </p>
          <div v-if="answernaire.type === 'fill_blank'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button>
          </div>
          <div v-if="answernaire.type === 'fill_blank'"  style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
          </div>
          <div v-if="answernaire.type === 'fill_blank'" class="demo-pagination-block">                        
            <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">显示表格</el-button>
            <el-dialog v-model="isDialogVisible[answernaire.qid]" title="表格数据" >
              <el-table :data="pageView[answernaire.qid].value" border stripe class="tableHead">
                <el-table-column prop="index" label="序号"  />
                <el-table-column prop="createTime" label="提交时间"  />
                <el-table-column prop="content" label="内容" />
              </el-table>
              <el-pagination
              v-model:current-page="currentPages[answernaire.qid]"
              v-model:page-size="pageSize[answernaire.qid]"
              :page-sizes="[5, 10, 20]"
              :background="background"
              layout="total, sizes, prev, pager, next, jumper"
              :total="answernaire.total"
              @size-change="(size) =>handleSizeChange(size, answernaire.qid,currentPages[answernaire.qid],answernaire)"
              @current-change="(page) => handleCurrentChange(page, answernaire.qid,pageSize[answernaire.qid],answernaire)"
            />
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="isDialogVisible[answernaire.qid] = false">Cancel</el-button>
                <el-button type="primary" @click="isDialogVisible[answernaire.qid] = false">
                  Confirm
                </el-button>
              </div>
            </template>
            </el-dialog>
                      
          </div>
          <!-- 词云 -->
          <div v-if="answernaire.type === 'fill_blank'"
              :id=" 'mywordcloud-' + index"
              :style="{ width: '100%', height: '300px' }"
              :data="answernaire.wordCloud"
            ></div>
          <p v-if="answernaire.type === 'fill_blank'">本题有效填写人数：{{answernaire.total}}</p>
          <!-- 代码题 -->
          <p v-if="answernaire.type === 'code'">
            第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;[代码题]
          </p>
          <div v-if="answernaire.type === 'code'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button>
          </div>
          <div v-if="answernaire.type === 'code'"  style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
          </div>
          
          <div v-if="answernaire.type === 'code'" class="demo-pagination-block"> 
          <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">显示表格</el-button>
          <el-dialog v-model="isDialogVisible[answernaire.qid]" title="表格数据" >
              <el-table  :data="pageView[answernaire.qid].value" border stripe class="tableHead">
                  <el-table-column prop="index" label="序号" width="80" />
                  <el-table-column prop="language" label="编程语言" width="100" />
                  <el-table-column prop="url"  label="文件url" />
              </el-table>
              <el-pagination
              v-if="answernaire.type === 'code'"
              v-model:current-page="currentPages[answernaire.qid]"
              v-model:page-size="pageSize[answernaire.qid]"
              :page-sizes="[5, 10, 20]"
              :background="background"
              layout="total, sizes, prev, pager, next, jumper"
              :total="answernaire.total"
              @size-change="(size) =>handleSizeChange(size, answernaire.qid,currentPages[answernaire.qid],answernaire)"
              @current-change="(page) => handleCurrentChange(page, answernaire.qid,pageSize[answernaire.qid],answernaire)"
              />
              <template #footer>
              <div class="dialog-footer">
                <el-button @click="isDialogVisible[answernaire.qid] = false">Cancel</el-button>
                <el-button type="primary" @click="isDialogVisible[answernaire.qid] = false">
                  Confirm
                </el-button>
              </div>
            </template>
          </el-dialog>
          </div>
      </li>
  </ul>
  
  <ul v-if="mode==1" class="infinite-list" style="overflow: auto"> 
  </ul>
  
</template>

<style scoped>
.artistic-text {
  font-family: 'Cursive', sans-serif; /* 使用艺术字体 */
  font-size: 48px; /* 设置字体大小 */
  color: #ff6347; /* 设置字体颜色 */
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3); /* 添加阴影效果 */
  letter-spacing: 2px; /* 字母间距 */
  text-align: center; /* 居中对齐 */
}
.infinite-list {
  
  border-radius: 10px;
  background-color: white;
  height: 80vh;
  padding: 20px;
  margin: 10px;
  list-style: none;
}
.infinite-list .infinite-list-item {
  overflow: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 1000px;
  margin: 10px;
  background-color:  lightblue;
  /* background: url('@/assets/img/2.jpg') no-repeat center center;
  background-size: cover;
  z-index: 20; */
}

.tableHead{
  width: 800px;
  border: 1px solid black; 
  margin-bottom: 30px;
}
.custom-button {
  position: relative;
  width: 100px;
  height: 45px;
  text-align: center;
  line-height: 60px;
  color: #fff;
  font-size: 18px;
  text-decoration: none;
  font-family: sans-serif;
  border-radius: 30px;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
  transition: background-position 0.5s;
}
.custom-button:hover {
  animation: animate 8s linear infinite;
}

@keyframes animate {
  from {
    background-position: 0%;
  }
  to {
    background-position: 480%;
  }
}

</style>
