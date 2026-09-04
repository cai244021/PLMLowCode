package com.jfseat.lowcode.plm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/plm-actions")
public class PlmActionController {

    private final PlmActionService service;

    public PlmActionController(PlmActionService service) {
        this.service = service;
    }

    /**
     * 查询PLM动作库
     **
     * @return PLM动作列表
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @GetMapping
    public List<PlmActionResponse> findAll() {
        return service.findAll();
    }

    /**
     * 保存PLM动作定义
     **
     * @param actionCode 动作编码
     * @param request 动作配置
     * @return 保存后的动作定义
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @PutMapping("/{actionCode}")
    public PlmActionResponse save(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String actionCode,
            @Valid @RequestBody PlmActionRequest request) {
        return service.save(actionCode, request);
    }

    /**
     * 删除PLM动作定义
     **
     * @param actionCode 动作编码
     * @return 空响应
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @DeleteMapping("/{actionCode}")
    public ResponseEntity<Void> delete(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String actionCode) {
        service.delete(actionCode);
        return ResponseEntity.noContent().build();
    }
}
