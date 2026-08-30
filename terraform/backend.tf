terraform {
  backend "azurerm" {
    resource_group_name  = "rg-webapp-tfstate"
    storage_account_name = "stwebapptfstate20260830"
    container_name       = "tfstate"
    key                  = "webapp-dev.tfstate"

    use_oidc         = true
    use_azuread_auth = true

    tenant_id       = "6c794cbc-3892-48c4-a876-3001cc64351a"
    client_id       = "c6a0888e-1895-40f8-a14d-5d3d6ef2ca1b"
  }
}