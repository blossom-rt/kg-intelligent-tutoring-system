package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.common.BusinessException;
import com.cupk.common.UserContext;
import com.cupk.dto.*;
import com.cupk.email.EmailService;
import com.cupk.mapper.*;
import com.cupk.pojo.*;
import com.cupk.pojo.SysEmailCode;
import com.cupk.pojo.SysRole;
import com.cupk.pojo.SysUser;
import com.cupk.service.SysUserService;
import com.cupk.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysEmailCodeMapper sysEmailCodeMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final StudyPathMapper studyPathMapper;
    private final PathDetailMapper pathDetailMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final ExamRecordMapper examRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final CourseMapper courseMapper;
    private final ExamMapper examMapper;
    private final CrossSubjectThemeMapper crossSubjectThemeMapper;
    private final SysNoticeMapper sysNoticeMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );

        if (user == null || !user.getPassword()
                .equals(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            return null;
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        if (role == null) {
            return null;
        }

        if (dto.getRole() != null && !role.getRoleCode().equals(dto.getRole())) {
            return null;
        }

        // 设置用户上下文，使 AOP 操作日志能记录 userId
        UserContext.set(user.getId(), user.getUsername(), role.getRoleCode());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role.getRoleCode());
        return new LoginVO(token, role.getRoleCode());
    }

    @Override
    public LoginVO getCurrentUserInfo() {
        SysUser user = sysUserMapper.selectById(UserContext.getUserId());
        if (user == null) return null;
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role.getRoleCode());
        return new LoginVO(token, role.getRoleCode());
    }

    @Override
    public void register(RegisterDTO dto) {
        // 校验账号唯一性
        if (sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 校验邮箱唯一性
        if (sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail())) > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        // 校验验证码
        SysEmailCode codeRecord = sysEmailCodeMapper.selectOne(
                new LambdaQueryWrapper<SysEmailCode>()
                        .eq(SysEmailCode::getEmail, dto.getEmail())
                        .eq(SysEmailCode::getType, "register")
                        .eq(SysEmailCode::getIsUsed, 0)
                        .orderByDesc(SysEmailCode::getCreateTime)
                        .last("LIMIT 1")
        );

        if (codeRecord == null || !codeRecord.getCode().equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }

        if (codeRecord.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期");
        }

        // 标记验证码已使用
        codeRecord.setIsUsed(1);
        sysEmailCodeMapper.updateById(codeRecord);

        // 创建用户（角色默认为学生 role_id=3）
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setRoleId(3);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
    }

    @Override
    public void sendEmailCode(String email, String type) {
        // 60 秒内限制重复发送
        SysEmailCode lastCode = sysEmailCodeMapper.selectOne(
                new LambdaQueryWrapper<SysEmailCode>()
                        .eq(SysEmailCode::getEmail, email)
                        .eq(SysEmailCode::getType, type)
                        .orderByDesc(SysEmailCode::getCreateTime)
                        .last("LIMIT 1")
        );

        if (lastCode != null && lastCode.getCreateTime().plusSeconds(60).isAfter(LocalDateTime.now())) {
            throw new BusinessException("60 秒内已发送过验证码，请稍后再试");
        }

        // 生成 6 位验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 存入数据库
        SysEmailCode record = new SysEmailCode();
        record.setEmail(email);
        record.setCode(code);
        record.setType(type);
        record.setExpireTime(LocalDateTime.now().plusMinutes(5));
        record.setIsUsed(0);
        sysEmailCodeMapper.insert(record);

        emailService.sendCode(email, code, type);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        // 校验验证码
        SysEmailCode codeRecord = sysEmailCodeMapper.selectOne(
                new LambdaQueryWrapper<SysEmailCode>()
                        .eq(SysEmailCode::getEmail, dto.getEmail())
                        .eq(SysEmailCode::getType, "reset")
                        .eq(SysEmailCode::getIsUsed, 0)
                        .orderByDesc(SysEmailCode::getCreateTime)
                        .last("LIMIT 1")
        );

        if (codeRecord == null || !codeRecord.getCode().equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }

        if (codeRecord.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期");
        }

        // 标记验证码已使用
        codeRecord.setIsUsed(1);
        sysEmailCodeMapper.updateById(codeRecord);

        // 更新密码
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail())
        );

        if (user == null) {
            throw new BusinessException("该邮箱未绑定账号");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        sysUserMapper.updateById(user);
    }

    @Override
    public void updatePassword(Integer userId, UpdatePasswordDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldMd5 = DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(oldMd5)) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes(StandardCharsets.UTF_8)));
        sysUserMapper.updateById(user);
    }

    @Override
    public SysUser getUserById(Integer id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public void updateProfile(Integer userId, ProfileDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 邮箱唯一性校验
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail())) > 0) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }

        sysUserMapper.updateById(user);
    }

    // 管理员方法 =====

    @Override
    public List<SysUser> listUsers(String keyword, Integer roleId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (roleId != null) {
            wrapper.eq(SysUser::getRoleId, roleId);
        }
        return sysUserMapper.selectList(wrapper);
    }

    @Override
    public void createUser(SysUser user) {
        if (sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getPassword() != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        sysUserMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        sysUserMapper.updateById(user);
    }

    @Override
    public void deleteUser(Integer id) {
        // 级联删除：先删所有关联子表数据，再删用户
        List<StudyPath> userPaths = studyPathMapper.selectList(
                new LambdaQueryWrapper<StudyPath>().eq(StudyPath::getUserId, id));
        for (StudyPath p : userPaths) {
            pathDetailMapper.delete(new LambdaQueryWrapper<PathDetail>().eq(PathDetail::getPathId, p.getId()));
        }
        studyPathMapper.delete(new LambdaQueryWrapper<StudyPath>().eq(StudyPath::getUserId, id));
        studyRecordMapper.delete(new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, id));
        examRecordMapper.delete(new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, id));
        wrongQuestionMapper.delete(new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getUserId, id));
        userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getUserId, id));
        courseMapper.delete(new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, id));
        examMapper.delete(new LambdaQueryWrapper<Exam>().eq(Exam::getCreatorId, id));
        crossSubjectThemeMapper.delete(new LambdaQueryWrapper<CrossSubjectTheme>().eq(CrossSubjectTheme::getPublisherId, id));
        sysNoticeMapper.delete(new LambdaQueryWrapper<SysNotice>().eq(SysNotice::getPublisherId, id));
        sysUserMapper.deleteById(id);
    }

    @Override
    public void updateUserStatus(Integer id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }
}
