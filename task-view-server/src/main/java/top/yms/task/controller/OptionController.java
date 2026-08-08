package top.yms.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yms.task.common.R;
import top.yms.task.entity.OptionEntity;
import top.yms.task.mapper.OptionMapper;

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

    /**
     * 查询某个类型的选项列表
     * @param type  选项类型: project / module
     * @param parentName 父级项目名（查询模块时使用，可选）
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
}
