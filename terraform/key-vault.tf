resource "azurerm_key_vault" "main" {
  name                = "${local.key_vault_name}-${data.azurerm_client_config.current.subscription_id}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  tenant_id = data.azurerm_client_config.current.tenant_id

  sku_name = "standard"

  public_network_access_enabled = false

  purge_protection_enabled   = false
  soft_delete_retention_days = 7

  rbac_authorization_enabled = true
  tags                       = local.common_tags
}

resource "azurerm_role_assignment" "app_key_vault_secrets_user" {
  scope                = azurerm_key_vault.main.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_user_assigned_identity.app.principal_id
}

resource "random_password" "jwt_secret" {
  length  = 64
  special = true
}

resource "azurerm_key_vault_secret" "jwt_secret" {
  name         = "jwt-secret"
  value        = random_password.jwt_secret.result
  key_vault_id = azurerm_key_vault.main.id
}