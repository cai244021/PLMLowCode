package com.jfseat.lowcode.page;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;

@Validated
@RestController
@RequestMapping("/api/pages")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * 查询页面当前版本
     **
     * @param pageCode 页面编码
     * @return 页面信息或404
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    @GetMapping("/{pageCode}")
    public ResponseEntity<PageResponse> getPage(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String pageCode) {
        return pageService.findByPageCode(pageCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 保存页面并创建新版本
     **
     * @param pageCode 页面编码
     * @param request 页面保存请求
     * @return 保存后的页面信息
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    @PutMapping("/{pageCode}")
    public PageResponse savePage(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String pageCode,
            @Valid @RequestBody PageSaveRequest request) {
        return pageService.save(pageCode, request);
    }

    /**
     * 获取设计器页面列表
     **
     * @return 页面摘要列表
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @GetMapping
    public List<PageSummary> listPages() {
        return pageService.listPages();
    }

    /**
     * 创建新页面或页面副本，不覆盖已有页面
     **
     * @param pageCode 唯一页面编码
     * @param request 页面内容和PLM绑定
     * @return 创建后的页面
     * @throws ResponseStatusException 页面编码冲突
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @PostMapping("/{pageCode}")
    public ResponseEntity<PageResponse> createPage(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String pageCode,
            @Valid @RequestBody PageSaveRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(pageService.create(pageCode, request));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "页面编码冲突，请刷新列表后重试");
        }
    }

    /**
     * 将非法页面编码转换成可读的参数错误
     **
     * @param exception 编码校验异常
     * @return HTTP400错误信息
     * @author caipan by codex
     * @date 2026/9/4 10:30
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> invalidPageCode(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", "页面编码只能使用大写字母、数字、下划线，长度1至100"));
    }

    /**
     * 删除设计器页面，不影响已导出或部署到PLM的页面
     **
     * @param pageCode 页面编码
     * @return 删除成功返回204
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    @DeleteMapping("/{pageCode}")
    public ResponseEntity<Void> deletePage(
            @PathVariable @Pattern(regexp = "[A-Z0-9_]{1,100}") String pageCode) {
        pageService.delete(pageCode);
        return ResponseEntity.noContent().build();
    }
}
