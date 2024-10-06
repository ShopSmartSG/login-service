namespace OTP_Verification.Services
{
    public static class OtpStore
    {
        private static Dictionary<string, (string OTP, DateTime ExpiryTime, int Attempts, DateTime? LockTime)> otpStore = new Dictionary<string, (string, DateTime, int Attempts, DateTime? LockTime)>();
        private static int maxAttempts = 3;  // Maximum failed attempts allowed
        private static int lockDurationDays = 1;  // 1 day 

        public static void StoreOtp(string email, string OTP, int expiryminutes= 3)
        {
            var expirytime = DateTime.Now.AddMinutes(expiryminutes);
            otpStore[email] = (OTP,expirytime,0,null);
        }
        public static bool CanRequestOtp(string email)
        {
            if (otpStore.TryGetValue(email, out var otpData))
            {
                // Check if the user is in lockout period
                if (otpData.LockTime.HasValue && DateTime.Now < otpData.LockTime.Value)
                {
                    return false;  // User is still in the lockout period
                }
            }
            return true;  // User can request a new OTP
        }

        public static void SetLockout(string email)
        {
            if (otpStore.TryGetValue(email, out var otpData))
            {
                otpStore[email] = (otpData.OTP, otpData.ExpiryTime, otpData.Attempts, DateTime.Now.AddDays(lockDurationDays));  // Set lockout time for 15 minutes
            }
        }

        public static string GetOtp(string email)
        {
            if( otpStore.TryGetValue(email, out var OTPdata))
            {
                if (DateTime.Now < OTPdata.ExpiryTime)
                {
                    return OTPdata.OTP;
                }
                else
                {
                    otpStore.Remove(email);
                }
            }
            return null;
        }

        public static bool IsOTPexpired(string email)
        {
            if (otpStore.TryGetValue(email, out var otpData))
            {
                return DateTime.Now >= otpData.ExpiryTime;
            }
            return true;
        }

        public static bool IncrementAttempts(string email)
        {
            if (otpStore.TryGetValue(email, out var otpData))
            {
                otpData.Attempts += 1;
                otpStore[email] = (otpData.OTP, otpData.ExpiryTime, otpData.Attempts, otpData.LockTime);  // Update the attempt count

                if (otpData.Attempts > maxAttempts)
                {
                    otpStore[email] = (otpData.OTP, otpData.ExpiryTime, otpData.Attempts, DateTime.Now);
                    return true;  // Exceeded maximum attempts
                }
            }
            return false;
        }
     

    }
}

