package com.ryy.schedule.client;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.schedule.dtos.Task;
import com.ryy.schedule.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ScheduleClient{

    @Autowired
    private ITaskService taskService;

    /**
     * 添加任务
     * @param task 任务对象
     * @return 任务id
     */
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) {
        return ResponseResult.okResult(taskService.addTask(task));
    }

    /**
     * 取消任务
     * @param taskId 任务id
     * @return 取消结果
     */
//    @GetMapping("/api/v1/task/cancel/{taskId}")
//    @Override
//    public ResponseResult cancelTask(@PathVariable("taskId") long taskId) {
//        return ResponseResult.okResult(taskService.cancelTask(taskId));
//    }
//
//    /**
//     * 按照类型和优先级来拉取任务
//     * @param type
//     * @param priority
//     * @return
//     */
//    @GetMapping("/api/v1/task/poll/{type}/{priority}")
//    @Override
//    public ResponseResult poll(@PathVariable("type") int type, @PathVariable("priority") int priority) {
//        return ResponseResult.okResult(taskService.poll(type,priority));
//    }
}