package com.archcore.core.repository;

import com.archcore.core.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<AuditLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String userId, Instant startDate, Instant endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.endpoint LIKE %:endpointPattern% ORDER BY a.createdAt DESC")
    Page<AuditLog> findByEndpointPattern(@Param("endpointPattern") String endpointPattern, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.httpStatusCode >= :minStatus AND a.httpStatusCode < :maxStatus ORDER BY a.createdAt DESC")
    Page<AuditLog> findByStatusCodeRange(@Param("minStatus") int minStatus, @Param("maxStatus") int maxStatus, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.createdAt >= :since")
    long countByUserIdSince(@Param("userId") String userId, @Param("since") Instant since);
}
