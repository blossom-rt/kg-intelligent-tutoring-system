package com.cupk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cupk.pojo.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 习题 Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}
