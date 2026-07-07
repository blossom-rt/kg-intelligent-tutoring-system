package com.cupk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cupk.pojo.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 课程 Mapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("""
            SELECT
                c.id AS id,
                c.subject AS subject,
                c.course_name AS courseName,
                c.description AS description,
                COUNT(DISTINCT sr2.user_id) AS sharedLearnerCount,
                COALESCE(SUM(sr2.study_minutes), 0) AS sharedStudyMinutes,
                COUNT(DISTINCT kn.id) AS nodeCount,
                LEAST(ROUND(
                    IF(c.subject = base.subject, 40, 0)
                    + COUNT(DISTINCT sr2.user_id) * 12
                    + LEAST(COALESCE(SUM(sr2.study_minutes), 0) / 60, 20)
                    + LEAST(COUNT(DISTINCT kn.id) * 2, 16),
                    1
                ), 100) AS score
            FROM course base
            JOIN course c ON c.id <> base.id
            LEFT JOIN knowledge_node kn ON kn.course_id = c.id
            LEFT JOIN study_record sr2 ON sr2.node_id = kn.id
                AND sr2.user_id IN (
                    SELECT DISTINCT sr1.user_id
                    FROM study_record sr1
                    JOIN knowledge_node kn1 ON kn1.id = sr1.node_id
                    WHERE kn1.course_id = #{courseId}
                      AND (sr1.study_minutes > 0 OR sr1.mastery_level > 0)
                )
                AND (sr2.study_minutes > 0 OR sr2.mastery_level > 0)
            WHERE base.id = #{courseId}
            GROUP BY c.id, c.subject, c.course_name, c.description, base.subject
            ORDER BY score DESC, sharedLearnerCount DESC, c.create_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectRelatedCourses(@Param("courseId") Integer courseId, @Param("limit") Integer limit);

    @Select("""
            SELECT
                c.id AS id,
                c.subject AS subject,
                c.course_name AS courseName,
                c.description AS description,
                COUNT(DISTINCT sr2.user_id) AS learnerCount,
                COALESCE(SUM(sr2.study_minutes), 0) AS totalStudyMinutes,
                ROUND(AVG(sr2.correct_rate), 1) AS avgCorrectRate
            FROM course c
            JOIN knowledge_node kn2 ON kn2.course_id = c.id
            JOIN study_record sr2 ON sr2.node_id = kn2.id
            WHERE c.id <> #{courseId}
              AND (sr2.study_minutes > 0 OR sr2.mastery_level > 0)
              AND sr2.user_id IN (
                  SELECT DISTINCT sr1.user_id
                  FROM study_record sr1
                  JOIN knowledge_node kn1 ON kn1.id = sr1.node_id
                  WHERE kn1.course_id = #{courseId}
                    AND (sr1.study_minutes > 0 OR sr1.mastery_level > 0)
              )
            GROUP BY c.id, c.subject, c.course_name, c.description
            ORDER BY learnerCount DESC, totalStudyMinutes DESC, avgCorrectRate DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectAlsoLearnedCourses(@Param("courseId") Integer courseId, @Param("limit") Integer limit);
}
