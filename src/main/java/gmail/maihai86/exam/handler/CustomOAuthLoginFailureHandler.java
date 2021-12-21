package gmail.maihai86.exam.handler;

import gmail.maihai86.exam.exception.OAuth2AuthenticationProcessingException;
import gmail.maihai86.exam.service.MessageService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("authenticationFailureHandler")
public class CustomOAuthLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Resource
    private MessageService messageService;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception)
            throws IOException, ServletException {
        String msg = messageService.getMessage("message.badCredentials");
        if (exception instanceof OAuth2AuthenticationProcessingException) {
            msg = exception.getMessage() == null ? "OAuth error occurred" : exception.getMessage();
        }
        setDefaultFailureUrl("/login?error=" + msg);
        super.onAuthenticationFailure(request, response, exception);
    }
}