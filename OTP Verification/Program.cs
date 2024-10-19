using OTP_Verification.Services;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using MongoDB.Driver;
using OtpLoginSystem.Models;
using OtpLoginSystem.Repositories;


var builder = WebApplication.CreateBuilder(args);
var mongoDbSettings = builder.Configuration.GetSection("MongoDb");
var connectionString = "mongodb://localhost:27017/";
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


var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
