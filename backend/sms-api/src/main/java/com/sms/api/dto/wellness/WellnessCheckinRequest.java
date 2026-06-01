package com.sms.api.dto.wellness;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record WellnessCheckinRequest(
    @NotNull  UUID   classId,
    @NotBlank
    @Pattern(regexp = "GREAT|GOOD|OKAY|SAD|STRESSED", message = "mood must be GREAT, GOOD, OKAY, SAD or STRESSED")
    String mood,
    String note,         // optional anonymous note (max 200 chars)
    boolean anonymous    // if false, student_id is stored (opt-in tracking)
) {}
