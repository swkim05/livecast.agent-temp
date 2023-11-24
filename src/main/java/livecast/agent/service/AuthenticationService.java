package livecast.agent.service;

import livecast.agent.model.BaseSimpleUser;
import livecast.agent.model.BaseUserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class AuthenticationService {
    private AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public BaseUserToken getToken(String id, String rawPassword, HttpSession session) {
        final Authentication request = new UsernamePasswordAuthenticationToken(id, rawPassword);
        final Authentication result = authenticationManager.authenticate(request);

        if ((result != null) && (result.isAuthenticated())) {
            SecurityContextHolder.getContext().setAuthentication(result);
        } else {
            return null;
        }

        return BaseUserToken.builder()
                .token(session.getId())
                .user((BaseSimpleUser) result.getDetails())
                .build();
    }

    public BaseSimpleUser getUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return (BaseSimpleUser) authentication.getDetails();
    }


}
