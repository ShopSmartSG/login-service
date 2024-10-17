# Use the official .NET SDK image for building the application
FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /app

# Copy the solution file and restore dependencies
COPY ["OTP Verification.sln", "./"]
COPY ["OTP Verification/OTP Verification.csproj", "OTP Verification/"]
RUN dotnet restore

# Copy the entire project and build the application
COPY . .
WORKDIR "/app/OTP Verification"
RUN dotnet publish -c Release -o out

# Use the official .NET runtime image for running the application
FROM mcr.microsoft.com/dotnet/aspnet:6.0
WORKDIR /app

# Copy the published output from the build stage
COPY --from=build /app/OTP Verification/out .

# Expose the port that the application will listen on
EXPOSE 80

# Set the environment variable to configure the listening port
ENV ASPNETCORE_URLS=http://+:80

# Set the entry point for the application
ENTRYPOINT ["dotnet", "OTP Verification.dll"]