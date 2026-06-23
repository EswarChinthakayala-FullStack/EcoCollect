import pymysql
import os
from dotenv import load_dotenv

# Load .env configurations
backend_dir = os.path.dirname(os.path.abspath(__file__))
load_dotenv(os.path.join(backend_dir, ".env"))

db_host = os.getenv("DB_HOST", "localhost")
db_port = int(os.getenv("DB_PORT", "3306"))
db_user = os.getenv("DB_USER", "root")
db_password = os.getenv("DB_PASSWORD", "")
db_name = os.getenv("DB_NAME", "wastereporting")

schema_path = os.path.abspath(os.path.join(backend_dir, "..", "database", "schema.sql"))
print(f"Connecting to MySQL server at {db_host}:{db_port}...")

# Connect to MySQL (initially without database name to drop and recreate)
connection = pymysql.connect(
    host=db_host,
    port=db_port,
    user=db_user,
    password=db_password,
    autocommit=True
)

try:
    with connection.cursor() as cursor:
        print(f"Dropping database '{db_name}' if it exists...")
        cursor.execute(f"DROP DATABASE IF EXISTS `{db_name}`")
        
        print(f"Creating database '{db_name}'...")
        cursor.execute(f"CREATE DATABASE `{db_name}`")
        cursor.execute(f"USE `{db_name}`")
        
        print(f"Reading schema SQL from {schema_path}...")
        with open(schema_path, "r", encoding="utf-8") as f:
            sql_script = f.read()
            
        # Execute individual SQL statements (split by semicolon)
        statements = sql_script.split(";")
        for i, statement in enumerate(statements):
            cleaned_statement = statement.strip()
            if cleaned_statement:
                # Strip comments from each line
                cleaned_lines = []
                for line in cleaned_statement.split("\n"):
                    if "--" in line:
                        line = line.split("--")[0]
                    line_stripped = line.strip()
                    if line_stripped:
                        cleaned_lines.append(line_stripped)
                
                sql_to_run = " ".join(cleaned_lines).strip()
                if sql_to_run:
                    try:
                        print(f"Executing statement {i+1}: {sql_to_run[:60]}...")
                        cursor.execute(sql_to_run)
                    except Exception as err:
                        print(f"Error executing statement {i+1}:")
                        print(f"SQL Code: {sql_to_run}")
                        raise err
        
        print("Database wiped clean and schema re-initialized successfully with default admin user!")
except Exception as e:
    print(f"Error occurred while cleaning the database: {e}")
finally:
    connection.close()
