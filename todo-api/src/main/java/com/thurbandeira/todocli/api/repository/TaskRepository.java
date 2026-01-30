package com.thurbandeira.todocli.api.repository;

import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByOwnerOrderByCompletedAscIdAsc(UserAccount owner);
    List<TaskEntity> findAllByOwnerAndCompletedOrderByIdAsc(UserAccount owner, boolean completed);
    List<TaskEntity> findAllByOwnerAndTitleContainingIgnoreCaseOrderByIdAsc(UserAccount owner, String keyword);
    Page<TaskEntity> findAllByOwner(UserAccount owner, Pageable pageable);
    Page<TaskEntity> findAllByOwnerAndCompleted(UserAccount owner, boolean completed, Pageable pageable);
    Page<TaskEntity> findAllByOwnerAndTitleContainingIgnoreCase(UserAccount owner, String keyword, Pageable pageable);
    Optional<TaskEntity> findByIdAndOwner(Long id, UserAccount owner);
    long deleteByOwnerAndCompleted(UserAccount owner, boolean completed);
    long countByOwnerAndCompleted(UserAccount owner, boolean completed);
    long countByOwner(UserAccount owner);
}
