
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import React from "react";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import type { Order, OrderItem } from "../../../types/orderTypes";
import { fetchPayoutsBySeller } from "../../../Redux Toolkit/Seller/payoutSlice";

const PayoutsTable = () => {
  const { sellerOrder } = useAppSelector((store) => store);
  const dispatch = useAppDispatch();

  React.useEffect(() => {
    dispatch(fetchPayoutsBySeller(localStorage.getItem("jwt") || ""));
  }, [dispatch]);

  return (
    <div>
      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 700 }} aria-label="payouts table">
          <TableHead>
            <TableRow>
              <TableCell>Date / Order ID</TableCell>
              <TableCell>Order Items</TableCell>
              <TableCell align="right">Status</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {sellerOrder.orders.map((item: Order) => (
              <TableRow key={item.id}>
                <TableCell align="left">{item.id}</TableCell>

                <TableCell component="th" scope="row">
                  <div className="flex gap-1 flex-wrap">
                    {item.orderItems.map((orderItem: OrderItem) => (
                      <div key={orderItem.id} className="flex gap-5">
                        <img
                          className="w-20 rounded-md"
                          src={orderItem.product.images[0]}
                          alt={orderItem.product.title}
                        />

                        <div className="flex flex-col justify-between py-2">
                          <h1>Title: {orderItem.product.title}</h1>
                          <h1>Price: Rs. {orderItem.product.sellingPrice}</h1>
                          <h1>Color: {orderItem.product.color}</h1>
                          <h1>Size: {orderItem.size}</h1>
                        </div>
                      </div>
                    ))}
                  </div>
                </TableCell>

                <TableCell align="right">{item.orderStatus}</TableCell>
              </TableRow>
            ))}

            {sellerOrder.orders.length === 0 && (
              <TableRow>
                <TableCell colSpan={3} align="center">
                  No orders found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default PayoutsTable;

