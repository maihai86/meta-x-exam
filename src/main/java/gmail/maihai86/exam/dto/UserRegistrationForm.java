package gmail.maihai86.exam.dto;

import gmail.maihai86.exam.validator.PasswordMatches;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author maihai86@gmail.com
 */
@PasswordMatches
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "getBuilder")
public class UserRegistrationForm implements PasswordContainer {

    private Long userID;

    private String providerUserId;

    @NotEmpty(message = "{NotEmpty.display-name.notnull}")
    private String displayName;

    @NotEmpty(message = "Email address cannot be empty")
    private String email;

    private SocialProvider socialProvider = SocialProvider.LOCAL;

    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @NotEmpty(message = "Confirm password cannot be empty")
    private String matchingPassword;
}
