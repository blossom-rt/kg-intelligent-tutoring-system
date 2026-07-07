package com.cupk.dto;

import lombok.Data;

import java.util.List;

/**
 * 测评提交 DTO
 */
@Data
public class ExamSubmitDTO {

    /** 所属课程ID */
    private Integer courseId;

    /** 测评ID */
    private Integer examId;

    /** 作答列表 */
    private List<AnswerItem> answers;

    /**
     * 单题作答项
     */
    @Data
    public static class AnswerItem {

        /** 题目ID */
        private Integer questionId;

        /** 用户答案 */
        private String userAnswer;
    }
}
