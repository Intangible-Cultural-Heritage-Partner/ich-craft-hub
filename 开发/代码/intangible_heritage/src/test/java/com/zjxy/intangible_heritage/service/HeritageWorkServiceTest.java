package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.HeritageWorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeritageWorkServiceTest {

    @Mock
    private HeritageWorkRepository heritageWorkRepository;

    @InjectMocks
    private HeritageWorkServiceImpl service;

    private User craftsman(Long id) {
        User u = new User();
        u.setId(id);
        u.setRole("1");
        return u;
    }

    @Test
    void listPublished_onlyQueriesApprovedStatus() {
        service.listPublished();
        //列表页只取审核通过（audit_status=1）的展品
        verify(heritageWorkRepository).findByAuditStatusOrderByCreateTimeDesc(1);
    }

    @Test
    void publish_setsPendingStatusAndCraftsman() {
        User c = craftsman(10L);
        HeritageWork work = new HeritageWork();
        work.setTitle("苏绣团扇");
        work.setCategory("苏绣");
        when(heritageWorkRepository.save(any(HeritageWork.class))).thenAnswer(inv -> inv.getArgument(0));

        HeritageWork saved = service.publish(work, c);

        assertEquals(0, saved.getAuditStatus());   //新发布默认待审核
        assertNull(saved.getAuditRemark());
        assertSame(c, saved.getCraftsman());
        verify(heritageWorkRepository).save(work);
    }

    @Test
    void update_byOwner_updatesFieldsAndResetsAudit() {
        User owner = craftsman(10L);
        HeritageWork existing = new HeritageWork();
        existing.setId(1L);
        existing.setCraftsman(owner);
        existing.setTitle("旧标题");
        existing.setAuditStatus(1);              //原本已通过
        existing.setAuditRemark("some");
        when(heritageWorkRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(heritageWorkRepository.save(any(HeritageWork.class))).thenAnswer(inv -> inv.getArgument(0));

        HeritageWork form = new HeritageWork();
        form.setTitle("新标题");
        form.setCategory("木雕");

        HeritageWork result = service.update(1L, form, owner);

        assertNotNull(result);
        assertEquals("新标题", result.getTitle());
        assertEquals("木雕", result.getCategory());
        assertEquals(0, result.getAuditStatus());  //编辑后重回待审核
        assertNull(result.getAuditRemark());
    }

    @Test
    void update_byNonOwner_isRejected() {
        User owner = craftsman(10L);
        User other = craftsman(99L);
        HeritageWork existing = new HeritageWork();
        existing.setId(1L);
        existing.setCraftsman(owner);
        when(heritageWorkRepository.findById(1L)).thenReturn(Optional.of(existing));

        HeritageWork form = new HeritageWork();
        form.setTitle("恶意修改");
        form.setCategory("剪纸");

        HeritageWork result = service.update(1L, form, other);

        assertNull(result);                        //越权返回 null
        verify(heritageWorkRepository, never()).save(any());
    }

    @Test
    void update_notFound_returnsNull() {
        when(heritageWorkRepository.findById(404L)).thenReturn(Optional.empty());
        HeritageWork form = new HeritageWork();
        form.setTitle("x");
        form.setCategory("y");

        assertNull(service.update(404L, form, craftsman(10L)));
        verify(heritageWorkRepository, never()).save(any());
    }
}
