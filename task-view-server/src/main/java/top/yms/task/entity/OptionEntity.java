package top.yms.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 下拉选项实体，对应 t_option 表
 */
@TableName("t_option")
public class OptionEntity extends BaseEntity {

    /** 类型: project / module */
    private String type;

    /** 选项名称 */
    private String name;

    /** 所属项目名（仅 module 类型使用，project 类型为 NULL） */
    @TableField("parent_name")
    private String parentName;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    // ======================== getter / setter ========================

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName == null ? null : parentName.trim();
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
