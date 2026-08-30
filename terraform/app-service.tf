resource "azurerm_service_plan" "app" {
  name                = local.app_service_plan_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  os_type  = "Linux"
  sku_name = "B1"

  tags = local.common_tags
}

resource "azurerm_user_assigned_identity" "app" {
  name                = "id-${local.name_prefix}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  tags = local.common_tags
}

resource "azurerm_linux_web_app" "app" {
  name                = local.app_service_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  service_plan_id     = azurerm_service_plan.app.id

  https_only = true

  identity {
    type = "UserAssigned"

    identity_ids = [
      azurerm_user_assigned_identity.app.id
    ]
  }

  site_config {
  always_on = true

  application_stack {
    java_version        = "21"
    java_server         = "JAVA"
    java_server_version = "21"
  }
}

  virtual_network_subnet_id = azurerm_subnet.app_service_integration.id

  app_settings = {
  AZURE_SQL_CONNECTION_STRING = "jdbc:sqlserver://${azurerm_mssql_server.main.fully_qualified_domain_name}:1433;databaseName=${azurerm_mssql_database.main.name};msiClientId=${azurerm_user_assigned_identity.app.client_id};authentication=ActiveDirectoryManagedIdentity;encrypt=true;trustServerCertificate=false;"

  JWT_SECRET = "@Microsoft.KeyVault(SecretUri=${azurerm_key_vault_secret.jwt_secret.versionless_id})"

  APPLICATIONINSIGHTS_CONNECTION_STRING = azurerm_application_insights.app.connection_string
}

  tags = local.common_tags
}