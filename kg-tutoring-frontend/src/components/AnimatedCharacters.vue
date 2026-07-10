<template>
  <div class="scene" ref="sceneRef">
    <!-- 紫色大角色 -->
    <div class="char purple" ref="purpleRef" :style="purpleStyle">
      <div class="char-body"></div>
      <div class="eyes">
        <div class="eye" :class="{ blinking: purpleBlink }">
          <div class="pupil" :style="purplePupilStyle"></div>
        </div>
        <div class="eye" :class="{ blinking: purpleBlink }">
          <div class="pupil" :style="purplePupilStyle"></div>
        </div>
      </div>
    </div>
    <!-- 黑色角色 -->
    <div class="char black" ref="blackRef" :style="blackStyle">
      <div class="char-body"></div>
      <div class="eyes small">
        <div class="eye" :class="{ blinking: blackBlink }">
          <div class="pupil" :style="blackPupilStyle"></div>
        </div>
        <div class="eye" :class="{ blinking: blackBlink }">
          <div class="pupil" :style="blackPupilStyle"></div>
        </div>
      </div>
    </div>
    <!-- 橙色角色 -->
    <div class="char orange" ref="orangeRef" :style="orangeStyle">
      <div class="char-body"></div>
      <div class="eyes">
        <div class="pupil-raw" :style="orangePupilStyle"></div>
        <div class="pupil-raw" :style="orangePupilStyle"></div>
      </div>
    </div>
    <!-- 黄色角色 -->
    <div class="char yellow" ref="yellowRef" :style="yellowStyle">
      <div class="char-body"></div>
      <div class="eyes small">
        <div class="pupil-raw" :style="yellowPupilStyle"></div>
        <div class="pupil-raw" :style="yellowPupilStyle"></div>
      </div>
      <div class="mouth" :class="{ surprised: showPassword }"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  isTyping: { type: Boolean, default: false },
  showPassword: { type: Boolean, default: false },
  passwordLength: { type: Number, default: 0 },
})

const mouseX = ref(0)
const mouseY = ref(0)
const purpleBlink = ref(false)
const blackBlink = ref(false)
const sceneRef = ref(null)
const purpleRef = ref(null)
const blackRef = ref(null)
const orangeRef = ref(null)
const yellowRef = ref(null)

// 鼠标追踪
const onMouseMove = (e) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

const calcPos = (el) => {
  if (!el) return { x: 0, y: 0, skew: 0 }
  const r = el.getBoundingClientRect()
  const cx = r.left + r.width / 2
  const cy = r.top + r.height / 2
  const dx = Math.max(-12, Math.min(12, (mouseX.value - cx) / 25))
  const dy = Math.max(-8, Math.min(8, (mouseY.value - cy) / 25))
  return { x: dx, y: dy, skew: (mouseX.value - cx) / 80 }
}

const purplePos = computed(() => calcPos(purpleRef.value))
const blackPos = computed(() => calcPos(blackRef.value))
const orangePos = computed(() => calcPos(orangeRef.value))
const yellowPos = computed(() => calcPos(yellowRef.value))

const pupilStyle = (pos) => ({
  transform: `translate(${pos.x}px, ${pos.y}px)`,
  transition: 'transform 0.15s ease-out',
})

const purplePupilStyle = computed(() => pupilStyle(purplePos.value))
const blackPupilStyle = computed(() => pupilStyle(blackPos.value))
const orangePupilStyle = computed(() => pupilStyle(orangePos.value))
const yellowPupilStyle = computed(() => pupilStyle(yellowPos.value))

// 鼠标追踪位移，身体随鼠标轻微摆动，底部保持对齐
const charStyle = (pos) => {
  return {
    transform: [
      `translateX(${pos.x * 3}px)`,
      `translateY(${pos.y * 2}px)`,
      `skewX(${pos.skew}deg)`,
    ].join(' '),
    transition: 'transform 0.3s ease-out',
  }
}

const purpleStyle = computed(() => charStyle(purplePos.value))
const blackStyle = computed(() => charStyle(blackPos.value))
const orangeStyle = computed(() => charStyle(orangePos.value))
const yellowStyle = computed(() => charStyle(yellowPos.value))

// 眨眼
let timers = []
const setT = (fn, ms) => {
  const t = setTimeout(fn, ms)
  timers.push(t)
  return t
}
onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  const blink = (ref) => {
    ref.value = true
    setT(() => {
      ref.value = false
    }, 150)
  }
  const loop = (ref, min, max) => {
    blink(ref)
    setT(() => loop(ref, min, max), min + Math.random() * (max - min))
  }
  setT(() => loop(purpleBlink, 3000, 7000), 2000)
  setT(() => loop(blackBlink, 4000, 8000), 3500)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  timers.forEach(clearTimeout)
})
</script>

<style scoped>
.scene {
  position: relative;
  width: 420px;
  height: 360px;
  margin: 0 auto;
}

.char {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center; /* 从底部旋转，像身体倾斜 */
}

.char-body {
  width: 100%;
  height: 100%;
  border-radius: 50% 50% 0 0;
}

/* 尺寸、位置 */
.purple {
  left: 40px;
  width: 170px;
  height: 340px;
  z-index: 1;
}
.purple .char-body {
  background: #c1784e;
}
.black {
  left: 185px;
  width: 110px;
  height: 260px;
  z-index: 2;
}
.black .char-body {
  background: #3d4a5c;
}
.orange {
  left: 5px;
  width: 200px;
  height: 170px;
  z-index: 3;
}
.orange .char-body {
  background: #ff7b3d;
}
.yellow {
  left: 240px;
  width: 130px;
  height: 200px;
  z-index: 4;
}
.yellow .char-body {
  background: #f5a623;
}

/* ── 眼睛 ── */
.eyes {
  position: absolute;
  top: 25%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 24px;
}
.eyes.small {
  gap: 16px;
  top: 22%;
}
.purple .eyes {
  top: 22%;
  gap: 28px;
}
.black .eyes {
  top: 25%;
  gap: 16px;
}
.orange .eyes {
  top: 30%;
  gap: 36px;
}
.yellow .eyes {
  top: 28%;
  gap: 18px;
}

.eye {
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    height 0.15s,
    transform 0.15s;
  overflow: hidden;
}
.eyes.small .eye {
  width: 28px;
  height: 28px;
}
.eye.blinking {
  height: 3px;
}
.pupil {
  width: 14px;
  height: 14px;
  background: #1a1a2e;
  border-radius: 50%;
}
.eyes.small .pupil {
  width: 10px;
  height: 10px;
}

.pupil-raw {
  width: 14px;
  height: 14px;
  background: #1a1a2e;
  border-radius: 50%;
}
.eyes.small .pupil-raw {
  width: 10px;
  height: 10px;
}
.orange .pupil-raw {
  width: 16px;
  height: 16px;
}

.mouth {
  position: absolute;
  bottom: 50px;
  left: 50%;
  transform: translateX(-50%);
  width: 36px;
  height: 12px;
  background: #c4a82d;
  border-radius: 0 0 18px 18px;
  transform-origin: center center;
}

/* 密码可见时，嘴巴变惊讶 O 形 */
/* 待机呼吸动画 */
.char-body {
  animation: breathe 3.5s ease infinite;
}
.purple .char-body {
  animation-duration: 3.8s;
}
.black .char-body {
  animation-duration: 3.2s;
  animation-delay: 0.5s;
}
.orange .char-body {
  animation-duration: 4s;
  animation-delay: 0.2s;
}
.yellow .char-body {
  animation-duration: 3s;
  animation-delay: 0.8s;
}
@keyframes breathe {
  0%,
  100% {
    transform: scaleY(1);
  }
  50% {
    transform: scaleY(1.025);
  }
}

.mouth {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.mouth.surprised {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: transparent;
  border: 3px solid #c4a82d;
  bottom: 44px;
}
</style>
