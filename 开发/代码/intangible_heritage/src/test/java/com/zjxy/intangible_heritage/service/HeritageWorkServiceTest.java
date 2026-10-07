package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.HeritageWorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeritageWorkServiceTest {

    @Mock
    private HeritageWorkRepository heritageWorkRepository;

    @InjectMocks
    private HeritageWorkServiceImpl heritageWorkService;

    @Test
    void craftsmanCanCreateWorkWithOwnerAndPendingAuditStatus() {
        User craftsman = user(10L, "CRAFTSMAN");
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                null,
                null,
                null,
                null
        );

        assertEquals(craftsman, saved.getCraftsman());
        assertEquals("Wood carving", saved.getTitle());
        assertEquals(0, saved.getAuditStatus());
        verify(heritageWorkRepository).save(saved);
    }

    @Test
    void ordinaryUserCannotCreateWork() {
        User ordinaryUser = user(11L, "USER");

        assertThrows(IllegalStateException.class, () -> heritageWorkService.create(
                ordinaryUser,
                "Wood carving",
                "A traditional carving",
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void nonOwnerCannotUpdateWork() {
        User owner = user(10L, "CRAFTSMAN");
        User otherCraftsman = user(11L, "CRAFTSMAN");
        HeritageWork work = work(100L, owner, "Original title", "Original content");
        when(heritageWorkRepository.findById(100L)).thenReturn(Optional.of(work));

        assertThrows(IllegalStateException.class, () -> heritageWorkService.update(
                100L,
                otherCraftsman,
                "Changed title",
                "Changed content",
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void ownerCanUpdateTitleAndContentAndRepositoryReceivesChanges() {
        User owner = user(10L, "CRAFTSMAN");
        HeritageWork work = work(100L, owner, "Original title", "Original content");
        when(heritageWorkRepository.findById(100L)).thenReturn(Optional.of(work));
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        heritageWorkService.update(
                100L,
                owner,
                "Updated title",
                "Updated content",
                null,
                null,
                null,
                null
        );

        ArgumentCaptor<HeritageWork> savedWork = ArgumentCaptor.forClass(HeritageWork.class);
        verify(heritageWorkRepository).save(savedWork.capture());
        assertEquals("Updated title", savedWork.getValue().getTitle());
        assertEquals("Updated content", savedWork.getValue().getDescription());
    }

    @Test
    void nonEmptyImageFileUsesGeneratedHeritageUploadPath() {
        User craftsman = user(10L, "CRAFTSMAN");
        MockMultipartFile cover = new MockMultipartFile(
                "cover",
                "cover.png",
                "image/png",
                "image-bytes".getBytes()
        );
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                cover,
                "https://example.com/fallback.png",
                null,
                null
        );

        assertTrue(saved.getCoverImg().startsWith("/uploads/heritage/"));
        assertTrue(saved.getCoverImg().endsWith(".png"));
        assertTrue(!saved.getCoverImg().equals("https://example.com/fallback.png"));
    }

    @Test
    void emptyImageFileFallsBackToSubmittedUrl() {
        User craftsman = user(10L, "CRAFTSMAN");
        MockMultipartFile emptyCover = new MockMultipartFile(
                "cover",
                "cover.png",
                "image/png",
                new byte[0]
        );
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                emptyCover,
                " https://example.com/fallback.png ",
                null,
                null
        );

        assertEquals("https://example.com/fallback.png", saved.getCoverImg());
    }

    private User user(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setPassword("password");
        user.setRole(role);
        return user;
    }

    private HeritageWork work(Long id, User owner, String title, String description) {
        HeritageWork work = new HeritageWork();
        work.setId(id);
        work.setCraftsman(owner);
        work.setTitle(title);
        work.setDescription(description);
        work.setAuditStatus(0);
        return work;
    }
}
