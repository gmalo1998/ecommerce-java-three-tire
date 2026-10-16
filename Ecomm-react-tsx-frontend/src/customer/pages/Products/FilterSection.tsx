
import {
  Button,
  Divider,
  FormControl,
  FormControlLabel,
  FormLabel,
  Radio,
  RadioGroup,
} from "@mui/material";
import { useState } from "react";
import type { ChangeEvent } from "react";
import { teal } from "@mui/material/colors";
import { colors } from "../../../data/Filter/color";
import { price } from "../../../data/Filter/price";
import { discount } from "../../../data/Filter/discount";
import { useSearchParams } from "react-router-dom";

const FilterSection = () => {
  const [expendColor, setExpendColor] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();

  const handleExpendColor = () => {
    setExpendColor(!expendColor);
  };

  const updateFilterParams = (e: ChangeEvent<HTMLInputElement>) => {
    const { value, name } = e.target;
    const updatedParams = new URLSearchParams(searchParams);

    if (value) {
      updatedParams.set(name, value);
    } else {
      updatedParams.delete(name);
    }

    setSearchParams(updatedParams);
  };

  const clearAllFilters = () => {
    setSearchParams(new URLSearchParams());
  };

  return (
    <div className="-z-50 space-y-5 bg-white">
      <div className="flex h-[40px] items-center justify-between px-9 lg:border-r">
        <p className="text-lg font-semibold">Filters</p>

        <Button
          onClick={clearAllFilters}
          size="small"
          className="cursor-pointer font-semibold text-teal-600"
        >
          Clear all
        </Button>
      </div>

      <Divider />

      <div className="space-y-6 px-9">
        <section>
          <FormControl sx={{ zIndex: 0 }}>
            <FormLabel
              sx={{
                fontSize: "16px",
                fontWeight: "bold",
                pb: "14px",
                color: teal[600],
              }}
              className="text-2xl font-semibold"
              id="color"
            >
              Color
            </FormLabel>

            <RadioGroup
              onChange={updateFilterParams}
              aria-labelledby="color"
              defaultValue=""
              name="color"
            >
              {colors
                .slice(0, expendColor ? colors.length : 5)
                .map((item) => (
                  <FormControlLabel
                    sx={{ fontSize: "12px" }}
                    key={item.name}
                    value={item.name}
                    control={<Radio size="small" />}
                    label={
                      <div className="flex items-center gap-3">
                        <p>{item.name}</p>
                        <span
                          style={{ backgroundColor: item.hex }}
                          className="h-5 w-5 rounded-full border"
                        />
                      </div>
                    }
                  />
                ))}
            </RadioGroup>
          </FormControl>

          <div>
            <button
              type="button"
              onClick={handleExpendColor}
              className="flex cursor-pointer items-center text-teal-600 hover:text-teal-900"
            >
              {expendColor ? "Hide" : `+ ${Math.max(colors.length - 5, 0)} more`}
            </button>
          </div>
        </section>

        <Divider />

        <section>
          <FormControl>
            <FormLabel
              sx={{
                fontSize: "16px",
                fontWeight: "bold",
                pb: "14px",
                color: teal[600],
              }}
              className="text-2xl font-semibold"
              id="price"
            >
              Price
            </FormLabel>

            <RadioGroup
              name="price"
              onChange={updateFilterParams}
              aria-labelledby="price"
              defaultValue=""
            >
              {price.map((item) => (
                <FormControlLabel
                  key={item.name}
                  value={item.value}
                  control={<Radio size="small" />}
                  label={item.name}
                />
              ))}
            </RadioGroup>
          </FormControl>
        </section>

        <Divider />

        <section>
          <FormControl>
            <FormLabel
              sx={{
                fontSize: "16px",
                fontWeight: "bold",
                pb: "14px",
                color: teal[600],
              }}
              className="text-2xl font-semibold"
              id="discount"
            >
              Discount
            </FormLabel>

            <RadioGroup
              name="discount"
              onChange={updateFilterParams}
              aria-labelledby="discount"
              defaultValue=""
            >
              {discount.map((item) => (
                <FormControlLabel
                  key={item.name}
                  value={item.value}
                  control={<Radio size="small" />}
                  label={item.name}
                />
              ))}
            </RadioGroup>
          </FormControl>
        </section>
      </div>
    </div>
  );
};

export default FilterSection;

