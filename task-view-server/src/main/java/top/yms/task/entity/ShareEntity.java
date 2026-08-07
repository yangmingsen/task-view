package top.yms.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 分享记录实体，对应 t_share 表
 */
@TableName("t_share")
public class ShareEntity extends BaseEntity {

    /** 关联任务ID */
    private String taskId;

    /** 访问令牌 (UUID) */
    private String token;

    /** 过期时间 */
    private Date expireTime;

    // ======================== getter / setter ========================

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
}
