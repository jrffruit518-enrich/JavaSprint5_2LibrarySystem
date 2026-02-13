<template>
  <UContainer class="py-10">
    <UCard :ui="{ body: 'p-6' }">
      <template #header>
        <div class="flex items-center gap-2 text-primary font-bold text-xl">
          <UIcon
            name="i-heroicons-building-library"
            class="w-6 h-6"
          />
          CloudLibrary: Where Knowledge Meets the Horizon
        </div>
      </template>

      <div class="space-y-6 text-gray-700 dark:text-gray-300">
        <section class="space-y-3">
          <h2 class="text-lg font-semibold text-primary">
            Introduction
          </h2>
          <p class="leading-relaxed">
            Welcome to <strong>CloudLibrary</strong>, your premier digital and physical gateway to the world's most influential thoughts and narratives. Our curated collection is designed for the curious mind, bridging the gap between timeless classics and modern scientific exploration.
          </p>
          <p class="leading-relaxed">
            Whether you are diving into the "Jazz Age" with F. Scott Fitzgerald's <em>The Great Gatsby</em>, exploring the vast mysteries of the universe through Carl Sagan’s <em>Cosmos</em>, or tracing the milestones of human history with Yuval Noah Harari’s <em>Sapiens</em>, CloudLibrary provides the resources you need to expand your horizons.
          </p>
        </section>

        <UDivider />

        <section class="space-y-4">
          <h2 class="text-lg font-semibold text-primary">
            Contact Information
          </h2>
          <p>We are here to help you navigate our collection and manage your reading journey.</p>

          <ul class="space-y-2">
            <li class="flex items-start gap-2">
              <UIcon
                name="i-heroicons-map-pin"
                class="w-5 h-5 mt-0.5 text-primary"
              />
              <span><strong>Central Information Desk:</strong> 123 Knowledge Way, Digital District, Tech City</span>
            </li>
            <li class="flex items-center gap-2">
              <UIcon
                name="i-heroicons-phone"
                class="w-5 h-5 text-primary"
              />
              <span><strong>Phone:</strong> +1 (555) LIBRARY-01</span>
            </li>
            <li class="flex items-center gap-2">
              <UIcon
                name="i-heroicons-envelope"
                class="w-5 h-5 text-primary"
              />
              <span><strong>Email Support:</strong> admin@library.com</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon
                name="i-heroicons-clock"
                class="w-5 h-5 mt-0.5 text-primary"
              />
              <div>
                <strong>Operating Hours:</strong>
                <ul class="ml-4 mt-1 text-sm space-y-1">
                  <li>• Monday – Friday: 9:00 AM – 8:00 PM</li>
                  <li>• Saturday: 10:00 AM – 6:00 PM</li>
                  <li>• Digital Portal: Accessible 24/7 for all registered members</li>
                </ul>
              </div>
            </li>
          </ul>
        </section>

        <div class="pt-4">
          <UButton
            to="/"
            label="Back to Home"
            variant="ghost"
            icon="i-heroicons-arrow-left"
          />
        </div>
      </div>
    </UCard>

    <div
      v-if="role === 'ADMIN'"
      class="mt-10 opacity-50 hover:opacity-100 transition-opacity"
    >
      <UDivider
        label="System Diagnosis (Admin Only)"
        label-pos="left"
        class="mb-4"
      />
      <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-dashed border-gray-400">
        <ul class="list-disc list-inside space-y-1 text-xs font-mono">
          <li>Role Cookie: <UKbd size="sm">{{ role }}</UKbd></li>
          <li>Token Cookie: <UKbd size="sm">{{ token ? 'Present' : 'Missing' }}</UKbd></li>
        </ul>
      </div>
    </div>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * CloudLibrary - About Page
 * Jules Fix: 暂时移除 layout 引用，等 layouts/ 文件夹就绪后再开启
 */
/*
definePageMeta({
  layout: 'default'
})
*/

const role = useCookie('user-role').value
const token = useCookie('auth-token').value
</script>
