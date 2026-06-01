package com.sms.api.service;

import com.sms.api.dto.feature.FeatureFlagDto;
import com.sms.api.dto.feature.UpdateFeatureFlagsRequest;
import com.sms.api.entity.FeatureFlag;
import com.sms.api.entity.School;
import com.sms.api.repository.FeatureFlagRepository;
import com.sms.api.repository.SchoolRepository;
import com.sms.core.enums.FeatureKey;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final SchoolRepository schoolRepository;

    public FeatureFlagService(FeatureFlagRepository featureFlagRepository, SchoolRepository schoolRepository) {
        this.featureFlagRepository = featureFlagRepository;
        this.schoolRepository      = schoolRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, FeatureFlagDto> getSchoolFlags(UUID schoolId) {
        List<FeatureFlag> flags = featureFlagRepository.findAllBySchoolId(schoolId);
        Map<FeatureKey, FeatureFlag> existing = flags.stream()
            .collect(Collectors.toMap(FeatureFlag::getFeatureKey, Function.identity()));

        return Arrays.stream(FeatureKey.values())
            .collect(Collectors.toMap(
                Enum::name,
                key -> {
                    FeatureFlag ff = existing.get(key);
                    return new FeatureFlagDto(
                        key,
                        ff != null && ff.isEnabled(),
                        ff != null ? ff.getConfig() : null,
                        ff != null ? ff.getUpdatedAt() : null
                    );
                }
            ));
    }

    public void updateSchoolFlags(UUID schoolId, UpdateFeatureFlagsRequest request, UUID updatedBy) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        request.flags().forEach((featureKey, enabled) -> {
            FeatureFlag ff = featureFlagRepository
                .findBySchoolIdAndFeatureKey(schoolId, featureKey)
                .orElseGet(() -> {
                    FeatureFlag newFf = new FeatureFlag();
                    newFf.setSchool(school);
                    newFf.setFeatureKey(featureKey);
                    return newFf;
                });
            ff.setEnabled(enabled);
            ff.setEnabledBy(updatedBy);
            featureFlagRepository.save(ff);
        });
    }
}
