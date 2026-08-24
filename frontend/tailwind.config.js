/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#f0f4fa',
          100: '#dce6f2',
          200: '#bccfe4',
          300: '#8fadcf',
          400: '#5c86b3',
          500: '#396899',
          600: '#28517e',
          700: '#1e4066',
          800: '#16304d',
          900: '#0c2340',
          950: '#08172e',
        },
        ocean: {
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
        },
        gold: {
          400: '#d4af37',
          500: '#c9a227',
          600: '#a8861b',
        },
      },
      fontFamily: {
        sans: ['Inter', 'Segoe UI', 'system-ui', 'sans-serif'],
        display: ['Sora', 'Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
