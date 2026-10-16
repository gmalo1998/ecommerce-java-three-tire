
import { Avatar, IconButton, Rating, Box, Grid } from "@mui/material";
import type { Review } from "../../../types/reviewTypes";
import DeleteIcon from "@mui/icons-material/Delete";
import { red } from "@mui/material/colors";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { deleteReview } from "../../../Redux Toolkit/Customer/ReviewSlice";

interface ProductReviewCardProps {
item: Review;
}

const ProductReviewCard = ({ item }: ProductReviewCardProps) => {
const { user } = useAppSelector((store) => store);
const dispatch = useAppDispatch();

const handleDeleteReview = () => {
dispatch(
deleteReview({
reviewId: item.id,
jwt: localStorage.getItem("jwt") || "",
})
);
};

return ( <div className="flex justify-between"> <Grid container spacing={2} gap={3}> <Grid size={1}> <Box>
<Avatar
className="text-white"
sx={{ width: 56, height: 56, bgcolor: "#9155FD" }}
alt={item.user.fullName}
src=""
>
{item.user.fullName[0].toUpperCase()} </Avatar> </Box> </Grid>


    <Grid size={9}>
      <div className="space-y-2">
        <div>
          <p className="font-semibold text-lg">{item.user.fullName}</p>
          <p className="opacity-70">{item.createdAt}</p>
        </div>

        <div>
          <Rating
            readOnly
            value={item.rating}
            name="half-rating"
            precision={0.5}
          />
        </div>

        <p>{item.reviewText}</p>

        <div>
          {item.productImages.map((image, index) => (
            <img
              key={`${image}-${index}`}
              className="w-24 h-24 object-cover"
              src={image}
              alt="Review"
            />
          ))}
        </div>
      </div>
    </Grid>
  </Grid>

  {item.user.id === user.user?.id && (
    <div>
      <IconButton onClick={handleDeleteReview}>
        <DeleteIcon sx={{ color: red[700] }} />
      </IconButton>
    </div>
  )}
</div>


);
};

export default ProductReviewCard;
