<script setup>
import { userGetQuestionnaireResult } from '@/api/questionnaire'
import { onMounted, ref, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'

const response = ref();
let intervalId = null;
const isShowDetail = ref(false)
const loadingInstance = ref(null); // 控制 loading 实例

const route = useRoute()
const rid = route.query.rid

const fetchData = async () => {
  try {
    const res = await userGetQuestionnaireResult(rid)

    if (res.msg !== "SUBMIT_IS_BEEN_PROCESSED") {
      clearInterval(intervalId); // 停止请求
      response.value = res.data;
      console.log(res.data);
      loadingInstance.value.close() // 数据加载完成后关闭 loading
    }
  } catch (error) {
    console.error('请求错误:', error)
    loadingInstance.value.close()
  }
};

// 初始化加载状态和轮询
onMounted(() => {
  loadingInstance.value = ElLoading.service({
    lock: true,
    text: '加载中，请稍候...',
    background: 'rgba(0, 0, 0, 0.7)',
  });
  fetchData();
  intervalId = setInterval(fetchData, 2000);
})

// 组件卸载前清除轮询
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

const toggleUrls = (index) => {
  visibleUrls.value[index] = !visibleUrls.value[index];
}

</script>

<template>
  <div v-if="response">
    <h2>结果信息</h2>
    <p>你的总分: {{ response.grade }}</p>

    <h3 @click="changeView">点击查看详情</h3> <!-- 修复了额外的引号 -->
    
    <div class="detail" v-if="isShowDetail">
      <div v-for="(item, index) in response.items" :key="index">
        <template v-if="item.question.type === 'single_choice'">
          <h4>
            Q{{ index + 1 }} {{ item.question.title }} (单选题) <br>
            问题描述：{{ item.question.description }} <br>
          </h4>
          <el-radio-group>
            <el-radio
            v-for="(option, optIndex) in item.question.options"
            :key="optIndex"
            :value="option"
            :class="{ 'correct-answer': item.answer.includes(option) }"
          >
            {{ option }}
          </el-radio>
          </el-radio-group>
          <br>
          <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
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
            :class="{ 'correct-answer': item.answer.includes(option) }"
            >
            {{ option }}
           </el-checkbox>
          </el-checkbox-group>
          <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
        </template>

        <template v-if="item.question.type === 'fill_blank'">
          <h4>
            Q{{ index + 1 }} {{ item.question.title }} (填空题) <br>
            问题描述：{{ item.question.description }} <br>
            <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
          </h4>

        </template>

        <template v-if="item.question.type === 'code'">
          <h4>
            Q{{ index + 1 }} {{ item.question.title }} (代码题) <br>
            问题描述：{{ item.question.description }} <br>
            <div v-if="item.grade !== null">本题得分：{{ item.grade }} </div>
          </h4>

          <div class="result-container" v-if="item.judge.caseJudgeResults.length > 0">
            <h2>测试点结果</h2>
            <div v-for="(test, index) in item.judge.caseJudgeResults" :key="index" class="test-case">
              <div class="test-case-details">
                <button @click="toggleUrls(index)" class="toggle-arrow">
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
                <span>点击下载:</span>
                <a :href="test.inputDataUrl" target="_blank">标准输入</a>
                <a :href="test.outputDataUrl" target="_blank">标准输出</a>
                <a :href="test.userOutput" target="_blank">输出</a>
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
.details { display: flex; color: #555; margin-left: 200px;}

/* 小箭头按钮 */
.toggle-arrow { cursor: pointer; background: none; border: none; color: blue; font-size: 1em; }

/* URL 列表样式 */
.url-list { 
    margin-top: 10px; 
    margin-left: 20px; /* 增加左边距，使它在详情下方居中 */
    color: blue; 
    display: flex; 
    gap: 15px; 
    align-items: center;
}
.url-list span { font-weight: bold; color: #333; }
.url-list a { text-decoration: none; color: #1a73e8; }
.url-list a:hover { text-decoration: underline; }

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
</style>
