
import {
  Button,
  CircularProgress,
  Step,
  StepLabel,
  Stepper,
} from "@mui/material";
import { useState } from "react";
import { useFormik } from "formik";

import BecomeSellerFormStep1 from "./BecomeSellerFormStep1";
import BecomeSellerFormStep2 from "./BecomeSellerFormStep2";
import BecomeSellerFormStep3 from "./BecomeSellerFormStep3";
import BecomeSellerFormStep4 from "./BecomeSellerFormStep4";

import {
  useAppDispatch,
  useAppSelector,
} from "../../../Redux Toolkit/Store";

import { createSeller } from "../../../Redux Toolkit/Seller/sellerAuthenticationSlice";

const steps = [
  "Tax Details & Mobile",
  "Pickup Address",
  "Bank Details",
  "Supplier Details",
];

const SellerAccountForm = () => {
  const [activeStep, setActiveStep] = useState(0);

  const dispatch = useAppDispatch();

  const sellerAuth = useAppSelector((store) => store.sellerAuth);

  const handleStep = (value: number) => {
    setActiveStep((currentStep) =>
      Math.max(0, Math.min(currentStep + value, steps.length - 1))
    );
  };

  const formik = useFormik({
    initialValues: {
      mobile: "",
      otp: "",
      gstin: "",
      pickupAddress: {
        name: "",
        mobile: "",
        pincode: "",
        address: "",
        locality: "",
        city: "",
        state: "",
      },
      bankDetails: {
        accountNumber: "",
        ifscCode: "",
        accountHolderName: "",
      },
      sellerName: "",
      email: "",
      businessDetails: {
        businessName: "",
        businessEmail: "",
        businessMobile: "",
        logo: "",
        banner: "",
        businessAddress: "",
      },
      password: "",
    },
    onSubmit: (values) => {
      dispatch(createSeller(values));
    },
  });

  return (
    <div>
      <Stepper activeStep={activeStep} alternativeLabel>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      <div className="mt-20 space-y-10">
        <div>
          {activeStep === 0 ? (
            <BecomeSellerFormStep1 formik={formik} />
          ) : activeStep === 1 ? (
            <BecomeSellerFormStep2 formik={formik} />
          ) : activeStep === 2 ? (
            <BecomeSellerFormStep3 formik={formik} />
          ) : (
            <BecomeSellerFormStep4 formik={formik} />
          )}
        </div>

        <div className="flex items-center justify-between">
          <Button
            disabled={activeStep === 0}
            onClick={() => handleStep(-1)}
            variant="contained"
          >
            Back
          </Button>

          <Button
            disabled={Boolean(sellerAuth?.loading)}
            onClick={() => {
              if (activeStep === steps.length - 1) {
                formik.handleSubmit();
              } else {
                handleStep(1);
              }
            }}
            variant="contained"
          >
            {activeStep === steps.length - 1 ? (
              sellerAuth?.loading ? (
                <CircularProgress size={27} />
              ) : (
                "Create Account"
              )
            ) : (
              "Continue"
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SellerAccountForm;

