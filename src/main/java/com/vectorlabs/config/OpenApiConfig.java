package com.vectorlabs.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "VectorCheck API",
                version = "v1",
                description = """
API for technical inspections and report generation in compliance with NR-12 and NR-13 standards.

---
### 🌐 Domain Disclaimer
The domain names, URLs, and hostnames used in this API documentation
(e.g., `api.vectorlabs.com`, `vectorlabs.com`) are **fictional and provisional**.

They are used **solely for demonstration and documentation purposes**
and do not represent a finalized or officially registered domain.

If any of the mentioned domains are currently in use by third parties,
there is **no affiliation, endorsement, or relationship** between this project
and such entities.

### 🚧 Project Status
This API is **under active development** and represents an evolving software product.
New features, improvements, and refinements are continuously being added.

### 🎯 Purpose
The main goal of this project is to:
- Demonstrate backend engineering skills
- Showcase API design, security, and documentation best practices
- Serve as a professional portfolio for freelance and full-time opportunities

### 🧠 Technical Focus
This project emphasizes:
- Clean REST API design
- Strong validation and error handling
- JWT and OAuth2-based security
- Well-structured OpenAPI (Swagger) documentation
- Production-oriented architecture

### 📌 Notes
Although this API is functional and stable, some features may be incomplete or subject to change as part of ongoing development.
"""
,

contact = @Contact(
                        name = "Sandro Abreu",
                        email = "salgadoengSa@gmail.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local environment"),
                @Server(url = "https://api.vectorlabs.com", description = "Production environment")
        }
)
public class OpenApiConfig {
}
