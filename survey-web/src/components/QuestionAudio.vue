<template>
    <div class="question-item">
        <h3 class="title">
            {{ index }}.{{ title }}
        </h3>
        
    </div>
</template>
<script>
import { compile, computed } from 'vue';
import { Microphone } from '@element-plus/icons-vue/dist/types';
import { ElMessage } from 'element-plus';

// 接受父传子
const props = defineProps({
    index: Number, //题号
    title: String , //题干
    required: Boolean, // 是否必须填写
    config: Object, 
    modelValue: String, //文件路径,双向绑定答案
})

const emit = defineEmits(['update: modelValue'])

//计算完整播放地址
const fullUrl = computed(()=>{
    if(!props.modelValue) return ''
    return `http://localhost: 8080${props.modelValue}`
})

</script>
<style scoped>
    
</style>