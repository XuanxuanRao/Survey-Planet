import request from "@/utils/request"
import { useUserStore } from "@/stores/user"

export const getUserInfo = () => {
    return request.get('/api/user', {
        headers: {
            'token': useUserStore().token
        }
    })
}

export const updateUserInfo = (password, avatar,description) => {
    const data = {
        password: password,
        avatar: avatar,
        description:description
    }
    const jsonData = JSON.stringify(data)
    return request.put('/api/user', jsonData, {
        headers: {
            'token': useUserStore().token
        }
    })
}