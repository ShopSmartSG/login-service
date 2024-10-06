using Microsoft.AspNetCore.Mvc;
using OTP_Verification.Models;
using OTP_Verification.Services;
using System.Threading.Tasks;

namespace OTP_Verification.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class OTPController : ControllerBase
    {
        private readonly OtpService _otpService;
        private readonly EmailService _emailService;

        public OTPController(OtpService otpService, EmailService emailService)
        {
            _otpService = otpService;
            _emailService = emailService;
        }

        // Step 1: Request OTP
        [HttpPost("request-OTP")]
        public async Task<IActionResult> RequestOtp([FromBody] LoginRequest request)
        {
            //Checking Locktime
            if (!OtpStore.CanRequestOtp(request.Email))
            {
                return BadRequest($"You have exceeded the maximum number of attempts. Please wait 15 minutes before requesting another OTP.");
            }

            string OTP = _otpService.GenerateOtp();
            OtpStore.StoreOtp(request.Email, OTP);
            await _emailService.SendOtpAsync(request.Email, OTP);
            return Ok("OTP sent to email.");
        }

        // OTP Verification below 

        [HttpPost("validate-OTP")]
        public IActionResult ValidateOtp([FromBody] OtpRequest request)
        {
            // OTP expiration checked
            if (OtpStore.IsOTPexpired(request.Email))
            {
                return BadRequest("OTP has expired.");
            }

            // Check if the user has exceeded the maximum number of attempts
            if (OtpStore.IncrementAttempts(request.Email))
            {
                OtpStore.SetLockout(request.Email);  // Set the lockout time for 15 minutes
                return BadRequest("Maximum OTP attempts exceeded. Please wait 15 minutes before requesting another OTP.");
            }

            //Validating OTP
            string storedOtp = OtpStore.GetOtp(request.Email);
            if (storedOtp == request.OTP)
            {
                return Ok("OTP validated successfully.");
            }
            return BadRequest("Invalid OTP.");
        }

        
    }
}

