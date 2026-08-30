provider "azurerm" {
  features {}
   use_oidc = true
}

data "azurerm_client_config" "current" {}

