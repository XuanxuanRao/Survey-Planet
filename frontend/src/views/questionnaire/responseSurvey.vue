<script setup>
import { ref, onMounted } from "vue";
import { getSurveyResponse, userGetQuestionnaire, userModifyAnswer } from "@/api/questionnaire";
import { useRoute } from "vue-router";
import router from "@/router";
import { PieChart, Promotion,InfoFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
const route = useRoute();
const sid = route.query.id;
const total = ref(0); // 总记录数

const filters = ref([{ questionId: "", type: "", value: "" }]); // 筛选条件数组
const questions = ref([]); // 问卷题目
const isLoading = ref(false); // 数据加载状态
const responseData = ref(null); // 问卷回答数据
const isShow = ref(false)
const mode = ref(0);
const queryParams = ref({
  sid: Number(sid),
  pageNum: 1,
  pageSize: 10,
  queryMap: {},
  valid: true, // 默认查询有效答卷
  gradeLb: null, // 分数下限
  gradeUb: null, // 分数上限
});
const switchMode = (newMode) => {
  console.log("newMode",newMode)
  if(newMode == 0) {
    router.push({ path: '/questionnaire/lookQuestionnaire', query: { id: sid } });
  }
  if(newMode==1){
    router.push({ path: '/releaseSurvey', query: { id: sid } });
  }
};

const type = ref(null);
// 加载问卷题目
const userGetQuestions = async () => {
  try {
    const res = await userGetQuestionnaire(sid);
    if (res.msg === "success") {
      questions.value = res.data.questions;
      type.value = res.data.type;

    } else {
      console.error("获取问卷失败");
    }
  } catch (error) {
    console.error("获取问卷失败：", error);
  }
};

// 处理题目变化
const handleQuestionChange = (filter) => {
  const selectedQuestion = questions.value.find((q) => q.qid === filter.questionId);
  filter.type = selectedQuestion ? selectedQuestion.type : "";
  filter.value = ""; // 清空之前填写的筛选值
};

// 执行查询
const handleSearch = async () => {
  queryParams.value.queryMap = filters.value.reduce((map, filter) => {
    if (filter.questionId && filter.value) {
      map[filter.questionId] = filter.value;
    }
    return map;
  }, {});

  isLoading.value = true;
  try {
    const res = await getSurveyResponse(queryParams.value);
    if (res.msg === "success") {
      responseData.value = res.data;
      total.value = res.data.total;
    } else {
      console.error("获取问卷回答失败");
    }
  } catch (error) {
    console.error("查询数据失败：", error);
  } finally {
    isLoading.value = false;
  }
};

// 处理分页变化
const handleSizeChange = async (size) => {
  queryParams.value.pageSize = size;
  await handleSearch();
};

const handleCurrentChange = async (page) => {
  queryParams.value.pageNum = page;
  await handleSearch();
};

// 初始化加载数据
onMounted(async () => {
  await userGetQuestions(); // 加载问卷题目
  await handleSearch(); // 加载所有数据
});

// Dialog相关逻辑
const isDialogVisible = ref(false); // 控制Dialog显示/隐藏
const detailUrl = ref(""); // 动态详情页URL

const openDetailDialog = (rid) => {
  console.log('Opening Dialog with RID:', rid)
  //router.push({ path: '/viewResult', query: { rid } })
  detailUrl.value = `/viewResult?rid=${rid}`
  console.log('Detail URL:', detailUrl.value)
  isDialogVisible.value = true
}

const deleteAnswer = async(sid) => {
    const res = await userModifyAnswer(sid, false)
    if (res.msg === 'success') {
      console.log('删除答卷成功')
      handleSearch()
    } else {
      console.error('删除答卷失败')
    }
}

const recoverAnswer = async(sid) => {
    const res = await userModifyAnswer(sid, true)
    if (res.msg === 'success') {
      console.log('删除答卷成功')
      handleSearch()
    } else {
      console.error('删除答卷失败')
    }
}


const setValidFilter = (valid) => {
  queryParams.value.valid = valid
  handleSearch()
};
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
  <div>
    <h1>问卷回答筛选</h1>

    <!-- 筛选有效与无效问卷按钮 -->
    <div class="valid-filter">
      <el-button @click="setValidFilter(true)">有效问卷</el-button>
      <el-button @click="setValidFilter(false)">无效问卷</el-button>
    </div>

    <div class="filter-container">
      <div class="filter-header" @click="isShow = !isShow">
        <span class="filter-title">筛选条件</span>
      </div>

      <div v-if="isShow" class="filter-body">
        <!-- 动态筛选条件 -->
        <div v-for="(filter, index) in filters" :key="index" class="filter-group">
          <el-select
            v-model="filter.questionId"
            placeholder="请选择题目"
            @change="handleQuestionChange(filter)"
            class="filter-select"
          >
            <el-option
              v-for="question in questions"
              :key="question.qid"
              :label="question.title"
              :value="question.qid"
            />
          </el-select>

          <el-input
            v-if="filter.type === 'fill_blank'"
            v-model="filter.value"
            placeholder="请输入答案内容"
            class="filter-input"
          />

          <el-select
            v-else-if="filter.type === 'multiple_choice' || filter.type === 'single_choice'"
            v-model="filter.value"
            placeholder="请选择选项"
            class="filter-select"
          >
            <el-option
              v-for="option in questions.find((q) => q.qid === filter.questionId)?.options || []"
              :key="option"
              :label="option"
              :value="option"
            />
          </el-select>

          <el-button type="danger" @click="filters.splice(index, 1)">删除</el-button>
        </div>

        <el-button type="primary" @click="filters.push({ questionId: '', type: '', value: '' })">添加筛选条件</el-button>

        <!-- 分数筛选 -->
        <div class="score-filter">
          <el-input-number
            v-model.number="queryParams.gradeLb"
            label="分数下限"
            placeholder="最低分"
            class="score-input"
          />
          <el-input-number
            v-model.number="queryParams.gradeUb"
            label="分数上限"
            placeholder="最高分"
            class="score-input"
          />
        </div>

        <!-- 查询按钮 -->
        <el-button type="success" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <p v-if="isLoading">数据加载中...</p>

    <!-- 数据展示 -->
    <div v-else-if="responseData && responseData.total > 0">
      <p>共 {{ responseData.total }} 条记录</p>
      <div class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th style="display: flex; align-items: center; justify-content: center;">操作</th>
            <th>序号</th>
            <th>用户ID</th>
            <th>提交时间</th>
            <th v-if="type !== 'normal'">答卷总分</th>

            <th v-for="(question, index) in questions" :key="question.qid">{{ question.title }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(record, index) in responseData.records" :key="record.rid">
            <td>
            <el-button @click="deleteAnswer(record.rid)" v-if="queryParams.valid === true">
              删除
            </el-button>
            <el-button @click="recoverAnswer(record.rid)" v-if="queryParams.valid === false">
              恢复
            </el-button>
            <el-button @click="openDetailDialog(record.rid)">
              查看
            </el-button>
            </td>
            <td>{{ (queryParams.pageNum - 1) * queryParams.pageSize + index + 1 }}</td>
            <td>{{ record.uid }}</td>
            <td>{{ record.createTime }}</td>

            <td v-if="type !== 'normal'">{{ record.grade ?? "N/A" }}</td>

            <td v-for="(item, idx) in record.items" :key="item.qid">
              <!-- <div v-for="(answer, index) in item.content" :key="index">
                {{ answer }}
                <span v-if="index < item.content.length - 1">, </span>
              </div> -->
              {{item.content.join(", ")}}
            </td>
          </tr>
        </tbody>
      </table>
      </div>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <p v-else-if="responseData && responseData.total === 0">暂无数据</p>

    <!-- 弹窗展示详情 -->
    <el-dialog
      title="答卷详情"
      v-model="isDialogVisible"
      width="80%"
    >
      <iframe
        v-if="detailUrl"
        :src="detailUrl"
        width="100%"
        :style="{ height: '70vh', padding: 10 }"
        style="border: none;"
      ></iframe>
    </el-dialog>

  </div>
</template>


<style scoped>

.table-container {
  max-width: 87vw; /* 最大宽度占满父容器 */
  overflow-x: auto; /* 启用水平滚动 */
  border: 1px solid #ddd; /* 边框样式 */
  border-radius: 8px; /* 圆角 */
  padding: 5px; /* 内边距 */
  background-color: #fff; /* 背景色 */
}

.table {
  width: 100%; /* 表格宽度 */
  min-width: 1200px; /* 设置表格的最小宽度，触发水平滚动 */
  border-collapse: collapse; /* 合并边框 */
}

th, td {
  text-align: left;
  padding: 8px;
  border: 1px solid #ddd; /* 单元格边框 */
  white-space: nowrap; /* 防止内容换行 */
}

th {
  background-color: #f4f4f4; /* 表头背景色 */
  font-weight: bold;
}


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
    background-color: rgba(0, 123, 255, 0.2); /* 背景色变淡蓝 */
    color: #007BFF; /* 文字颜色变蓝 */
    border-radius: 5px; /* 圆角效果 */
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
.filter-group {
  margin-bottom: 10px;
}

.filter-group select,
.filter-group input {
  margin-right: 10px;
}

.score-filter {
  margin: 20px 0;
}

.filter-container {
  margin: 20px 0;
  padding: 15px;
  width: 85vw;
  border: 1px solid #ebebeb;
  border-radius: 8px;
  background-color: #f9f9f9;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.filter-title {
  margin-left: 8px;
}

.filter-toggle {
  margin-left: auto;
  color: #666;
}

.filter-body {
  margin-top: 15px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.filter-select {
  width: 200px;
}

.filter-input {
  flex: 1;
}

.score-filter {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 20px 0;
}

.score-input {
  width: 150px;
}

</style>
