terraform {
  backend "azurerm" {
    resource_group_name  = "rg-webapp-tfstate"
    storage_account_name = "stwebapptfstate20260830"
    container_name       = "tfstate"
    key                  = "webapp-dev.tfstate"

    use_oidc         = true
    use_azuread_auth = true
  }
}