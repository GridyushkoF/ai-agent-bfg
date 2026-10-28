package com.aichat.aichatdemo2.feature.user.dto.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 3, max = 64, message = "длина должна быть от {min} до {max}")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "может содержать только латиницу, цифры, точку, подчёркивание и дефис")
    private String login;

    @NotBlank(message = "не должно быть пустым")
    @Email(message = "должно быть корректным адресом электронной почты")
    @Size(max = 255, message = "длина должна быть не больше {max}")
    private String email;

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 8, max = 72, message = "длина должна быть от {min} до {max}")
    private String password;
}
