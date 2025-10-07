package ru.nsu.problem_forge.service

import ru.nsu.problem_forge.dto.TaskDto
import ru.nsu.problem_forge.entity.Task
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.repository.TaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val taskRepository: TaskRepository
) {
    
    fun getUserTasks(userId: Long): List<TaskDto> {
        return taskRepository.findByUserId(userId).map { it.toDto() }
    }
    
    fun createTask(taskDto: TaskDto, user: User): TaskDto {
        val task = Task(
            title = taskDto.title,
            description = taskDto.description,
            user = user
        )
        return taskRepository.save(task).toDto()
    }
    
    private fun Task.toDto() = TaskDto(
        id = id,
        title = title,
        description = description
    )
}