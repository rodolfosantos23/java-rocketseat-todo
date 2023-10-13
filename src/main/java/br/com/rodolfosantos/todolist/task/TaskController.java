package br.com.rodolfosantos.todolist.task;

import br.com.rodolfosantos.todolist.Utils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setUserId((UUID) request.getAttribute("userId"));

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest().body("Start/End date must be greater than current date");
        }

        if (taskModel.getEndAt().isBefore(taskModel.getStartAt())) {
            return ResponseEntity.badRequest().body("End date must be greater or equal than start date");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpServletResponse.SC_CREATED).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request, HttpServletResponse response) {
        return this.taskRepository.findByUserId((UUID) request.getAttribute("userId"));
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = this.taskRepository.findById(id);
        if (task.isEmpty()) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body("Task not found");
        }

        if (!task.get().getUserId().equals(request.getAttribute("userId"))) {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).body("You don't have permission to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task.get());
        this.taskRepository.save(task.get());
        return ResponseEntity.status(200).body(task);
    }
}
