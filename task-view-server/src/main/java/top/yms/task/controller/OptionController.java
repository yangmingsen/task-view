package top.yms.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.yms.task.common.R;
import top.yms.task.entity.OptionEntity;
import top.yms.task.mapper.OptionMapper;
import top.yms.task.util.IdWorker;

import javax.annotation.Resource;
import java.util.List;

/**
 * 下拉选项管理
 */
@RestController
@RequestMapping("/api/options")
public class OptionController {

    private static final Logger log = LoggerFactory.getLogger(OptionController.class);

    @Resource
    private OptionMapper optionMapper;

    @Resource
    private IdWorker idWorker;

    /**
     * 查询某个类型的选项列表（供前端下拉框使用）
     */
    @GetMapping
    public R list(@RequestParam String type,
                  @RequestParam(required = false) String parentName) {
        LambdaQueryWrapper<OptionEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(OptionEntity::getType, type);
        if (parentName != null && !parentName.isEmpty()) {
            qw.eq(OptionEntity::getParentName, parentName);
        }
        qw.orderByAsc(OptionEntity::getSortOrder);
        List<OptionEntity> list = optionMapper.selectList(qw);
        return R.ok(list);
    }

    /**
     * 查询全部选项（管理页面使用，可选 type 过滤）
     */
    @GetMapping("/all")
    public R all(@RequestParam(required = false) String type) {
        LambdaQueryWrapper<OptionEntity> qw = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            qw.eq(OptionEntity::getType, type);
        }
        qw.orderByAsc(OptionEntity::getSortOrder);
        List<OptionEntity> list = optionMapper.selectList(qw);
        return R.ok(list);
    }

    /**
     * 新增选项
     */
    @PostMapping
    public R create(@RequestBody OptionEntity entity) {
        entity.setId(String.valueOf(idWorker.nextId()));
        if (entity.getParentName() == null) {
            entity.setParentName("");
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        optionMapper.insert(entity);
        return R.ok(entity);
    }

    /**
     * 更新选项
     */
    @PutMapping("/{id}")
    public R update(@PathVariable String id, @RequestBody OptionEntity entity) {
        entity.setId(id);
        optionMapper.updateById(entity);
        return R.ok();
    }

    /**
     * 删除选项
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable String id) {
        optionMapper.deleteById(id);
        return R.ok();
    }
}
