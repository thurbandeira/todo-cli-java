package com.thurbandeira.todocli.api.application.task.support;

import com.thurbandeira.todocli.api.exception.ValidationException;

public final class TaskTitleValidator {

    private static final int MAX_TITLE = 200;

    private TaskTitleValidator() {
    }

    public static String normalize(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Titulo invalido.");
        }
        String normalized = title.trim();
        if (normalized.length() > MAX_TITLE) {
            throw new ValidationException("Titulo deve ter no maximo 200 caracteres.");
        }
        return normalized;
    }
}
