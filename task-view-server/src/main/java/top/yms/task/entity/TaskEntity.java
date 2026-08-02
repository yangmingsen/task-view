package top.yms.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 待办任务实体，对应 t_task 表
 */
@TableName("t_task")
public class TaskEntity extends BaseEntity {

    /** 标题 */
    private String title;

    /** 类型: story/bug/task */
    private String type;

    /** 优先级: 1紧急 2高 3中 4低 */
    private Integer priority;

    /** 状态: wait/doing/done/closed */
    private String status;

    /** 负责人 */
    private String assignedTo;

    /** 截止日期 */
    private Date deadline;

    /** 描述 (Markdown) */
    private String description;

    /** 所属项目 */
    private String project;

    /** 所属模块 */
    private String module;

    /** 进度 0-100 */
    private Integer progress;

    /** 创建人 */
    private String createdBy;

    /** 创建日期 */
    private Date createdDate;

    // ======================== getter / setter ========================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo == null ? null : assignedTo.trim();
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project == null ? null : project.trim();
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module == null ? null : module.trim();
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
