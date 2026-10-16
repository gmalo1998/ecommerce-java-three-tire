
/* eslint-disable @typescript-eslint/no-explicit-any */

import { Button, CircularProgress, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import OTPInput from "../../components/OtpFild/OTPInput";
import { useFormik } from "formik";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { useNavigate } from "react-router-dom";
import {
  sendLoginSignupOtp,
  signup,
} from "../../../Redux Toolkit/Customer/AuthSlice";

const SignupForm = () => {
  const navigate = useNavigate();

  const [otp, setOtp] = useState("");
  const [timer, setTimer] = useState<number>(30);
  const [isTimerActive, setIsTimerActive] = useState<boolean>(false);

  const dispatch = useAppDispatch();
  const { auth } = useAppSelector((store) => store);

  const formik = useFormik({
    initialValues: {
      email: "",
      otp: "",
      name: "",
    },
    onSubmit: (values: any) => {
      dispatch(
        signup({
          fullName: values.name,
          email: values.email,
          otp,
          navigate,
        })
      );
    },
  });

  const handleOtpChange = (value: any) => {
    setOtp(value);
  };

  const handleResendOTP = () => {
    dispatch(sendLoginSignupOtp({ email: formik.values.email }));

    setTimer(30);
    setIsTimerActive(true);
  };

  const handleSentOtp = () => {
    handleResendOTP();
  };

  const handleSignup = () => {
    formik.handleSubmit();
  };

  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | undefined;

    if (isTimerActive) {
      interval = setInterval(() => {
        setTimer((prev) => {
          if (prev === 1) {
            setIsTimerActive(false);
            return 30;
          }

          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isTimerActive]);

  return (
    <div>
      <h1 className="text-center font-bold text-xl text-primary-color pb-5">
        Signup
      </h1>

      <form
        className="space-y-5"
        onSubmit={(event) => event.preventDefault()}
      >
        <TextField
          fullWidth
          name="email"
          label="Enter Your Email"
          value={formik.values.email}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.email && Boolean(formik.errors.email)}
          helperText={
            formik.touched.email
              ? (formik.errors.email as string)
              : undefined
          }
        />

        {auth.otpSent && (
          <div className="space-y-2">
            <p className="font-medium text-sm">
              Enter OTP sent to your mobile number
            </p>

            <OTPInput
              length={6}
              onChange={handleOtpChange}
              error={false}
            />

            <p className="text-xs space-x-2">
              {isTimerActive ? (
                <span>Resend OTP in {timer} seconds</span>
              ) : (
                <>
                  Didn't receive OTP?{" "}
                  <span
                    onClick={handleResendOTP}
                    className="text-teal-600 cursor-pointer hover:text-teal-800 font-semibold"
                  >
                    Resend OTP
                  </span>
                </>
              )}
            </p>

            {formik.touched.otp && formik.errors.otp && (
              <p>{formik.errors.otp as string}</p>
            )}
          </div>
        )}

        {auth.otpSent && (
          <TextField
            fullWidth
            name="name"
            label="Enter Your Name"
            value={formik.values.name}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.name && Boolean(formik.errors.name)}
            helperText={
              formik.touched.name
                ? (formik.errors.name as string)
                : undefined
            }
          />
        )}

        {auth.otpSent && (
          <div>
            <Button
              type="button"
              disabled={auth.loading}
              onClick={handleSignup}
              fullWidth
              variant="contained"
              sx={{ py: "11px" }}
            >
              {auth.loading ? (
                <CircularProgress
                  size={27}
                  sx={{ width: "27px", height: "27px" }}
                />
              ) : (
                "Signup"
              )}
            </Button>
          </div>
        )}

        {!auth.otpSent && (
          <Button
            type="button"
            fullWidth
            variant="contained"
            onClick={handleSentOtp}
            disabled={auth.loading}
            sx={{ py: "11px" }}
          >
            {auth.loading ? (
              <CircularProgress
                size={27}
                sx={{ width: "27px", height: "27px" }}
              />
            ) : (
              "Send OTP"
            )}
          </Button>
        )}
      </form>
    </div>
  );
};

export default SignupForm;

