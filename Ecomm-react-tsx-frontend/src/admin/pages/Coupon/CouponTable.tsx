import * as React from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell, { tableCellClasses } from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import {
IconButton,
MenuItem,
Select,
styled,
} from "@mui/material";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import type { Coupon } from "../../../types/couponTypes";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { deleteCoupon } from "../../../Redux Toolkit/Admin/AdminCouponSlice";

const StyledTableCell = styled(TableCell)(({ theme }) => ({
[`&.${tableCellClasses.head}`]: {
backgroundColor: theme.palette.common.black,
color: theme.palette.common.white,
},
[`&.${tableCellClasses.body}`]: {
fontSize: 14,
},
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
"&:nth-of-type(odd)": {
backgroundColor: theme.palette.action.hover,
},
"&:last-child td, &:last-child th": {
border: 0,
},
}));

export default function CouponTable() {
const [status, setStatus] = React.useState("ALL");

const { adminCoupon } = useAppSelector((store) => store);
const dispatch = useAppDispatch();

const handleDeleteCoupon = (id: number) => {
dispatch(
deleteCoupon({
id,
jwt: localStorage.getItem("jwt") || "",
})
);
};

const coupons = adminCoupon.coupons ?? [];

const filteredCoupons = coupons.filter((coupon: Coupon) => {
if (status === "ALL") {
return true;
}

if (status === "ACTIVE") {
  return coupon.active;
}

return !coupon.active;


});

return (
<> <div className="pb-5 w-60">
<Select
id="coupon-status-filter"
value={status}
onChange={(event) => setStatus(event.target.value)}
color="primary"
fullWidth
className="text-primary-color"
> <MenuItem value="ALL">All Coupons</MenuItem> <MenuItem value="ACTIVE">Active</MenuItem> <MenuItem value="INACTIVE">Inactive</MenuItem> </Select> </div>


  <TableContainer component={Paper}>
    <Table sx={{ minWidth: 700 }} aria-label="coupons table">
      <TableHead>
        <TableRow>
          <StyledTableCell>Coupon Code</StyledTableCell>
          <StyledTableCell>Start Date</StyledTableCell>
          <StyledTableCell>End Date</StyledTableCell>
          <StyledTableCell>Min Order Value</StyledTableCell>
          <StyledTableCell>Discount %</StyledTableCell>
          <StyledTableCell align="right">Status</StyledTableCell>
          <StyledTableCell align="right">Delete</StyledTableCell>
        </TableRow>
      </TableHead>

      <TableBody>
        {filteredCoupons.map((coupon: Coupon) => (
          <StyledTableRow key={coupon.id}>
            <StyledTableCell component="th" scope="row">
              {coupon.code}
            </StyledTableCell>

            <StyledTableCell>
              {coupon.validityStartDate}
            </StyledTableCell>

            <StyledTableCell>
              {coupon.validityEndDate}
            </StyledTableCell>

            <StyledTableCell>
              {coupon.minimumOrderValue}
            </StyledTableCell>

            <StyledTableCell>
              {coupon.discountPercentage}
            </StyledTableCell>

            <StyledTableCell align="right">
              {coupon.active ? "Active" : "Inactive"}
            </StyledTableCell>

            <StyledTableCell align="right">
              <IconButton
                aria-label={`Delete coupon ${coupon.code}`}
                onClick={() => handleDeleteCoupon(coupon.id)}
              >
                <DeleteOutlineIcon className="text-red-700 cursor-pointer" />
              </IconButton>
            </StyledTableCell>
          </StyledTableRow>
        ))}

        {filteredCoupons.length === 0 && (
          <StyledTableRow>
            <StyledTableCell colSpan={7} align="center">
              No coupons found.
            </StyledTableCell>
          </StyledTableRow>
        )}
      </TableBody>
    </Table>
  </TableContainer>
</>


);
}
