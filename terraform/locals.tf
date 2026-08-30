locals {
  name_prefix = "${var.project_name}-${var.environment}"

  resource_group_name = "rg-${local.name_prefix}"

  vnet_name = "vnet-${local.name_prefix}"

  app_service_plan_name = "asp-${local.name_prefix}"

  app_service_name = "app-${local.name_prefix}"

  key_vault_name = "kv-${local.name_prefix}"

  sql_server_name = "sql-${local.name_prefix}"

  sql_database_name = var.sql_database_name

  log_analytics_name = "law-${local.name_prefix}"

  application_insights_name = "appi-${local.name_prefix}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}