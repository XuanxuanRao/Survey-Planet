<script setup>
import { userGetQuestionnaireResult } from '@/api/questionnaire'
import { onMounted, ref, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import{Download,Check,Close} from '@element-plus/icons-vue'
const response = ref();
let intervalId = null;
const isShowDetail = ref(true)
const loadingInstance = ref(null); // 控制 loading 实例
const total=ref();
const route = useRoute()
const tableData = ref([{  }]); // 声明为只有一个元素的数组
const rid = route.query.rid
const fileContent = ref(''); // 用于存储文件内容
const fileContent1 = ref(''); // 用于存储文件内容
const fileContent2 = ref(''); // 用于存储文件内容
const fetchFileContent = async (url) => {
      try {
        const response = await fetch(url);
        console.log("response",response);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        fileContent.value = await response.text(); // 获取文件内容并存储
        console.log("fileContent.value",fileContent.value)
      } catch (error) {
        console.error('Error fetching file:', error);
      }
};

const fetchFileContent1 = async (url) => {
      try {
        const response = await fetch(url);
        console.log("response",response);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        fileContent1.value = await response.text(); // 获取文件内容并存储
        console.log("fileContent1.value",fileContent1.value)
      } catch (error) {
        console.error('Error fetching file:', error);
      }
};

const fetchFileContent2 = async (url) => {
      try {
        const response = await fetch(url);
        console.log("response",response);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        fileContent2.value = await response.text(); // 获取文件内容并存储
        console.log("fileContent2.value",fileContent2.value)
      } catch (error) {
        console.error('Error fetching file:', error);
      }
};
const fetchData = async () => {
  try {
    const res = await userGetQuestionnaireResult(rid)
    if (res.msg !== "SUBMIT_IS_BEEN_PROCESSED") {
      clearInterval(intervalId); // 停止请求
      response.value = res.data;
      // 计算总得分
      total.value = res.data.items.reduce((accumulator, item) => {
          return accumulator + (item.question.score || 0); // 确保 score 存在，避免 NaN
      }, 0);
      
      console.log("response.value",response.value);
      // 将judge转化为数组a
      res.data.items.forEach(item => {
          if (item.judge !== null) {
              item.a = [ { ...item.judge } ]; // 新增属性 a，包含 judge 对象的所有属性
              console.log("item.a",item.a);
              // 特殊处理 item.status
              switch (item.judge.status) {
    //             case -3: return "输出格式错误";
    // case -2: return "编译错误";
    // case -1: return "答案错误";
    // case 0: return "通过";
    // case 1: return "CPU时间超出限制";
    // case 2: return "真实时间超出限制";
    // case 3: return "空间超出限制";
    // case 4: return "运行错误";
    // case 5: return "系统错误";
    // case 10: return "提交失败";
    // default: return "未知状态";
                  case 10: 
                      item.a[0].status ="提交失败";
                      break;
                  case 5: 
                      item.a[0].status = "系统错误";
                      break;
                  case 4:
                     item.a[0].status = "运行错误";
                      break;
                  case 3:
                      item.a[0].status = "空间超出限制";
                      break;
                  case 2: 
                      item.a[0].status ="真实时间超出限制";
                      break;
                  case 1: 
                      item.a[0].status = "CPU时间超出限制";
                      break;
                  case 0:
                     item.a[0].status = "通过";
                      break;
                  case -1:
                      item.a[0].status = "答案错误";
                      break;
                  case -2:
                      item.a[0].status = "编译错误";
                      break;
                  case -3: 
                      item.a[0].status = "输出格式错误";
                      break;
                  default:
                      item.a[0].status = "unknownError";
                      break;
              }
              console.log(item.judge.status)
              console.log("item.a",item.a);
          }
      });
      loadingInstance.value.close() // 数据加载完成后关闭 loading
    }
  } catch (error) {
    console.error('请求错误:', error)
    loadingInstance.value.close()
  }
};
const loadingContent= `
<svg width="50" height="50" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg">
  <!-- 外部圆 -->
  <circle cx="25" cy="25" r="50" stroke="#409EFF" stroke-width="4" fill="none" />
  <!-- 充满效果的圆 -->
  <circle cx="25" cy="25" r="0" fill="#409EFF">
    <!-- 动画:从半径0扩展到20 -->
    <animate attributeName="r" from="0" to="50" dur="2.0s" fill="freeze" />
  </circle>
</svg>
      `;
// 初始化加载状态和轮询
onMounted(() => {
  loadingInstance.value = ElLoading.service({
    lock: true,
    text: '加载中，请稍候...',
    svg: loadingContent, // 使用 SVG 动画
    background: 'rgba(0, 0, 0, 0.7)',
  });
  fetchData();
  intervalId = setInterval(fetchData, 2000);
})

// 组件卸载前清除轮询，这里是必要的，确保fetchData的clearInterval(intervalId);loadingInstance.value.close()必定完成
onBeforeUnmount(() => {
  clearInterval(intervalId);
  if (loadingInstance.value) {
    loadingInstance.value.close();
  }
})

const changeView = () => {
  isShowDetail.value = !isShowDetail.value;
}

const getStatusMeaning = (status) => {
  switch (status) {
    case -3: return "输出格式错误";
    case -2: return "编译错误";
    case -1: return "答案错误";
    case 0: return "通过";
    case 1: return "CPU时间超出限制";
    case 2: return "真实时间超出限制";
    case 3: return "空间超出限制";
    case 4: return "运行错误";
    case 5: return "系统错误";
    case 10: return "提交失败";
    default: return "未知状态";
  }
}

// 用于切换每个测试点的 URL 显示状态
const visibleUrls = ref({})

const toggleUrls = async(index,test1) => {
  visibleUrls.value[index] = !visibleUrls.value[index];
  fetchFileContent(test1.inputDataUrl);
  fetchFileContent1(test1.outputDataUrl);
  // fetchFileContent2(test1.userOutput);

}

</script>

<template>
  <div v-if="response">
    <h1>结果信息</h1>
    <div class="score-card">
      <div class="score">{{ response.grade }}</div>
      <div class="total">总分{{ total }}
    </div>
  </div>
    <!-- <h3 @click="changeView">点击查看详情</h3>  -->
    
    <div  v-if="isShowDetail">
      <div class="lookUnder" v-for="(item, index) in response.items" :key="index">
        <template class="single_choice" v-if="item.question.type === 'single_choice'">
          <!-- <div> -->
            <h2>
            Q{{ index + 1 }} {{ item.question.title }} (单选题) <br>
            问题描述：{{ item.question.description }} <br>
            </h2>
          <!-- </div> -->
          <br>
          <!-- <div> -->
              <el-radio-group>
                <el-radio
                v-for="(option, optIndex) in item.question.options"
                :key="optIndex"
                :value="option"
                :class="{ 'correct-answer': item.content.includes(option),
                          'wrong-answer': !item.answer.includes(option) && item.content.includes(option) }"
              >
                {{ option }}
              </el-radio>
              </el-radio-group>
          <!-- </div> -->
          
          <br>
          <div v-if="item.grade !== null">
            本题得分：{{ item.grade }} 
          </div>
          <br>
          <div v-if="item.answer">
            正确答案：{{ item.answer.join(", ") }}
          </div>
        </template>

        <template v-if="item.question.type === 'multiple_choice'">
          <h4>
            Q{{ index + 1 }} {{ item.question.title }} (多选题) <br>
            问题描述：{{ item.question.description }} <br>
          </h4>
          <el-checkbox-group>
            <el-checkbox 
            v-for="(option, optIndex) in item.question.options" 
            :key="optIndex" 
            :value="option"
            :class="{ 'correct-answer': item.content.includes(option),
                      'wrong-answer': !item.answer.includes(option) && item.content.includes(option) }"
            >
            {{ option }}
           </el-checkbox>
          </el-checkbox-group>
          <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
          <div v-if="item.answer">
            正确答案：{{ item.answer.join(", ") }}
          </div>
        </template>

        <template v-if="item.question.type === 'fill_blank'">
          <h4>
            Q{{ index + 1 }} {{ item.question.title }} (填空题) <br>
            问题描述：{{ item.question.description }} <br>
            <div v-if="item.content">
              你的答案：{{ item.content.join(", ") }}
            </div>
            <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
            <div v-if="item.answer">
              正确答案：{{ item.answer.join(", ") }}
            </div>
          </h4>
        </template>

        <template v-if="item.question.type === 'file'">
          <h4>
            Q{{ index + 1 }} {{ item.question.title }} (文件题) <br>
            问题描述：{{ item.question.description }} <br>
            <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
          </h4>
        </template>

        <template v-if="item.question.type === 'code'">
          <h3>
            Q{{ index + 1 }} {{ item.question.title }} (代码题) <br>
            
          </h3>
          <h4>
            问题描述：{{ item.question.description }} <br>
            
          </h4>
          <div class="result-container" v-if="item.judge.caseJudgeResults.length > 0">
            <!-- 代码题总的评测结果 -->
              <h4>该题总评测结果</h4>
              <el-table :data="item.a" border style="width: 100%">
                <!-- <el-table-column prop="status" label="状态" width="180" /> -->
                <el-table-column  label="状态" width="180">
                    <span :class="{ 'green-text': item.a.status == '通过', 'red-text': item.a.status != '通过' }">
                      <i v-if="item.a[0].status == '通过'" ><el-icon><Check /></el-icon></i> <!-- 打勾图标 -->
                      <i v-else ><el-icon><Close /></el-icon></i> <!-- 错误图标 -->
                      {{ item.a[0].status }}
                    </span>
                </el-table-column>
                <el-table-column  label="分数" width="80">
                    <span :class="{ 'green-text': item.a.status == '通过', 'red-text': item.a.status != '通过' }">
                      {{ item.a[0].score }}
                    </span>
                </el-table-column>
                <!-- <el-table-column prop="score" label="分数" width="180" /> -->
                <el-table-column prop="language" label="语言" width="80"/>
                <el-table-column prop="createTime" label="提交时间" width="180" />
                <el-table-column   prop="errorMessage" label="错误信息" width="280"/>               
              </el-table>
            
            <h4 v-if="item.a[0].status!='编译错误'&&item.a[0].status!='系统错误'&&item.a[0].status!='提交失败'">各个测试点结果</h4>
            <div v-if="item.a[0].status!='编译错误'&&item.a[0].status!='系统错误'&&item.a[0].status!='提交失败'" v-for="(test, index) in item.judge.caseJudgeResults" :key="index" class="test-case">
              <div class="test-case-details">
                <button @click="toggleUrls(index,test)" class="toggle-arrow">
                  {{ visibleUrls[index] ? '▼' : '▶' }}
                </button>
                <div class="test-case-index">测试点 #{{ index + 1 }}</div>
                <div class="status" :class="{ 'passed': test.status === 0, 'failed': test.status !== 0 }">
                  {{ getStatusMeaning(test.status) }}
                </div>
                <div class="details">
                  <div style="margin-right: 20px;margin-left: 20px;">运行时间: {{ test.time }} ms</div>
                  <div>内存: {{ test.memory }} K</div>
                </div>
              </div>


              <!-- URL 列表，位于详细信息下方 -->
              <div v-if="visibleUrls[index]" class="url-list">
                <!-- <span>点击下载:</span> -->
                <!-- {{test.inputDataUrl}} -->
                
                <h4 style="color: black;" @click="fetchFileContent(test.inputDataUrl)">标准输入：
                  <!-- <i class="fas fa-download"></i> -->
                  <a style="margin-left: 500px;" :href="test.inputDataUrl" target="_blank"><el-icon><Download /></el-icon></a>
                  <br><el-input
                    type="textarea"
                    :rows="5"
                    v-model="fileContent"
                    placeholder="文件内容"
                    style="width: 600px;"
                  ></el-input></h4>
                
                <h4 style="color: black;" @click="fetchFileContent1(test.outputDataUrl)">标准输出：
                  <a style="margin-left: 500px;" :href="test.outputDataUrl" target="_blank"><el-icon><Download /></el-icon></a>
                  <br><el-input
                    type="textarea"
                    :rows="5"
                    v-model="fileContent1"
                    placeholder="文件内容"
                    style="width: 600px;"
                  ></el-input></h4>
                <h4 style="color: black;" >你的输出：
                  <a style="margin-left: 500px; " :href="test.userOutput" target="_blank"></a>
                  <br><el-input
                    type="textarea"
                    :rows="5"
                    v-model="test.userOutput"
                    placeholder="文件内容"
                    style="width: 600px;"
                  ></el-input></h4>
                <!-- <a :href="test.inputDataUrl" target="_blank">标准输入</a>
                <a :href="test.outputDataUrl" target="_blank">标准输出</a>
                <a :href="test.userOutput" target="_blank">输出</a> -->
              </div>
            </div>
          </div>
        </template> 
      </div>
    </div>
  </div>
  
  <div v-else>
  </div>
</template>

<style scoped>
.green-text {
    color: green;
}

.red-text {
    color: red;
}

.result-container { max-width: 800px;}
.test-case { border-bottom: 1px solid #ddd; padding: 10px 0; display: flex; flex-direction: column; }

/* 分离标题和状态 */
.test-case-header { display: flex; align-items: center; justify-content: space-between; }
.test-case-details { display: flex; align-items: center; gap: 15px; }
.test-case-index { font-weight: bold; }
.status { font-weight: bold; }
.status.passed { color: green; }
.status.failed { color: red; }

/* 详情和URL列表的布局 */
.details { display: flex; color: #555; margin-left: 200px; }

/* 小箭头按钮 */
.toggle-arrow { cursor: pointer; background: none; border: none; color: blue; font-size: 1em; }

/* URL 列表样式 */
.url-list { 
    margin-top: 10px; 
    margin-left: 20px; /* 增加左边距，使它在详情下方居中 */
    color: blue; 
    display: flex; 
    flex-direction: column; /* 设置为竖直方向 */
    gap: 15px; 
    align-items: flex-start; /* 可以根据需要调整对齐方式 */
}
.url-list span { font-weight: bold; color: #333; }
.url-list a { text-decoration: none; color: #1a73e8; }
.url-list a:hover { text-decoration: underline; }

 /* 父容器样式 */
 .score-card {
      display: flex;
      flex-direction: column; /* 垂直排列 */
      align-items: center; /* 居中对齐 */
      justify-content: center;
      width: 200px; /* 卡片宽度 */
      height: 200px; /* 卡片高度 */
      background-color: #f9fbfd; /* 卡片背景颜色 */
      border-radius: 12px; /* 圆角边框 */
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 卡片阴影 */
      font-family: 'Arial', sans-serif; /* 字体样式 */
      text-align: center;
      margin: 50px auto; /* 居中页面 */
    }

    /* 分数样式 */
    .score {
      font-size: 4rem; /* 大字号 */
      color: #ff5722; /* 明亮橙色 */
      font-weight: bold; /* 加粗字体 */
      text-shadow: 0px 2px 5px rgba(0, 0, 0, 0.2); /* 文本阴影 */
      position: relative; /* 为伪元素定位做准备 */
    }

    /* 分数下划线样式 */
    .score::after {
      content: ''; /* 创建伪元素 */
      position: absolute;
      bottom: -10px; /* 调整下划线位置 */
      left: 50%;
      transform: translateX(-50%) rotate(-10deg); /* 居中并倾斜 */
      width: 40%; /* 下划线长度 */
      height: 2px; /* 下划线高度 */
      background-color: #ff5722; /* 下划线颜色 */
    }

    /* 总分样式 */
    .total {
      font-size: 1rem; /* 小字号 */
      color: #b39c8e; /* 柔和棕色 */
      background-color: #fef5e6; /* 柔和背景色 */
      padding: 5px 10px; /* 内边距 */
      border-radius: 8px; /* 圆角背景 */
      margin-top: 15px; /* 与分数的间距 */
    }
    
.single_choice{
  display: flex;
  flex-direction: column; 
  align-items: flex-start; 
  justify-content: space-between; 

}
/* .lookUnder{
   align-items: center;
   justify-content: center; 
   display: flex;
} */

.download-icon {
  margin-left: 10px; /* 左边距 */
  color: #4CAF50; /* 图标颜色 */
  font-size: 20px; /* 图标大小 */
}

.correct-answer {
  color: green; /* 选项文字变为绿色 */
}

.correct-answer ::v-deep .el-radio__inner {
  border-color: green; /* 边框变绿 */
  background-color: green; /* 背景变浅绿色 */
}

.correct-answer ::v-deep .el-checkbox__inner {
  border-color: green; /* 边框变绿 */
  background-color: green; /* 背景变浅绿色 */
}


.wrong-answer {
  color: red;
}

.wrong-answer ::v-deep .el-radio__inner {
  border-color: red;
  background-color: red;
}

.wrong-answer ::v-deep .el-checkbox__inner {
  border-color: red; 
  background-color: red;
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
