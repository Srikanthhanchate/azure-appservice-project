provider "azurerm" {
  features {}

  subscription_id = "fe469853-da26-42bb-b404-4e47372192f0"

  skip_provider_registration = true
}

data "azurerm_client_config" "current" {}