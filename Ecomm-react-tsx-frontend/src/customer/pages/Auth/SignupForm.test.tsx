
import { beforeEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import SignupForm from "./SignupForm";

const {
  mockDispatch,
  mockNavigate,
  mockUseAppSelector,
  mockSignup,
  mockSendOtp,
} = vi.hoisted(() => ({
  mockDispatch: vi.fn(),
  mockNavigate: vi.fn(),
  mockUseAppSelector: vi.fn(),
  mockSignup: vi.fn(),
  mockSendOtp: vi.fn(),
}));

vi.mock("react-router-dom", () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock("../../../Redux Toolkit/Store", () => ({
  useAppDispatch: () => mockDispatch,
  useAppSelector: mockUseAppSelector,
}));

vi.mock("../../../Redux Toolkit/Customer/AuthSlice", () => ({
  signup: mockSignup,
  sendLoginSignupOtp: mockSendOtp,
}));

vi.mock("../../components/OtpFild/OTPInput", () => ({
  default: ({ onChange }: { onChange: (value: string) => void }) => (
    <input
      aria-label="OTP"
      onChange={(event) => onChange(event.target.value)}
    />
  ),
}));

describe("Customer SignupForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    mockUseAppSelector.mockReturnValue({
      auth: {
        otpSent: false,
        loading: false,
      },
    });

    mockSendOtp.mockReturnValue({ type: "auth/sendOtp" });
    mockSignup.mockReturnValue({ type: "auth/signup" });
  });

  it("TC26 - renders the Signup heading and email field", () => {
    render(<SignupForm />);

    expect(
      screen.getByRole("heading", { name: "Signup" }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("textbox", { name: "Enter Your Email" }),
    ).toBeInTheDocument();
  });

  it("TC27 - dispatches an OTP request with the entered email", () => {
    render(<SignupForm />);

    fireEvent.change(
      screen.getByRole("textbox", { name: "Enter Your Email" }),
      { target: { value: "ganesh@example.com" } },
    );

    fireEvent.click(screen.getByRole("button", { name: "Send OTP" }));

    expect(mockSendOtp).toHaveBeenCalledWith({
      email: "ganesh@example.com",
    });

    expect(mockDispatch).toHaveBeenCalled();
  });

  it("TC28 - displays OTP and name fields after OTP is sent", () => {
    mockUseAppSelector.mockReturnValue({
      auth: {
        otpSent: true,
        loading: false,
      },
    });

    render(<SignupForm />);

    expect(screen.getByRole("textbox", { name: "OTP" }))
      .toBeInTheDocument();

    expect(screen.getByRole("textbox", { name: "Enter Your Name" }))
      .toBeInTheDocument();

    expect(screen.getByRole("button", { name: "Signup" }))
      .toBeInTheDocument();
  });

  
it("TC29 - dispatches signup with the entered name, email and OTP", async () => {
  mockUseAppSelector.mockReturnValue({
    auth: { otpSent: true, loading: false },
  });

  render(<SignupForm />);

  fireEvent.change(screen.getByLabelText(/enter your email/i), {
    target: { value: "ganesh@example.com" },
  });

  fireEvent.change(screen.getByLabelText(/enter your name/i), {
    target: { value: "Ganesh" },
  });

  fireEvent.change(screen.getByLabelText("OTP"), {
    target: { value: "123456" },
  });

  fireEvent.click(screen.getByRole("button", { name: /signup/i }));

  await vi.waitFor(() => {
    expect(mockSignup).toHaveBeenCalledWith(
      expect.objectContaining({
        fullName: "Ganesh",
        email: "ganesh@example.com",
        otp: "123456",
        navigate: expect.any(Function),
      }),
    );
  });
});

  it("TC30 - disables the Send OTP button while loading", () => {
    mockUseAppSelector.mockReturnValue({
      auth: {
        otpSent: false,
        loading: true,
      },
    });

    render(<SignupForm />);

    expect(screen.getByRole("button")).toBeDisabled();
  });
});