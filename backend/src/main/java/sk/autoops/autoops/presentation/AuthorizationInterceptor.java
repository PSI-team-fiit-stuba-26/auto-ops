package sk.autoops.autoops.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import sk.autoops.autoops.application.UserService;
import sk.autoops.autoops.domain.User;
import sk.autoops.autoops.domain.enums.UserRole;

import java.util.Arrays;
import java.util.UUID;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public AuthorizationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole == null) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);
        try {
            UUID userId = UUID.fromString(token);
            User user = userService.findById(userId).orElse(null);
            
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            boolean hasRole = Arrays.asList(requireRole.value()).contains(user.role);
            if (!hasRole) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            request.setAttribute("user", user);
            return true;
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
