workspace "Mail System" "Three-tier web mail system with layered Angular client and layered Spring Boot server." {

    model {
        endUser = person "Mail User" "Creates, reads, updates, deletes, and sends emails."

        mailServer = softwareSystem "Mail Server" "External mail transmission, implemented by another team. In this project it is mocked." {
            tags "External"
        }

        mailSystem = softwareSystem "Mail System" "Web-based system to manage emails and their attachments." {

            webUi = container "Mail App" "Single Page Application running in the user's browser." "Angular, TypeScript" {

                group "UI Layer" {
                    loginUi = component "Login UI" "Login form with client-side validation." "Angular Component"
                    emailListUi = component "Email List UI" "Lists all emails of the user." "Angular Component"
                    emailDetailUi = component "Email Detail UI" "Shows all email data, metadata, and attachment metadata." "Angular Component"
                    emailFormUi = component "Email Form UI" "Creates and edits emails and adds attachments, with client-side validation." "Angular Component"
                }

                group "Business Layer" {
                    authBusiness = component "Auth Business Service" "Coordinates login, logout, and the current session state." "Angular Service"
                    emailBusiness = component "Email Business Service" "Coordinates email workflows and UI state, e.g., sent emails are read-only." "Angular Service"
                }

                group "Service Layer" {
                    authClient = component "Auth API Client" "Calls the authentication REST endpoint and stores the token." "Angular HttpClient"
                    emailClient = component "Email API Client" "Calls the email, attachment, and transmission REST endpoints." "Angular HttpClient"
                    tokenInterceptor = component "Token Interceptor" "Adds the JWT to every outgoing request." "Angular HTTP Interceptor"
                }
            }

            application = container "Mail API" "REST API with business logic." "Spring Boot, Kotlin" {

                group "Api Layer" {
                    jwtFilter = component "JWT Authentication Filter" "Validates the JWT of each request without server-side session state." "Spring Security Filter"
                    authController = component "Auth RestApi" "Exposes the login endpoint." "Spring REST Controller"
                    emailController = component "Email RestApi" "Exposes email endpoints and maps DTOs." "Spring REST Controller"
                    attachmentController = component "Attachment RestApi" "Exposes attachment endpoints for upload, download, and delete." "Spring REST Controller"
                    transmissionController = component "Transmission RestApi" "Exposes the transmission resource used to send an email." "Spring REST Controller"
                    errorHandler = component "Error Handler" "Translates exceptions into error responses with fitting status codes." "Spring ControllerAdvice"
                }

                group "Business Layer" {
                    authService = component "Auth Service" "Verifies credentials and issues tokens." "Spring Service"
                    emailService = component "Email Service" "Applies the email business rules, status workflow, and metadata handling." "Spring Service"
                    attachmentService = component "Attachment Service" "Applies the attachment business rules and limits." "Spring Service"
                    transmissionService = component "Transmission Service" "Sends an email and sets its status to sent or error." "Spring Service"
                }

                group "Repository Layer" {
                    userRepository = component "User Repository" "Persists users." "Spring Data JPA"
                    emailRepository = component "Email Repository" "Persists emails with their metadata and attachment metadata." "Spring Data JPA"
                    attachmentFileStore = component "Attachment File Store" "Reads and writes attachment binaries in the configured folder." "Java NIO"
                    mailGateway = component "Mail Transmission Gateway" "Hides the external mail server behind an interface. Implemented by a mock." "Spring Component"
                }
            }

            database = container "Database" "Data tier: stores users, emails, metadata, and attachment metadata." "PostgreSQL" {
                tags "Database"
            }

            fileStorage = container "File Storage" "Stores attachment binaries in a configurable folder." "File System" {
                tags "Storage"
            }
        }

        # Level 1 and 2
        endUser -> webUi "Manages emails"
        webUi -> application "Calls" "HTTPS/JSON"
        application -> database "Reads and writes" "JDBC"
        application -> fileStorage "Reads and writes attachments" "File I/O"
        application -> mailServer "Sends emails" "Mocked interface"

        # Level 3: Angular client
        endUser -> loginUi "Logs in"
        endUser -> emailListUi "Browses emails"
        endUser -> emailDetailUi "Inspects emails"
        endUser -> emailFormUi "Writes emails"
        loginUi -> authBusiness "Uses"
        emailListUi -> emailBusiness "Uses"
        emailDetailUi -> emailBusiness "Uses"
        emailFormUi -> emailBusiness "Uses"
        authBusiness -> authClient "Uses"
        emailBusiness -> emailClient "Uses"
        authClient -> authController "Calls" "HTTPS/JSON"
        emailClient -> emailController "Calls" "HTTPS/JSON"
        emailClient -> attachmentController "Calls" "HTTPS/JSON"
        emailClient -> transmissionController "Calls" "HTTPS/JSON"
        tokenInterceptor -> emailClient "Adds token to requests"

        # Level 3: Spring Boot server
        jwtFilter -> authService "Validates tokens with"
        authController -> authService "Delegates to"
        emailController -> emailService "Delegates to"
        attachmentController -> attachmentService "Delegates to"
        transmissionController -> transmissionService "Delegates to"
        authService -> userRepository "Reads users"
        emailService -> emailRepository "Reads and writes emails"
        emailService -> attachmentFileStore "Deletes attachment files with the email"
        attachmentService -> attachmentFileStore "Reads and writes binaries"
        attachmentService -> emailRepository "Reads and writes emails and their attachment metadata"
        transmissionService -> emailRepository "Reads emails and updates status"
        transmissionService -> mailGateway "Sends through"
        userRepository -> database "Reads and writes" "JDBC"
        emailRepository -> database "Reads and writes" "JDBC"
        attachmentFileStore -> fileStorage "Reads and writes" "File I/O"
        mailGateway -> mailServer "Sends emails" "Mocked interface"

        production = deploymentEnvironment "Production" {
            deploymentNode "User Device" {
                deploymentNode "Web Browser" "Executes the Angular SPA." "Browser" {
                    containerInstance webUi
                }
            }
            deploymentNode "Application Runtime" "Executes the Spring Boot API." "JVM" {
                containerInstance application
            }
            deploymentNode "Database Runtime" "Hosts the application database." "PostgreSQL" {
                containerInstance database
            }
            deploymentNode "Server Disk" "Holds the attachment folder." "File System" {
                containerInstance fileStorage
            }
        }
    }

    views {
        systemContext mailSystem "SystemContext" {
            include *
            autoLayout lr
        }

        container mailSystem "ThreeTiers" {
            include *
            autoLayout lr
        }

        component webUi "AngularLayers" {
            include *
            autoLayout lr
        }

        component application "SpringBootLayers" {
            include *
            autoLayout lr
        }

        deployment mailSystem production "ProductionDeployment" {
            include *
            autoLayout lr
        }

        styles {
            element "Person" {
                shape Person
                background #08427B
                color #FFFFFF
            }
            element "Software System" {
                background #1168BD
                color #FFFFFF
            }
            element "External" {
                background #999999
                color #FFFFFF
            }
            element "Container" {
                background #1168BD
                color #FFFFFF
            }
            element "Component" {
                background #85BBF0
                color #000000
            }
            element "Database" {
                shape Cylinder
            }
            element "Storage" {
                shape Folder
            }
        }
    }
}
