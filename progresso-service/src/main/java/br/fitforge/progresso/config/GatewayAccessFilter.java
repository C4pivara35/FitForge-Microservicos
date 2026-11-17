package br.fitforge.progresso.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class GatewayAccessFilter extends OncePerRequestFilter {
    private final byte[] secret;

    public GatewayAccessFilter(@Value("${app.internal-key:}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getServletPath();
        boolean protectedRoute = path.startsWith("/api/internal/")
                || request.getHeader("X-FitForge-Owner") != null;
        if (!path.startsWith("/api/") || (secret.length == 0 && !protectedRoute)) {
            chain.doFilter(request, response);
            return;
        }

        String supplied = request.getHeader("X-FitForge-Internal-Key");
        if (secret.length > 0 && supplied != null && MessageDigest.isEqual(secret, supplied.getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
