
/* eslint-disable @typescript-eslint/no-explicit-any */

import { Button, CircularProgress, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import { useFormik } from "formik";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { useNavigate } from "react-router-dom";
import {
  sendLoginSignupOtp,
  signin,
} from "../../../Redux Toolkit/Customer/AuthSlice";
import OTPInput from "../../../customer/components/OtpFild/OTPInput";

const AdminLoginForm = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { auth } = useAppSelector((store) => store);

  const [otp, setOtp] = useState("");
  const [timer, setTimer] = useState<number>(30);
  const [isTimerActive, setIsTimerActive] = useState<boolean>(false);

  const formik = useFormik({
    initialValues: {
      email: "",
      otp: "",
    },
    onSubmit: (values: any) => {
      dispatch(signin({ email: values.email, otp, navigate }));
    },
  });

  const handleOtpChange = (value: string) => {
    setOtp(value);
  };

  const handleResendOTP = () => {
    dispatch(
      sendLoginSignupOtp({
        email: "signing_" + formik.values.email,
      })
    );

    setTimer(30);
    setIsTimerActive(true);
  };

  const handleSentOtp = () => {
    handleResendOTP();
  };

  const handleLogin = () => {
    formik.handleSubmit();
  };

  useEffect(() => {
    if (!isTimerActive) {
      return;
    }

    const interval = window.setInterval(() => {
      setTimer((previousTimer) => {
        if (previousTimer <= 1) {
          window.clearInterval(interval);
          setIsTimerActive(false);
          return 30;
        }

        return previousTimer - 1;
      });
    }, 1000);

    return () => {
      window.clearInterval(interval);
    };
  }, [isTimerActive]);

  return (
    <div>
      <h1 className="text-center font-bold text-xl text-primary-color pb-8">
        Login
      </h1>

      <form
        className="space-y-5"
        onSubmit={(event) => {
          event.preventDefault();

          if (auth.otpSent) {
            handleLogin();
          } else {
            handleSentOtp();
          }
        }}
      >
        <TextField
          fullWidth
          name="email"
          type="email"
          label="Enter Your Email"
          value={formik.values.email}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.email && Boolean(formik.errors.email)
          }
          helperText={
            formik.touched.email
              ? (formik.errors.email as string)
              : undefined
          }
        />

        {auth.otpSent && (
          <div className="space-y-2">
            <p className="font-medium text-sm">
              Enter the OTP sent to your registered email or mobile.
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
                  <button
                    type="button"
                    onClick={handleResendOTP}
                    disabled={auth.loading}
                    className="text-teal-600 cursor-pointer hover:text-teal-800 font-semibold"
                  >
                    Resend OTP
                  </button>
                </>
              )}
            </p>

            {formik.touched.otp && formik.errors.otp && (
              <p>{formik.errors.otp as string}</p>
            )}

            <Button
              type="submit"
              disabled={auth.loading}
              fullWidth
              variant="contained"
              sx={{ py: "11px" }}
            >
              {auth.loading ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                "Login"
              )}
            </Button>
          </div>
        )}

        {!auth.otpSent && (
          <Button
            type="submit"
            disabled={auth.loading || !formik.values.email.trim()}
            fullWidth
            variant="contained"
            sx={{ py: "11px" }}
          >
            {auth.loading ? (
              <CircularProgress size={24} color="inherit" />
            ) : (
              "Send OTP"
            )}
          </Button>
        )}
      </form>
    </div>
  );
};

export default AdminLoginForm;

