
package com.gaurav.controller;

import com.gaurav.exception.CartItemException;
import com.gaurav.exception.ProductException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Cart;
import com.gaurav.model.CartItem;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.request.AddItemRequest;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.CartItemService;
import com.gaurav.service.CartService;
import com.gaurav.service.ProductService;
import com.gaurav.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<Cart> findUserCartHandler(
            @RequestHeader("Authorization") String jwt)
            throws UserException {

        User user = userService.findUserProfileByJwt(jwt);
        Cart cart = cartService.findUserCart(user);

        log.debug("Cart retrieved successfully for user ID: {}", user.getId());

        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(
            @RequestBody AddItemRequest req,
            @RequestHeader("Authorization") String jwt)
            throws UserException, ProductException {

        User user = userService.findUserProfileByJwt(jwt);
        Product product = productService.findProductById(req.getProductId());

        CartItem item = cartService.addCartItem(
                user,
                product,
                req.getSize(),
                req.getQuantity()
        );

        log.info(
                "Item added to cart successfully for user ID: {}, product ID: {}",
                user.getId(),
                req.getProductId()
        );

        return new ResponseEntity<>(item, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt)
            throws CartItemException, UserException {

        User user = userService.findUserProfileByJwt(jwt);

        cartItemService.removeCartItem(user.getId(), cartItemId);

        ApiResponse response =
                new ApiResponse("Item Remove From Cart", true);

        log.info(
                "Cart item removed successfully. User ID: {}, cart item ID: {}",
                user.getId(),
                cartItemId
        );

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestBody CartItem cartItem,
            @RequestHeader("Authorization") String jwt)
            throws CartItemException, UserException {

        User user = userService.findUserProfileByJwt(jwt);

        if (cartItem.getQuantity() <= 0) {
            log.warn(
                    "Cart item update rejected because quantity is not positive. Cart item ID: {}",
                    cartItemId
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        CartItem updatedCartItem = cartItemService.updateCartItem(
                user.getId(),
                cartItemId,
                cartItem
        );

        log.info(
                "Cart item updated successfully. User ID: {}, cart item ID: {}",
                user.getId(),
                cartItemId
        );

        return new ResponseEntity<>(updatedCartItem, HttpStatus.ACCEPTED);
    }
}