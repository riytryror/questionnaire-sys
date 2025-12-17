<template>
    <div class="question-item">
        <h3 class="title">
            {{ index }}.{{ title }}
            <el-tag type="warning" effect="dark" size="small" >录音题</el-tag>
            <span v-if="required" class="required">*</span>
        </h3>

        <p class="desc" v-if="config.placeholder">{{ config.placeholder }}</p>       
        <p class="desc" v-if="config.maxDuration">最长录制：{{ config.placeholder }} 秒</p>

        <el-upload
            class="upload-demo"
            action = "/api/upload/file"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            accept=".mp2,.wav,.m4a"
            >
        <el-button type="primary" plain>
            <el-icon><Microphone /></el-icon>
            {{ modelValue ? '重新上传' : '点击上传录音文件' }}
        </el-button>
        </el-upload>
        
        <div v-if="modelValue" class="audio-player">
            <p>已上传：</p>
            <audio controls :src="fullUrl"></audio>
        </div>
    </div>
</template>
<script setup>
import { computed } from 'vue';
import { Microphone } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';


// 接受父传子
const props = defineProps({
    index: Number, //题号
    title: String , //题干
    required: Boolean, // 是否必须填写
    config: Object, 
    modelValue: String, //文件路径,双向绑定答案
})

//更新modelValue
const emit = defineEmits(['update:modelValue'])

//计算完整播放地址
const fullUrl = computed(()=>{
    if(!props.modelValue) return ''
    return `http://localhost: 8080${props.modelValue}`
})

//上传成功回调
const handleUploadSuccess = (response) => {
    console.log("Audio return:", response)

    if (response.code === 200){
        ElMessage.success('Upload successfully')
        //传路径给父组件
        console.log('子传父：',response.data)
        emit('update:modelValue',response.data)
    }else{
        ElMessage.error('Upload failed:' + response.msg)
    }
}

//上传前校验
const beforeUpload = (file) => {
    const isAudio = file.type.startsWith('audio/')
    if (!isAudio){
        ElMessage.error("请上传音频文件！")
    }
    return isAudio
}
</script>

<style scoped>
.question-item { margin-bottom: 20px; padding: 15px; border: 1px solid #eee; border-radius: 8px; background: #fff;}
.title { margin-bottom: 10px; font-size: 16px; font-weight: bold; }
.required { color: red; margin-left: 5px; }
.desc { color: #999; font-size: 12px; margin-bottom: 10px; }
.audio-player { margin-top: 15px; background: #f5f7fa; padding: 10px; border-radius: 4px; }
</style>