# Security Policy

We take the security of Brasil SaaS ERP seriously. If you believe you have
found a security vulnerability, please report it responsibly.

## How to Report

**Do not open public GitHub issues for security vulnerabilities.**
Instead, email us at:

**euripedesdark@gmail.com**

Include the following information:

* A description of the vulnerability
* Steps to reproduce the issue
* The potential impact
* Any suggested fixes (if available)

## What to Expect

* We will acknowledge your report within 48 hours.
* We will investigate the issue and provide a timeline for a fix.
* We will credit you in the release notes (unless you prefer to remain anonymous).
* We will release a fix as soon as possible and notify you when it is available.

## Scope

This security policy applies to:

* The Brasil SaaS ERP application code
* The API endpoints
* The database schema and migrations
* The deployment scripts and configurations

## Out of Scope

* Third-party libraries (please report vulnerabilities to the respective projects)
* Issues in the documentation
* General questions about security best practices

## Security Measures

The project implements the following security measures:

* **Authentication:** JWT-based authentication with refresh tokens
* **Authorization:** Role-based access control (RBAC) with permissions
* **Multi-tenancy:** Data isolation between companies
* **Encryption:** TLS for all communications, mTLS for database connections
* **Input validation:** Bean Validation on all API endpoints
* **SQL injection prevention:** Parameterized queries via JPA
* **XSS prevention:** React's built-in escaping
* **CSRF protection:** Stateless API design
* **Audit logging:** Access logs and audit trails

## Disclosure Policy

We follow a coordinated disclosure policy. We ask that you:

* Give us a reasonable time to fix the issue before disclosing it publicly.
* Do not exploit the vulnerability beyond what is necessary to demonstrate it.
* Do not access or modify data belonging to other users.
