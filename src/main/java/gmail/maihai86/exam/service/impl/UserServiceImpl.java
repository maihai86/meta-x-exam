package gmail.maihai86.exam.service.impl;

import gmail.maihai86.exam.dto.*;
import gmail.maihai86.exam.event.OnRegistrationCompleteEvent;
import gmail.maihai86.exam.exception.OAuth2AuthenticationProcessingException;
import gmail.maihai86.exam.exception.UserAlreadyExistAuthenticationException;
import gmail.maihai86.exam.model.Role;
import gmail.maihai86.exam.model.User;
import gmail.maihai86.exam.model.VerificationToken;
import gmail.maihai86.exam.oauth2.user.OAuth2UserInfo;
import gmail.maihai86.exam.oauth2.user.OAuth2UserInfoFactory;
import gmail.maihai86.exam.repository.RoleRepository;
import gmail.maihai86.exam.repository.UserRepository;
import gmail.maihai86.exam.repository.VerificationTokenRepository;
import gmail.maihai86.exam.service.UserService;
import gmail.maihai86.exam.util.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author maihai86@gmail.com
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    @Qualifier(value = "localUserDetailService")
    private UserDetailsService userDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";

    @Override
    @Transactional
    public User registerNewUser(final UserRegistrationForm userRegistrationForm, HttpServletRequest request, boolean enabled) throws UserAlreadyExistAuthenticationException {
        if (userRegistrationForm.getUserID() != null && userRepository.existsById(userRegistrationForm.getUserID())) {
            throw new UserAlreadyExistAuthenticationException("User with User id " + userRegistrationForm.getUserID() + " already exist");
        } else if (userRepository.existsByEmail(userRegistrationForm.getEmail())) {
            throw new UserAlreadyExistAuthenticationException("User with email id " + userRegistrationForm.getEmail() + " already exist");
        }
        User user = buildUser(userRegistrationForm, enabled);
        Date now = Calendar.getInstance().getTime();
        user.setCreatedDate(now);
        user.setModifiedDate(now);
        user = userRepository.save(user);
        userRepository.flush();

        // success, send email
        if (request != null) {
            String appUrl = request.getContextPath();

            final String strToken = UUID.randomUUID().toString();
            VerificationToken token = createVerificationTokenForUser(user, strToken);

            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl, token.getToken()));
        }

        return user;
    }

    private User buildUser(final UserRegistrationForm formDTO, boolean enabled) {
        User user = new User();
        user.setDisplayName(formDTO.getDisplayName());
        user.setEmail(formDTO.getEmail());
        user.setPassword(passwordEncoder.encode(formDTO.getPassword()));
        final HashSet<Role> roles = new HashSet<Role>();
        roles.add(roleRepository.findByName(Role.ROLE_USER));
        user.setRoles(roles);
        user.setProvider(formDTO.getSocialProvider().getProviderType());
        user.setEnabled(enabled);
        user.setProviderUserId(formDTO.getProviderUserId());
        return user;
    }

    @Override
    public User findUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public LocalUser processUserRegistration(String registrationId, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        if (StringUtils.isEmpty(oAuth2UserInfo.getName())) {
            throw new OAuth2AuthenticationProcessingException("Name not found from OAuth2 provider");
        } else if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }
        UserRegistrationForm userDetails = toUserRegistrationObject(registrationId, oAuth2UserInfo);
        User user = findUserByEmail(oAuth2UserInfo.getEmail());
        if (user != null) {
            if (!user.getProvider().equals(registrationId) && !user.getProvider().equals(SocialProvider.LOCAL.getProviderType())) {
                throw new OAuth2AuthenticationProcessingException(
                        "Looks like you're signed up with " + user.getProvider() + " account. Please use your " + user.getProvider() + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(userDetails, null, true);
        }

        return LocalUser.create(user, attributes, idToken, userInfo);
    }

    @Override
    @Transactional
    public String resetPassword(LocalUser principal, ResetPasswordReq resetPasswordReq) {
        User user = findUserByEmail(principal.getUser().getEmail());
        if (user == null) {
            throw new RuntimeException(String.format("User with email \"%s\" not found!", principal.getUser().getEmail()));
        }

        user.setPassword(passwordEncoder.encode(resetPasswordReq.getPassword()));
        userRepository.save(user);
        return "success";
    }

    @Override
    @Transactional
    public String resetDisplayName(LocalUser principal, ResetNameReq resetNameReq) {
        User user = findUserByEmail(principal.getUser().getEmail());
        if (user == null) {
            throw new RuntimeException(String.format("User with email \"%s\" not found!", principal.getUser().getEmail()));
        }

        user.setDisplayName(resetNameReq.getDisplayName());
        userRepository.save(user);

        reloadAuthentication(user);

        return "success";
    }

    @Override
    public boolean reloadAuthentication(User user) {
        // reload authen
        LocalUser newLocalUser = (LocalUser) userDetailService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(newLocalUser, newLocalUser.getPassword(), newLocalUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        return true;
    }

    @Override
    @Transactional
    public VerificationToken createVerificationTokenForUser(User user, String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        return tokenRepository.save(myToken);
    }

    @Override
    @Transactional
    public String validateVerificationToken(String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
                .getTime() - cal.getTime()
                .getTime()) <= 0) {
            tokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        // tokenRepository.delete(verificationToken);
        userRepository.save(user);
        return TOKEN_VALID;
    }

    @Override
    public User getUserByVerificationToken(String verificationToken) {
        final VerificationToken token = tokenRepository.findByToken(verificationToken);
        if (token != null) {
            return token.getUser();
        }
        return null;
    }

    @Override
    public String resendEmailVerification(HttpServletRequest request, String verificationToken) {
        final VerificationToken token = tokenRepository.findByToken(verificationToken);
        if (token == null) {
            throw new RuntimeException(String.format("Token \"%s\" not found!", token));
        }
        if (request != null) {
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(token.getUser(), request.getLocale(), appUrl, token.getToken()));
        }
        return "success";
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setDisplayName(oAuth2UserInfo.getName());
        return userRepository.save(existingUser);
    }

    private UserRegistrationForm toUserRegistrationObject(String registrationId, OAuth2UserInfo oAuth2UserInfo) {
        return UserRegistrationForm.getBuilder().providerUserId(oAuth2UserInfo.getId()).displayName(oAuth2UserInfo.getName()).email(oAuth2UserInfo.getEmail())
                .socialProvider(GeneralUtils.toSocialProvider(registrationId)).password("changeit").build();
    }
}
