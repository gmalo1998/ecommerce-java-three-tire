import { describe, it, expect } from "vitest";
import { formatDate } from "./fomateDate";

describe("formatDate", () => {
  it("TC11 - formats a valid date", () => {
    expect(formatDate("2025-01-15")).toBe("Wed, Jan 15");
  });

  it("TC12 - accepts a Date object", () => {
    expect(formatDate(new Date(2025, 0, 15))).toBe("Wed, Jan 15");
  });

  it("TC13 - formats a date with a two-digit day", () => {
    expect(formatDate(new Date(2025, 0, 5))).toBe("Sun, Jan 05");
  });
});