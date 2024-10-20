using OTP_Verification.Services;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using MongoDB.Driver;
using OtpLoginSystem.Models;
using OtpLoginSystem.Repositories;


var builder = WebApplication.CreateBuilder(args);
var mongoDbSettings = builder.Configuration.GetSection("MongoDb");
var connectionString = "mongodb+srv://Rishi:rishi@shopsmart.ubphc.mongodb.net/?retryWrites=true&w=majority&appName=Shopsmart";
var databaseName = "Shopsmart";
Console.WriteLine($"MongoDB Connection String: {connectionString}");
Console.WriteLine($"MongoDB Database Name: {databaseName}");

// Add services to the container.
builder.Services.AddScoped<EmailService>();
builder.Services.AddControllers();
builder.Services.AddSingleton(new OtpRepository(connectionString, databaseName));

// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.WebHost.ConfigureKestrel(serverOptions =>
{
    serverOptions.ListenAnyIP(8080); // Ensure the app listens to port 8080 in Docker
});


var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

//if (!app.Environment.IsDevelopment())  // Only redirect to HTTPS in development
//{
//    app.UseHttpsRedirection();  // This can be skipped in Docker container to avoid HTTPS errors
//}


//app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
