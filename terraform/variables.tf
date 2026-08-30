variable "project_name" {
  description = "Project name"
  type        = string
  default     = "webapp"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "test", "prod"], var.environment)
    error_message = "Environment must be dev, test, or prod."
  }
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "East US"
}

variable "vnet_address_space" {
  description = "VNet address space"
  type        = list(string)
  default     = ["10.10.0.0/16"]
}

variable "app_service_subnet_address_prefixes" {
  description = "Subnet for App Service VNet integration"
  type        = list(string)
  default     = ["10.10.1.0/24"]
}

variable "private_endpoint_subnet_address_prefixes" {
  description = "Subnet for private endpoints"
  type        = list(string)
  default     = ["10.10.2.0/24"]
}

variable "sql_database_name" {
  description = "SQL database name"
  type        = string
  default     = "appdb"
}