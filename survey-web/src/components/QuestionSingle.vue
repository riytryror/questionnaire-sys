<template>
  <div class="question-item">
    <h3 class="title">
      {{ index }}. {{ title }}
      <el-tag size="small">单选</el-tag>
    </h3>

    <el-radio-group :model-value="modelValue" @update:model-value="val => emit('update:modelValue', val)">
      <el-radio 
        v-for="opt in options" 
        :key="opt" 
        :label="opt" 
        size="large" 
        border
        style="margin-bottom: 10px; width: 100%;"
      >
        {{ opt }}
      </el-radio>
    </el-radio-group>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  index: Number,
  title: String,
  config: Object, // { options: ["喜欢", "不喜欢"] }
  modelValue: String
})

const emit = defineEmits(['update:modelValue'])

// 防御性编程：防止 config 为空报错
const options = computed(() => {
  return props.config?.options || []
})
</script>

<style scoped>
.question-item { margin-bottom: 20px; padding: 15px; border: 1px solid #eee; border-radius: 8px; background: #fff;}
.title { margin-bottom: 15px; }
</style>