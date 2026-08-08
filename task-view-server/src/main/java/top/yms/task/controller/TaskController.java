package top.yms.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.yms.task.common.R;
import top.yms.task.entity.TaskEntity;
import top.yms.task.mapper.TaskMapper;
import top.yms.task.service.TaskService;
import top.yms.task.util.IdWorker;

import javax.annotation.Resource;
import java.util.*;

/**
 * 待办任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    @Resource
    private IdWorker idWorker;

    // ======================== 分页列表 ========================

    @GetMapping
    public R<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String project,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        LambdaQueryWrapper<TaskEntity> qw = new LambdaQueryWrapper<>();

        // 关键字搜索（标题 + 描述）
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                    .like(TaskEntity::getTitle, keyword)
                    .or()
                    .like(TaskEntity::getDescription, keyword));
        }
        // 类型筛选
        if (StringUtils.hasText(type)) {
            qw.eq(TaskEntity::getType, type);
        }
        // 状态筛选
        if (StringUtils.hasText(status)) {
            qw.eq(TaskEntity::getStatus, status);
        }
        // 所属项目筛选
        if (StringUtils.hasText(project)) {
            qw.eq(TaskEntity::getProject, project);
        }
        // 所属模块筛选
        if (StringUtils.hasText(module)) {
            qw.eq(TaskEntity::getModule, module);
        }

        qw.orderByDesc(TaskEntity::getCreateTime);
        qw.select(TaskEntity.class, info -> {
            // 列表不返回 description 大字段，减少传输量
            return !"description".equals(info.getColumn());
        });

        Page<TaskEntity> pageParam = new Page<>(page, pageSize);
        IPage<TaskEntity> result = taskService.page(pageParam, qw);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        data.put("totalPages", result.getPages());

        return R.ok(data);
    }

    // ======================== 详情 ========================

    @GetMapping("/{id}")
    public R<TaskEntity> detail(@PathVariable String id) {
        TaskEntity task = taskService.getById(id);
        if (task == null) {
            return R.fail(404, "待办不存在");
        }
        return R.ok(task);
    }

    // ======================== 新建 ========================

    @PostMapping
    public R<Map<String, String>> create(@RequestBody TaskEntity task) {
        if (!StringUtils.hasText(task.getTitle())) {
            return R.fail(400, "标题不能为空");
        }
        String id = task.getId();
        if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
            task.setId(idWorker.nextId()+"");
        }
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        if (task.getStatus() == null) {
            task.setStatus("wait");
        }
        if (task.getProgress() == null) {
            task.setProgress(0);
        }
        if (task.getPriority() == null) {
            task.setPriority(4);
        }
        if (task.getType() == null) {
            task.setType("task");
        }
        if (task.getCreatedDate() == null) {
            task.setCreatedDate(new Date());
        }

        taskService.save(task);

        Map<String, String> result = new HashMap<>();
        result.put("id", task.getId());
        return R.ok(result);
    }

    // ======================== 更新 ========================

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable String id, @RequestBody TaskEntity task) {
        TaskEntity exist = taskService.getById(id);
        if (exist == null) {
            return R.fail(404, "待办不存在");
        }

        if (!StringUtils.hasText(task.getTitle())) {
            return R.fail(400, "标题不能为空");
        }

        task.setId(id);
        task.setUpdateTime(new Date());

        // createTime / createdBy 不允许被覆盖
        task.setCreateTime(null);
        task.setCreatedBy(null);

        taskService.updateById(task);
        return R.ok();
    }

    // ======================== 删除 ========================

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        boolean removed = taskService.removeById(id);
        if (!removed) {
            return R.fail(404, "待办不存在");
        }
        return R.ok();
    }

    // ======================== 全局搜索 ========================

    @GetMapping("/search")
    public R<List<Map<String, Object>>> search(@RequestParam String q) {
        if (!StringUtils.hasText(q)) {
            return R.ok(Collections.emptyList());
        }

        String keyword = q.trim();
        List<TaskEntity> matched = taskMapper.search(keyword);
        List<Map<String, Object>> results = new ArrayList<>();

        for (TaskEntity t : matched) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("title", t.getTitle());
            item.put("type", t.getType());
            item.put("status", t.getStatus());
            item.put("priority", t.getPriority());
            item.put("assignedTo", t.getAssignedTo());
            item.put("deadline", t.getDeadline());
            item.put("progress", t.getProgress());
            item.put("_keyword", keyword);

            List<Map<String, String>> matches = new ArrayList<>();
            String kw = keyword.toLowerCase();

            // 标题匹配
            if (t.getTitle() != null && t.getTitle().toLowerCase().contains(kw)) {
                Map<String, String> m = new HashMap<>();
                m.put("field", "title");
                m.put("text", t.getTitle());
                m.put("keyword", keyword);
                matches.add(m);
            }

            // 描述匹配
            if (t.getDescription() != null && t.getDescription().toLowerCase().contains(kw)) {
                String plainDesc = t.getDescription();
                int idx = plainDesc.toLowerCase().indexOf(kw);
                int start = Math.max(0, idx - 30);
                int end = Math.min(plainDesc.length(), idx + kw.length() + 50);
                String snippet = plainDesc.substring(start, end).replace("\n", " ");
                if (start > 0) snippet = "..." + snippet;
                if (end < plainDesc.length()) snippet += "...";

                Map<String, String> m = new HashMap<>();
                m.put("field", "desc");
                m.put("text", snippet);
                m.put("keyword", keyword);
                matches.add(m);
            }

            // 负责人匹配
            if (t.getAssignedTo() != null && t.getAssignedTo().toLowerCase().contains(kw)) {
                Map<String, String> m = new HashMap<>();
                m.put("field", "assignedTo");
                m.put("text", t.getAssignedTo());
                m.put("keyword", keyword);
                matches.add(m);
            }

            // 项目匹配
            if (t.getProject() != null && t.getProject().toLowerCase().contains(kw)) {
                Map<String, String> m = new HashMap<>();
                m.put("field", "project");
                m.put("text", t.getProject());
                m.put("keyword", keyword);
                matches.add(m);
            }

            if (!matches.isEmpty()) {
                item.put("matches", matches);
                results.add(item);
            }
        }

        return R.ok(results);
    }
}
