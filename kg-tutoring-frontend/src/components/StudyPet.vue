<template>
  <div class="pet-container" :class="{ hidden: !visible }" @click="poke">
    <!-- 气泡 -->
    <Transition name="bubble">
      <div v-if="bubbleText" class="speech-bubble">{{ bubbleText }}</div>
    </Transition>

    <!-- 粒子 -->
    <span v-for="p in particles" :key="p.id" class="particle" :style="p.style">{{ p.emoji }}</span>

    <!-- 宠物本体 -->
    <div class="pet-body" :class="[mood, { jumping: isJumping }]">
      <div class="pet-shadow"></div>
      <div class="eyes">
        <span class="eye left" :class="{ blink: blinking }">{{ leftEye }}</span>
        <span class="eye right" :class="{ blink: blinking }">{{ rightEye }}</span>
      </div>
      <div class="mouth">{{ mouthIcon }}</div>
      <div class="blush left-blush"></div>
      <div class="blush right-blush"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const visible = ref(true)
const mood = ref('idle')       // idle | happy | sad | fired
const bubbleText = ref('')
const blinking = ref(false)
const isJumping = ref(false)
const particles = ref([])
let bubbleTimer = null
let blinkTimer = null
let idleTimer = null
let pid = 0

// ── 表情映射 ──
const moodConfig = {
  idle:    { left: '●', right: '●', mouth: '﹀' },
  happy:   { left: '★', right: '★', mouth: '▼' },
  sad:     { left: '◕', right: '◕', mouth: '︿' },
  fired:   { left: '🔥', right: '🔥', mouth: '○' },
}
const leftEye  = ref('●')
const rightEye = ref('●')
const mouthIcon = ref('﹀')

const applyMood = (m) => {
  mood.value = m
  const cfg = moodConfig[m] || moodConfig.idle
  leftEye.value = cfg.left
  rightEye.value = cfg.right
  mouthIcon.value = cfg.mouth
}

// ── 粒子特效 ──
const spawnParticles = (emojiList, count = 6) => {
  for (let i = 0; i < count; i++) {
    const id = ++pid
    const angle = (Math.PI * 2 * i) / count + (Math.random() - 0.5) * 0.5
    const dist = 40 + Math.random() * 40
    const x = Math.cos(angle) * dist
    const y = Math.sin(angle) * dist - 20
    const emoji = emojiList[Math.floor(Math.random() * emojiList.length)]
    particles.value.push({
      id,
      emoji,
      style: {
        '--tx': `${x}px`,
        '--ty': `${y}px`,
        animationDelay: `${Math.random() * 0.15}s`
      }
    })
    setTimeout(() => {
      particles.value = particles.value.filter(p => p.id !== id)
    }, 900)
  }
}

// ── 公共方法 ──
const celebrate = () => {
  applyMood('happy')
  isJumping.value = true
  spawnParticles(['✨','🌟','💫','🎉','💖'], 8)
  say(['太棒了！','答对了！','你真厉害！','继续保持！'][Math.floor(Math.random()*4)])
  setTimeout(() => { isJumping.value = false; applyMood('idle') }, 1200)
}

const comfort = () => {
  applyMood('sad')
  spawnParticles(['💪','📚','🌟'], 4)
  say(['没关系，再试一次！','错题是进步的阶梯','看看解析，下次一定对','加油，你可以的！'][Math.floor(Math.random()*4)])
  setTimeout(() => applyMood('idle'), 2500)
}

const say = (text) => {
  bubbleText.value = text
  clearTimeout(bubbleTimer)
  bubbleTimer = setTimeout(() => { bubbleText.value = '' }, 2500)
}

const fireUp = () => {
  applyMood('fired')
  spawnParticles(['🔥','⚡','💥'], 6)
  say('连击！势不可挡！')
  setTimeout(() => applyMood('idle'), 2000)
}

const poke = () => {
  isJumping.value = true
  spawnParticles(['💕'], 3)
  say(['嘿！','别戳我~','一起学习吧！'][Math.floor(Math.random()*3)])
  setTimeout(() => isJumping.value = false, 500)
}

// ── 定时眨眼 ──
const setupBlink = () => {
  blinkTimer = setInterval(() => {
    blinking.value = true
    setTimeout(() => { blinking.value = false }, 150)
  }, 2500 + Math.random() * 4000)
}

// ── 空闲小动作 ──
const setupIdle = () => {
  idleTimer = setInterval(() => {
    if (mood.value !== 'idle') return
    const r = Math.random()
    if (r < 0.3) {
      isJumping.value = true
      setTimeout(() => { isJumping.value = false }, 300)
    } else if (r < 0.5) {
      spawnParticles(['💭','📖'], 2)
      say(['加油学习！','今天进步一点点','休息一下眼睛吧'][Math.floor(Math.random()*3)])
    }
  }, 15000 + Math.random() * 20000)
}

onMounted(() => { setupBlink(); setupIdle() })
onUnmounted(() => { clearTimeout(bubbleTimer); clearInterval(blinkTimer); clearInterval(idleTimer) })

defineExpose({ celebrate, comfort, say, fireUp, poke })
</script>

<style scoped>
.pet-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9990;
  cursor: pointer;
  user-select: none;
  transition: opacity 0.3s ease;
}
.pet-container.hidden { opacity: 0; pointer-events: none; }

/* ── 宠物身体 ── */
.pet-body {
  width: 64px; height: 64px;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffb347, #ff7b3d);
  box-shadow: 0 4px 16px rgba(255,123,61,0.35), inset 0 -4px 8px rgba(0,0,0,0.1);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  position: relative;
  transition: transform 0.2s ease;
  animation: bob 2.5s ease infinite;
}
.pet-body.sad    { background: linear-gradient(145deg, #a0b4c8, #7895b0); box-shadow: 0 4px 16px rgba(120,149,176,0.35), inset 0 -4px 8px rgba(0,0,0,0.1); }
.pet-body.happy  { animation: bob 0.4s ease infinite; }
.pet-body.fired  { background: linear-gradient(145deg, #ff6b6b, #e74c3c); box-shadow: 0 4px 20px rgba(231,76,60,0.5), inset 0 -4px 8px rgba(0,0,0,0.1); }
.pet-body.jumping { animation: jump 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); }

.pet-shadow {
  position: absolute;
  bottom: -8px; left: 25%; width: 50%; height: 6px;
  background: rgba(0,0,0,0.08); border-radius: 50%;
  transition: transform 0.3s ease;
}
.pet-body.jumping .pet-shadow { transform: scale(0.5); }

@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
@keyframes jump {
  0% { transform: translateY(0) scale(1); }
  30% { transform: translateY(-18px) scale(1.08); }
  60% { transform: translateY(-10px) scale(0.95); }
  100% { transform: translateY(0) scale(1); }
}

/* ── 眼睛 ── */
.eyes {
  display: flex; gap: 8px;
  margin-bottom: 2px;
  font-size: 12px; line-height: 1;
  transition: all 0.15s ease;
}
.eye {
  display: inline-block; width: 14px;
  text-align: center;
  transition: transform 0.1s ease;
}
.eye.blink { transform: scaleY(0.1); }

/* ── 嘴巴 ── */
.mouth {
  font-size: 12px; line-height: 1;
  color: rgba(0,0,0,0.5);
  transition: all 0.2s ease;
}

/* ── 腮红 ── */
.blush {
  position: absolute; width: 10px; height: 6px;
  background: rgba(255,255,255,0.3); border-radius: 50%;
  bottom: 18px;
}
.left-blush  { left: 10px; }
.right-blush { right: 10px; }

/* ── 气泡 ── */
.speech-bubble {
  position: absolute;
  bottom: 76px;
  right: -10px;
  background: #fff;
  color: #2d2a26;
  padding: 8px 14px;
  border-radius: 14px 14px 4px 14px;
  font-size: 13px; font-weight: 500;
  white-space: normal;
  line-height: 1.6;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  width: max-content;
  max-width: 240px;
}
.bubble-enter-active { animation: popIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.bubble-leave-active { animation: popIn 0.2s ease reverse; }
@keyframes popIn {
  from { opacity: 0; transform: translateY(6px) scale(0.8); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* ── 粒子 ── */
.particle {
  position: absolute;
  bottom: 30px;
  right: 20px;
  font-size: 16px;
  pointer-events: none;
  animation: particleFly 0.8s ease-out forwards;
  animation-delay: var(--delay, 0s);
}
@keyframes particleFly {
  0%   { opacity: 1; transform: translate(0, 0) scale(1); }
  100% { opacity: 0; transform: translate(var(--tx, 0px), var(--ty, -60px)) scale(0.3); }
}
</style>
