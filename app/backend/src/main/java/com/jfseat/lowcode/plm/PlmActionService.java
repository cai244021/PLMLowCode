package com.jfseat.lowcode.plm;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.List;
import java.util.Set;

@Service
public class PlmActionService {

    private final PlmActionRepository repository;

    public PlmActionService(PlmActionRepository repository) {
        this.repository = repository;
    }

    /**
     * 查询全部PLM动作定义
     **
     * @return 动作定义列表
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional(readOnly = true)
    public List<PlmActionResponse> findAll() {
        return repository.findAllByOrderByActionCodeAsc().stream()
                .map(PlmActionResponse::from)
                .toList();
    }

    /**
     * 按编码查询可发布的已启用PLM动作
     **
     * @param actionCodes 页面引用的动作编码
     * @return 按动作库顺序返回的已启用动作
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    @Transactional(readOnly = true)
    public List<PlmActionResponse> findEnabledByCodes(List<String> actionCodes) {
        Set<String> requestedCodes = Set.copyOf(actionCodes);
        return repository.findAllByOrderByActionCodeAsc().stream()
                .filter(action -> action.isEnabled() && requestedCodes.contains(action.getActionCode()))
                .map(PlmActionResponse::from)
                .toList();
    }

    /**
     * 新增或更新PLM动作定义
     **
     * @param actionCode 动作编码
     * @param request 动作配置
     * @return 保存后的动作定义
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional
    public PlmActionResponse save(String actionCode, PlmActionRequest request) {
        var inputMapping = request.inputMapping() == null
                ? JsonNodeFactory.instance.objectNode()
                : request.inputMapping();
        var outputMapping = request.outputMapping() == null
                ? JsonNodeFactory.instance.objectNode()
                : request.outputMapping();
        var action = repository.findByActionCode(actionCode)
                .orElseGet(() -> new PlmActionDefinition(
                        actionCode, request, inputMapping, outputMapping
                ));
        action.update(request, inputMapping, outputMapping);
        return PlmActionResponse.from(repository.save(action));
    }

    /**
     * 删除PLM动作定义
     **
     * @param actionCode 动作编码
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional
    public void delete(String actionCode) {
        repository.findByActionCode(actionCode).ifPresent(repository::delete);
    }
}
