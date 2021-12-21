package gmail.maihai86.exam.dto;

import gmail.maihai86.exam.validator.PasswordMatches;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@PasswordMatches
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordReq implements PasswordContainer {

    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @NotEmpty(message = "Confirm password cannot be empty")
    private String matchingPassword;

}
