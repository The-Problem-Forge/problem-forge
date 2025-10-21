# Agent Guidelines for Problem Forge

## Build Commands
- **Backend (Kotlin/Spring Boot)**: `./gradlew build` (full build), `./gradlew bootRun` (dev server)
- **Frontend (React)**: `npm run build` (production), `npm start` (dev server)
- **Full deployment**: `./deploy.sh` (Docker Compose)

## Code Style Guidelines

### Kotlin (Backend)
- **Naming**: camelCase for functions/methods, PascalCase for classes/interfaces
- **Imports**: Group by package, blank line between groups
- **Formatting**: 4-space indentation, no semicolons, trailing commas in multi-line calls
- **Types**: Explicit types preferred, use `val` over `var`
- **Error handling**: Use try-catch with specific exceptions, avoid generic Exception

### JavaScript/React (Frontend)

#### Mandatory requirements
- **Naming**: camelCase for variables/functions, PascalCase for components
- **Imports**: ES6 imports, group React imports first, then third-party, then local
- **Formatting**: run `npx prettier frontend --write` after EVERY change
- **JSDOC**: ALL functions, classes and interfaces have to have JSDoc comments
    - Mandatory params (if applicable): @param, @return, @throws
    - Example: ```javascript
    Sends message to passed api url
    @param url - api url to send message to
    @param message - text content of the message
    @return - Promise with send result
    @throws { Error } If validation fails
    ```
- **Files**: DO NOT CHANGE FILES THAT USER CHANGED BEFORE INVOCATIONS - ask if this file needs to change

#### Additional requirements
- Error handling: try-catch in async functions, use console.error to log user-readable messages about errors
- Styling: Use separate CSS files for styles
- Reuse components and code where possible
- Try to find a library that does what you need before implementing it yourself. Ask before adding a library
- Comments:
    - Comment complex business logic
    - Do not comment on obvious code

### General
- **Commits**: Use imperative mood, 50-char subject, 72-char body lines
- **Security**: Never log sensitive data, validate inputs, use HTTPS in production