import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/global.css'

/* ========== Markdown 编辑器 ========== */
import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'

import VMdPreview from '@kangc/v-md-editor/lib/preview'
import '@kangc/v-md-editor/lib/style/preview.css'

/* ========== GitHub 主题 (基于 highlight.js) ========== */
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'

/* ========== highlight.js 语言包 ========== */
import Hljs from 'highlight.js/lib/core'
import 'highlight.js/lib/languages/xml'
import 'highlight.js/lib/languages/javascript'
import 'highlight.js/lib/languages/css'
import 'highlight.js/lib/languages/json'
import 'highlight.js/lib/languages/bash'
import 'highlight.js/lib/languages/sql'
import 'highlight.js/lib/languages/python'
import 'highlight.js/lib/languages/java'
import 'highlight.js/lib/languages/typescript'
import 'highlight.js/lib/languages/yaml'
import 'highlight.js/lib/languages/markdown'

VMdEditor.use(githubTheme, { Hljs })
VMdPreview.use(githubTheme, { Hljs })

/* ========== 启动应用 ========== */
const app = createApp(App)
app.use(router)
app.use(VMdEditor)
app.use(VMdPreview)
app.mount('#app')
