
import * as React from "react";

import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell, { tableCellClasses } from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";

import {
  Button,
  FormControl,
  Menu,
  MenuItem,
  Select,
  styled,
} from "@mui/material";

import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import {
  fetchSellers,
  updateSellerAccountStatus,
} from "../../../Redux Toolkit/Seller/sellerSlice";

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

const accountStatuses = [
  {
    status: "PENDING_VERIFICATION",
    title: "Pending Verification",
    description: "Account is created but not yet verified",
  },
  {
    status: "ACTIVE",
    title: "Active",
    description: "Account is active and in good standing",
  },
  {
    status: "SUSPENDED",
    title: "Suspended",
    description:
      "Account is temporarily suspended, possibly due to violations",
  },
  {
    status: "DEACTIVATED",
    title: "Deactivated",
    description:
      "Account is deactivated, user may have chosen to deactivate it",
  },
  {
    status: "BANNED",
    title: "Banned",
    description: "Account is permanently banned due to severe violations",
  },
  {
    status: "CLOSED",
    title: "Closed",
    description: "Account is permanently closed, possibly at user request",
  },
];

export default function SellersTable() {
  const [accountStatus, setAccountStatus] = React.useState("ACTIVE");

  const [anchorEl, setAnchorEl] = React.useState<{
    [key: number]: HTMLElement | null;
  }>({});

  const { sellers } = useAppSelector((store) => store);
  const dispatch = useAppDispatch();

  React.useEffect(() => {
    dispatch(fetchSellers(accountStatus));
  }, [accountStatus, dispatch]);

  const handleAccountStatusChange = (event: { target: { value: string } }) => {
    setAccountStatus(event.target.value);
  };

  const handleUpdateSellerAccountStatus = (id: number, status: string) => {
    dispatch(updateSellerAccountStatus({ id, status }));
  };

  const handleClick = (
    event: React.MouseEvent<HTMLButtonElement>,
    sellerId: number
  ) => {
    setAnchorEl((prev) => ({
      ...prev,
      [sellerId]: event.currentTarget,
    }));
  };

  const handleClose = (sellerId: number) => {
    setAnchorEl((prev) => ({
      ...prev,
      [sellerId]: null,
    }));
  };

  return (
    <>
      <div className="pb-5 w-60">
        <FormControl color="primary" fullWidth>
          <Select
            id="demo-simple-select"
            value={accountStatus}
            onChange={handleAccountStatusChange}
            color="primary"
            className="text-primary-color"
          >
            {accountStatuses.map((status) => (
              <MenuItem key={status.status} value={status.status}>
                {status.title}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </div>

      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 700 }} aria-label="customized table">
          <TableHead>
            <TableRow>
              <StyledTableCell>Seller Name</StyledTableCell>
              <StyledTableCell>Email</StyledTableCell>
              <StyledTableCell>Mobile</StyledTableCell>
              <StyledTableCell>GSTIN</StyledTableCell>
              <StyledTableCell>Business Name</StyledTableCell>
              <StyledTableCell align="right">
                Account Status
              </StyledTableCell>
              <StyledTableCell align="right">
                Change Status
              </StyledTableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {sellers.sellers?.map((seller) => {
              const sellerId = seller.id;

              if (sellerId == null) return null;

              return (
                <StyledTableRow key={sellerId}>
                  <StyledTableCell component="th" scope="row">
                    {seller.sellerName}
                  </StyledTableCell>

                  <StyledTableCell>{seller.email}</StyledTableCell>
                  <StyledTableCell>{seller.mobile}</StyledTableCell>
                  <StyledTableCell>{seller.gstin}</StyledTableCell>
                  <StyledTableCell>
                    {seller.businessDetails?.businessName}
                  </StyledTableCell>

                  <StyledTableCell align="right">
                    {seller.accountStatus}
                  </StyledTableCell>

                  <StyledTableCell align="right">
                    <Button
                      id={`basic-button${sellerId}`}
                      onClick={(event) => handleClick(event, sellerId)}
                    >
                      Change Status
                    </Button>

                    <Menu
                      id={`basic-menus${sellerId}`}
                      anchorEl={anchorEl[sellerId]}
                      open={Boolean(anchorEl[sellerId])}
                      onClose={() => handleClose(sellerId)}
                    >
                      {accountStatuses.map((status) => (
                        <MenuItem
                          key={status.status}
                          onClick={() =>
                            handleUpdateSellerAccountStatus(
                              sellerId,
                              status.status
                            )
                          }
                          value={status.status}
                        >
                          {status.title}
                        </MenuItem>
                      ))}
                    </Menu>
                  </StyledTableCell>
                </StyledTableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}

