using System;
using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;

namespace ChengetaBackend
{

    public class ChengetaContext : DbContext {

        public static string DB_PASSWORD = "123";
        public static int DB_PORT = 5432;

        public DbSet<Event> events { get; set; } = null!;

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder) {
            optionsBuilder.UseNpgsql($"User ID=postgres;Password={DB_PASSWORD};Host=localhost;port={DB_PORT};Database=ChengetaApp;Pooling=true");
        }
        
    }

    public class Event {
        public int Id { get; set; }
        
        [Required]
        public int NodeId { get; set; }
        [Required]
        public DateTime Date { get; set; }

        [Required]
        public float Latitude { get; set; }
        [Required]
        public float Longitude { get; set; }

        [Required]
        public string SoundLabel { get; set; }
        [Required]
        public int Probability { get; set; }

        [Required]
        public string SoundURL { get; set; }
    }

}