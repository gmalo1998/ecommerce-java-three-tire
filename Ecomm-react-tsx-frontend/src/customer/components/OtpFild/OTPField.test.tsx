
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import OTPField from "./OTPField";

describe("OTPField", () => {
  it("TC36 - renders the provided label", () => {
    render(
      <OTPField
        id="otp"
        name="otp"
        label="Enter OTP"
        value=""
        onChange={() => {}}
      />,
    );

    expect(screen.getByText("Enter OTP")).toBeInTheDocument();
  });

  it("TC37 - renders six OTP input fields", () => {
    render(
      <OTPField
        id="otp"
        name="otp"
        label="Enter OTP"
        value=""
        onChange={() => {}}
      />,
    );

    expect(screen.getAllByRole("textbox")).toHaveLength(6);
  });

  it("TC38 - displays helper text when provided", () => {
    render(
      <OTPField
        id="otp"
        name="otp"
        label="Enter OTP"
        value=""
        onChange={() => {}}
        helperText="OTP is required"
      />,
    );

    expect(screen.getByText("OTP is required")).toBeInTheDocument();
  });

  it("TC39 - applies error styling when error is true", () => {
    render(
      <OTPField
        id="otp"
        name="otp"
        label="Enter OTP"
        value=""
        onChange={() => {}}
        error={true}
      />,
    );

    const inputs = screen.getAllByRole("textbox");

    expect(inputs[0]).toHaveClass("border-red-500");
  });

  it("TC40 - displays helper text with error styling", () => {
    render(
      <OTPField
        id="otp"
        name="otp"
        label="Enter OTP"
        value=""
        onChange={() => {}}
        error={true}
        helperText="Invalid OTP"
      />,
    );

    expect(screen.getByText("Invalid OTP")).toHaveClass("text-red-600");
  });
});