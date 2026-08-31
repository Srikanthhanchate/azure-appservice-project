locals {
  resource_group_name  = "rg-webapp-tfstate"
  storage_account_name = "stwebapptfstate20260830"
  container_name       = "tfstate"
}

resource "azurerm_resource_group" "tfstate" {
  name     = local.resource_group_name
  location = var.location
}

resource "azurerm_storage_account" "tfstate" {
  name                     = local.storage_account_name
  resource_group_name      = azurerm_resource_group.tfstate.name
  location                 = azurerm_resource_group.tfstate.location
  account_tier             = "Standard"
  account_replication_type = "LRS"

  shared_access_key_enabled = false
}

resource "azurerm_storage_container" "tfstate" {
  name                  = local.container_name
  storage_account_id    = azurerm_storage_account.tfstate.id
  container_access_type = "private"
}

# Grant GitHub Actions managed identity permission to read/write tfstate

resource "azurerm_role_assignment" "github_actions_storage_blob_contributor" {
  scope                = azurerm_storage_container.tfstate.resource_manager_id
  role_definition_name = "Storage Blob Data Contributor"
  principal_id         = var.github_actions_managed_identity_principal_id
  principal_type       = "ServicePrincipal"
}