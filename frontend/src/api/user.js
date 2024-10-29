import request from "@/utils/request"
import { useUserStore } from "@/stores/user"

export const getUserInfo = () => {
    return request.get('/api/user', {
        headers: {
            'token': useUserStore().token
        }
    })
}