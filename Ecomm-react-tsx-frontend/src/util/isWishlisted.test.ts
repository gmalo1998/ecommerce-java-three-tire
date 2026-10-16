import { describe, it, expect } from "vitest";
import { isWishlisted } from "./isWishlisted";
import type { Wishlist } from "../types/wishlistTypes";
import type { Product } from "../types/productTypes";

const createProduct = (id: number) =>
  ({ id }) as Product;

const createWishlist = (products: Product[]) =>
  ({ products }) as Wishlist;

describe("isWishlisted", () => {
  it("TC07 - returns true when product exists in wishlist", () => {
    const product = createProduct(1);
    const wishlist = createWishlist([product]);

    expect(isWishlisted(wishlist, product)).toBe(true);
  });

  it("TC08 - returns false when product is not in wishlist", () => {
    const product = createProduct(2);
    const wishlist = createWishlist([createProduct(1)]);

    expect(isWishlisted(wishlist, product)).toBe(false);
  });

  it("TC09 - returns false for an empty wishlist", () => {
    const product = createProduct(1);
    const wishlist = createWishlist([]);

    expect(isWishlisted(wishlist, product)).toBe(false);
  });

  it("TC10 - matches products by ID", () => {
    const product = createProduct(5);
    const sameIdProduct = createProduct(5);
    const wishlist = createWishlist([sameIdProduct]);

    expect(isWishlisted(wishlist, product)).toBe(true);
  });
});