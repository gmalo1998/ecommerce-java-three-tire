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

import { mainCategory } from "../../../data/category/mainCategory";
import { menLevelTwo } from "../../../data/category/level two/menLevelTwo";
import { womenLevelTwo } from "../../../data/category/level two/womenLevelTwo";
import { menLevelThree } from "../../../data/category/level three/menLevelThree";
import { womenLevelThree } from "../../../data/category/level three/womenLevelThree";
import { colors } from "../../../data/Filter/color";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { createProduct } from "../../../Redux Toolkit/Seller/sellerProductSlice";
import { uploadToCloudinary } from "../../../util/uploadToCloudnary";
import { electronicsLevelThree } from "../../../data/category/level three/electronicsLevelThree";
import { electronicsLevelTwo } from "../../../data/category/level two/electronicsLavelTwo";
import { furnitureLevelTwo } from "../../../data/category/level two/furnitureLevleTwo";
import { furnitureLevelThree } from "../../../data/category/level three/furnitureLevelThree";

const categoryTwo: Record<string, any[]> = {
men: menLevelTwo,
women: womenLevelTwo,
kids: [],
home_furniture: furnitureLevelTwo,
beauty: [],
electronics: electronicsLevelTwo,
};

const categoryThree: Record<string, any[]> = {
men: menLevelThree,
women: womenLevelThree,
kids: [],
home_furniture: furnitureLevelThree,
beauty: [],
electronics: electronicsLevelThree,
};

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
category: Yup.string().required("Category is required"),
category2: Yup.string().required("Second category is required"),
category3: Yup.string().required("Third category is required"),
sizes: Yup.string().required("Sizes are required"),
});

const ProductForm = () => {
const [uploadImage, setUploadingImage] = useState(false);
const [snackbarOpen, setOpenSnackbar] = useState(false);

const dispatch = useAppDispatch();
const { sellerProduct } = useAppSelector((store) => store);

const formik = useFormik({
initialValues: {
title: "",
description: "",
mrpPrice: "",
sellingPrice: "",
quantity: "",
color: "",
images: [] as string[],
category: "",
category2: "",
category3: "",
sizes: "",
},


validationSchema,

onSubmit: (values) => {
  const jwt = localStorage.getItem("jwt");

  dispatch(
    createProduct({
      request: values,
      jwt,
    })
  );
},


});

const handleImageChange = async (
event: React.ChangeEvent<HTMLInputElement>
) => {
const file = event.target.files?.[0];


if (!file) return;

setUploadingImage(true);

try {
  const image = await uploadToCloudinary(file);

  if (image) {
    await formik.setFieldValue("images", [
      ...formik.values.images,
      image,
    ]);
  }
} catch (error) {
  console.error("Image upload failed:", error);
} finally {
  setUploadingImage(false);
  event.target.value = "";
}


};

const handleRemoveImage = (index: number) => {
const updatedImages = formik.values.images.filter(
(_, imageIndex) => imageIndex !== index
);


formik.setFieldValue("images", updatedImages);


};

const handleCloseSnackbar = () => {
setOpenSnackbar(false);
};

const childCategory = (
category: any[],
parentCategoryId: string
) => {
return category.filter(
(child) => String(child.parentCategoryId) === parentCategoryId
);
};

const selectedCategory = formik.values.category;

const secondCategories = categoryTwo[selectedCategory] ?? [];
const thirdCategories = categoryThree[selectedCategory] ?? [];

useEffect(() => {
if (sellerProduct.productCreated || sellerProduct.error) {
setOpenSnackbar(true);
}
}, [sellerProduct.productCreated, sellerProduct.error]);

useEffect(() => {
if (sellerProduct.productCreated) {
formik.resetForm();
}
}, [sellerProduct.productCreated]);

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
            <div className="relative" key={`${image}-${index}`}>
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
            formik.touched.mrpPrice && Boolean(formik.errors.mrpPrice)
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
            formik.touched.sellingPrice && formik.errors.sellingPrice
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
            formik.touched.quantity && Boolean(formik.errors.quantity)
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

      <Grid size={{ xs: 12, sm: 6, lg: 4 }}>
        <FormControl
          fullWidth
          error={
            formik.touched.category && Boolean(formik.errors.category)
          }
          required
        >
          <InputLabel id="category-label">Category</InputLabel>

          <Select
            labelId="category-label"
            id="category"
            name="category"
            value={formik.values.category}
            onChange={(event) => {
              formik.setFieldValue("category", event.target.value);
              formik.setFieldValue("category2", "");
              formik.setFieldValue("category3", "");
            }}
            onBlur={formik.handleBlur}
            label="Category"
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>

            {mainCategory.map((item) => (
              <MenuItem
                key={item.categoryId}
                value={String(item.categoryId)}
              >
                {item.name}
              </MenuItem>
            ))}
          </Select>

          {formik.touched.category && formik.errors.category && (
            <FormHelperText>{formik.errors.category}</FormHelperText>
          )}
        </FormControl>
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 4 }}>
        <FormControl
          fullWidth
          error={
            formik.touched.category2 && Boolean(formik.errors.category2)
          }
          required
        >
          <InputLabel id="category2-label">Second Category</InputLabel>

          <Select
            labelId="category2-label"
            id="category2"
            name="category2"
            value={formik.values.category2}
            onChange={(event) => {
              formik.setFieldValue("category2", event.target.value);
              formik.setFieldValue("category3", "");
            }}
            onBlur={formik.handleBlur}
            label="Second Category"
            disabled={!formik.values.category}
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>

            {secondCategories.map((item) => (
              <MenuItem
                key={item.categoryId}
                value={String(item.categoryId)}
              >
                {item.name}
              </MenuItem>
            ))}
          </Select>

          {formik.touched.category2 && formik.errors.category2 && (
            <FormHelperText>{formik.errors.category2}</FormHelperText>
          )}
        </FormControl>
      </Grid>

      <Grid size={{ xs: 12, sm: 6, lg: 4 }}>
        <FormControl
          fullWidth
          error={
            formik.touched.category3 && Boolean(formik.errors.category3)
          }
          required
        >
          <InputLabel id="category3-label">Third Category</InputLabel>

          <Select
            labelId="category3-label"
            id="category3"
            name="category3"
            value={formik.values.category3}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            label="Third Category"
            disabled={!formik.values.category2}
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>

            {childCategory(
              thirdCategories,
              formik.values.category2
            ).map((item) => (
              <MenuItem
                key={item.categoryId}
                value={String(item.categoryId)}
              >
                {item.name}
              </MenuItem>
            ))}
          </Select>

          {formik.touched.category3 && formik.errors.category3 && (
            <FormHelperText>{formik.errors.category3}</FormHelperText>
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
            "Add Product"
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
        : "Product created successfully"}
    </Alert>
  </Snackbar>
</div>


);
};

export default ProductForm;
