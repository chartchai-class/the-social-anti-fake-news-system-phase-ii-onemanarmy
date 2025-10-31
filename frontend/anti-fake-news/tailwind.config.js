/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}"
  ],
  theme: {
    extend: {
      fontFamily: {
        'alfa-slab': ['"Alfa Slab One"', 'serif'],
        'blackops': ['"Black Ops One"', 'cursive'],
        oswald: ['Oswald', 'Arial', 'sans-serif'],
        russo: ['Russo One', 'sans-serif'],
        orbitron: ['Orbitron', 'sans-serif'],
        press2p: ['"Press Start 2P"', 'monospace'],
        sharemono: ['"Share Tech Mono"', 'monospace'],
        anton: ['Anton', 'Arial Black', 'sans-serif'],
      },
      colors: {
        'my-blue': '#1C325B',
        'nike-yellow': '#FFCC00',
        'nike-orange': '#FF8200',
        'nike-black': '#111111',
        'nike-white': '#fff',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        }
      }
    },
    plugins: [],
  }
}
