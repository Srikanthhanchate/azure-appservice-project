package com.orginsight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTeamMemberResponse {
    private Long employeeId;
    private String fullName;
    private String designation;
    private String department;
    private String roleOnProject;
}
