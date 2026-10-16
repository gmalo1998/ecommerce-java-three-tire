import { useEffect } from "react";

import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";

import {
useAppDispatch,
useAppSelector,
} from "../../../Redux Toolkit/Store";

import { fetchTransactionsBySeller } from "../../../Redux Toolkit/Seller/transactionSlice";
import type { Transaction } from "../../../types/Transaction";
import { redableDateTime } from "../../../util/redableDateTime";

export default function TransactionTable() {
const { transaction } = useAppSelector((store) => store);
const dispatch = useAppDispatch();

useEffect(() => {
dispatch(
fetchTransactionsBySeller(localStorage.getItem("jwt") || "")
);
}, [dispatch]);

return ( <TableContainer component={Paper}>
<Table sx={{ minWidth: 700 }} aria-label="transactions table"> <TableHead> <TableRow> <TableCell>Date</TableCell> <TableCell>Customer Details</TableCell> <TableCell>Order</TableCell> <TableCell align="right">Amount</TableCell> </TableRow> </TableHead>


    <TableBody>
      {transaction.transactions.map((item: Transaction) => (
        <TableRow key={item.id}>
          <TableCell align="left">
            <div className="space-y-1">
              <h1 className="font-medium">
                {redableDateTime(item.date).split("at")[0]}
              </h1>
              <h1 className="text-xs text-gray-600 font-semibold">
                {redableDateTime(item.date).split("at")[1]}
              </h1>
            </div>
          </TableCell>

          <TableCell component="th" scope="row">
            <div className="space-y-2">
              <h1>{item.customer.fullName}</h1>
              <h1 className="font-semibold">
                {item.customer.email}
              </h1>
              <h1 className="font-bold text-gray-600">
                {item.customer.mobile}
              </h1>
            </div>
          </TableCell>

          <TableCell>
            Order Id: <strong>{item.order.id}</strong>
          </TableCell>

          <TableCell align="right">
            ₹{item.order.totalSellingPrice}
          </TableCell>
        </TableRow>
      ))}

      {transaction.transactions.length === 0 && (
        <TableRow>
          <TableCell colSpan={4} align="center">
            No transactions found.
          </TableCell>
        </TableRow>
      )}
    </TableBody>
  </Table>
</TableContainer>


);
}
