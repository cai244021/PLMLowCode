package com.jfseat.lowcode.plm;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlmFieldService {

    private final PlmFieldRepository repository;

    public PlmFieldService(PlmFieldRepository repository) {
        this.repository = repository;
    }

    /**
     * 查询全部PLM字段定义
     **
     * @return 字段定义列表
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional(readOnly = true)
    public List<PlmFieldResponse> findAll() {
        return repository.findAllByOrderByFieldCodeAsc().stream()
                .map(PlmFieldResponse::from)
                .toList();
    }

    /**
     * 按页面引用的字段编码查询发布所需的PLM字段快照
     **
     * @param fieldCodes 页面引用的字段编码
     * @return 页面发布所需的字段定义列表
     * @author caipan by codex
     * @date 2026/9/6 16:30
     */
    @Transactional(readOnly = true)
    public List<PlmFieldResponse> findByCodes(List<String> fieldCodes) {
        Set<String> codes = fieldCodes == null ? Set.of() : fieldCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toSet());
        return findAll().stream()
                .filter(field -> codes.contains(field.fieldCode()))
                .toList();
    }

    /**
     * 新增或更新PLM字段定义
     **
     * @param fieldCode 字段编码
     * @param request 字段配置
     * @return 保存后的字段定义
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional
    public PlmFieldResponse save(String fieldCode, PlmFieldRequest request) {
        var rangeConfig = request.rangeConfig() == null
                ? JsonNodeFactory.instance.objectNode()
                : request.rangeConfig();
        var field = repository.findByFieldCode(fieldCode)
                .orElseGet(() -> new PlmFieldDefinition(fieldCode, request, rangeConfig));
        field.update(request, rangeConfig);
        return PlmFieldResponse.from(repository.save(field));
    }

    /**
     * 删除PLM字段定义
     **
     * @param fieldCode 字段编码
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional
    public void delete(String fieldCode) {
        repository.findByFieldCode(fieldCode).ifPresent(repository::delete);
    }
}
