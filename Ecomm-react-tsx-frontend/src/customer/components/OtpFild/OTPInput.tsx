
import React, { useState, useEffect } from "react";
import type { ChangeEvent, KeyboardEvent } from "react";

interface OTPInputProps {
  length: number;
  onChange: (otp: string) => void;
  error?: boolean;
}

const OTPInput: React.FC<OTPInputProps> = ({
  length,
  onChange,
  error = false,
}) => {
  const [otp, setOtp] = useState<string[]>(Array(length).fill(""));

  useEffect(() => {
    onChange(otp.join(""));
  }, [otp, onChange]);

  const handleChange = (
    e: ChangeEvent<HTMLInputElement>,
    index: number
  ) => {
    const value = e.target.value.replace(/[^0-9]/g, "");

    if (value.length <= 1) {
      const newOtp = [...otp];
      newOtp[index] = value;
      setOtp(newOtp);

      // Move to the next input when a digit is entered.
      if (value && index < otp.length - 1) {
        const nextInput = document.getElementById(
          `otp-input-${index + 1}`
        );

        nextInput?.focus();
      }
    }
  };

  const handleKeyDown = (
    e: KeyboardEvent<HTMLInputElement>,
    index: number
  ) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      const prevInput = document.getElementById(
        `otp-input-${index - 1}`
      );

      prevInput?.focus();
    }
  };

  return (
    <div className="flex gap-3">
      {otp.map((item, index) => (
        <input
          key={index}
          id={`otp-input-${index}`}
          type="text"
          inputMode="numeric"
          value={item}
          onChange={(e) => handleChange(e, index)}
          onKeyDown={(e) => handleKeyDown(e, index)}
          maxLength={1}
          aria-label={`OTP digit ${index + 1}`}
          className={`mt-1 block h-10 w-10 rounded-md border px-3 py-2 text-center shadow-sm focus:outline-none sm:text-sm ${
            error
              ? "border-red-500"
              : "border-gray-300 focus:border-teal-500"
          }`}
        />
      ))}
    </div>
  );
};

export default OTPInput;

