import { useEffect, useState } from "react";

import {
Box,
IconButton,
Modal,
Paper,
styled,
Table,
TableBody,
TableCell,
tableCellClasses,
TableContainer,
TableHead,
TableRow,
} from "@mui/material";

import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";

import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import {
deleteDeal,
getAllDeals,
} from "../../../Redux Toolkit/Admin/DealSlice";

import UpdateDealForm from "./UpdateDealForm";

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

const style = {
position: "absolute" as const,
top: "50%",
left: "50%",
transform: "translate(-50%, -50%)",
width: 400,
bgcolor: "background.paper",
boxShadow: 24,
p: 4,
};

const DealsTable = () => {
const { adminDeals } = useAppSelector((store) => store);

const [selectedDealId, setSelectedDealId] = useState<number>();
const [open, setOpen] = useState(false);

const dispatch = useAppDispatch();

const handleOpen = (id: number | undefined) => () => {
setSelectedDealId(id);
setOpen(true);
};

const handleClose = () => {
setOpen(false);
};

const handleDelete = (id: number) => () => {
dispatch(deleteDeal(id));
};

useEffect(() => {
dispatch(getAllDeals());
}, [dispatch]);

return (
<> <TableContainer component={Paper}>
<Table sx={{ minWidth: 700 }} aria-label="customized table"> <TableHead> <TableRow> <StyledTableCell>No</StyledTableCell> <StyledTableCell>Image</StyledTableCell> <StyledTableCell>Category</StyledTableCell> <StyledTableCell>Discount</StyledTableCell> <StyledTableCell align="right">Edit</StyledTableCell> <StyledTableCell align="right">Delete</StyledTableCell> </TableRow> </TableHead>


      <TableBody>
        {adminDeals.deals.map((dealItem: any, index: number) => (
          <StyledTableRow key={dealItem.id}>
            <StyledTableCell component="th" scope="row">
              {index + 1}
            </StyledTableCell>

            <StyledTableCell component="th" scope="row">
              <img
                className="w-20 rounded-md"
                src={dealItem.category.image}
                alt="Category"
              />
            </StyledTableCell>

            <StyledTableCell component="th" scope="row">
              {dealItem.category.categoryId}
            </StyledTableCell>

            <StyledTableCell component="th" scope="row">
              {dealItem.discount}%
            </StyledTableCell>

            <StyledTableCell align="right">
              <IconButton onClick={handleOpen(dealItem.id)}>
                <EditIcon className="cursor-pointer text-orange-400" />
              </IconButton>
            </StyledTableCell>

            <StyledTableCell align="right">
              <IconButton onClick={handleDelete(dealItem.id)}>
                <DeleteIcon className="cursor-pointer text-red-600" />
              </IconButton>
            </StyledTableCell>
          </StyledTableRow>
        ))}
      </TableBody>
    </Table>
  </TableContainer>

  {selectedDealId !== undefined && (
    <Modal
      open={open}
      onClose={handleClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box sx={style}>
        <UpdateDealForm id={selectedDealId} />
      </Box>
    </Modal>
  )}
</>


);
};

export default DealsTable;
