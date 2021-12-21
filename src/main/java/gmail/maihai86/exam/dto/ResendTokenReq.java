package gmail.maihai86.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResendTokenReq {

    @NotEmpty(message = "Token cannot be empty")
    private String token;

}
