import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import NotFound from "./NotFound";

describe("NotFound component", () => {
  it("TC14 - displays the Page Not Found heading", () => {
    render(<NotFound />);

    expect(
      screen.getByRole("heading", { name: "Page Not Found" })
    ).toBeInTheDocument();
  });

  it("TC15 - renders the heading as an h1", () => {
    render(<NotFound />);

    expect(
      screen.getByRole("heading", {
        level: 1,
        name: "Page Not Found",
      })
    ).toBeInTheDocument();
  });
});