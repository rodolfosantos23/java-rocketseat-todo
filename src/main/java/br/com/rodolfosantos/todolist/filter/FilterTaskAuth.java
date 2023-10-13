package br.com.rodolfosantos.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.rodolfosantos.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    private String username;
    private String password;
    private final String[] allowedPaths = new String[]{"/users/"};

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (Arrays.asList(this.allowedPaths).contains(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        this.setUsernameAndPassword(request.getHeader("Authorization"));
        this.validateUserOrSendError(response);

        filterChain.doFilter(request, response);
    }

    private void validateUserOrSendError(HttpServletResponse response) throws IOException {
        var user = this.userRepository.findByUsername(this.username);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        var passwordVerify = BCrypt.verifyer().verify(this.password.toCharArray(), user.getPassword().toCharArray());
        if (!passwordVerify.verified) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid password");
        }
    }

    private void setUsernameAndPassword(String auth) {
        if (auth == null || auth.isEmpty()) {
            return;
        }

        var authEncoded = auth.substring("Basic".length()).trim();
        if (authEncoded.isEmpty()) {
            return;
        }

        byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
        var authString = new String(authDecoded);
        String[] usernameAndPassword = authString.split(":");
        this.username = usernameAndPassword[0];
        this.password = usernameAndPassword[1];
    }
}


