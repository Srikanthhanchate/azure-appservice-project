project_name = "webapp"
environment  = "dev"
location     = "East US"

vnet_address_space = [
  "10.10.0.0/16"
]

app_service_subnet_address_prefixes = [
  "10.10.1.0/24"
]

private_endpoint_subnet_address_prefixes = [
  "10.10.2.0/24"
]

sql_database_name = "appdb"