package com.sms.api.dto.school;

public record DeleteSchoolResponse(
    String schoolName,
    String deleteType,
    int usersAffected,
    int studentsAffected,
    int totalRecordsDeleted
) {}
