package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;

import java.time.LocalDateTime;
import com.cupk.pojo.SysNotice;
import com.cupk.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统通知公告控制器 —— 公告的增删改查（管理员操作，公开可读）
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final SysNoticeService sysNoticeService;

    /**
     * 检查当前用户是否为管理员，否则抛出无权限异常
     */
    private void checkAdmin() {
        if (!"admin".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询通知公告列表（公开可读，可按角色筛选）
     */
    @GetMapping
    public Result<?> list() {
        return Result.success(sysNoticeService.listPublic());
    }

    /**
     * 新增通知公告（仅管理员）
     */
    @OperLog(module = "公告管理", operation = "新增公告")
    @PostMapping
    public Result<?> create(@RequestBody SysNotice notice) {
        checkAdmin();
        notice.setPublisherId(UserContext.getUserId());
        notice.setCreateTime(LocalDateTime.now());
        sysNoticeService.save(notice);
        return Result.success("新增公告成功");
    }

    /**
     * 修改通知公告（仅管理员）
     */
    @OperLog(module = "公告管理", operation = "修改公告")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody SysNotice notice) {
        checkAdmin();
        notice.setId(id);
        sysNoticeService.update(notice);
        return Result.success("修改公告成功");
    }

    /**
     * 删除通知公告（仅管理员）
     */
    @OperLog(module = "公告管理", operation = "删除公告")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkAdmin();
        sysNoticeService.delete(id);
        return Result.success("删除公告成功");
    }
}
