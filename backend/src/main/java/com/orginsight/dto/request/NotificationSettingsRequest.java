package com.orginsight.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean weeklyDigest;
}
