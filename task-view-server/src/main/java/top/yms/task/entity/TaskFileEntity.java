package top.yms.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 任务附件实体，对应 t_task_file 表
 */
@TableName("t_task_file")
public class TaskFileEntity extends BaseEntity {

    /** 关联任务ID */
    private String taskId;

    /** 原始文件名 */
    private String fileName;

    /** 存储后的文件名 (UUID) */
    private String storedName;

    /** 文件相对路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME 类型 */
    private String fileType;

    /** 上传人 */
    private String createdBy;

    // ======================== getter / setter ========================

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
