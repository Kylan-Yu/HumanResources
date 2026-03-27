package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.service.WorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Workflow template API entry.
 * Controller only handles request mapping, auth and response.
 */
@RestController
@RequestMapping("/workflow/templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private final WorkflowTemplateService workflowTemplateService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        return workflowTemplateService.page(pageNum, pageSize, keyword, category, status);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<String>> categories() {
        return workflowTemplateService.categories();
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> detail(@PathVariable String templateId) {
        return workflowTemplateService.detail(templateId);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return workflowTemplateService.create(body);
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> save(@PathVariable String templateId, @RequestBody Map<String, Object> body) {
        return workflowTemplateService.save(templateId, body);
    }

    @PostMapping("/{templateId}/publish")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> publish(
            @PathVariable String templateId,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        return workflowTemplateService.publish(templateId, body);
    }

    @PostMapping("/{templateId}/duplicate")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> duplicate(@PathVariable String templateId) {
        return workflowTemplateService.duplicate(templateId);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> delete(@PathVariable String templateId) {
        return workflowTemplateService.delete(templateId);
    }

    @GetMapping("/{templateId}/versions")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<Map<String, Object>>> versions(@PathVariable String templateId) {
        return workflowTemplateService.versions(templateId);
    }

    @GetMapping("/{templateId}/versions/{versionNo}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> versionDetail(@PathVariable String templateId, @PathVariable Integer versionNo) {
        return workflowTemplateService.versionDetail(templateId, versionNo);
    }

    @PostMapping("/{templateId}/versions/{versionNo}/restore")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> restoreVersion(
            @PathVariable String templateId,
            @PathVariable Integer versionNo,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        return workflowTemplateService.restoreVersion(templateId, versionNo, body);
    }
}
