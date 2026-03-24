package com.hrms.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.Org;

import java.util.List;

/**
 * 组织服务接口
 *
 * @author HRMS
 */
public interface OrgService extends IService<Org> {

    /**
     * 获取组织树
     */
    List<Org> getOrgTree();

    /**
     * 创建组织
     */
    void createOrg(Org org);

    /**
     * 更新组织
     */
    void updateOrg(Org org);

    /**
     * 删除组织
     */
    void deleteOrg(Long id);

    /**
     * 批量删除组织
     */
    void batchDeleteOrgs(List<Long> ids);

    /**
     * 更新组织状态
     */
    void updateOrgStatus(Long id, Integer status);

    /**
     * 获取所有启用的组织
     */
    List<Org> listEnabledOrgs();
}
