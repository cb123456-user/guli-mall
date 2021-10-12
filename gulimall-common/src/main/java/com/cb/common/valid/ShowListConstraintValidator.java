package com.cb.common.valid;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 自定义注解校验器
 * @author chenbin
 */
public class ShowListConstraintValidator implements ConstraintValidator<ShowList, Integer> {
    Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ShowList constraintAnnotation) {
        Arrays.stream(constraintAnnotation.value()).filter(Objects::nonNull).forEach(it -> set.add(it));
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(value);
    }
}
