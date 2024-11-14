<script setup>
import * as echarts from 'echarts';
import { ref, onMounted,nextTick } from 'vue'
import { ElTable, ElTableColumn, ElProgress } from 'element-plus';

import {userGetAnswernaireResult,userGetQuestionnaire} from '@/api/questionnaire'
import { useRoute } from 'vue-router'
const route = useRoute()
const sid = route.query.id
const analyseQuestionnaireList = ref([]);   // 响应式变量，存储用户数据
const qids = ref([]);
const questions = ref([]); // 定义一个响应式容器
const answers = ref([]); // 用于存储每个 qid 的答案
const mergedData = ref([]);
const tableData = ref([]);
const newArray = ref([]);
const newArray1 = ref([]);
const mode = ref(0);
const mode1 = ref(Array(100).fill(0)); // 初始化为长度为 n 的数组，所有元素为 0
// 在组件挂载时获取数据
onMounted(async () => {
    const res = await userGetQuestionnaire(sid);
    analyseQuestionnaireList.value = res.data;
    const questionnaires = res.data.questions;  
    questions.value = res.data.questions;
    qids.value = questionnaires.map(questionnaire => questionnaire.qid); 
    // 获取每个 qid 的答案，并提取 data 属性
    const results = await Promise.all(qids.value.map(qid => userGetAnswernaireResult(qid)));
    
    // 合并所有结果的 data 到 answers.value
    answers.value = results.map(result => result.data); 
    console.log("answers.value",answers.value)
    mergedData.value = questions.value.map(question => {
        const answer = answers.value.find(ans => ans.qid === question.qid) || {};
        return {
            ...question,
            ...answer // 合并问题和答案
        };
    });
    
    tableData.value = mergedData.value.map(item => {
    return {
      options: item.options ? Array.from(item.options) : [], // 将 Proxy(Array) 转换为普通数组
      answerCount: item.answerCount ? Object.fromEntries(Object.entries(item.answerCount)) : {}, // 将 Proxy(Object) 转换为普通对象
        total: item.total
    };
    });
    console.log("tableData.value",tableData.value)
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
    
    mergedData.value.forEach((answernaire, index) => {
      if (answernaire.type === 'single_choice' || answernaire.type === 'multiple_choice') {
        initCharts(answernaire, index); // 重新初始化图表
        }
      });
      console.log("mergedData.value",mergedData.value)
})
const switchMode = (newMode) => {
  mode.value = newMode;
  if (newMode === 0) {
    nextTick(() => {
      mergedData.value.forEach((answernaire, index) => {
        if (answernaire.type === 'single_choice' || answernaire.type === 'multiple_choice') {
        initCharts(answernaire, index); // 重新初始化图表
        }
      });
    });
  }
};
const switchMode1 = async(newMode1,answernaire, index) => {
  console.log("newMode1",newMode1)
    mode1.value[index] = newMode1;
    
    initCharts(answernaire, index); // 重新初始化图表
  }
const initCharts = async(answernaire, index)  => {
      // 图表初始化逻辑
      await nextTick(); // 确保 DOM 更新完成
      const pieChartDom = document.getElementById(`main-${index}`);
      const barChartDom = document.getElementById(`main1-${index}`);
      
      if (mode1.value[index] === 0 && pieChartDom){
        
        const pieChart = echarts.init(pieChartDom);
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
      }
      else if(mode1.value[index] ===1  && barChartDom){
      const barChart = echarts.init(barChartDom);
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
      }

      
    };
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
      <li v-for="(answernaire, index) in mergedData" :key="index" class="infinite-list-item">
          <!-- 单选题，多选题 -->    
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">第{{index+1}}题: &nbsp;     {{ answernaire.description }}&nbsp;
          {{ answernaire.type==='single_choice'? '[单选题]':'[多选题]' }}</p>

          <el-table v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'"
           :data="answernaire.newArray1Data"
           :default-sort="{ prop: 'answerCount', order: 'descending' }" border stripe   class="tableHead" >
            <el-table-column prop="option"  label="选项" width="400"  />
            <el-table-column prop="answerCount" sortable  label="小计" width="150" class-name="column-border"/>
            <el-table-column prop="rate" label="比例" class-name="column-border"/>
          </el-table>
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">本题有效填写人数：{{answernaire.total}}</p>
          <div style="display: flex;justify-content: center; gap: 10px;">
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
          <el-table v-if="answernaire.type === 'fill_blank'" :data="answernaire.newArray1Data" border stripe   class="tableHead" >
            <el-table-column prop="answerCount"  label="填写内容" width="150" class-name="column-border"/>
            <el-table-column prop="rate" label="填写次数" class-name="column-border"/>
          </el-table>
          <p v-if="answernaire.type === 'fill_blank'">本题有效填写人数：{{answernaire.total}}</p>
          <!-- 文件题 -->
          <p v-if="answernaire.type === 'fill_blank'"></p>
      </li>
  </ul>
  
  <ul v-if="mode==1" class="infinite-list" style="overflow: auto"> 
  </ul>
  
</template>

<style scoped>

.infinite-list {
  border-radius: 10px;
  background-color: white;
  height: 80vh;
  padding: 20px;
  margin: 10px;
  list-style: none;
}
.infinite-list .infinite-list-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 800px;
  background: wheat; /* 背景颜色 */
  margin: 10px;
  /* color: white; 字体颜色 */
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
.tableHead .el-table__cell {
    border: 1px solid #dcdfe6; /* 边框颜色 */
}

.tableHead .el-table__header th {
    border: 1px solid #dcdfe6; /* 表头边框 */
}
</style>
