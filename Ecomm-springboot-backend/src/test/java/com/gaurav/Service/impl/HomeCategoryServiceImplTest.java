
package com.gaurav.service.impl;

import com.gaurav.model.HomeCategory;
import com.gaurav.repository.HomeCategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeCategoryServiceImplTest {

    @Mock
    private HomeCategoryRepository homeCategoryRepository;

    @InjectMocks
    private HomeCategoryServiceImpl homeCategoryService;

    @Test
    void createCategory_whenValidCategory_savesAndReturnsCategory() {
        HomeCategory category = mock(HomeCategory.class);

        when(homeCategoryRepository.save(category))
                .thenReturn(category);

        HomeCategory result =
                homeCategoryService.createCategory(category);

        assertSame(category, result);
        verify(homeCategoryRepository).save(category);
    }

    @Test
    void createCategory_whenNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> homeCategoryService.createCategory(null)
        );

        verifyNoInteractions(homeCategoryRepository);
    }

    @Test
    void createCategories_whenRepositoryIsEmpty_savesInitialCategories() {
        List<HomeCategory> categories = List.of(
                mock(HomeCategory.class),
                mock(HomeCategory.class)
        );

        when(homeCategoryRepository.findAll())
                .thenReturn(List.of());

        when(homeCategoryRepository.saveAll(categories))
                .thenReturn(categories);

        List<HomeCategory> result =
                homeCategoryService.createCategories(categories);

        assertEquals(2, result.size());
        verify(homeCategoryRepository).findAll();
        verify(homeCategoryRepository).saveAll(categories);
    }

    @Test
    void createCategories_whenCategoriesAlreadyExist_returnsExistingCategories() {
        List<HomeCategory> existingCategories = List.of(
                mock(HomeCategory.class)
        );

        List<HomeCategory> newCategories = List.of(
                mock(HomeCategory.class)
        );

        when(homeCategoryRepository.findAll())
                .thenReturn(existingCategories);

        List<HomeCategory> result =
                homeCategoryService.createCategories(newCategories);

        assertSame(existingCategories, result);
        verify(homeCategoryRepository, never()).saveAll(any());
    }

    @Test
    void createCategories_whenListIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> homeCategoryService.createCategories(null)
        );

        verifyNoInteractions(homeCategoryRepository);
    }

    @Test
    void createCategories_whenListIsEmpty_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> homeCategoryService.createCategories(List.of())
        );

        verifyNoInteractions(homeCategoryRepository);
    }

    @Test
    void getAllCategories_returnsCategoriesFromRepository() {
        List<HomeCategory> expected = List.of(
                mock(HomeCategory.class),
                mock(HomeCategory.class)
        );

        when(homeCategoryRepository.findAll()).thenReturn(expected);

        List<HomeCategory> result =
                homeCategoryService.getAllCategories();

        assertSame(expected, result);
        verify(homeCategoryRepository).findAll();
    }

    @Test
    void updateCategory_whenCategoryExists_updatesImage() throws Exception {
        Long id = 1L;
        HomeCategory existingCategory = mock(HomeCategory.class);
        HomeCategory updates = mock(HomeCategory.class);

        when(homeCategoryRepository.findById(id))
                .thenReturn(Optional.of(existingCategory));
        when(updates.getImage()).thenReturn("updated-image.jpg");
        when(updates.getCategoryId()).thenReturn(null);
        when(homeCategoryRepository.save(existingCategory))
                .thenReturn(existingCategory);

        HomeCategory result =
                homeCategoryService.updateCategory(updates, id);

        verify(existingCategory).setImage("updated-image.jpg");
        verify(homeCategoryRepository).save(existingCategory);
        assertSame(existingCategory, result);
    }

    @Test
    void updateCategory_whenImageIsNull_doesNotUpdateImage() throws Exception {
        Long id = 1L;
        HomeCategory existingCategory = mock(HomeCategory.class);
        HomeCategory updates = mock(HomeCategory.class);

        when(homeCategoryRepository.findById(id))
                .thenReturn(Optional.of(existingCategory));
        when(updates.getImage()).thenReturn(null);
        when(updates.getCategoryId()).thenReturn(null);
        when(homeCategoryRepository.save(existingCategory))
                .thenReturn(existingCategory);

        homeCategoryService.updateCategory(updates, id);

        verify(existingCategory, never()).setImage(any());
        verify(existingCategory, never()).setCategoryId(any());
        verify(homeCategoryRepository).save(existingCategory);
    }

    @Test
    void updateCategory_whenCategoryDoesNotExist_throwsException() {
        Long id = 99L;
        HomeCategory updates = mock(HomeCategory.class);

        when(homeCategoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> homeCategoryService.updateCategory(updates, id)
        );

        verify(homeCategoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_whenUpdatesAreNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> homeCategoryService.updateCategory(null, 1L)
        );

        verifyNoInteractions(homeCategoryRepository);
    }
}

