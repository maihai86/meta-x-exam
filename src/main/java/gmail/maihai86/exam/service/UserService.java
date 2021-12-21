package gmail.maihai86.exam.service;

import gmail.maihai86.exam.dto.LocalUser;
import gmail.maihai86.exam.dto.ResetNameReq;
import gmail.maihai86.exam.dto.ResetPasswordReq;
import gmail.maihai86.exam.dto.UserRegistrationForm;
import gmail.maihai86.exam.exception.UserAlreadyExistAuthenticationException;
import gmail.maihai86.exam.model.User;
import gmail.maihai86.exam.model.VerificationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author maihai86@gmail.com
 */
public interface UserService {

    User registerNewUser(UserRegistrationForm userRegistrationForm, HttpServletRequest request, boolean enabled) throws UserAlreadyExistAuthenticationException;

    User findUserByEmail(String email);

    LocalUser processUserRegistration(String registrationId, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo);

    String resetPassword(LocalUser principal, ResetPasswordReq resetPasswordReq);

    String resetDisplayName(LocalUser principal, ResetNameReq resetNameReq);

    boolean reloadAuthentication(User user);

    VerificationToken createVerificationTokenForUser(User user, String token);

    String validateVerificationToken(String token);

    User getUserByVerificationToken(String verificationToken);

    String resendEmailVerification(HttpServletRequest request, String token);
}
