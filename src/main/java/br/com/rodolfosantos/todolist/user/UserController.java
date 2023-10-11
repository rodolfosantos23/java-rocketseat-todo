package br.com.rodolfosantos.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public UserModel create(@RequestBody UserModel userModel) throws Exception {

        var user = this.userRepository.findByUsername(userModel.getUsername());
        if (user != null) {
            throw new Exception("User already exists");
        }

        return this.userRepository.save(userModel);
    }
}
