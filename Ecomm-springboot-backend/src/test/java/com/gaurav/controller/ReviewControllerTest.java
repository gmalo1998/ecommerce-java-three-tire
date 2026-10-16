
package com.gaurav.controller;

import com.gaurav.exception.ProductException;
import com.gaurav.exception.ReviewNotFoundException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Product;
import com.gaurav.model.Review;
import com.gaurav.model.User;
import com.gaurav.request.CreateReviewRequest;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.ProductService;
import com.gaurav.service.ReviewService;
import com.gaurav.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    void getReviewsByProductId_shouldReturnReviews() {
        Long productId = 1L;
        List<Review> reviews = List.of(mock(Review.class));

        when(reviewService.getReviewsByProductId(productId))
                .thenReturn(reviews);

        ResponseEntity<List<Review>> response =
                reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(reviews, response.getBody());

        verify(reviewService).getReviewsByProductId(productId);
    }

    @Test
    void getReviewsByProductId_shouldReturnEmptyListWhenNoReviewsExist() {
        Long productId = 2L;

        when(reviewService.getReviewsByProductId(productId))
                .thenReturn(List.of());

        ResponseEntity<List<Review>> response =
                reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void writeReview_shouldCreateAndReturnReview()
            throws UserException, ProductException {

        Long productId = 1L;
        String jwt = "Bearer test-token";

        User user = mock(User.class);
        Product product = mock(Product.class);
        Review review = mock(Review.class);
        CreateReviewRequest request = mock(CreateReviewRequest.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(productService.findProductById(productId)).thenReturn(product);
        when(reviewService.createReview(request, user, product))
                .thenReturn(review);

        ResponseEntity<Review> response =
                reviewController.writeReview(request, productId, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(review, response.getBody());

        verify(userService).findUserProfileByJwt(jwt);
        verify(productService).findProductById(productId);
        verify(reviewService).createReview(request, user, product);
    }

    @Test
    void updateReview_shouldUpdateAndReturnReview()
            throws UserException, ReviewNotFoundException,
            AuthenticationException {

        Long reviewId = 10L;
        Long userId = 5L;
        String jwt = "Bearer test-token";

        User user = mock(User.class);
        Review review = mock(Review.class);
        CreateReviewRequest request = mock(CreateReviewRequest.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(reviewService.updateReview(
                reviewId,
                request.getReviewText(),
                request.getReviewRating(),
                userId
        )).thenReturn(review);

        ResponseEntity<Review> response =
                reviewController.updateReview(request, reviewId, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(review, response.getBody());

        verify(reviewService).updateReview(
                reviewId,
                request.getReviewText(),
                request.getReviewRating(),
                userId
        );
    }

    @Test
    void deleteReview_shouldReturnSuccessResponse()
            throws UserException, ReviewNotFoundException,
            AuthenticationException {

        Long reviewId = 10L;
        Long userId = 5L;
        String jwt = "Bearer test-token";

        User user = mock(User.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        ResponseEntity<ApiResponse> response =
                reviewController.deleteReview(reviewId, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Review deleted successfully",
                response.getBody().getMessage());
        assertTrue(response.getBody().isStatus());

        verify(reviewService).deleteReview(reviewId, userId);
    }
}