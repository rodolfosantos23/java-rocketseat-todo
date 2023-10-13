package br.com.rodolfosantos.todolist.task;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
}
