package com.cupk.service;

import com.cupk.pojo.SysRole;

import java.util.List;

/**
 * 系统角色服务
 */
public interface SysRoleService {

    /** 查询所有角色 */
    List<SysRole> listAll();

    /** 根据ID查询角色 */
    SysRole getById(Integer id);

    /** 新增角色 */
    void save(SysRole role);

    /** 更新角色 */
    void update(SysRole role);

    /** 删除角色 */
    void delete(Integer id);
}
