
import { beforeEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import LoginForm from "./LoginForm";

const { mockDispatch, mockNavigate, mockUseAppSelector, mockSignin, mockSendOtp } =
  vi.hoisted(() => ({
    mockDispatch: vi.fn(),
    mockNavigate: vi.fn(),
    mockUseAppSelector: vi.fn(),
    mockSignin: vi.fn(),
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
  signin: mockSignin,
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

describe("Customer LoginForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAppSelector.mockReturnValue({
      auth: {
        otpSent: false,
        loading: false,
      },
    });
  });

  it("TC21 - renders the login heading and email field", () => {
    render(<LoginForm />);

    expect(
      screen.getByRole("heading", { name: "Login" }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("textbox", { name: "Enter Your Email" }),
    ).toBeInTheDocument();
  });

  it("TC22 - displays Send OTP when OTP has not been sent", () => {
    render(<LoginForm />);

    expect(
      screen.getByRole("button", { name: "Send OTP" }),
    ).toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: "Login" }),
    ).not.toBeInTheDocument();
  });

  it("TC23 - dispatches the OTP request when Send OTP is clicked", () => {
    render(<LoginForm />);

    fireEvent.change(
      screen.getByRole("textbox", { name: "Enter Your Email" }),
      { target: { value: "ganesh@example.com" } },
    );

    fireEvent.click(screen.getByRole("button", { name: "Send OTP" }));

    expect(mockSendOtp).toHaveBeenCalledWith({
      email: "signing_ganesh@example.com",
    });

    expect(mockDispatch).toHaveBeenCalled();
  });

  it("TC24 - displays OTP input and Login button after OTP is sent", () => {
    mockUseAppSelector.mockReturnValue({
      auth: {
        otpSent: true,
        loading: false,
      },
    });

    render(<LoginForm />);

    expect(screen.getByRole("textbox", { name: "OTP" })).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: "Login" }),
    ).toBeInTheDocument();

    expect(screen.getByText(/Enter OTP sent to your mobile number/i))
      .toBeInTheDocument();
  });

  it("TC25 - disables the action button while authentication is loading", () => {
    mockUseAppSelector.mockReturnValue({
      auth: {
        otpSent: false,
        loading: true,
      },
    });

    render(<LoginForm />);

    expect(screen.getByRole("button")).toBeDisabled();
  });
});