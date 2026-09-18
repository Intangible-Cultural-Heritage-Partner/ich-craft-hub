package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;

import java.util.List;
import java.util.Optional;

public interface HeritageWorkService {

    //审核通过的展品列表（前台展示）
    List<HeritageWork> listPublished();

    //某匠人发布的全部展品（个人中心）
    List<HeritageWork> listByCraftsman(Long craftsmanId);

    Optional<HeritageWork> findById(Long id);

    //匠人发布新展品，默认待审核
    HeritageWork publish(HeritageWork work, User craftsman);

    //匠人编辑本人展品；越权返回 null。编辑后重置为待审核
    HeritageWork update(Long id, HeritageWork form, User craftsman);
}
