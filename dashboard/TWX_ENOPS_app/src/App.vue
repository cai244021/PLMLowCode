<script setup lang="ts">
import { baseApi } from './utils/env';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import en from 'element-plus/dist/locale/en.mjs';
const { locale } = useI18n();

const elementLocale = computed(() => {
	// @ts-ignore
	locale.value = parent.dsLang || 'zh';
	localStorage.setItem('language', locale.value);
	return locale.value === 'zh' ? zhCn : en;
});

import { useBaseInfoStore } from '@/store';
const baseInfoStore = useBaseInfoStore();
baseInfoStore.fetchSpaceUrl();
</script>

<template>
	<el-config-provider :locale="elementLocale"><RouterView /></el-config-provider>
</template>
