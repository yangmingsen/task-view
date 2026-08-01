import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/global.css'

// 引入 v-md-editor
import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'

// 引入预览组件
import VMdPreview from '@kangc/v-md-editor/lib/preview'
import '@kangc/v-md-editor/lib/style/preview.css'

// 引入代码高亮语言包
import Prism from 'prismjs'
import 'prismjs/components/prism-javascript'
import 'prismjs/components/prism-css'
import 'prismjs/components/prism-json'
import 'prismjs/components/prism-bash'
import 'prismjs/components/prism-sql'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-java'
import 'prismjs/components/prism-typescript'

VMdEditor.use(githubTheme, { Prism })
VMdPreview.use(githubTheme, { Prism })

const app = createApp(App)
app.use(router)
app.use(VMdEditor)
app.use(VMdPreview)
app.mount('#app')
