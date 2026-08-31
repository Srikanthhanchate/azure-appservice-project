terraform {
  backend "azurerm" {
    resource_group_name  = "rg-webapp-tfstate"
    storage_account_name = "stwebapptfstate20260830"
    container_name       = "tfstate"
    key                  = "webapp-dev.tfstate"

    subscription_id = "fe469853-da26-42bb-b404-4e47372192f0"

    use_oidc         = true
    use_azuread_auth = true
  }
}