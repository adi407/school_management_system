package com.sms.api.dto.academic;

import java.util.UUID;

public record ClassSubjectDto(
    UUID   id,
    UUID   classId,
    UUID   subjectId,
    String subjectName,
    String subjectCode,
    String subjectType,
    Integer subjectCreditHours,
    UUID   teacherId,
    String teacherName,
    String teacherEmail
) {}
