<script setup>
import { ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { searchTodos, getTypeLabel, getStatusLabel } from '@/api/task.js'

const router = useRouter()

const emits = defineEmits(['close'])
const visible = ref(false)
const query = ref('')
const results = ref([])
const searching = ref(false)
const selectedIndex = ref(0)
const inputRef = ref(null)

let debounceTimer = null

function open() {
  visible.value = true
  query.value = ''
  results.value = []
  selectedIndex.value = 0
  nextTick(() => {
    inputRef.value?.focus()
  })
}

function close() {
  visible.value = false
  query.value = ''
  results.value = []
  emits('close')
}

function doSearch() {
  if (debounceTimer) clearTimeout(debounceTimer)
  const q = query.value
  if (!q.trim()) {
    results.value = []
    selectedIndex.value = 0
    return
  }
  searching.value = true
  selectedIndex.value = 0
  debounceTimer = setTimeout(async () => {
    try {
      results.value = await searchTodos(q)
    } finally {
      searching.value = false
    }
  }, 250)
}

watch(query, () => {
  doSearch()
})

function highlightText(text, keyword) {
  if (!keyword) return text
  const lowerText = text.toLowerCase()
  const lowerKw = keyword.toLowerCase()
  const idx = lowerText.indexOf(lowerKw)
  if (idx === -1) return text
  const before = text.slice(0, idx)
  const match = text.slice(idx, idx + keyword.length)
  const after = text.slice(idx + keyword.length)
  return before + '\u0000' + match + '\u0001' + after
}

function goToResult(item) {
  close()
  router.push(`/task/${item.id}`)
}

function onKeydown(e) {
  if (e.key === 'Escape') {
    close()
  } else if (e.key === 'ArrowDown') {
    e.preventDefault()
    if (results.value.length > 0) {
      selectedIndex.value = (selectedIndex.value + 1) % results.value.length
    }
    scrollToSelected()
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    if (results.value.length > 0) {
      selectedIndex.value = (selectedIndex.value - 1 + results.value.length) % results.value.length
    }
    scrollToSelected()
  } else if (e.key === 'Enter') {
    e.preventDefault()
    if (results.value.length > 0 && selectedIndex.value >= 0) {
      goToResult(results.value[selectedIndex.value])
    }
  }
}

function scrollToSelected() {
  nextTick(() => {
    const el = document.querySelector('.search-result-item.active')
    el?.scrollIntoView({ block: 'nearest' })
  })
}

function onOverlayClick(e) {
  if (e.target === e.currentTarget) close()
}

defineExpose({ open, close })
</script>

<template>
  <Teleport to="body">
    <div class="search-overlay" v-if="visible" @click="onOverlayClick" @keydown="onKeydown" tabindex="-1">
      <div class="search-panel">
        <!-- 输入区 -->
        <div class="search-input-row">
          <span class="search-icon">🔍</span>
          <input
            ref="inputRef"
            v-model="query"
            type="text"
            class="search-input"
            placeholder="搜索标题、描述、负责人、项目..."
            @keydown="onKeydown"
          />
          <kbd class="shortcut-hint">Esc</kbd>
        </div>

        <!-- 空状态：仅一行键盘提示 -->
        <div class="search-hint-bar" v-if="!query.trim()">
          <span><kbd>↑</kbd><kbd>↓</kbd> 导航</span>
          <span><kbd>Enter</kbd> 打开</span>
          <span><kbd>Esc</kbd> 关闭</span>
        </div>

        <!-- 搜索结果 -->
        <div class="search-results" v-else>
          <div class="searching-hint" v-if="searching">搜索中...</div>

          <template v-else-if="results.length > 0">
            <div class="results-count">{{ results.length }} 条结果</div>
            <div
              v-for="(item, idx) in results"
              :key="item.id"
              :class="['search-result-item', { active: idx === selectedIndex }]"
              @click="goToResult(item)"
              @mouseenter="selectedIndex = idx"
            >
              <div class="result-header">
                <span :class="['type-tag', 'type-' + item.type]">{{ getTypeLabel(item.type) }}</span>
                <span :class="['status-tag', 'status-' + item.status]">{{ getStatusLabel(item.status) }}</span>
                <span class="result-id">#{{ item.id }}</span>
              </div>
              <div class="result-title">
                <span
                  v-for="(part, pi) in highlightText(item.title, item._keyword).split('\u0000')"
                  :key="'t-' + pi"
                >
                  <template v-if="part.includes('\u0001')">
                    <mark class="highlight-mark">{{ part.split('\u0001')[0] }}</mark>
                    <span>{{ part.split('\u0001')[1] }}</span>
                  </template>
                  <template v-else>{{ part }}</template>
                </span>
              </div>
              <div class="result-matches" v-for="(m, mi) in item.matches" :key="'m-' + mi">
                <span class="match-field">{{ m.field === 'title' ? '标题' : m.field === 'desc' ? '描述' : m.field === 'assignedTo' ? '负责人' : '项目' }}:</span>
                <span class="match-text">
                  <span
                    v-for="(part, pi) in highlightText(m.text, m.keyword).split('\u0000')"
                    :key="pi"
                  >
                    <template v-if="part.includes('\u0001')">
                      <mark class="highlight-mark">{{ part.split('\u0001')[0] }}</mark>
                      <span>{{ part.split('\u0001')[1] }}</span>
                    </template>
                    <template v-else>{{ part }}</template>
                  </span>
                </span>
              </div>
              <div class="result-meta">
                <span>{{ item.assignedTo }}</span>
                <span class="meta-sep">·</span>
                <span>{{ item.deadline }}</span>
                <span class="meta-sep">·</span>
                <span>进度 {{ item.progress }}%</span>
              </div>
            </div>
          </template>

          <div class="no-results" v-else>未找到匹配的待办</div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 遮罩 */
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 12vh;
  outline: none;
}

/* 搜索面板 */
.search-panel {
  width: 640px;
  max-width: 90vw;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18);
  overflow: hidden;
  animation: slideDown 0.15s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 输入行 */
.search-input-row {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 10px;
}

.search-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  color: #333;
  background: transparent;
}

.search-input::placeholder {
  color: #bbb;
}

.shortcut-hint {
  font-size: 11px;
  color: #aaa;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 2px 6px;
  flex-shrink: 0;
}

/* 结果区 */
.search-results {
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 50vh;
  padding: 8px;
  border-top: 1px solid #f0f0f0;
}

.searching-hint {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 14px;
}

.results-count {
  padding: 4px 12px 8px;
  font-size: 12px;
  color: #999;
}

/* 结果项 */
.search-result-item {
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.12s;
}

.search-result-item:hover,
.search-result-item.active {
  background: #f0f5ff;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.result-id {
  font-size: 12px;
  color: #bbb;
  margin-left: auto;
}

.result-title {
  font-weight: 600;
  font-size: 15px;
  color: #222;
  margin-bottom: 6px;
}

.result-matches {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  display: flex;
  gap: 4px;
}

.match-field {
  color: #aaa;
  flex-shrink: 0;
  font-size: 12px;
}

.match-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #aaa;
  margin-top: 6px;
}

.meta-sep {
  color: #ddd;
}

/* 高亮 */
:deep(.highlight-mark) {
  background: #ff4d4f;
  color: #fff;
  padding: 1px 2px;
  border-radius: 2px;
}

/* 空状态：紧凑键位提示 */
.search-hint-bar {
  display: flex;
  justify-content: center;
  gap: 18px;
  padding: 14px 20px;
  font-size: 12px;
  color: #aaa;
  border-top: 1px solid #f5f5f5;
}

.search-hint-bar kbd {
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 3px;
  padding: 1px 5px;
  font-size: 11px;
}

/* 无结果 */
.no-results {
  text-align: center;
  padding: 32px;
  color: #999;
  font-size: 14px;
}

</style>
