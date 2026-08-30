resource "azurerm_mssql_server" "main" {
  name                = local.sql_server_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location

  version = "12.0"

  minimum_tls_version          = "1.2"
  public_network_access_enabled = false  # Changed for security

  azuread_administrator {
    login_username              = data.azurerm_client_config.current.client_id  # Use service principal
    object_id                   = data.azurerm_client_config.current.object_id
    tenant_id                   = data.azurerm_client_config.current.tenant_id
    azuread_authentication_only = true  # AAD-only auth (no SQL passwords)
  }

  tags = local.common_tags
}

resource "azurerm_mssql_database" "main" {
  name      = local.sql_database_name
  server_id = azurerm_mssql_server.main.id

  sku_name = "S0"

  tags = local.common_tags
}