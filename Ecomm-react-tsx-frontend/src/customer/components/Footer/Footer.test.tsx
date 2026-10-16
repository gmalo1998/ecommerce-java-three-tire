import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import Footer from "./Footer";

describe("Footer component", () => {
  it("TC17 - renders the Shopzy brand name", () => {
    render(<Footer />);

    expect(screen.getByText("Shopzy")).toBeInTheDocument();
  });

  it("TC18 - displays the current year in copyright text", () => {
    render(<Footer />);

    const currentYear = new Date().getFullYear();

    expect(
      screen.getByText(
        `© ${currentYear} Shopzy. All rights reserved.`
      )
    ).toBeInTheDocument();
  });

  it("TC19 - displays all footer navigation links", () => {
    render(<Footer />);

    expect(screen.getByRole("link", { name: "Home" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "About" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Services" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Contact" })).toBeInTheDocument();
  });

  it("TC20 - renders a footer landmark", () => {
    render(<Footer />);

    expect(screen.getByRole("contentinfo")).toBeInTheDocument();
  });
});