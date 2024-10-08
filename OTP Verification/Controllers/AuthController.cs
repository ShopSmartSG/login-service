using Microsoft.AspNetCore.Mvc;
using OTP_Verification.Models;
using OtpLoginSystem.Models;
using OtpLoginSystem.Repositories;
using MailKit.Net.Smtp;
using MimeKit;
using System;
using System.Threading.Tasks;
using static System.Net.WebRequestMethods;
using System.Net.Mail;
using System.Net;
using OTP_Verification.Services;

namespace OtpLoginSystem.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly OtpRepository _otpRepository;
        private readonly EmailService _emailService;

        public AuthController(OtpRepository otpRepository)
        {
            _otpRepository = otpRepository;
            _emailService = new EmailService();
        }

        [HttpPost("request-otp")]
        public async Task<IActionResult> RequestOtp([FromBody] LoginRequest request)
        {
            var otpRecord = await _otpRepository.GetOtpByEmailAsync(request.Email);

            // Check if the user is blocked from requesting a new OTP
            if (otpRecord != null && otpRecord.BlockedUntil.HasValue && DateTime.Now < otpRecord.BlockedUntil.Value)
            {
                return BadRequest("You have exceeded the maximum attempts. Please wait 15 minutes before requesting a new OTP.");
            }

            // Generate a new OTP
            var otp = GenerateOtp();  // Your method to generate an OTP
            var expiryTime = DateTime.Now.AddMinutes(485); // Set expiry time

            // Create a new OtpRecord
            otpRecord = new OtpRecord
            {
                Email = request.Email,
                Otp = otp,
                Expiry = expiryTime,
                Attempts = otpRecord?.Attempts ?? 0,
                BlockedUntil = otpRecord?.BlockedUntil
            };

            // Store or update the OTP in the database
            await _otpRepository.CreateOrUpdateOtpAsync(otpRecord);

            // Send OTP via email (your email sending logic here)
            await _emailService.SendOtpAsync(request.Email, otp);
           


            return Ok("OTP sent to email.");
        }

        [HttpPost("validate-otp")]
        public async Task<IActionResult> ValidateOtp([FromBody] OtpRequest request)
        {
            var otpRecord = await _otpRepository.GetOtpByEmailAsync(request.Email);

            // Check if OTP has expired
            if (otpRecord == null || DateTime.Now >= otpRecord.Expiry)
            {
                if (otpRecord != null)
                {
                    await _otpRepository.RemoveOtpAsync(request.Email);  // Remove expired OTP from the store
                }
                return BadRequest("OTP has expired.");
            }

            // Check if the user has exceeded the maximum number of attempts
            if (otpRecord.Attempts >= 3)
            {
                return BadRequest("You have exceeded the maximum attempts. Please wait 15 minutes before requesting a new OTP.");
            }

            // Validate OTP
            if (otpRecord.Otp == request.Otp)
            {
                //await _otpRepository.RemoveOtpAsync(request.Email);  // Clear OTP after successful validation
                return Ok("OTP validated successfully.");
            }

            // Increment attempts and update the record
            otpRecord.Attempts++;
            if (otpRecord.Attempts >= 3)
            {
                otpRecord.BlockedUntil = DateTime.Now.AddMinutes(15);  // Set the block duration
            }
            await _otpRepository.CreateOrUpdateOtpAsync(otpRecord);  // Update the record in the database

            return BadRequest("Invalid OTP.");
        }

        private string GenerateOtp()
        {
            Random random = new Random();
            return random.Next(100000, 999999).ToString(); // Generate a 6-digit OTP
        }
    }
}