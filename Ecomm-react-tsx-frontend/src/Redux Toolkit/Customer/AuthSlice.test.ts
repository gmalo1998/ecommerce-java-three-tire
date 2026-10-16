import { vi, beforeEach, describe, it, expect } from "vitest";
import { api } from "../../Config/Api";
import store from "../Store";
import reducer, {
logout,
sendLoginSignupOtp,
signin,
} from "./AuthSlice";

vi.mock("../../Config/Api", () => ({
api: {
post: vi.fn(),
},
}));

const mockedPost = vi.mocked(api.post);

beforeEach(() => {
vi.clearAllMocks();
localStorage.clear();
});

describe("Customer AuthSlice", () => {
it("TC41 - returns the initial authentication state", () => {
const state = reducer(undefined, { type: "unknown" });


expect(state).toEqual({
  jwt: null,
  role: null,
  loading: false,
  error: null,
  otpSent: false,
});


});

it("TC42 - sets loading when OTP request starts", () => {
const state = reducer(
undefined,
sendLoginSignupOtp.pending("request-1", {
email: "[ganesh@example.com](mailto:ganesh@example.com)",
}),
);


expect(state.loading).toBe(true);
expect(state.error).toBeNull();


});

it("TC43 - marks OTP as sent when the request succeeds", () => {
const state = reducer(
{
jwt: null,
role: null,
loading: true,
error: null,
otpSent: false,
},
sendLoginSignupOtp.fulfilled(
{ message: "OTP sent" } as never,
"request-2",
{ email: "[ganesh@example.com](mailto:ganesh@example.com)" },
),
);


expect(state.loading).toBe(false);
expect(state.otpSent).toBe(true);


});

it("TC44 - stores the JWT and role after successful sign-in", () => {
const state = reducer(
undefined,
signin.fulfilled(
{ jwt: "test-jwt", role: "CUSTOMER" } as never,
"request-3",
{
email: "[ganesh@example.com](mailto:ganesh@example.com)",
otp: "123456",
navigate: (() => {}) as never,
},
),
);


expect(state.jwt).toBe("test-jwt");
expect(state.role).toBe("CUSTOMER");
expect(state.loading).toBe(false);


});

it("TC45 - clears JWT and role on logout", () => {
const state = reducer(
{
jwt: "test-jwt",
role: "CUSTOMER",
loading: false,
error: null,
otpSent: true,
},
logout(),
);


expect(state.jwt).toBeNull();
expect(state.role).toBeNull();


});

it("TC46 - sends an OTP request to the correct endpoint", async () => {
mockedPost.mockResolvedValueOnce({
data: { message: "OTP sent" },
} as never);


const result = await store.dispatch(
  sendLoginSignupOtp({ email: "ganesh@example.com" }),
);

expect(mockedPost).toHaveBeenCalledWith(
  "/auth/sent/login-signup-otp",
  { email: "ganesh@example.com" },
);

expect(sendLoginSignupOtp.fulfilled.match(result)).toBe(true);


});
});
