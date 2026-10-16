
package com.gaurav.service.impl;

import com.gaurav.exception.ReviewNotFoundException;
import com.gaurav.model.Product;
import com.gaurav.model.Review;
import com.gaurav.model.User;
import com.gaurav.repository.ReviewRepository;
import com.gaurav.request.CreateReviewRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Product product;
    private Review review;
    private CreateReviewRequest request;
    private List<Review> reviews;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        product = mock(Product.class);
        request = mock(CreateReviewRequest.class);

        reviews = new ArrayList<>();

        review = new Review();
        review.setId(100L);
        review.setReviewText("Excellent product");
        review.setRating(5.0);
        review.setUser(user);
        review.setProduct(product);
    }

    // CREATE REVIEW

    @Test
    void createReview_shouldSaveAndReturnReview() {
        when(user.getId()).thenReturn(1L);
        when(product.getId()).thenReturn(10L);
        when(product.getReviews()).thenReturn(reviews);
        when(request.getReviewText()).thenReturn("Excellent product");
        when(request.getReviewRating()).thenReturn(5.0);

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        Review result = reviewService.createReview(
                request, user, product
        );

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Excellent product", result.getReviewText());
        assertEquals(5.0, result.getRating(), 0.001);
        assertSame(user, result.getUser());
        assertSame(product, result.getProduct());

        assertEquals(1, reviews.size());
        assertSame(result, reviews.get(0));

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_shouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(null, user, product)
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(request, null, product)
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_shouldRejectNullProduct() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(request, user, null)
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_shouldRejectBlankReviewText() {
        when(user.getId()).thenReturn(1L);
        when(product.getId()).thenReturn(10L);
        when(request.getReviewText()).thenReturn("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(request, user, product)
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_shouldRejectRatingAboveFive() {
        when(user.getId()).thenReturn(1L);
        when(product.getId()).thenReturn(10L);
        when(request.getReviewText()).thenReturn("Excellent product");
        when(request.getReviewRating()).thenReturn(6.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(request, user, product)
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_shouldRejectRatingBelowOne() {
        when(user.getId()).thenReturn(1L);
        when(product.getId()).thenReturn(10L);
        when(request.getReviewText()).thenReturn("Excellent product");
        when(request.getReviewRating()).thenReturn(0.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(request, user, product)
        );

        verifyNoInteractions(reviewRepository);
    }

    // GET REVIEWS

    @Test
    void getReviewsByProductId_shouldReturnReviews() {
        when(reviewRepository.findReviewsByProductId(10L))
                .thenReturn(List.of(review));

        List<Review> result =
                reviewService.getReviewsByProductId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Excellent product", result.get(0).getReviewText());

        verify(reviewRepository).findReviewsByProductId(10L);
    }

    @Test
    void getReviewsByProductId_shouldReturnEmptyListWhenNoReviewsExist() {
        when(reviewRepository.findReviewsByProductId(10L))
                .thenReturn(List.of());

        List<Review> result =
                reviewService.getReviewsByProductId(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getReviewsByProductId_shouldRejectNullProductId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.getReviewsByProductId(null)
        );

        verifyNoInteractions(reviewRepository);
    }

    // UPDATE REVIEW

    @Test
    void updateReview_shouldUpdateReviewWhenUserOwnsIt()
            throws Exception {
        when(user.getId()).thenReturn(1L);
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        Review result = reviewService.updateReview(
                100L,
                "Updated review",
                4.0,
                1L
        );

        assertNotNull(result);
        assertEquals("Updated review", result.getReviewText());
        assertEquals(4.0, result.getRating(), 0.001);

        verify(reviewRepository).save(review);
    }

    @Test
    void updateReview_shouldThrowWhenReviewDoesNotExist() {
        when(reviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        999L, "Updated review", 4.0, 1L
                )
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_shouldRejectDifferentUser()
            throws Exception {
        when(user.getId()).thenReturn(1L);
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));

        assertThrows(
                AuthenticationException.class,
                () -> reviewService.updateReview(
                        100L, "Updated review", 4.0, 2L
                )
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_shouldRejectBlankText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.updateReview(
                        100L, "   ", 4.0, 1L
                )
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void updateReview_shouldRejectInvalidRating() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.updateReview(
                        100L, "Updated review", 6.0, 1L
                )
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void updateReview_shouldRejectNullReviewId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.updateReview(
                        null, "Updated review", 4.0, 1L
                )
        );

        verifyNoInteractions(reviewRepository);
    }

    // DELETE REVIEW

    @Test
    void deleteReview_shouldDeleteReviewWhenUserOwnsIt()
            throws Exception {
        when(user.getId()).thenReturn(1L);
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));

        reviewService.deleteReview(100L, 1L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_shouldThrowWhenReviewDoesNotExist() {
        when(reviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(999L, 1L)
        );

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void deleteReview_shouldRejectDifferentUser()
            throws Exception {
        when(user.getId()).thenReturn(1L);
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));

        assertThrows(
                AuthenticationException.class,
                () -> reviewService.deleteReview(100L, 2L)
        );

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void deleteReview_shouldRejectNullReviewId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.deleteReview(null, 1L)
        );

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void deleteReview_shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.deleteReview(100L, null)
        );

        verifyNoInteractions(reviewRepository);
    }
}

