import request from "@/utils/request"
import { useUserStore } from "@/stores/user"
import FileSaver from 'file-saver'
import axios from 'axios'

export const getCreatedQuestionnaireList = () => {
    console.log(useUserStore().token)
    return request.get('/api/survey/list?type=created', {
        headers: {
            'token': useUserStore().token
        }
    })
}
export const getCreatedQuestionnaireList1 = (pageNum) => {
    console.log(useUserStore().token)
    return request.get(`/api/survey/list?type=created&pageNum=${pageNum}`, {
        headers: {
            'token': useUserStore().token
        }
    })
}
export const getFilledQuestionnaireList = () => {
    return request.get('/api/questionnaire/fill/list')
}

export const userGetFilledQuestionnaireList = () => {
    return request.get('/api/survey/list?type=filled', {
        headers: {
            'token': useUserStore().token
        }
    })
}

export const userSendQuestionnaireList = (type, title, description, questions) => {
    const data = {
        type: type,
        title: title,
        description: description,
        questions: questions
    };
    const jsonData = JSON.stringify(data)
    console.log(useUserStore().token)
    console.log(jsonData)
    return request.post('/api/survey/add',jsonData, {
        headers: {
            'token': useUserStore().token,
            'Content-Type': 'application/json'
        }
    })
}

export const userModifyQuestionnaireList = (sid, type, title, description, questions) => {
    console.log(sid)
    console.log(type)
    console.log(title)
    console.log(description)
    console.log(questions)
    const data = {
        sid: sid,
        type: type,
        title: title,
        description: description,
        questions: questions
    };
    const jsonData = JSON.stringify(data)
    console.log(useUserStore().token)
    console.log(jsonData)
    return request.put(`/api/survey/${sid}`, jsonData, {
        headers: {
            'token': useUserStore().token,
            'Content-Type': 'application/json'
        }
    })
}

export const userDeleteQuestionnaire = (sid) => {
    return request.delete(`/api/survey/${sid}`,{
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userGetQuestionnaire = (sid) => {
    return request.get(`/api/survey/${sid}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userShareQuestionnaire = (sid) => {
    const data = {};
    const jsonData = JSON.stringify(data)
    return request.post(`/api/survey/${sid}/share`, jsonData, {
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userCloseQuestionnaire = (sid) => {
    return request.put(`/api/survey/${sid}/close`, null, {
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userFillQuestionnaire = (code) => {
    return request.get(`/api/fill/${code}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userSubmitQuestionnaire = (sid, answer) => {
    const data = {
        sid: sid,
        items: answer
    };
    const jsonData = JSON.stringify(data)
    console.log(jsonData)
    return request.post(`/api/submit`, jsonData, {
        headers: {
            'token': useUserStore().token,
            'Content-Type': 'application/json'
        }
    })
}

export const userUploadFile = (file) => {
    console.log("userUploadFile:"+file)
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`/api/common/upload`, formData, {
        headers: {
            'token': useUserStore().token,
            'Content-Type': 'multipart/form-data'
        }
    })
}


export const userExportResult = async (id) => {
    try {
        const token = useUserStore().token;
        const response = await axios.get(`http://59.110.163.198:8088/api/survey/${id}/export`, {
            params: { quePaperId: id },
            headers: { 'token': token },
            responseType: 'blob' // 确保响应类型为 Blob
        });

        console.log(response)
        console.log(response.headers)
        // 从响应中获取文件名
        const contentDisposition = response.headers["content-disposition"]
        let fileName = 'submitRecords.xlsx';
        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(contentDisposition);
            if (matches != null && matches[1]) {
                fileName = matches[1].replace(/['"]/g, '');
            }
        }

        // 创建一个Blob对象
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

        // 使用FileSaver.js保存文件
        FileSaver.saveAs(blob, fileName);
    } catch (error) {
        console.error("There was an error downloading the questionnaire:", error);
    }
}
export const userGetAnswernaireResult = (qid) => {
    return request.get(`/api/report/stat/question/${qid}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}
export const userGetPageResult = (qid,pageNum,pageSize) => {
    return request.get(`/api/report/detail/${qid}?pageNum=${pageNum}&pageSize=${pageSize}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}
export const userGetQuestionnaireResult = (rid) => {
    return request.get(`/api/response/${rid}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}
export const getSurveyResponse = (data) => {
    const jsonData = JSON.stringify(data)
    console.log(jsonData)
    console.log(useUserStore().token)
    return request.post(`/api/survey/response`, jsonData, {
        headers: {
            'token': useUserStore().token,
            'Content-Type': 'application/json'
        }
    })
}

export const userModifyAnswer = (rid, valid) => {
    const data = {
        rid: rid,
        valid: valid
    }
    const jsonData = JSON.stringify(data)
    console.log(jsonData)
    return request.put(`/api/response/${rid}`, jsonData, {
        headers: {
            'token': useUserStore().token,
            'Content-Type': 'application/json'
        }
    })
}

export const userGetUnreadmessage = (isRead) => {
    return request.get(`/api/message/list?isRead=${isRead}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userGetMessageDetail = (mid) => {
    return request.get(`/api/message/${mid}`, {
        headers: {
            'token': useUserStore().token,
        }
    })
}

export const userSetMessageUnread = (mid, isRead) => {
    return request.put(`/api/message/${mid}?isRead=${isRead}`, {}, {
        headers: {
            'token': useUserStore().token,
        }
    })
}