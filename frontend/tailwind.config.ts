import type { Config } from 'tailwindcss'

export default <Partial<Config>>{
  theme: {
    extend: {
      animation: {
        // Soft breathing effect for "Overdue" status or primary buttons
        'pulse-soft': 'pulse-soft 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        // Spring-loaded pop for modals and panels
        'spring-in': 'spring-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)',
      },
      keyframes: {
        'pulse-soft': {
          '0%, 100%': { opacity: '1', transform: 'scale(1)' },
          '50%': { opacity: '0.85', transform: 'scale(1.02)' },
        },
        'spring-in': {
          '0%': { opacity: '0', transform: 'scale(0.9) translateY(10px)' },
          '100%': { opacity: '1', transform: 'scale(1) translateY(0)' },
        }
      }
    }
  }
}
