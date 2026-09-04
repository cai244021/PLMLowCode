package com.jfseat.lowcode.page;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface LowCodePageRepository extends JpaRepository<LowCodePage, UUID> {

    Optional<LowCodePage> findByPageCode(String pageCode);

    /**
     * 查询未删除的页面
     **
     * @param sort 排序条件
     * @return 未删除页面列表
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    List<LowCodePage> findByDeletedAtIsNull(Sort sort);

    /**
     * 锁定页面，避免保存与删除同时执行导致已删除页面被覆盖
     **
     * @param pageCode 页面编码
     * @return 页面记录
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from LowCodePage p where p.pageCode = :pageCode")
    Optional<LowCodePage> findForUpdate(@Param("pageCode") String pageCode);
}
