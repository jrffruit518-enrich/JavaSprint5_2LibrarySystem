<template>
  <UContainer class="py-10">
    <UCard>
      <template #header>
        <div class="flex items-center gap-2 text-primary font-bold">
          <UIcon
            name="i-lucide-info"
            class="w-5 h-5"
          />
          系统诊断页 (System Diagnosis)
        </div>
      </template>

      <div class="space-y-4">
        <p class="text-lg">
          如果你能看到这个页面，说明 <strong>Nuxt 路由系统正常</strong>。
        </p>

        <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-default">
          <h3 class="font-semibold mb-2">
            当前环境检查：
          </h3>
          <ul class="list-disc list-inside space-y-1 text-sm">
            <li>Role Cookie: <UKbd>{{ role || '未发现' }}</UKbd></li>
            <li>Token Cookie: <UKbd>{{ token ? '已存在 (Sensitive)' : '未发现' }}</UKbd></li>
          </ul>
        </div>

        <UButton
          to="/"
          label="返回首页"
          variant="outline"
          icon="i-lucide-arrow-left"
        />
      </div>
    </UCard>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - 诊断页
 * 用于排查白屏问题
 */

// English Comment: Disable the problematic admin layout for testing
definePageMeta({
  layout: false
})

const role = useCookie('user-role').value
const token = useCookie('auth-token').value
</script>
