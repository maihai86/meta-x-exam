package gmail.maihai86.exam.handler;

import gmail.maihai86.exam.service.MessageService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("customAuthenticationFailureHandler")
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Resource
    private MessageService messageService;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception)
            throws IOException, ServletException {
        String msg = messageService.getMessage("message.badCredentials");
        if (exception instanceof DisabledException) {
            /*response.sendRedirect(String.format("/resend-email-verify?token=%s", request.getParameter("token")));
            return;*/
            msg = messageService.getMessage("message.user-not-activated");
        }
        setDefaultFailureUrl("/login?error=" + msg);
        super.onAuthenticationFailure(request, response, exception);
    }
}