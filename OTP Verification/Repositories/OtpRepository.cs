using MongoDB.Driver;
using OtpLoginSystem.Models;
using System.Threading.Tasks;

namespace OtpLoginSystem.Repositories
{
    public class OtpRepository
    {
        private readonly IMongoCollection<OtpRecord> _otpCollection;

        public OtpRepository(string connectionString, string databaseName)
        {
            var client = new MongoClient(connectionString);
            var database = client.GetDatabase(databaseName);
            _otpCollection = database.GetCollection<OtpRecord>("Shopsmart"); // Collection name
        }

        public async Task<OtpRecord> GetOtpByEmailAsync(string email)
        {
            return await _otpCollection.Find(x => x.Email == email).FirstOrDefaultAsync();
        }

        public async Task CreateOrUpdateOtpAsync(OtpRecord otpRecord)
        {
            var existingRecord = await GetOtpByEmailAsync(otpRecord.Email);

            if (existingRecord == null)
            {
                await _otpCollection.InsertOneAsync(otpRecord);
            }
            else
            {
                otpRecord.Id = existingRecord.Id; // Keep the existing ID
                await _otpCollection.ReplaceOneAsync(x => x.Id == existingRecord.Id, otpRecord);
            }
        }

        public async Task RemoveOtpAsync(string email)
        {
            await _otpCollection.DeleteOneAsync(x => x.Email == email);
        }
    }
}