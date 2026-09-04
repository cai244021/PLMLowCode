package com.jfseat.lowcode.page;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PageService {

    private final LowCodePageRepository pageRepository;
    private final LowCodePageVersionRepository versionRepository;

    public PageService(LowCodePageRepository pageRepository,
                       LowCodePageVersionRepository versionRepository) {
        this.pageRepository = pageRepository;
        this.versionRepository = versionRepository;
    }

    /**
     * 按页面编码查询当前页面
     **
     * @param pageCode 页面编码
     * @return 当前页面，不存在时返回空
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    @Transactional(readOnly = true)
    public Optional<PageResponse> findByPageCode(String pageCode) {
        //20260904 update by caipan 已删除页面不再提供读取
        return pageRepository.findByPageCode(pageCode).filter(page -> !page.isDeleted()).map(PageResponse::from);
    }

    /**
     * 保存页面并创建不可变历史版本
     **
     * @param pageCode 页面编码
     * @param request 页面保存请求
     * @return 保存后的页面
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    @Transactional
    public PageResponse save(String pageCode, PageSaveRequest request) {
        var plmConfig = request.plmConfig() == null
                ? JsonNodeFactory.instance.objectNode()
                : request.plmConfig();
        //20260904 update by caipan 保存与删除串行处理，拒绝旧窗口写回已删除页面
        LowCodePage page = pageRepository.findForUpdate(pageCode)
                .orElseGet(() -> new LowCodePage(
                        pageCode, request.pageName(), request.schema(), plmConfig
                ));
        if (page.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.GONE, "页面已删除，请返回页面管理");
        }
        page.update(request.pageName().trim(), request.schema(), plmConfig);
        LowCodePage saved = pageRepository.save(page);
        versionRepository.save(new LowCodePageVersion(
                saved.getId(), saved.getCurrentVersion(), saved.getSchemaJson(),
                saved.getPlmConfigJson()
        ));
        return PageResponse.from(saved);
    }

    /**
     * 列出页面摘要，不返回页面JSON和绑定配置
     **
     * @return 按更新时间倒序排列的页面摘要
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @Transactional(readOnly = true)
    public List<PageSummary> listPages() {
        //20260904 update by caipan 页面列表排除软删除记录
        return pageRepository.findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(page -> new PageSummary(page.getPageCode(), page.getPageName(),
                        page.getCurrentVersion(), page.getUpdatedAt()))
                .toList();
    }

    /**
     * 新建独立页面，禁止覆盖已有编码
     **
     * @param pageCode 唯一页面编码
     * @param request 页面内容及绑定
     * @return 初始版本页面
     * @throws ResponseStatusException 编码已存在时返回409
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @Transactional
    public PageResponse create(String pageCode, PageSaveRequest request) {
        if (pageRepository.findByPageCode(pageCode).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "页面编码已存在，请使用其他编码");
        }
        var config = request.plmConfig() == null ? JsonNodeFactory.instance.objectNode() : request.plmConfig();
        LowCodePage page = new LowCodePage(pageCode, request.pageName().trim(), request.schema(), config);
        page.update(request.pageName().trim(), request.schema(), config);
        LowCodePage saved = pageRepository.saveAndFlush(page);
        versionRepository.save(new LowCodePageVersion(saved.getId(), saved.getCurrentVersion(),
                saved.getSchemaJson(), saved.getPlmConfigJson()));
        return PageResponse.from(saved);
    }

    /**
     * 软删除页面，重复删除不改变内容和版本
     **
     * @param pageCode 页面编码
     * @throws ResponseStatusException 页面不存在时返回404
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    @Transactional
    public void delete(String pageCode) {
        LowCodePage page = pageRepository.findForUpdate(pageCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "页面不存在"));
        if (!page.isDeleted()) {
            page.markDeleted();
            pageRepository.save(page);
        }
    }
}
