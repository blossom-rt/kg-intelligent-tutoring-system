package com.cupk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cupk.pojo.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测评题目关联 Mapper
 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
}
