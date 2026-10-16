
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<Review>> getReviewsByProductId(
            @PathVariable Long productId) {

        log.debug("Fetching reviews for product ID: {}", productId);

        List<Review> reviews = reviewService.getReviewsByProductId(productId);

        log.debug("Review lookup completed for product ID: {}. Result count: {}",
                productId, reviews.size());

        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<Review> writeReview(
            @RequestBody CreateReviewRequest req,
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt)
            throws UserException, ProductException {

        log.debug("Creating review for product ID: {}", productId);

        User user = userService.findUserProfileByJwt(jwt);
        Product product = productService.findProductById(productId);

        Review review = reviewService.createReview(req, user, product);

        log.info("Review created successfully for product ID: {}", productId);

        return ResponseEntity.ok(review);
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @RequestBody CreateReviewRequest req,
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt)
            throws UserException, ReviewNotFoundException, AuthenticationException {

        log.debug("Updating review ID: {}", reviewId);

        User user = userService.findUserProfileByJwt(jwt);

        Review review = reviewService.updateReview(
                reviewId,
                req.getReviewText(),
                req.getReviewRating(),
                user.getId()
        );

        log.info("Review updated successfully. Review ID: {}", reviewId);

        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt)
            throws UserException, ReviewNotFoundException, AuthenticationException {

        log.debug("Deleting review ID: {}", reviewId);

        User user = userService.findUserProfileByJwt(jwt);

        reviewService.deleteReview(reviewId, user.getId());

        ApiResponse response = new ApiResponse();
        response.setMessage("Review deleted successfully");
        response.setStatus(true);

        log.info("Review deleted successfully. Review ID: {}", reviewId);

        return ResponseEntity.ok(response);
    }
}