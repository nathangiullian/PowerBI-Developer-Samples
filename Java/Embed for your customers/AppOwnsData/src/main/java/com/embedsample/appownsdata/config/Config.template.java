// ----------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.
// ----------------------------------------------------------------------------

// ============================================================================
// SETUP INSTRUCTIONS:
// 1. Copy this file and rename it to Config.java (in the same folder)
// 2. Fill in your actual values below
// 3. Config.java is in .gitignore and will NOT be committed
// ============================================================================

package com.embedsample.appownsdata.config;

/**
 * Configuration class
 *
 * Copy this file to Config.java and fill in your values.
 * Config.java is ignored by git to keep secrets safe.
 */
public abstract class Config {

	// Set this to true, to show debug statements in console
	public static final boolean DEBUG = false;

	//	Two possible Authentication methods:
	//	- For authentication with master user credential choose MasterUser as AuthenticationType.
	//	- For authentication with app secret choose ServicePrincipal as AuthenticationType.
	//	More details here: https://aka.ms/EmbedServicePrincipal
	public static final String authenticationType = "ServicePrincipal";

	//	Common configuration properties for both authentication types
	// Enter workspaceId / groupId
	public static final String workspaceId = "YOUR_WORKSPACE_ID";

	// The id of the report to embed.
	public static final String reportId = "YOUR_REPORT_ID";

	// Enter Application Id / Client Id
	public static final String clientId = "YOUR_CLIENT_ID";

	// Enter MasterUser credentials (leave empty if using ServicePrincipal)
	public static final String pbiUsername = "";
	public static final String pbiPassword = "";

	// Enter ServicePrincipal credentials
	public static final String tenantId = "YOUR_TENANT_ID";
	public static final String appSecret = "YOUR_CLIENT_SECRET";

	// Effective Identity for DirectQuery/Live Connection reports
	// Set the username for the effective identity (e.g., user's email or UPN)
	// For RLS with a custom identifier like WebUserKey, put that value here
	public static final String effectiveIdentityUsername = "YOUR_USER_IDENTIFIER";
	// Set roles if using Row-Level Security (comma-separated), leave empty if not using RLS
	public static final String effectiveIdentityRoles = "RLS";

	//	DO NOT CHANGE
	public static final String authorityUrl = "https://login.microsoftonline.com/";
	public static final String scopeBase = "https://analysis.windows.net/powerbi/api/.default";


	private Config(){
		//Private Constructor will prevent the instantiation of this class directly
		throw new IllegalStateException("Config class");
	}

}
