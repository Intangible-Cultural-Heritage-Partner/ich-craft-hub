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
    void legacyUpdatePreservesExistingCategory() {
        User owner = user(10L, "CRAFTSMAN");
        HeritageWork existing = work(100L, owner, "Original", "Content");
        existing.setCategory("缂丝");
        when(heritageWorkRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(heritageWorkRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.update(100L, owner, "Updated", "Content",
                null, null, null, null);

        assertEquals("缂丝", saved.getCategory());
        assertEquals(0, saved.getAuditStatus());
    }

    @Test
    void selectedCategoryIsSaved() {
        when(heritageWorkRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HeritageWork saved = heritageWorkService.create(user(10L, "1"), "Title", null,
                null, null, null, null, null, null, null, " 木雕 ");
        assertEquals("木雕", saved.getCategory());
    }

    @Test
    void unknownCategoryIsRejectedBeforeSave() {
        assertThrows(IllegalArgumentException.class, () -> heritageWorkService.create(
                user(10L, "1"), "Title", null, null, null, null, null, null, null, null, "unknown"));
        verify(heritageWorkRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void publicCategoryQueryAlwaysRequiresApprovedStatus() {
        heritageWorkService.findAll(" 木雕 ");
        verify(heritageWorkRepository).findPublicByCategory(1, "木雕");
    }

    @Test
    void personalCategoryQueryAlwaysRequiresOwner() {
        heritageWorkService.findByCraftsman(10L, "苏绣");
        verify(heritageWorkRepository).findOwnedByCategory(10L, "苏绣");
    }

    @Test
    void blankFilterMeansAllApprovedWorks() {
        heritageWorkService.findAll("  ");
        verify(heritageWorkRepository).findPublicByCategory(1, null);
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

    @Test
    void modelUrlIsTrimmedWhenCreatingWork() {
        User craftsman = user(10L, "CRAFTSMAN");
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                null,
                " https://example.com/model.glb ",
                null,
                null,
                null,
                null,
                null
        );

        assertEquals("https://example.com/model.glb", saved.getModelUrl());
    }

    @Test
    void blankModelUrlIsStoredAsNull() {
        User craftsman = user(10L, "CRAFTSMAN");
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                null,
                "  ",
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(null, saved.getModelUrl());
    }

    @Test
    void glbModelFileUsesGeneratedModelUploadPath() {
        User craftsman = user(10L, "CRAFTSMAN");
        MockMultipartFile model = new MockMultipartFile(
                "model",
                "carving.glb",
                "model/gltf-binary",
                "model-bytes".getBytes()
        );
        when(heritageWorkRepository.save(any(HeritageWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeritageWork saved = heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                null,
                null,
                model,
                null,
                null,
                null,
                null
        );

        assertTrue(saved.getModelUrl().startsWith("/uploads/heritage/models/"));
        assertTrue(saved.getModelUrl().endsWith(".glb"));
    }

    @Test
    void unsupportedModelFileIsRejectedBeforeSaving() {
        User craftsman = user(10L, "CRAFTSMAN");
        MockMultipartFile model = new MockMultipartFile(
                "model",
                "carving.fbx",
                "application/octet-stream",
                "model-bytes".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> heritageWorkService.create(
                craftsman,
                "Wood carving",
                "A traditional carving",
                null,
                null,
                model,
                null,
                null,
                null,
                null
        ));
        verify(heritageWorkRepository, org.mockito.Mockito.never()).save(any(HeritageWork.class));
    }

    @Test
    void explicitRemovalClearsModelEvenWhenOldUrlIsSubmitted() {
        User owner = user(10L, "1");
        HeritageWork existing = work(100L, owner, "Original", "Content");
        existing.setModelUrl("/uploads/heritage/models/old.glb");
        when(heritageWorkRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(heritageWorkRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HeritageWork saved = heritageWorkService.update(100L, owner, "Updated", null, null,
                existing.getModelUrl(), null, null, null, null, null, "木雕", true);
        assertEquals(null, saved.getModelUrl());
        assertEquals("木雕", saved.getCategory());
        assertEquals(0, saved.getAuditStatus());
    }

    @Test
    void blankModelInputWithoutRemovalKeepsExistingModel() {
        User owner = user(10L, "1");
        HeritageWork existing = work(100L, owner, "Original", "Content");
        existing.setModelUrl("/models/old.glb");
        when(heritageWorkRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(heritageWorkRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HeritageWork saved = heritageWorkService.update(100L, owner, "Updated", null, null,
                "", null, null, null, null, null, null, false);
        assertEquals("/models/old.glb", saved.getModelUrl());
    }

    @Test
    void removalAndUploadConflictDoesNotChangeExistingWork() {
        User owner = user(10L, "1");
        HeritageWork existing = work(100L, owner, "Original", "Content");
        existing.setCategory("缂丝");
        existing.setModelUrl("/models/old.glb");
        when(heritageWorkRepository.findById(100L)).thenReturn(Optional.of(existing));
        MockMultipartFile model = new MockMultipartFile("modelFile", "new.glb", "model/gltf-binary", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> heritageWorkService.update(100L, owner,
                "Updated", null, null, null, model, null, null, null, null, "木雕", true));
        assertEquals("Original", existing.getTitle());
        assertEquals("缂丝", existing.getCategory());
        assertEquals("/models/old.glb", existing.getModelUrl());
        verify(heritageWorkRepository, org.mockito.Mockito.never()).save(any());
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
