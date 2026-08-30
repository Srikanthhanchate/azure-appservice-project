package com.orginsight.dto.request;

import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThemeSettingsRequest {

    @Pattern(regexp = "light|dark|system", message = "Theme must be light, dark or system")
    private String theme;
}
