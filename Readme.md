# Exemplo de integração Spring Boot com Keycloack

### 1. Requisitos
- Docker e Docker Compose
- Java 17
- Postman (para testar as requisições)

### 2. Executando o projeto
1. Clone o repositório
2. Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:
```
MYSQL_USER=
MYSQL_PASSWORD=
MYSQL_ROOT_PASSWORD=
KEYCLOAK_ADMIN_PASSWORD=
KEYCLOAK_ADMIN=
```
3. Execute o comando `docker-compose up -d` para subir o keycloack e mysql

#### 2.1. Keycloack
- Acesse o keycloack em `http://localhost:8080`
- Faça o login com o usuário que foi definido na variável `KEYCLOAK_ADMIN` e senha `KEYCLOAK_ADMIN_PASSWORD`
- Acesse o realm `marlon-example`
- Crie um novo usuário na aba `Users` e adicione a role `ADMIN` para o usuário criado

#### 2.2. Spring Boot
- Na classe UserService no método `addDefaultUser`, altere as informações para o usuário criado no keycloack.
- Execute o projeto

### 3. Testando
- Importe o arquivo `keycloak example.postman_collection.json` no Postman. Ele está localizado na pasta `imports`
- Dentro da collection há um script que insere automaticamente o token no header das requisições. Para que funcione, 
você precisa preencher a aba `Variables` com as informações do seu usuário criado no keycloack.