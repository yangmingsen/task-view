<template>
  <div class="admin-container">
    <!-- 左侧菜单 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <router-link to="/" class="back-link">← 返回首页</router-link>
      </div>
      <nav class="sidebar-nav">
        <router-link
          to="/admin/options"
          class="nav-item"
          :class="{ active: $route.path === '/admin/options' }"
        >
          <span class="nav-icon">📋</span>
          <span class="nav-label">选项管理</span>
        </router-link>
        <!-- 未来新增菜单项在这里添加 -->
      </nav>
    </aside>

    <!-- 右侧内容区 -->
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-container {
  display: flex;
  height: 100vh;
  background: #f5f6f8;
}

/* ===== 左侧菜单 ===== */
.admin-sidebar {
  width: 220px;
  min-width: 220px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.back-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color .2s;
}
.back-link:hover {
  color: #1890ff;
}

.sidebar-nav {
  flex: 1;
  padding: 8px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  transition: all .2s;
  border-left: 3px solid transparent;
}
.nav-item:hover {
  background: #f0f5ff;
  color: #1890ff;
}
.nav-item.active {
  background: #e6f4ff;
  color: #1890ff;
  border-left-color: #1890ff;
  font-weight: 500;
}
.nav-icon {
  font-size: 16px;
}
.nav-label {
  line-height: 1;
}

/* ===== 右侧内容区 ===== */
.admin-main {
  flex: 1;
  overflow: auto;
  padding: 24px;
}
</style>
