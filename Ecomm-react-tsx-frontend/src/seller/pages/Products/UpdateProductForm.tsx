import { useEffect, useState } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";

import {
TextField,
Button,
MenuItem,
Select,
InputLabel,
FormControl,
FormHelperText,
Grid,
CircularProgress,
IconButton,
Snackbar,
Alert,
} from "@mui/material";

import AddPhotoAlternateIcon from "@mui/icons-material/AddPhotoAlternate";
import CloseIcon from "@mui/icons-material/Close";
import { useParams } from "react-router-dom";

import { colors } from "../../../data/Filter/color";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { updateProduct } from "../../../Redux Toolkit/Seller/sellerProductSlice";
import { uploadToCloudinary } from "../../../util/uploadToCloudnary";
import { fetchProductById } from "../../../Redux Toolkit/Customer/ProductSlice";

import type { Seller } from "../../../types/sellerTypes";

interface FormValues {
title: string;
description: string;
mrpPrice: number;
sellingPrice: number;
quantity: number;
color: string;
images: string[];
category: any;
sizes: string;
seller: Seller | undefined;
createdAt: any;
numRatings: number;
in_stock: boolean;
}

const validationSchema = Yup.object({
title: Yup.string()
.min(5, "Title should be at least 5 characters long")
.required("Title is required"),

description: Yup.string()
.min(10, "Description should be at least 10 characters long")
.required("Description is required"),

mrpPrice: Yup.number()
.positive("MRP Price should be greater than zero")
.required("MRP Price is required"),

sellingPrice: Yup.number()
.positive("Selling Price should be greater than zero")
.required("Selling Price is required"),

quantity: Yup.number()
.integer("Quantity must be a whole number")
.min(0, "Quantity cannot be negative")
.required("Quantity is required"),

color: Yup.string().required("Color is required"),
sizes: Yup.string().required("Sizes are required"),
});

const UpdateProductForm = () => {
const [uploadImage, setUploadingImage] = useState(false);
const [snackbarOpen, setOpenSnackbar] = useState(false);

const dispatch = useAppDispatch();
const { sellerProduct, products } = useAppSelector((store) => store);
const { productId } = useParams();

const formik = useFormik<FormValues>({
initialValues: {
title: "",
description: "",
mrpPrice: 0,
sellingPrice: 0,
quantity: 0,
color: "",
images: [],
category: null,
sizes: "",
seller: undefined,
createdAt: null,
numRatings: 0,
in_stock: true,
},


validationSchema,

onSubmit: (values) => {
  if (!productId) return;

  dispatch(
    updateProduct({
      productId: Number(productId),
      product: values,
    })
  );
},


});

const { setValues } = formik;

const handleImageChange = async (
event: React.ChangeEvent<HTMLInputElement>
) => {
const file = event.target.files?.[0];


if (!file) return;

setUploadingImage(true);

try {
  const image = await uploadToCloudinary(file);

  if (image) {
    setValues((previousValues) => ({
      ...previousValues,
      images: [...previousValues.images, image],
    }));
  }
} catch (error) {
  console.error("Image upload failed:", error);
} finally {
  setUploadingImage(false);
  event.target.value = "";
}


};

const handleRemoveImage = (index: number) => {
setValues((previousValues) => ({
...previousValues,
images: previousValues.images.filter(
(_, imageIndex) => imageIndex !== index
),
}));
};

const handleCloseSnackbar = () => {
setOpenSnackbar(false);
};

useEffect(() => {
if (productId) {
dispatch(fetchProductById(Number(productId)));
}
}, [dispatch, productId]);

useEffect(() => {
if (sellerProduct.productCreated || sellerProduct.error) {
setOpenSnackbar(true);
}
}, [sellerProduct.productCreated, sellerProduct.error]);

useEffect(() => {
const product = products.product;


if (!product) return;

setValues({
  title: product.title || "",
  description: product.description || "",
  mrpPrice: product.mrpPrice || 0,
  sellingPrice: product.sellingPrice || 0,
  quantity: product.quantity || 0,
  color: product.color || "",
  images: product.images || [],
  category: product.category,
  sizes: product.sizes || "",
  seller: product.seller,
  createdAt: product.createdAt || null,
  numRatings: product.numRatings || 0,
  in_stock: product.in_stock ?? true,
});


}, [products.product, setValues]);

return ( <div> <form onSubmit={formik.handleSubmit} className="space-y-4 p-4"> <Grid container spacing={2}> <Grid size={12} className="flex flex-wrap gap-5">
<input
type="file"
accept="image/*"
id="fileInput"
style={{ display: "none" }}
onChange={handleImageChange}
/>


        <label className="relative" htmlFor="fileInput">
          <span className="w-24 h-24 cursor-pointer flex items-center justify-center p-3 border rounded-md border-gray-400">
            <AddPhotoAlternateIcon className="text-gray-700" />
          </span>

          {uploadImage && (
            <div className="absolute left-0 right-0 top-0 bottom-0 w-24 h-24 flex justify-center items-center">
              <CircularProgress />
            </div>
          )}
        </label>

        <div className="flex flex-wrap gap-2">
          {formik.values.images.map((image, index) => (
            <div
              className="relative"
              key={`${image}-${index}`}
            >
              <img
                className="w-24 h-24 object-cover"
                src={image}
                alt={`ProductImage ${index + 1}`}
              />

              <IconButton
                type="button"
                onClick={() => handleRemoveImage(index)}
                size="small"
                color="error"
                sx={{
                  position: "absolute",
                  top: 0,
                  right: 0,
                  outline: "none",
                }}
              >
                <CloseIcon sx={{ fontSize: "1rem" }} />
              </IconButton>
            </div>
          ))}
        </div>
      </Grid>

      <Grid size={12}>
        <TextField
          fullWidth
          id="title"
          name="title"
          label="Title"
          value={formik.values.title}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.title && Boolean(formik.errors.title)}
          helperText={formik.touched.title && formik.errors.title}
          required
        />
      </Grid>

      <Grid size={12}>
        <TextField
          multiline
          rows={4}
          fullWidth
          id="description"
          name="description"
          label="Description"
          value={formik.values.description}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.description &&
            Boolean(formik.errors.description)
          }
          helperText={
            formik.touched.description && formik.errors.description
          }
          required
        />
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <TextField
          fullWidth
          id="mrpPrice"
          name="mrpPrice"
          label="MRP Price"
          type="number"
          value={formik.values.mrpPrice}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.mrpPrice &&
            Boolean(formik.errors.mrpPrice)
          }
          helperText={
            formik.touched.mrpPrice && formik.errors.mrpPrice
          }
          required
        />
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <TextField
          fullWidth
          id="sellingPrice"
          name="sellingPrice"
          label="Selling Price"
          type="number"
          value={formik.values.sellingPrice}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.sellingPrice &&
            Boolean(formik.errors.sellingPrice)
          }
          helperText={
            formik.touched.sellingPrice &&
            formik.errors.sellingPrice
          }
          required
        />
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <TextField
          fullWidth
          id="quantity"
          name="quantity"
          label="Quantity"
          type="number"
          value={formik.values.quantity}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.quantity &&
            Boolean(formik.errors.quantity)
          }
          helperText={
            formik.touched.quantity && formik.errors.quantity
          }
          required
        />
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <FormControl
          fullWidth
          error={formik.touched.color && Boolean(formik.errors.color)}
          required
        >
          <InputLabel id="color-label">Color</InputLabel>

          <Select
            labelId="color-label"
            id="color"
            name="color"
            value={formik.values.color}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            label="Color"
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>

            {colors.map((color) => (
              <MenuItem key={color.name} value={color.name}>
                <div className="flex gap-3">
                  <span
                    style={{ backgroundColor: color.hex }}
                    className={`h-5 w-5 rounded-full ${
                      color.name === "White" ? "border" : ""
                    }`}
                  />
                  <p>{color.name}</p>
                </div>
              </MenuItem>
            ))}
          </Select>

          {formik.touched.color && formik.errors.color && (
            <FormHelperText>{formik.errors.color}</FormHelperText>
          )}
        </FormControl>
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <FormControl
          fullWidth
          error={formik.touched.sizes && Boolean(formik.errors.sizes)}
          required
        >
          <InputLabel id="sizes-label">Sizes</InputLabel>

          <Select
            labelId="sizes-label"
            id="sizes"
            name="sizes"
            value={formik.values.sizes}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            label="Sizes"
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>
            <MenuItem value="FREE">FREE</MenuItem>
            <MenuItem value="S">S</MenuItem>
            <MenuItem value="M">M</MenuItem>
            <MenuItem value="L">L</MenuItem>
            <MenuItem value="XL">XL</MenuItem>
          </Select>

          {formik.touched.sizes && formik.errors.sizes && (
            <FormHelperText>{formik.errors.sizes}</FormHelperText>
          )}
        </FormControl>
      </Grid>

      <Grid size={12}>
        <Button
          sx={{ p: "14px" }}
          color="primary"
          variant="contained"
          fullWidth
          type="submit"
          disabled={sellerProduct.loading || uploadImage}
        >
          {sellerProduct.loading ? (
            <CircularProgress size={27} color="inherit" />
          ) : (
            "Update Product"
          )}
        </Button>
      </Grid>
    </Grid>
  </form>

  <Snackbar
    anchorOrigin={{ vertical: "top", horizontal: "right" }}
    open={snackbarOpen}
    autoHideDuration={6000}
    onClose={handleCloseSnackbar}
  >
    <Alert
      onClose={handleCloseSnackbar}
      severity={sellerProduct.error ? "error" : "success"}
      variant="filled"
      sx={{ width: "100%" }}
    >
      {sellerProduct.error
        ? sellerProduct.error
        : "Product updated successfully"}
    </Alert>
  </Snackbar>
</div>


);
};

export default UpdateProductForm;
