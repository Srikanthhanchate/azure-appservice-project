provider "azurerm" {
  features {}
  skip_provider_registration = false
}

data "azurerm_client_config" "current" {}