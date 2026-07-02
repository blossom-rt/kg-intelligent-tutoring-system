package com.cupk.service.impl;

import com.cupk.mapper.SysRoleMapper;
import com.cupk.pojo.SysRole;
import com.cupk.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(null);
    }

    @Override
    public SysRole getById(Integer id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public void save(SysRole role) {
        sysRoleMapper.insert(role);
    }

    @Override
    public void update(SysRole role) {
        sysRoleMapper.updateById(role);
    }

    @Override
    public void delete(Integer id) {
        sysRoleMapper.deleteById(id);
    }
}
