package ru.nsu.problem_forge.controller

import ru.nsu.problem_forge.dto.TaskDto
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.service.TaskService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    fun getUserTasks(@AuthenticationPrincipal user: User): List<TaskDto> {
        return taskService.getUserTasks(user.id!!)
    }

    @PostMapping
    fun createTask(
        @RequestBody taskDto: TaskDto,
        @AuthenticationPrincipal user: User
    ): TaskDto {
        return taskService.createTask(taskDto, user)
    }
}