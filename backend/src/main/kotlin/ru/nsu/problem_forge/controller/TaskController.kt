package ru.nsu.problem_forge.controller

import ru.nsu.problem_forge.dto.TaskDto
import ru.nsu.problem_forge.service.TaskService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService,
    private val userService: UserService
) {

    @GetMapping
    fun getUserTasks(@AuthenticationPrincipal userDetails: UserDetails): List<TaskDto> {
        val user = userService.findUserByLogin(userDetails.username)
        return taskService.getUserTasks(user.id!!)
    }

    @PostMapping
    fun createTask(
        @RequestBody taskDto: TaskDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): TaskDto {
        val user = userService.findUserByLogin(userDetails.username)
        return taskService.createTask(taskDto, user)
    }
}