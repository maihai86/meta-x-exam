package gmail.maihai86.exam.validator;

import gmail.maihai86.exam.dto.PasswordContainer;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordContainer> {

    @Override
    public boolean isValid(final PasswordContainer user, final ConstraintValidatorContext context) {
        return user.getPassword().equals(user.getMatchingPassword());
    }

}
