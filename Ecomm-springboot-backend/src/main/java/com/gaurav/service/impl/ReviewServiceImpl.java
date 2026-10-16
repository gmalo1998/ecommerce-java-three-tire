
package com.gaurav.service.impl;

import com.gaurav.exception.ReviewNotFoundException;
import com.gaurav.model.Product;
import com.gaurav.model.Review;
import com.gaurav.model.User;
import com.gaurav.repository.ReviewRepository;
import com.gaurav.request.CreateReviewRequest;
import com.gaurav.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public Review createReview(
            CreateReviewRequest req,
            User user,
            Product product
    ) {
        if (req == null) {
            throw new IllegalArgumentException(
                    "Review request must not be null"
            );
        }

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "A valid user is required to create a review"
            );
        }

        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException(
                    "A valid product is required to create a review"
            );
        }

        if (req.getReviewText() == null
                || req.getReviewText().isBlank()) {
            throw new IllegalArgumentException(
                    "Review text must not be blank"
            );
        }

        double rating = req.getReviewRating();

        if (!Double.isFinite(rating) || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Review rating must be between 1 and 5"
            );
        }

        Review newReview = new Review();
        newReview.setReviewText(req.getReviewText());
        newReview.setRating(rating);
        newReview.setProductImages(req.getProductImages());
        newReview.setUser(user);
        newReview.setProduct(product);

        if (product.getReviews() != null) {
            product.getReviews().add(newReview);
        }

        Review savedReview = reviewRepository.save(newReview);

        log.info(
                "Review created successfully. reviewId={}, productId={}, userId={}",
                savedReview.getId(),
                product.getId(),
                user.getId()
        );

        return savedReview;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID must not be null"
            );
        }

        List<Review> reviews =
                reviewRepository.findReviewsByProductId(productId);

        log.debug(
                "Reviews retrieved. productId={}, count={}",
                productId,
                reviews.size()
        );

        return reviews;
    }

    @Override
    @Transactional
    public Review updateReview(
            Long reviewId,
            String reviewText,
            double rating,
            Long userId
    ) throws ReviewNotFoundException, AuthenticationException {

        if (reviewId == null || userId == null) {
            throw new IllegalArgumentException(
                    "Review ID and user ID must not be null"
            );
        }

        if (reviewText == null || reviewText.isBlank()) {
            throw new IllegalArgumentException(
                    "Review text must not be blank"
            );
        }

        if (!Double.isFinite(rating) || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Review rating must be between 1 and 5"
            );
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn(
                            "Review not found for update. reviewId={}",
                            reviewId
                    );
                    return new ReviewNotFoundException(
                            "Review not found"
                    );
                });

        if (review.getUser() == null
                || !Objects.equals(review.getUser().getId(), userId)) {
            log.warn(
                    "Unauthorized review update attempt. reviewId={}, userId={}",
                    reviewId,
                    userId
            );

            throw new AuthenticationException(
                    "You do not have permission to update this review"
            );
        }

        review.setReviewText(reviewText);
        review.setRating(rating);

        Review updatedReview = reviewRepository.save(review);

        log.info(
                "Review updated successfully. reviewId={}, userId={}",
                reviewId,
                userId
        );

        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(
            Long reviewId,
            Long userId
    ) throws ReviewNotFoundException, AuthenticationException {

        if (reviewId == null || userId == null) {
            throw new IllegalArgumentException(
                    "Review ID and user ID must not be null"
            );
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn(
                            "Review not found for deletion. reviewId={}",
                            reviewId
                    );
                    return new ReviewNotFoundException(
                            "Review not found"
                    );
                });

        if (review.getUser() == null
                || !Objects.equals(review.getUser().getId(), userId)) {
            log.warn(
                    "Unauthorized review deletion attempt. reviewId={}, userId={}",
                    reviewId,
                    userId
            );

            throw new AuthenticationException(
                    "You do not have permission to delete this review"
            );
        }

        reviewRepository.delete(review);

        log.info(
                "Review deleted successfully. reviewId={}, userId={}",
                reviewId,
                userId
        );
    }
}

