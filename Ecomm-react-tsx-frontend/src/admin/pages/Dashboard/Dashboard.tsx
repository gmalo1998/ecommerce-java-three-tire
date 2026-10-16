import { useEffect, useState } from "react";

import AdminRoutes from "../../../routes/AdminRoutes";
import Navbar from "../../../admin seller/components/navbar/Navbar";
import AdminDrawerList from "../../components/DrawerList";

import { Alert, Snackbar } from "@mui/material";
import { useAppSelector } from "../../../Redux Toolkit/Store";

const AdminDashboard = () => {
const { admin } = useAppSelector((store) => store);

const [snackbarOpen, setOpenSnackbar] = useState(false);

const handleCloseSnackbar = () => {
setOpenSnackbar(false);
};

useEffect(() => {
if (admin.categoryUpdated) {
setOpenSnackbar(true);
}
}, [admin.categoryUpdated]);

return (
<> <div className="min-h-screen"> <Navbar DrawerList={AdminDrawerList} />


    <section className="lg:flex lg:h-[90vh]">
      <div className="hidden h-full lg:block">
        <AdminDrawerList />
      </div>

      <div className="w-full overflow-y-auto p-10 lg:w-[80%]">
        <AdminRoutes />
      </div>
    </section>
  </div>

  <Snackbar
    anchorOrigin={{ vertical: "top", horizontal: "right" }}
    open={snackbarOpen}
    autoHideDuration={6000}
    onClose={handleCloseSnackbar}
  >
    <Alert
      onClose={handleCloseSnackbar}
      severity="success"
      variant="filled"
      sx={{ width: "100%" }}
    >
      Category Updated successfully
    </Alert>
  </Snackbar>
</>


);
};

export default AdminDashboard;
