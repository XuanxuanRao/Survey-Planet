<script setup>
import { ref, onMounted } from "vue";
import { getSurveyResponse, userGetQuestionnaire } from "@/api/questionnaire";
import { useRoute } from "vue-router";

const route = useRoute();
const sid = route.query.id

// 筛选条件管理
const filters = ref([
  { questionId: "", option: "" }, // 初始化默认一个空筛选条件
]);

const responseData = ref(null);
const queryParams = ref({
  sid: Number(sid),
  pageNum: 1,
  pageSize: 10,
  queryMap: {}, // 筛选条件映射
});

const questions = ref([]); // 存储题目列表
const isLoading = ref(false);

// 加载问卷题目
const userGetQuestions = async () => {
  try {
    const res = await userGetQuestionnaire(sid);
    if (res.msg === "success") {
      questions.value = res.data.questions;
    } else {
      console.error("获取问卷失败");
    }
  } catch (error) {
    console.error("获取问卷失败：", error);
  }
};

// 加载问卷回答数据
const fetchSurveyResponses = async () => {
  isLoading.value = true;
  try {
    const res = await getSurveyResponse(queryParams.value);
    if (res.msg === "success") {
      responseData.value = res.data;
    } else {
      console.error("获取问卷回答失败");
    }
  } catch (error) {
    console.error("获取问卷回答失败：", error);
  } finally {
    isLoading.value = false;
  }
};

// 更新筛选条件并重新查询
const updateQueryConditions = () => {
  // 清空 queryMap，重构筛选条件
  queryParams.value.queryMap = {};
  filters.value.forEach((filter) => {
    if (filter.questionId && filter.option) {
      queryParams.value.queryMap[filter.questionId] = filter.option;
    }
  });
  fetchSurveyResponses(); // 查询数据
};

// 新增筛选条件
const addFilter = () => {
  filters.value.push({ questionId: "", option: "" });
};

// 删除筛选条件
const removeFilter = (index) => {
  filters.value.splice(index, 1);
  updateQueryConditions();
};

// 初始化加载数据
onMounted(() => {
  userGetQuestions();
  fetchSurveyResponses();
});
</script>

<template>
  <div class="survey-analysis">
    <h1>问卷回答数据分析</h1>

    <!-- 筛选条件 -->
    <div class="filter-panel">
      <h3>筛选条件：</h3>
      <div
        class="filter-item"
        v-for="(filter, index) in filters"
        :key="index"
      >
        <!-- 问卷题目选择框 -->
        <select
          v-model="filter.questionId"
          @change="updateQueryConditions"
        >
          <option value="">问题题目</option>
          <option v-for="question in questions" :key="question.qid" :value="question.qid">
            {{ question.title }}
          </option>
        </select>

        <!-- 答案选项选择框 -->
        <select
          v-model="filter.option"
          @change="updateQueryConditions"
        >
          <option value="">选项</option>
          <option
            v-for="option in questions.find(q => q.qid === filter.questionId)?.options || []"
            :key="option"
            :value="option"
          >
            {{ option }}
          </option>
        </select>

        <!-- 删除按钮 -->
        <button @click="removeFilter(index)">删除</button>
      </div>

      <!-- 新增筛选条件按钮 -->
      <button @click="addFilter">添加筛选条件</button>
    </div>

    <!-- 数据表格 -->
    <div v-if="responseData">
      <p>共 {{ responseData.total }} 条记录</p>
      <table>
        <thead>
          <tr>
            <th>序号</th>
            <th>用户ID</th>
            <th>提交时间</th>
            <th>更新时间</th>
            <th>答卷总分</th>
            <th>是否完成</th>
            <th>详细回答</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(record, index) in responseData.records" :key="record.rid">
            <td>{{ index + 1 }}</td>
            <td>{{ record.uid }}</td>
            <td>{{ record.createTime }}</td>
            <td>{{ record.updateTime }}</td>
            <td>{{ record.grade ?? "N/A" }}</td>
            <td>{{ record.finished ? "是" : "否" }}</td>
            <td>
              <ul>
                <li v-for="item in record.items" :key="item.submitId">
                  题目ID: {{ item.qid }} - 答案: {{ item.content.join(", ") }}
                </li>
              </ul>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else-if="isLoading">数据加载中...</p>
    <p v-else>暂无数据</p>
  </div>
</template>

<style scoped>
.survey-analysis {
  font-family: Arial, sans-serif;
}

.filter-panel {
  margin-bottom: 20px;
}

.filter-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.filter-item select {
  margin-right: 10px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

table th,
table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
</style>
