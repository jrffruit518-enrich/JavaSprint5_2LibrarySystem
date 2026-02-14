<template>
  <UContainer class="py-12 animate-spring-in">
    <UCard 
      class="glass-effect border-none shadow-2xl relative overflow-hidden"
      :ui="{ 
        body: 'p-8',
        header: { base: 'border-b border-white/20 bg-white/60 dark:bg-white/10' }
      }"
    >
      <div class="absolute -right-20 -top-20 w-64 h-64 bg-emerald-500/15 blur-[100px] rounded-full"></div>

      <template #header>
        <div class="flex items-center gap-3 text-emerald-700 dark:text-emerald-400">
          <UIcon name="i-heroicons-building-library-solid" class="w-8 h-8" />
          <h1 class="text-2xl font-black uppercase tracking-tighter italic">
            CloudLibrary<span class="text-slate-900 dark:text-white font-normal">: Knowledge Horizon</span>
          </h1>
        </div>
      </template>

      <div class="relative z-10 space-y-10">
        <section class="space-y-4">
          <div class="flex items-center gap-2">
            <div class="w-1.5 h-5 bg-emerald-500 rounded-full"></div>
            <h2 class="text-base font-black uppercase tracking-widest text-emerald-700 dark:text-emerald-400">
              Introduction
            </h2>
          </div>
          <div class="space-y-4 text-base leading-relaxed font-bold text-slate-900 dark:text-white">
            <p>
              Welcome to <strong class="text-emerald-600 dark:text-emerald-400 underline underline-offset-4">CloudLibrary</strong>, your premier digital and physical gateway to the world's most influential thoughts and narratives. Our curated collection is designed for the curious mind, bridging the gap between timeless classics and modern scientific exploration.
            </p>
            <p>
              Whether you are diving into the "Jazz Age" with F. Scott Fitzgerald's <em>The Great Gatsby</em>, exploring the vast mysteries of the universe through Carl Sagan’s <em>Cosmos</em>, or tracing the milestones of human history with Yuval Noah Harari’s <em>Sapiens</em>.
            </p>
          </div>
        </section>

        <UDivider class="border-slate-300 dark:border-white/20" />

        <section class="space-y-6">
          <div class="flex items-center gap-2">
            <div class="w-1.5 h-5 bg-emerald-500 rounded-full"></div>
            <h2 class="text-base font-black uppercase tracking-widest text-emerald-700 dark:text-emerald-400">
              Contact Information
            </h2>
          </div>

          <div class="grid md:grid-cols-2 gap-8">
            <ul class="space-y-5">
              <li class="flex items-start gap-4">
                <UIcon name="i-heroicons-map-pin-solid" class="w-6 h-6 text-emerald-600 flex-shrink-0" />
                <span class="text-sm font-bold text-slate-900 dark:text-white">
                  <span class="block text-emerald-700 dark:text-emerald-400 uppercase text-[10px] tracking-widest mb-1">Location</span>
                  123 Knowledge Way, Digital District, Tech City
                </span>
              </li>
              <li class="flex items-center gap-4">
                <UIcon name="i-heroicons-phone-solid" class="w-6 h-6 text-emerald-600 flex-shrink-0" />
                <span class="text-sm font-bold text-slate-900 dark:text-white">
                   <span class="block text-emerald-700 dark:text-emerald-400 uppercase text-[10px] tracking-widest mb-1">Hotline</span>
                  +1 (555) LIBRARY-01
                </span>
              </li>
              <li class="flex items-center gap-4">
                <UIcon name="i-heroicons-envelope-solid" class="w-6 h-6 text-emerald-600 flex-shrink-0" />
                <span class="text-sm font-bold text-slate-900 dark:text-white">
                   <span class="block text-emerald-700 dark:text-emerald-400 uppercase text-[10px] tracking-widest mb-1">Support</span>
                  admin@library.com
                </span>
              </li>
            </ul>

            <div class="bg-emerald-500/10 dark:bg-emerald-500/5 p-6 rounded-2xl border-2 border-emerald-500/20 shadow-inner">
              <div class="flex items-center gap-2 mb-4">
                <UIcon name="i-heroicons-clock-solid" class="w-5 h-5 text-emerald-600" />
                <span class="text-xs font-black uppercase tracking-[0.2em] text-emerald-700 dark:text-emerald-400">Operating Hours</span>
              </div>
              <ul class="text-sm space-y-3 font-bold text-slate-800 dark:text-slate-100">
                <li class="flex justify-between border-b border-emerald-500/10 pb-2">
                  <span>Monday – Friday</span> 
                  <span class="font-mono text-emerald-700 dark:text-emerald-400">09:00 - 20:00</span>
                </li>
                <li class="flex justify-between border-b border-emerald-500/10 pb-2">
                  <span>Saturday</span> 
                  <span class="font-mono text-emerald-700 dark:text-emerald-400">10:00 - 18:00</span>
                </li>
                <li class="flex justify-between items-center pt-2">
                  <span class="flex items-center gap-2">
                    <span class="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
                    Digital Portal
                  </span> 
                  <span class="bg-emerald-500 text-white text-[10px] px-2 py-0.5 rounded shadow-sm">24/7 ACCESS</span>
                </li>
              </ul>
            </div>
          </div>
        </section>

        <div class="pt-6 border-t border-white/10">
          <UButton
            to="/"
            label="Back to Portal"
            variant="soft"
            color="emerald"
            icon="i-heroicons-arrow-left"
            class="font-black px-6 py-2 rounded-xl transition-all active:scale-95"
          />
        </div>
      </div>
    </UCard>

    <div v-if="role === 'ROLE_ADMIN'" class="mt-10">
      <div class="glass-effect p-4 border-none rounded-xl bg-rose-500/5">
        <div class="flex items-center gap-2 mb-2">
          <div class="w-2 h-2 rounded-full bg-rose-500 shadow-[0_0_8px_rgba(244,63,94,0.6)]"></div>
          <span class="text-[10px] font-black uppercase tracking-widest text-rose-600 dark:text-rose-400">System Diagnosis</span>
        </div>
        <div class="flex gap-6 text-[11px] font-mono font-bold text-slate-700 dark:text-slate-300">
          <p>ROLE: <UKbd size="xs" class="bg-rose-500/10">{{ role }}</UKbd></p>
          <p>AUTH: <UKbd size="xs" class="bg-rose-500/10">{{ token ? 'ENCRYPTED' : 'MISSING' }}</UKbd></p>
        </div>
      </div>
    </div>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * About Page (Jules v4.4 - High Legibility Edition)
 * Fixed: Increased font size and contrast for contact/hours sections.
 */
const role = useCookie('user-role').value
const token = useCookie('auth-token').value
</script>

<style scoped>
.animate-spring-in {
  animation: spring-in 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes spring-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
