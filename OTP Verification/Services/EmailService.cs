using System.Net;
using System.Net.Mail;
using System.Threading.Tasks;
using static System.Net.WebRequestMethods;
namespace OTP_Verification.Services
{
    public class EmailService
    {
        public async Task SendOtpAsync(string email, string OTP)
        {
            using (var client = new SmtpClient("smtp.gmail.com"))
            {
                client.Port = 587;
                client.Credentials = new NetworkCredential("krishnarishi50@gmail.com", "xigenotojuatkpzv");
                client.EnableSsl = true;

                var mailMessage = new MailMessage
                {
                    From = new MailAddress("krishnarishi50@gmail.com"),
                    Subject = "Your OTP Code",
                    Body = $"Your OTP code is {OTP}",
                    IsBodyHtml = true,
                };

                mailMessage.To.Add(email);
                await client.SendMailAsync(mailMessage);
            }
        }
    }
}
