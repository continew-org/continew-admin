<!--
Sync Impact Report:
- Version change: N/A → 1.0.0 (initial constitution with specified principles)
- Added principles: Modular Design with COLA Architecture, Alibaba Development Guidelines Compliance, Minimum Dependency Principle, Code Standardization and Style Consistency, Chinese Language for Speckit-Related Files
- Added sections: Architecture and Design Constraints, Development Workflow
- Templates requiring updates: ✅ plan-template.md, ✅ spec-template.md, ✅ tasks-template.md
- Follow-up TODOs: None
-->
# ContiNew Admin Constitution

## Core Principles

### Modular Design with COLA Architecture
All system components MUST follow the COLA (Clean Object-oriented and Layered Architecture) pattern. This includes clear separation of concerns across Domain, Application, Interface, and Infrastructure layers. Modules MUST be designed with high cohesion and low coupling, enabling independent development, testing, and deployment. Each module MUST have well-defined interfaces and minimal cross-module dependencies.

### Alibaba Development Guidelines Compliance
All code MUST strictly adhere to the Alibaba Java Coding Guidelines. This includes naming conventions, code structure, documentation standards, and best practices for security and performance. Static analysis tools MUST be used to enforce compliance, and code reviews MUST verify adherence to these standards. All code submissions MUST pass SonarQube quality gates.

### Minimum Dependency Principle
Code dependencies MUST be minimized following the principle of least privilege. External libraries SHOULD only be introduced when essential functionality cannot be achieved with existing dependencies. Internal module dependencies MUST be kept to a minimum, and circular dependencies are strictly prohibited. Dependency injection MUST be used to manage component relationships and reduce tight coupling.

### Code Standardization and Style Consistency
Code formatting, naming conventions, and architectural patterns MUST be consistent across the entire codebase. All team members MUST use the same code formatters and style configurations. Code reviews MUST verify style consistency, and automated tools MUST enforce formatting standards. Documentation MUST follow a consistent structure and use standardized terminology.

### Chinese Language for Speckit-Related Files
All Speckit-related files and documentation MUST be written in Chinese to ensure accessibility for the development team. This includes configuration files, templates, documentation, and comments within Speckit-related code. Internationalization considerations MUST be made for user-facing content, while internal development artifacts remain in Chinese for team efficiency.

## Architecture and Design Constraints

Module boundaries MUST be clearly defined with well-documented interfaces. The system MUST follow the COLA architecture pattern with distinct Domain, Application, Interface, and Infrastructure layers. Dependency injection frameworks MUST be used consistently across all modules. Service layer design MUST follow the principles of high cohesion and low coupling. Component isolation MUST be maintained to enable independent testing and deployment.

## Development Workflow

Code reviews are mandatory for all pull requests with at least one senior team member approval required. All changes MUST pass automated CI checks including code formatting, static analysis, unit tests, and security scans. Branch naming conventions MUST be followed, and feature branches SHOULD be kept small and focused to enable efficient review and integration. All code MUST comply with Alibaba Java Coding Guidelines before merging.

## Governance

This constitution supersedes all other development practices and guidelines within the ContiNew Admin project. Amendments to this constitution require formal documentation, team discussion, approval from project maintainers, and a migration plan for existing code. All pull requests and code reviews MUST verify compliance with these principles, and complexity must be justified with clear benefits that outweigh the added maintenance burden.

**Version**: 1.0.0 | **Ratified**: 2024-01-01 | **Last Amended**: 2025-12-30
