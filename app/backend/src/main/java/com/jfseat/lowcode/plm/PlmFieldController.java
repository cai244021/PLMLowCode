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
@RequestMapping("/api/plm-fields")
public class PlmFieldController {

    private final PlmFieldService service;

    public PlmFieldController(PlmFieldService service) {
        this.service = service;
    }

    /**
     * 查询PLM字段库
     **
     * @return PLM字段列表
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @GetMapping
    public List<PlmFieldResponse> findAll() {
        return service.findAll();
    }

    /**
     * 保存PLM字段定义
     **
     * @param fieldCode 字段编码
     * @param request 字段配置
     * @return 保存后的字段定义
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @PutMapping("/{fieldCode}")
    public PlmFieldResponse save(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String fieldCode,
            @Valid @RequestBody PlmFieldRequest request) {
        return service.save(fieldCode, request);
    }

    /**
     * 删除PLM字段定义
     **
     * @param fieldCode 字段编码
     * @return 空响应
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @DeleteMapping("/{fieldCode}")
    public ResponseEntity<Void> delete(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String fieldCode) {
        service.delete(fieldCode);
        return ResponseEntity.noContent().build();
    }
}
