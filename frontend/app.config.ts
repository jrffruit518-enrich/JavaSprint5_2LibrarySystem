export default defineAppConfig({
  ui: {
    primary: 'emerald',
    gray: 'zinc',
    container: {
      base: 'mx-auto',
      padding: 'px-4 sm:px-6 lg:px-8',
      constrained: 'max-w-7xl'
    },
    card: {
      // Glassmorphism effect applied to base
      base: 'overflow-hidden transition-all duration-300 backdrop-blur-md shadow-xl shadow-gray-200/30 dark:shadow-none',
      background: 'bg-white/70 dark:bg-gray-900/70',
      ring: 'ring-1 ring-gray-200/50 dark:ring-gray-800/50',
      divide: 'divide-y divide-gray-200/50 dark:divide-gray-800/50',
      body: { base: '' }, // Removed redundant blur from body
      header: { base: 'border-b border-gray-200/50 dark:border-gray-800/50' },
      footer: { base: 'border-t border-gray-200/50 dark:border-gray-800/50' }
    },
    button: {
      rounded: 'rounded-md',
      default: { size: 'md' }
    },
    modal: {
      overlay: { background: 'bg-gray-950/50 backdrop-blur-sm' }
    }
  }
})
