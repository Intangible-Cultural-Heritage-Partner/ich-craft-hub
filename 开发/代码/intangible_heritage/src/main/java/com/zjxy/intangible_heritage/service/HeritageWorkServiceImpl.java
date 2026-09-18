package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.HeritageWorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HeritageWorkServiceImpl implements HeritageWorkService {

    private final HeritageWorkRepository heritageWorkRepository;

    @Override
    public List<HeritageWork> listPublished() {
        //audit_status = 1 审核通过
        return heritageWorkRepository.findByAuditStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public List<HeritageWork> listByCraftsman(Long craftsmanId) {
        return heritageWorkRepository.findByCraftsmanIdOrderByCreateTimeDesc(craftsmanId);
    }

    @Override
    public Optional<HeritageWork> findById(Long id) {
        return heritageWorkRepository.findById(id);
    }

    @Override
    public HeritageWork publish(HeritageWork work, User craftsman) {
        work.setCraftsman(craftsman);
        work.setAuditStatus(0);
        work.setAuditRemark(null);
        return heritageWorkRepository.save(work);
    }

    @Override
    public HeritageWork update(Long id, HeritageWork form, User craftsman) {
        HeritageWork existing = heritageWorkRepository.findById(id).orElse(null);
        if (existing == null || craftsman == null
                || existing.getCraftsman() == null
                || !existing.getCraftsman().getId().equals(craftsman.getId())) {
            //不存在或非本人，越权保护
            return null;
        }
        existing.setTitle(form.getTitle());
        existing.setCategory(form.getCategory());
        existing.setSkillBackground(form.getSkillBackground());
        existing.setDescription(form.getDescription());
        //封面/多图/模型：仅当本次上传了新文件才覆盖，否则保留原值
        if (form.getCoverImg() != null) {
            existing.setCoverImg(form.getCoverImg());
        }
        if (form.getImageList() != null) {
            existing.setImageList(form.getImageList());
        }
        if (form.getModelUrl() != null) {
            existing.setModelUrl(form.getModelUrl());
        }
        //编辑后重新进入待审核
        existing.setAuditStatus(0);
        existing.setAuditRemark(null);
        return heritageWorkRepository.save(existing);
    }
}
