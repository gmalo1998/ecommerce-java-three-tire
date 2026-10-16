import { useEffect, useState } from 'react'
import LoginForm from './LoginForm'
import { Alert, Button, Snackbar } from '@mui/material'
import SignupForm from './SignupForm'
import { useAppSelector } from '../../../Redux Toolkit/Store'
import login_banner from '../../../../public/login_banner.jpg'

const Auth = () => {
const [isLoginPage, setIsLoginPage] = useState(true)
const [snackbarOpen, setSnackbarOpen] = useState(false)
const { auth } = useAppSelector(store => store)


const handleCloseSnackbar = () => setSnackbarOpen(false)

useEffect(() => {
    if (auth.otpSent || auth.error) {
        setSnackbarOpen(true)
        console.log("store ", auth.error)
    }
}, [auth.otpSent, auth.error])

return (
    <div className="relative flex justify-center items-center h-[90vh] overflow-hidden">

        {/* Background image */}
        <img
            src={login_banner}
            alt=""
            className="absolute inset-0 w-full h-full object-cover"
        />

        {/* Optional overlay for better contrast */}
        <div className="absolute inset-0 bg-black/10" />

        {/* Login card */}
        <div className="relative z-10 max-w-md w-full mx-4 rounded-md border bg-white/95 shadow-lg">
            <div className="px-10 py-8">
                {isLoginPage ? <LoginForm /> : <SignupForm />}

                <div className="flex items-center gap-1 justify-center mt-5">
                    <p>{isLoginPage && "Don't"} have Account?</p>
                    <Button
                        onClick={() => setIsLoginPage(!isLoginPage)}
                        size="small"
                    >
                        {isLoginPage ? "create account" : "login"}
                    </Button>
                </div>
            </div>
        </div>

        <Snackbar
            anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            open={snackbarOpen}
            autoHideDuration={6000}
            onClose={handleCloseSnackbar}
        >
            <Alert
                onClose={handleCloseSnackbar}
                severity={auth.error ? 'error' : 'success'}
                variant="filled"
                sx={{ width: '100%' }}
            >
                {auth.error ? auth.error : 'OTP sent to your email!'}
            </Alert>
        </Snackbar>
    </div>
)


}

export default Auth
