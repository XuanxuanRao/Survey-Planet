<script  setup >
import * as echarts from 'echarts';
import { ref, onMounted,nextTick,reactive,onBeforeUnmount, } from 'vue'
import 'echarts-wordcloud'; // 引入 Word Cloud 插件
import {userGetAnswernaireResult,userGetQuestionnaire,userGetPageResult} from '@/api/questionnaire'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { PieChart, Promotion,InfoFilled } from '@element-plus/icons-vue'
const listItems = ref([]);
const router = useRouter()
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
const mode1 = ref(Array(10000).fill(99)); // 初始化为长度为 n 的数组，所有元素为 0
const transformedWordClouds = ref({}); // 创建一个对象来存储不同的 wordCloud
const surveyType=ref()
const codes = ref({});
const selectedSubmitIds = ref([]);
const title=ref()
const isActive = ref([]); // 用于存储每个按钮的状态
//初始化
for (let i = 0; i <= 1000; i++) {
  currentPages[i] = 1;
  isDialogVisible[i] = false;  
  pageSize[i] = 5; 
  pageView[i] = [];
}

// 在组件挂载时获取数据
onMounted(async () => {
    console.log("lookAt")
    //获取问卷数据,包括questions(questionnaires),type
    const res = await userGetQuestionnaire(sid);
    title.value=res.data.title
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
      //初始时不显示
    // if (answernaire.type === 'single_choice' || answernaire.type === 'multiple_choice'||answernaire.type === 'code'||(surveyType.value=='exam'&&answernaire.type === 'fill_blank')) {
    //   initCharts(answernaire, index); // 重新初始化图表
    // }
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
const adjustHeight = () => {
      const listItems = document.querySelectorAll('.infinite-list-item');
      listItems.forEach(item => {
        const contentHeight = item.scrollHeight; // 获取内容高度
        const newMinHeight = Math.max(contentHeight , 200);
        console.log("contentHeight", contentHeight);
        if (item.style.minHeight !== `${newMinHeight}px`) {
          item.style.minHeight = `${newMinHeight}px`; // 设置最小高度
        }
      });
    };
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
  
  if(newMode == 1){
    console.log("sid",sid)
    router.push({ path: '/releaseSurvey', query: { id: sid } });
  }
  if(newMode == 2) {
    router.push({ path: '/responseSurvey', query: { id: sid } });
  }
  
};
//模式转换1,对应查看表格
const switchMode1 = async(newMode1,answernaire, index) => {
  console.log("newMode1",newMode1)
  if(mode1.value[index]==2&&newMode1==2){
    mode1.value[index]=99;
  }else{
    mode1.value[index] = newMode1;
  }
    
    if(newMode1==1||newMode1==0)initCharts(answernaire, index); // 重新初始化图表
    else if(newMode1==2)initChart1(transformedWordClouds[answernaire.qid], index); 
}
//模式转换1,对应查看表格
const switchMode1AndtoggleActive = async(newMode1,answernaire, index,index1) => {
  console.log("newMode1",newMode1)
  isActive.value[index1] = !isActive.value[index1]; // 切换对应按钮的状态
  //选择是否取消3 4对应按钮的高亮,保证同一时间只有至多一个高亮
  if(index1==3){
    if(isActive.value[4]==1)isActive.value[4]=0;
  }else if(index1==4){
    if(isActive.value[3]==1)isActive.value[3]=0;
  }
  //1 2
  if(index1==2){
    if(isActive.value[1]==1)isActive.value[1]=0;
  }else if(index1==1){
    if(isActive.value[2]==1)isActive.value[2]=0;
  }
  //5 6 7
  if(index1==5){
    if(isActive.value[6]==1)isActive.value[6]=0;
    if(isActive.value[7]==1)isActive.value[7]=0;
  }else if(index1==6){
    if(isActive.value[5]==1)isActive.value[5]=0;
    if(isActive.value[7]==1)isActive.value[7]=0;
  }else if(index1==7){
    if(isActive.value[5]==1)isActive.value[5]=0;
    if(isActive.value[6]==1)isActive.value[6]=0;
  }
  //9 10
  if(index1==9){
    if(isActive.value[10]==1)isActive.value[10]=0;
  }else if(index1==10){
    if(isActive.value[9]==1)isActive.value[9]=0;
  }
  if(mode1.value[index]==2&&newMode1==2){
    mode1.value[index]=99;
  }else if(mode1.value[index]==1&&newMode1==1){
    mode1.value[index]=99;
  }else if(mode1.value[index]==0&&newMode1==0){
    mode1.value[index]=99;
  }else{
    mode1.value[index] = newMode1;
  }
    if(newMode1==1||newMode1==0)initCharts(answernaire, index); // 重新初始化图表
    else if(newMode1==2)initChart1(transformedWordClouds[answernaire.qid], index); 
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
            data: answernaire.newArray1Data
                .filter(item => item.answerCount > 0) // 过滤掉 answerCount 为 0 的选项
                .map(item => ({
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
        //exam 代码题,填空题的饼图
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
            data: answernaire.codes
            .filter(item => item.count > 0)
            .map(item => ({
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
      console.log("answernaire.newArray1Data",answernaire)
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
          data: answernaire.codes.map(item => item.grade),
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
    }
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
    <button class="navbar-item" style="background: none; border: none; padding: 0; cursor: pointer;" @click="switchMode(0)">
        <el-icon :size="40"><PieChart /></el-icon>
        <span>统计分析</span>
    </button>
    <button class="navbar-item" style="background: none; border: none; padding: 0; cursor: pointer;" @click="switchMode(1)">
        <el-icon :size="40"><Promotion /></el-icon>
        <span>发布问卷</span>
    </button>
    <button class="navbar-item" style="background: none; border: none; padding: 0; cursor: pointer;" @click="switchMode(2)">
        <el-icon :size="40"><InfoFilled /></el-icon>
        <span>查看问卷</span>
    </button>
  </div>
  <div style="display: flex; justify-content: center; align-content: center; text-align: center; ">
  <ul v-if="mode==0" class="infinite-list" style="overflow: auto"> 
    <h1 style="display: flex;justify-content: center; ">{{ title }}</h1>
      <!--normal  -->
      <li v-if="surveyType=='normal'" v-for="(answernaire, index) in mergedData" :key="index" class="infinite-list-item" ref="listItems">
          <!-- 单选题，多选题 -->    
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">第{{index+1}}题: &nbsp;{{ answernaire.title }}&nbsp;
          {{ answernaire.type==='single_choice'? '[单选题]':'[多选题]' }}</p>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">
            <el-table v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice' "
              :data="answernaire.newArray1Data"
              :default-sort="{ prop: '', order: '' }" border stripe   class="tableHead" >
                <el-table-column prop="option"  label="选项"   />
                <el-table-column prop="answerCount" sortable  label="小计" />
                <el-table-column prop="rate" label="比例" />
            </el-table>
          </div>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" >
              表格
            </el-button>
            
            <el-button 
              :class="{'custom-button': isActive[3], 'custom-buttonDark': !isActive[3]}" 
              @click="switchMode1AndtoggleActive(0,answernaire, index,3)">
              饼状图
            </el-button>
            <el-button 
              :class="{'custom-button': isActive[4], 'custom-buttonDark': !isActive[4]}" 
              @click="switchMode1AndtoggleActive(1,answernaire, index,4)">
              柱状图
            </el-button>
            <!-- <el-button class="custom-button" @click="switchMode1(3,answernaire, index)">
            表格</el-button>
            <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button> -->
          </div>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
          </div>
          <!-- 填空题 -->
          <p v-if="answernaire.type === 'fill_blank'">
            第{{index+1}}题: &nbsp;     {{ answernaire.title }}&nbsp;[填空题]
          </p>

          <div v-if="answernaire.type === 'fill_blank'" class="demo-pagination-block">            
            <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">显示表格</el-button>
            <el-button class="custom-button" @click="switchMode1(2,answernaire, index)">显示词云</el-button>
            <!-- <el-button class="custom-button" @click="switchMode1(4,answernaire, index)">关闭词云</el-button> -->
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
          <div v-if="answernaire.type === 'fill_blank'" >
            <span v-if="mode1[index] === 2"  style="display: flex; justify-content: center; align-items: center; width: 1000px; height: 400px;">
                <!-- 词云 -->
                <div class="borederCiYun" v-if="answernaire.type === 'fill_blank'"
                    :id=" 'mywordcloud-' + index"
                    :style="{ width: '100%', height: '300px' }"
                    :data="answernaire.wordCloud"
                ></div>
            </span>
          </div>
          
          <!-- <p v-if="answernaire.type === 'fill_blank'">本题有效填写人数：{{answernaire.total}}</p> -->
          <!-- 文件题 -->
          <p v-if="answernaire.type === 'file'">
            第{{index+1}}题: &nbsp;     {{ answernaire.title }}&nbsp;[文件题]
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
      <!-- exam -->
      <li v-if="surveyType=='exam'" v-for="(answernaire, index) in mergedData" :key="index" class="infinite-list-item" ref="listItems">
          <!-- 单选题，多选题 -->    
          <p v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'">第{{index+1}}题: &nbsp;     {{ answernaire.title }}&nbsp;
          {{ answernaire.type==='single_choice'? '[单选题]':'[多选题]' }}</p>
          <div>
            <el-table v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice' "
              :data="answernaire.newArray1Data"
              :default-sort="{ prop: '', order: '' }" border stripe   class="tableHead" >
                <el-table-column prop="option"  label="选项"   />
                <el-table-column prop="answerCount" sortable  label="小计" />
                <el-table-column prop="rate"  label="比例" />
              </el-table>
          </div>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" >
              表格
            </el-button>
            <el-button 
              :class="{'custom-button': isActive[1], 'custom-buttonDark': !isActive[1]}" 
              @click="switchMode1AndtoggleActive(0,answernaire, index,1)">
              饼状图
            </el-button>
            <el-button 
              :class="{'custom-button': isActive[2], 'custom-buttonDark': !isActive[2]}" 
              @click="switchMode1AndtoggleActive(1,answernaire, index,2)">
              柱状图
            </el-button>
            <!-- <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button> -->
          </div>
          <div v-if="answernaire.type === 'single_choice'||answernaire.type === 'multiple_choice'" style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>           
          </div>
          <!-- 填空题 -->
          <p v-if="answernaire.type === 'fill_blank'">
            第{{index+1}}题: &nbsp;     {{ answernaire.title }}&nbsp;[填空题]
          </p>
          <div v-if="answernaire.type === 'fill_blank'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">显示表格</el-button>
            <el-button 
              :class="{'custom-button': isActive[5], 'custom-buttonDark': !isActive[5]}" 
              @click="switchMode1AndtoggleActive(0,answernaire, index,5)">
              饼状图
            </el-button>
            <el-button 
              :class="{'custom-button': isActive[6], 'custom-buttonDark': !isActive[6]}" 
              @click="switchMode1AndtoggleActive(1,answernaire, index,6)">
              柱状图
            </el-button>
            <el-button 
              :class="{'custom-button': isActive[7], 'custom-buttonDark': !isActive[7]}" 
              @click="switchMode1AndtoggleActive(2,answernaire, index,7)">
              显示词云
            </el-button> 
          </div>
          <div v-if="answernaire.type === 'fill_blank'"  style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 2"  style="display: flex; justify-content: center; align-items: center; width: 1000px; height: 400px;">
                <!-- 词云 -->
                <div class="borederCiYun"
                  :id=" 'mywordcloud-' + index"
                  :style="{ width: '50%', height: '300px' }"
                  :data="answernaire.wordCloud"
                ></div>
            </span>
          </div>

          <div v-if="answernaire.type === 'fill_blank'" class="demo-pagination-block">                        
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
          <!-- 代码题 -->
          <p v-if="answernaire.type === 'code'">
            第{{index+1}}题: &nbsp;     {{ answernaire.title }}&nbsp;[代码题]
          </p>         
          <div v-if="answernaire.type === 'code'" style="display: flex;justify-content: center; gap: 10px;">
            <el-button class="custom-button" @click="isDialogVisible[answernaire.qid]=true">详细信息</el-button>
            <el-button 
              :class="{'custom-button': isActive[9], 'custom-buttonDark': !isActive[9]}" 
              @click="switchMode1AndtoggleActive(0,answernaire, index,9)">
              饼状图
            </el-button>
            <el-button 
              :class="{'custom-button': isActive[10], 'custom-buttonDark': !isActive[10]}" 
              @click="switchMode1AndtoggleActive(1,answernaire, index,10)">
              柱状图
            </el-button>
            <!-- <el-button class="custom-button" @click="switchMode1(0,answernaire, index)">
              饼状图
            </el-button>
            <el-button class="custom-button" @click="switchMode1(1,answernaire, index)">
              柱状图
            </el-button> -->
          </div>
          <div v-if="answernaire.type === 'code'"  style="display: flex; justify-content: space-between;">
            <span v-if="mode1[index] === 0"  :id="'main-' + index" style="width: 400px; height: 400px;"></span>
            <span v-if="mode1[index] === 1"  :id="'main1-' + index" style="width: 400px; height: 400px;"></span>
          </div>
          <!-- 表格 -->
          <div v-if="answernaire.type === 'code'" class="demo-pagination-block"> 
            <el-dialog width="800px"  v-model="isDialogVisible[answernaire.qid]" title="表格数据" >
                <el-table  :data="pageView[answernaire.qid].value" border stripe class="tableHead">
                    <el-table-column prop="index" label="序号" width="80" />
                    <el-table-column prop="language" label="编程语言" width="100" />
                    <el-table-column label="文件url">
                      <template #default="scope">
                        <el-tooltip content="点击此处下载源代码" placement="top">
                          <el-link :href="scope.row.content[0]" target="_blank">{{scope.row.content[0]}}</el-link>
                        </el-tooltip>
                        
                      </template>
                    </el-table-column>  
                    <!-- <el-table-column prop="url"  label="文件url" /> -->
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
  </div>
</template>

<style scoped>
/* 顶部导航栏 */
.navbar {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中 */
  justify-content: space-around; /* 等间距分布 */
  background-color: #f8f8f8; /* 背景色 */
  border-bottom: 1px solid #ddd; /* 下边框 */
  padding: 10px 0; /* 内边距 */
  position: fixed; /* 固定在顶部 */
  top: 0;
  left: 0;
  width: 100%; /* 占满宽度 */
  z-index: 1000; /* 层级最高 */
}

/* 每个按钮的样式 */
.navbar-item {
    display: flex; /* 图标和文字水平排列 */
    flex-direction: column; /* 图标在上，文字在下 */
    align-items: center; /* 居中对齐 */
    text-decoration: none; /* 去掉下划线 */
    color: #333; /* 文本颜色 */
    font-size: 25px; /* 字体大小 */
    padding: 5px 8px; /* 内边距 */
    width: 170px; /* 每个按钮的固定宽度 */
    transition: background-color 0.3s ease, color 0.3s ease; /* 添加过渡效果 */
}

.navbar-item:hover {
  color: #007BFF; /* 悬停时文字变蓝 */
}

.navbar-item:hover, .navbar-item:focus {
    background-color: rgba(5, 5, 6, 0.2); /* 背景色变淡蓝 */
    color: #007BFF; /* 文字颜色变蓝 */
    border-radius: 5px; /* 圆角效果 */
}

.infinite-list {  
  border-radius: 10px;
  background-color: white;
  align-items: center;
  justify-content: center;
  height: 80vh;
  padding: 20px;
  margin: 10px;
  list-style: none;
  width: 1000px;
}
.infinite-list .infinite-list-item {
  height: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 10px;
  background-color:  lightcyan;
  border: 2px solid lightblue; /* 添加黑色边框，宽度为2px */
  border-radius: 4px; /* 可选：设置圆角边框 */
  /* min-height: 290px;  */
}
.borederCiYun{
  border: 1px solid white; 
}
.tableHead{
  width: 800px;
  border: 1px solid black; 
  margin-bottom: 30px;
}
.custom-buttonDark {
  border-color: #d3d3d3; /* 灰暗边框颜色 */
  position: relative;
  width: 80px;
  height: 35px;
  text-align: center;
  line-height: 60px;
  color: #666; /* 灰暗字体颜色 */
  font-size: 18px;
  text-decoration: none;
  font-family: sans-serif;
  border-radius: 30px;
  background-color: #d3d3d3; /* 灰暗颜色 */
  background-size: 400%;
  transition: background-position 0.5s;
  margin-bottom: 10px;
}
.custom-button {
  position: relative;
  width: 80px;
  height: 35px;
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
  margin-bottom: 10px;
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
