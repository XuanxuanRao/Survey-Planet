import request from "@/utils/request"
import { useUserStore } from "@/stores/user"

export const getCreatedQuestionnaireList = () => {
    console.log(useUserStore().token)
    return request.get('/api/survey/list?type=created', {
        headers: {
            'token': useUserStore().token
        }
    })
}

export const getFilledQuestionnaireList = () => {
    return request.get('/api/questionnaire/fill/list')
}


export const userSendQuestionnaireList = (type, title, description, questions) => {
    console.log(type)
    console.log(title)
    console.log(description)
    console.log(questions)
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