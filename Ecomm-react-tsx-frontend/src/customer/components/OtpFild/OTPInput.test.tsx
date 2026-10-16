
import { describe, it, expect, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import OTPInput from "./OTPInput";

describe("OTPInput", () => {
  it("TC31 - renders the requested number of OTP inputs", () => {
    const onChange = vi.fn();

    render(<OTPInput length={6} onChange={onChange} />);

    expect(screen.getAllByRole("textbox")).toHaveLength(6);
  });

  it("TC32 - accepts a numeric digit", () => {
    const onChange = vi.fn();

    render(<OTPInput length={6} onChange={onChange} />);

    fireEvent.change(screen.getByLabelText("OTP digit 1"), {
      target: { value: "5" },
    });

    expect(screen.getByLabelText("OTP digit 1")).toHaveValue("5");
  });

  it("TC33 - rejects non-numeric input", () => {
    const onChange = vi.fn();

    render(<OTPInput length={6} onChange={onChange} />);

    fireEvent.change(screen.getByLabelText("OTP digit 1"), {
      target: { value: "a" },
    });

    expect(screen.getByLabelText("OTP digit 1")).toHaveValue("");
  });

  it("TC34 - moves focus to the next input after entering a digit", () => {
    const onChange = vi.fn();

    render(<OTPInput length={6} onChange={onChange} />);

    const firstInput = screen.getByLabelText("OTP digit 1");
    const secondInput = screen.getByLabelText("OTP digit 2");

    fireEvent.change(firstInput, { target: { value: "4" } });

    expect(secondInput).toHaveFocus();
  });

  it("TC35 - sends the combined OTP to the onChange callback", () => {
    const onChange = vi.fn();

    render(<OTPInput length={6} onChange={onChange} />);

    fireEvent.change(screen.getByLabelText("OTP digit 1"), {
      target: { value: "1" },
    });

    fireEvent.change(screen.getByLabelText("OTP digit 2"), {
      target: { value: "2" },
    });

    fireEvent.change(screen.getByLabelText("OTP digit 3"), {
      target: { value: "3" },
    });

    expect(onChange).toHaveBeenLastCalledWith("123");
  });
});