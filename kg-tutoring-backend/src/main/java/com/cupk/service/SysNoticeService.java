package com.cupk.service;

import com.cupk.pojo.SysNotice;

import java.util.List;

/**
 * 系统公告服务
 */
public interface SysNoticeService {

    /** 查询公告列表 */
    List<SysNotice> list();

    /** 根据ID查询公告 */
    SysNotice getById(Integer id);

    /** 新增公告 */
    void save(SysNotice notice);

    /** 更新公告 */
    void update(SysNotice notice);

    /** 删除公告 */
    void delete(Integer id);

    /** 查询公开公告列表 */
    List<SysNotice> listPublic();
}
