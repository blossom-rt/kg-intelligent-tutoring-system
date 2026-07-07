package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 学生学习记录控制器 —— 更新知识点学习进度记录
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentRecordController {

    private final StudyRecordService studyRecordService;

    /**
     * 更新学习记录（掌握程度、正确率、学习时长）
     */
    @PutMapping("/study-record")
    public Result<?> updateStudyRecord(@RequestBody Map<String, Object> body) {
        Integer userId = UserContext.getUserId();
        Integer nodeId = (Integer) body.get("nodeId");
        Integer masteryLevel = body.get("masteryLevel") != null ? (Integer) body.get("masteryLevel") : null;
        BigDecimal correctRate = body.get("correctRate") != null ? BigDecimal.valueOf(((Number) body.get("correctRate")).doubleValue()) : null;
        Integer studyMinutes = body.get("studyMinutes") != null ? (Integer) body.get("studyMinutes") : null;
        studyRecordService.updateRecord(userId, nodeId, masteryLevel, correctRate, studyMinutes);
        return Result.success("学习记录更新成功");
    }
}
