using System;
using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations.Schema;

namespace ChengetaBackend
{
    public class ChengetaContext : DbContext
    {
        public static string DB_PASSWORD = "123";
        public static int DB_PORT = 5432;

        public DbSet<Event> events { get; set; } = null!;
        public DbSet<Account> accounts { get; set; } = null!;
        public DbSet<Session> sessions { get; set; } = null!;

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.UseNpgsql(
                $"UserID=postgres;Password={DB_PASSWORD};Host=localhost;port={DB_PORT};Database=ChengetaApp;Pooling=true"
            );
        }

        // protected override void OnModelCreating(ModelBuilder modelBuilder) {
        //     modelBuilder.Entity<Session>().HasOne(a => a.Ranger).WithOne().HasForeignKey<Session>(b => b.Id);
        // }  
    }

    public class Event
    {
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

    public class Account
    {
        public int Id { get; set; }

        [Required]
        public DateTime CreationDate { get; set; }

        [Required]
        [Column(TypeName = "varchar(24)")]
        public string Username { get; set; }

        [Required]
        public string Password { get; set; }

        [Required]
        public string Salt { get; set; }

        [Required]
        public AccountType Role { get; set; }

        public enum AccountType
        {
            RANGER,
            ADMIN
        }
    }

    // public class statussen
    // {
    //     public int Id { get; set; }

    //     [Required]
    //     public int RangerId { get; set; }

    //     [Required]
    //     public int Status { get; set; }
    // }

    public class Session
    {
        public int Id { get; set; }

        [Required]
        public DateTime Start { get; set; }

        [Required]
        [ForeignKey("RangerId")]
        public Account Ranger { get; set; }
        
        public int RangerId {get;set;}
    }
}
