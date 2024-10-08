using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace OtpLoginSystem.Models
{
    public class OtpRecord
    {
        [BsonId]
        public ObjectId Id { get; set; }

        public string Email { get; set; }
        public string Otp { get; set; }
        public DateTime Expiry { get; set; }
        public int Attempts { get; set; }
        public DateTime? BlockedUntil { get; set; }
    }
}