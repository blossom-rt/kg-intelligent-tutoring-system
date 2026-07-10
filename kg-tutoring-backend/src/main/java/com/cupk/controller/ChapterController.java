package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.Chapter;
import com.cupk.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 章节管理控制器（教师操作）
 */
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /** 按课程查询章节列表 */
    @GetMapping
    public Result<List<Chapter>> list(@RequestParam Integer courseId) {
        return Result.success(chapterService.listByCourse(courseId));
    }

    @GetMapping("/{id}")
    public Result<Chapter> getOne(@PathVariable Integer id) {
        return Result.success(chapterService.getById(id));
    }

    @OperLog(module = "章节管理", operation = "新增章节")
    @PostMapping
    public Result<?> create(@RequestBody Chapter chapter) {
        checkTeacher();
        chapterService.save(chapter);
        return Result.success("新增章节成功");
    }

    @OperLog(module = "章节管理", operation = "修改章节")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Chapter chapter) {
        checkTeacher();
        chapter.setId(id);
        chapterService.update(chapter);
        return Result.success("修改章节成功");
    }

    @OperLog(module = "章节管理", operation = "删除章节")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        chapterService.delete(id);
        return Result.success("删除章节成功");
    }

    /** 拖拽排序 */
    @PutMapping("/{id}/sort")
    public Result<?> updateSort(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        checkTeacher();
        chapterService.updateSort(id, body.get("sort"));
        return Result.success("排序更新成功");
    }
}
