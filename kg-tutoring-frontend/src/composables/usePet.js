import { ref } from 'vue'

const petRef = ref(null)

export function usePet() {
  const register = (pet) => { petRef.value = pet }

  const celebrate = () => petRef.value?.celebrate()
  const comfort   = () => petRef.value?.comfort()
  const say       = (text) => petRef.value?.say(text)
  const fireUp    = () => petRef.value?.fireUp()

  return { register, celebrate, comfort, say, fireUp }
}
