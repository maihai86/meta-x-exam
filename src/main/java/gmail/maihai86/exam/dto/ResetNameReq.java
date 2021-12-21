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
public class ResetNameReq {

    @NotEmpty(message = "{NotEmpty.display-name.notnull}")
    private String displayName;

}
