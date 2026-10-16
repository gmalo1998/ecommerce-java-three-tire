import { describe, it, expect } from "vitest";
import {
  sumCartItemSellingPrice,
  sumCartItemMrpPrice,
} from "./cartCalculator";
import type { CartItem } from "../types/cartTypes";

const createItem = (
  sellingPrice: number,
  mrpPrice: number
): CartItem =>
  ({
    sellingPrice,
    mrpPrice,
  }) as CartItem;

describe("Cart Calculator", () => {
  it("TC01 - calculates total selling price", () => {
    const items = [
      createItem(500, 700),
      createItem(300, 400),
    ];

    expect(sumCartItemSellingPrice(items)).toBe(800);
  });

  it("TC02 - calculates total MRP price", () => {
    const items = [
      createItem(500, 700),
      createItem(300, 400),
    ];

    expect(sumCartItemMrpPrice(items)).toBe(1100);
  });

  it("TC03 - returns zero selling price for an empty cart", () => {
    expect(sumCartItemSellingPrice([])).toBe(0);
  });

  it("TC04 - returns zero MRP price for an empty cart", () => {
    expect(sumCartItemMrpPrice([])).toBe(0);
  });

  it("TC05 - calculates price for a single item", () => {
    const items = [createItem(250, 350)];

    expect(sumCartItemSellingPrice(items)).toBe(250);
    expect(sumCartItemMrpPrice(items)).toBe(350);
  });

  it("TC06 - handles zero-priced items", () => {
    const items = [createItem(0, 0)];

    expect(sumCartItemSellingPrice(items)).toBe(0);
    expect(sumCartItemMrpPrice(items)).toBe(0);
  });
});