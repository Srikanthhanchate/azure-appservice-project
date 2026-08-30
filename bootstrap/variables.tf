variable "location" {
  description = "Azure region"
  type        = string
  default     = "East US"
}

variable "github_actions_managed_identity_principal_id" {
  description = "Principal ID (Object ID) of the GitHub Actions managed identity used for OIDC authentication"
  type        = string
  sensitive   = true
}