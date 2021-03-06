package com.studyolle.settings.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@Data
public class NicknameForm {

    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[0-9a-zA-Zㄱ-ㅎ가-힣_-]{3,20}$")
    private String nickname;
}
