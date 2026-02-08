<template>
  <div class="overflow-x-auto">
    <UTable
      :rows="data"
      :columns="columns"
    >
      <template #actions-data="{ row }">
        <div class="flex gap-2">
          <template v-if="role === 'admin'">
            <UButton
              icon="i-lucide-edit"
              size="sm"
              color="neutral"
              variant="ghost"
              @click="$emit('edit', row)"
            />
            <UButton
              icon="i-lucide-trash"
              size="sm"
              color="red"
              variant="ghost"
              @click="$emit('delete', row)"
            />
          </template>

          <template v-else-if="role === 'user'">
            <UButton
              label="Borrow"
              icon="i-lucide-hand-helping"
              size="sm"
              :disabled="row.status === 'Borrowed'"
              @click="$emit('borrow', row)"
            />
          </template>

          <template v-else>
            <span class="text-xs text-muted italic">Login to borrow</span>
          </template>
        </div>
      </template>

      <template #status-data="{ row }">
        <UBadge
          :color="row.status === 'Available' ? 'green' : 'orange'"
          variant="subtle"
        >
          {{ row.status }}
        </UBadge>
      </template>
    </UTable>
  </div>
</template>

<script setup>
/* Props:
  - data: 图书列表数组
  - role: 当前身份 ('admin', 'user', 'guest')
*/
defineProps({
  data: Array,
  role: {
    type: String,
    default: 'guest'
  }
})

/* 定义表格列 */
const columns = [
  { key: 'title', label: 'Title' },
  { key: 'author', label: 'Author' },
  { key: 'category', label: 'Category' },
  { key: 'status', label: 'Status' },
  { key: 'actions', label: 'Actions' }
]

/* 定义组件发出的事件 */
defineEmits(['edit', 'delete', 'borrow'])
</script>
