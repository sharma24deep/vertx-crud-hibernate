package xyz.mednikov.sandbox.hibernate.model;

import java.time.LocalDateTime;
import java.util.Optional;

public record TaskDTO(Integer id, Integer userId, String content, boolean completed, LocalDateTime createdAt, Optional<ProjectDTO> project) {
}
