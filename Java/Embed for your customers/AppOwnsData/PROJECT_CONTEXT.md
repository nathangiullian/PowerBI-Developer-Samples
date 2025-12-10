# Power BI Embedded - Java Spring Boot Implementation

## Overview

This project demonstrates embedding Power BI reports and individual visuals into a Java Spring Boot application using the "Embed for Your Customers" (App Owns Data) approach with Row-Level Security (RLS).

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Browser                           │
│  ┌─────────────────┐  ┌─────────────────┐                      │
│  │  Visual 1       │  │  Visual 2       │                      │
│  │  (iframe)       │  │  (iframe)       │                      │
│  └────────┬────────┘  └────────┬────────┘                      │
└───────────┼────────────────────┼────────────────────────────────┘
            │                    │
            ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Power BI JavaScript SDK                        │
│                   (powerbi-client)                               │
└────────────────────────────┬────────────────────────────────────┘
                             │ GET /getembedinfo
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot Backend                            │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │ EmbedController  │──│ PowerBIService   │                    │
│  └──────────────────┘  └────────┬─────────┘                    │
│                                 │                               │
│  ┌──────────────────┐           │                               │
│  │ AzureADService   │◄──────────┘                               │
│  └────────┬─────────┘                                           │
└───────────┼─────────────────────────────────────────────────────┘
            │ OAuth 2.0 Client Credentials
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Azure AD (Entra ID)                            │
│                   Service Principal Auth                         │
└────────────────────────────┬────────────────────────────────────┘
                             │ Access Token
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Power BI REST API                              │
│  - Get Report Details                                            │
│  - Generate Embed Token (with RLS identity)                      │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### Backend (Java)

| File | Purpose |
|------|---------|
| `Application.java` | Spring Boot entry point |
| `Config.java` | Configuration (credentials, workspace/report IDs, RLS settings) |
| `EmbedController.java` | REST endpoints (`/`, `/getembedinfo`) |
| `AzureADService.java` | Azure AD authentication via MSAL |
| `PowerBIService.java` | Power BI API calls, embed token generation with RLS |

### Frontend

| File | Purpose |
|------|---------|
| `index.html` | Static HTML with Power BI JavaScript SDK integration |

## Prerequisites

### 1. Azure AD App Registration

1. Go to https://aka.ms/EmbedForCustomer
2. Sign in with your organizational account
3. Register a new application
4. Save these values:
   - **Client ID** (Application ID)
   - **Tenant ID**
   - **Client Secret** (generate one and save immediately)

### 2. Power BI Admin Settings

In Power BI Admin Portal (https://app.powerbi.com/admin-portal/tenantSettings):

1. Enable **"Allow service principals to use Power BI APIs"**
2. Enable **"Embed content in apps"**
3. Add your security group (containing the service principal) to both settings

### 3. Workspace Access

1. Create or use an existing Power BI workspace
2. Add the Service Principal (or its security group) as **Admin** or **Member**

### 4. Development Environment

- **Java 17** (Amazon Corretto recommended for Windows stability)
- **Maven** for dependency management
- **VS Code** or any IDE

## Configuration

### Config.java

```java
// Authentication type
public static final String authenticationType = "ServicePrincipal";

// Power BI workspace and report
public static final String workspaceId = "your-workspace-guid";
public static final String reportId = "your-report-guid";

// Azure AD credentials
public static final String clientId = "your-client-id";
public static final String tenantId = "your-tenant-id";
public static final String appSecret = "your-client-secret";

// Row-Level Security (for DirectQuery/Live Connection)
public static final String effectiveIdentityUsername = "user-identifier";
public static final String effectiveIdentityRoles = "RLS";  // Your RLS role name
```

### Finding Workspace and Report IDs

From your Power BI report URL:
```
https://app.powerbi.com/groups/{workspaceId}/reports/{reportId}/...
```

## Row-Level Security (RLS)

This implementation supports RLS for DirectQuery and Live Connection data sources.

### How It Works

1. The `effectiveIdentityUsername` is passed to Power BI when generating the embed token
2. Power BI applies this username to your RLS DAX filter (using `USERNAME()` or `USERPRINCIPALNAME()`)
3. Users only see data matching their identity

### Configuration

In `Config.java`:
```java
public static final String effectiveIdentityUsername = "1327";  // e.g., WebUserKey
public static final String effectiveIdentityRoles = "RLS";      // RLS role name in Power BI
```

### Making It Dynamic

For production, modify `PowerBIService.getEmbedToken()` to accept the username as a parameter instead of reading from Config.

## Embedding Individual Visuals

### Finding Visual Names

1. Save your Power BI report as `.pbip` (Power BI Project) format
2. Navigate to: `YourReport.Report/definition/pages/{pageName}/visuals/`
3. Each folder is a visual; open `visual.json` to see the visual type and data

### Configuration in index.html

```javascript
var visualsToEmbed = [
    {
        containerId: "visual-container-1",
        pageName: "c16e63727a3151412611",      // From report URL or PBIP
        visualName: "788f90a6494ba18eff1a"     // Folder name in PBIP
    },
    {
        containerId: "visual-container-2",
        pageName: "c16e63727a3151412611",
        visualName: "0c399b8a4831f39005a3"
    }
];
```

### Embed Configuration

```javascript
var visualEmbedConfig = {
    type: "visual",                              // "visual" not "report"
    tokenType: models.TokenType.Embed,
    accessToken: embedData.embedToken,
    embedUrl: embedData.embedReports[0].embedUrl,
    pageName: visualConfig.pageName,
    visualName: visualConfig.visualName,
    settings: {
        filterPaneEnabled: false,
        navContentPaneEnabled: false
    }
};
```

## Running the Application

### Start the Server

```bash
cd "Java/Embed for your customers/AppOwnsData"
mvn spring-boot:run
```

### Access the Application

Open http://localhost:8080

### Stop the Server

Press `Ctrl+C` in the terminal

## Project Structure

```
AppOwnsData/
├── pom.xml                          # Maven dependencies (Spring Boot 2.7.18)
├── src/
│   └── main/
│       ├── java/com/embedsample/appownsdata/
│       │   ├── Application.java     # Spring Boot entry point
│       │   ├── config/
│       │   │   └── Config.java      # All configuration
│       │   ├── controllers/
│       │   │   └── EmbedController.java
│       │   ├── models/
│       │   │   ├── EmbedConfig.java
│       │   │   ├── EmbedToken.java
│       │   │   └── ReportConfig.java
│       │   └── services/
│       │       ├── AzureADService.java
│       │       └── PowerBIService.java
│       └── resources/
│           ├── application.properties
│           └── static/
│               └── index.html       # Frontend with Power BI JS SDK
```

## Dependencies (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JSON processing -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20231013</version>
    </dependency>

    <!-- Azure AD authentication -->
    <dependency>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>msal4j</artifactId>
        <version>1.14.0</version>
    </dependency>
</dependencies>
```

## Troubleshooting

### 401 Unauthorized

- Verify Service Principal is added to the Power BI workspace
- Check that admin settings are enabled in Power BI Admin Portal
- Confirm Client ID, Tenant ID, and Secret are correct

### 400 Bad Request - Effective Identity Required

Your report uses DirectQuery or Live Connection. You must provide:
- `effectiveIdentityUsername` in Config.java
- `effectiveIdentityRoles` matching your RLS role name

### JVM Crashes on Windows

If you see `EXCEPTION_ACCESS_VIOLATION` errors:
1. Use Amazon Corretto 17 instead of Temurin
2. Use static HTML instead of JSP (already done in this project)

### Visual Not Loading

- Verify `pageName` and `visualName` are correct
- Check browser console for specific error messages
- Ensure the visual exists on the specified page

## Security Notes

1. **Never commit secrets** - Use environment variables or a secret manager in production
2. **Rotate secrets regularly** - Azure AD allows multiple secrets for zero-downtime rotation
3. **Use security groups** - Manage Service Principal access via security groups
4. **Validate user input** - When making RLS dynamic, validate the user identifier

## Recreating in Another Java Application

### Step 1: Add Dependencies

Add to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>msal4j</artifactId>
    <version>1.14.0</version>
</dependency>
```

### Step 2: Copy Core Files

Copy these files to your project:
- `Config.java` - Update with your credentials
- `AzureADService.java` - No changes needed
- `PowerBIService.java` - Contains RLS support

### Step 3: Create REST Endpoint

Create a controller with `/getembedinfo` endpoint (see `EmbedController.java`)

### Step 4: Add Frontend

Include the Power BI JavaScript SDK:
```html
<script src="https://cdn.jsdelivr.net/npm/powerbi-client@2.22.0/dist/powerbi.min.js"></script>
```

Call your `/getembedinfo` endpoint and use `powerbi.embed()` with the returned token.

### Step 5: Configure Azure

1. Register app at https://aka.ms/EmbedForCustomer
2. Enable admin settings in Power BI
3. Add Service Principal to workspace

## References

- [Power BI Embedded Documentation](https://docs.microsoft.com/en-us/power-bi/developer/embedded/)
- [Power BI JavaScript SDK](https://github.com/microsoft/PowerBI-JavaScript)
- [Service Principal Authentication](https://aka.ms/EmbedServicePrincipal)
- [Row-Level Security](https://docs.microsoft.com/en-us/power-bi/developer/embedded/embedded-row-level-security)
