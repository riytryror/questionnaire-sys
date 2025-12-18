import axios from "axios";
import { ElMessage } from "element-plus";

// Create an Axios instance
const service = axios.create({
    baseURL: 'http://localhost:8080', // API base URL
    timeout: 5000, // Request timeout
});

//相应拦截器
service.interceptors.response.use(
    response => {
        if (response.config.responseType === 'blob') {
      return response.data
    }

        const res = response.data;
        if (res.code === 200) {
            return res.data;
        }else{
            ElMessage.error(res.msg || 'Error');
            return Promise.reject(new Error(res.msg || 'Error'));
        }
    },
    error => {
        console.log('err' + error);
        ElMessage.error(error.message);
        return Promise.reject(error);
    }
);

export default service;

