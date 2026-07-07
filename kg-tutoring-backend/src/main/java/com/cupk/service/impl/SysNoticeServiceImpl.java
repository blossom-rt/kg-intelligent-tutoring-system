package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.SysNoticeMapper;
import com.cupk.pojo.SysNotice;
import com.cupk.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统公告服务实现
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements SysNoticeService {

    private final SysNoticeMapper sysNoticeMapper;

    @Override
    public List<SysNotice> list() {
        return sysNoticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>().orderByDesc(SysNotice::getCreateTime));
    }

    @Override
    public SysNotice getById(Integer id) {
        return sysNoticeMapper.selectById(id);
    }

    @Override
    public void save(SysNotice notice) {
        sysNoticeMapper.insert(notice);
    }

    @Override
    public void update(SysNotice notice) {
        sysNoticeMapper.updateById(notice);
    }

    @Override
    public void delete(Integer id) {
        sysNoticeMapper.deleteById(id);
    }

    @Override
    public List<SysNotice> listPublic() {
        return sysNoticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>()
                        .orderByDesc(SysNotice::getCreateTime));
    }
}
