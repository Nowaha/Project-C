using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ChengetaBackend.Migrations
{
    public partial class mi4 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "Status",
                table: "events",
                type: "integer",
                nullable: false,
                defaultValueSql: "0");

            migrationBuilder.AddColumn<string>(
                name: "FirstName",
                table: "accounts",
                type: "text",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "LastName",
                table: "accounts",
                type: "text",
                nullable: true);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Status",
                table: "events");

            migrationBuilder.DropColumn(
                name: "FirstName",
                table: "accounts");

            migrationBuilder.DropColumn(
                name: "LastName",
                table: "accounts");
        }
    }
}
