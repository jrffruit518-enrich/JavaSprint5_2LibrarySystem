<template>
  <div class="flex items-center group" v-bind="$attrs">
    <NuxtLink
      to="/"
      class="flex items-center gap-2.5 transition-all duration-300 transform active:scale-95"
    >
      <div class="relative flex items-center justify-center">
        <div class="absolute inset-0 bg-emerald-500/20 blur-lg rounded-full group-hover:bg-emerald-500/30 transition-all"></div>
        <UIcon
          name="i-heroicons-building-library-20-solid"
          class="w-8 h-8 text-emerald-600 dark:text-emerald-400 relative z-10 transition-transform group-hover:rotate-3"
        />
      </div>

      <div class="flex flex-col">
        <span class="font-black text-xl tracking-tighter leading-none bg-gradient-to-r from-slate-900 to-slate-700 dark:from-white dark:to-slate-400 bg-clip-text text-transparent">
          Cloud<span class="text-emerald-600 dark:text-emerald-400">Library</span>
        </span>
        <span class="text-[8px] font-black uppercase tracking-[0.3em] text-slate-400 dark:text-slate-500 leading-tight">
          System Core
        </span>
      </div>
    </NuxtLink>
  </div>
</template>

<script setup lang="ts">
/**
 * AppLogo (Jules v4.0 - Brand Identity)
 * Final Design: Immersive branding with gradient text and glow effects.
 */

defineOptions({
  inheritAttrs: false
})
</script>

<style scoped>
/* 可以在这里添加一些针对 Logo 的细微动画 */
</style>
