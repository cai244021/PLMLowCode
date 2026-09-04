package com.jfseat.lowcode.page;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PageServiceTest {
    /**
     * 验证软删除隐藏页面、保留版本，并阻止旧窗口继续保存
     **
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    @Test
    void deletePreservesDataAndRejectsStaleSave() {
        var pages = mock(LowCodePageRepository.class);
        var versions = mock(LowCodePageVersionRepository.class);
        var json = JsonNodeFactory.instance.objectNode().put("type", "page");
        var page = new LowCodePage("DELETE_TEST", "测试", json, json);
        page.update("测试", json, json);
        when(pages.findForUpdate("DELETE_TEST")).thenReturn(Optional.of(page));
        when(pages.findByPageCode("DELETE_TEST")).thenReturn(Optional.of(page));
        var service = new PageService(pages, versions);
        service.delete("DELETE_TEST");
        service.delete("DELETE_TEST");
        assertTrue(page.isDeleted());
        assertEquals(1, page.getCurrentVersion());
        assertEquals(json, page.getSchemaJson());
        assertTrue(service.findByPageCode("DELETE_TEST").isEmpty());
        var error = assertThrows(ResponseStatusException.class, () -> service.save("DELETE_TEST",
                new PageSaveRequest("旧窗口", json, json)));
        assertEquals(410, error.getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.create("DELETE_TEST",
                new PageSaveRequest("复用编码", json, json))).getStatusCode().value());
        verify(pages, times(1)).save(page);
        verifyNoInteractions(versions);
    }

    /**
     * 验证删除不存在页面时不写入数据
     **
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    @Test
    void missingDeleteReturnsNotFound() {
        var pages = mock(LowCodePageRepository.class);
        var versions = mock(LowCodePageVersionRepository.class);
        when(pages.findForUpdate("MISSING")).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> new PageService(pages, versions).delete("MISSING")).getStatusCode().value());
        verify(pages, never()).save(any());
        verifyNoInteractions(versions);
    }

    /**
     * 验证新建页面版本与绑定保存
     **
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @Test
    void createStartsAtVersionOneAndPreservesBindings() {
        var pages = mock(LowCodePageRepository.class);
        var versions = mock(LowCodePageVersionRepository.class);
        when(pages.findByPageCode("NEW_PAGE")).thenReturn(Optional.empty());
        when(pages.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var schema = JsonNodeFactory.instance.objectNode().put("type", "page");
        var config = JsonNodeFactory.instance.objectNode().put("testBinding", "TITLE");
        var result = new PageService(pages, versions).create("NEW_PAGE", new PageSaveRequest(" 新页面 ", schema, config));
        assertEquals("NEW_PAGE", result.pageCode());
        assertEquals("新页面", result.pageName());
        assertEquals(1, result.currentVersion());
        assertEquals(schema, result.schema());
        assertEquals(config, result.plmConfig());
        verify(versions).save(any());
    }

    /**
     * 验证重复编码不会覆盖原页面或新增版本
     **
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @Test
    void duplicateCreateDoesNotWrite() {
        var pages = mock(LowCodePageRepository.class);
        var versions = mock(LowCodePageVersionRepository.class);
        var json = JsonNodeFactory.instance.objectNode();
        var original = new LowCodePage("EXISTING", "原页面", json, json);
        when(pages.findByPageCode("EXISTING")).thenReturn(Optional.of(original));
        var error = assertThrows(ResponseStatusException.class, () -> new PageService(pages, versions)
                .create("EXISTING", new PageSaveRequest("覆盖尝试", json, json)));
        assertEquals(409, error.getStatusCode().value());
        assertEquals("原页面", original.getPageName());
        verify(pages, never()).saveAndFlush(any());
        verifyNoInteractions(versions);
    }
}
