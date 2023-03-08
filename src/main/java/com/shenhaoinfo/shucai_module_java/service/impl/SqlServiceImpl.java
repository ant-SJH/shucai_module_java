package com.shenhaoinfo.shucai_module_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shenhao.robot.po.Task;
import com.shenhaoinfo.shucai_module_java.mapper.TaskMapper;
import com.shenhaoinfo.shucai_module_java.service.SqlService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinhang
 * @date 2023/3/8
 */
@Service
public class SqlServiceImpl implements SqlService {
    @Resource
    private TaskMapper taskMapper;

    @Override
    public String getTaskNameByTaskId(String taskId) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getTaskid, taskId);
        queryWrapper.last("limit 1");
        Task task = taskMapper.selectOne(queryWrapper);
        return task == null ? null : task.getTaskname();
    }
}
