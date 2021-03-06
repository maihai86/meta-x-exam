package gmail.maihai86.exam.event;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gmail.maihai86.exam.model.User;
import gmail.maihai86.exam.service.MessageService;
import gmail.maihai86.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Value("${support.email}")
    private String supportEmail;

    @Value("${support.host}")
    private String supportHost;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) {
        final SimpleMailMessage email = constructEmailMessage(event, event.getUser(), event.getToken());

        try {
            HttpResponse<String> response = Unirest.post("https://api.mailgun.net/v3/" + System.getenv("MAILGUN_DOMAIN") + "/messages")
                    .basicAuth("api", System.getenv("MAILGUN_API_KEY"))
                    .queryString("from", supportEmail)
                    .queryString("to", email.getTo()[0])
                    .queryString("subject", email.getSubject())
                    .queryString("text", email.getText())
                    .asString();
            log.info("send email result: {}, {}", response.getStatus(), response.getBody().toString());
            if (response.getStatus() != 200) {
                throw new RuntimeException(String.format("Send activation email to this address \"%s\" failed!", email.getTo()[0]));
            }
        } catch (UnirestException e) {
            log.error("confirmRegistration ERROR", e);
            throw new RuntimeException(e);
        }
    }

    private SimpleMailMessage constructEmailMessage(final OnRegistrationCompleteEvent event, final User user, final String token) {
        final String recipientAddress = user.getEmail();
        final String subject = "Registration Confirmation";
        final String confirmationUrl = supportHost + "/registration-confirm?token=" + token;
        final String message = messageService.getMessage("message.regSucc.link");
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + " \r\n" + confirmationUrl);
        email.setFrom(supportEmail);
        return email;
    }
}
