package com.dongnguyen248.add2num.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdditionRequest(
        @NotBlank(message = "The first number is required.")
        @Pattern(regexp = "\\d+", message = "The first number must contain only decimal digits.")
        String firstNumber,
        @NotBlank(message = "The second number is required.")
        @Pattern(regexp = "\\d+", message = "The second number must contain only decimal digits.")
        String secondNumber) {
}