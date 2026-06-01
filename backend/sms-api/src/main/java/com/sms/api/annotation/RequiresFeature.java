package com.sms.api.annotation;

import com.sms.core.enums.FeatureKey;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresFeature {
    FeatureKey value();
}
