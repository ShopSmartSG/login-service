using System;
namespace OTP_Verification.Services
{
    public class OtpService
    {
        private readonly Random _random = new Random();

        public string GenerateOtp()
        {
            return _random.Next(100000, 999999).ToString();
        }
    }
}
