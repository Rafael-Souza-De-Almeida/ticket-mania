# 🎟️ Ticket Mania API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/Rabbitmq-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

## 📖 Sobre o Projeto

O **Ticket Mania** é uma API RESTful desenvolvida em Spring Boot para gerenciamento e processamento de pedidos de ingressos. A arquitetura foi desenhada para ser escalável e resiliente, utilizando mensageria para processamento assíncrono e cache para alta performance.

## 🚀 Tecnologias Utilizadas

*   **Linguagem & Framework:** Java 17+, Spring Boot
*   **Banco de Dados Relacional:** PostgreSQL (Armazenamento principal de pedidos e usuários)
*   **Cache & Lock Distribuído:** Redis / Redisson (Gerenciamento de concorrência e alta performance)
*   **Mensageria:** RabbitMQ (Fila de processamento de pedidos)
*   **Build & Containerização:** Gradle, Docker
*   **Integrações Externas:** Upstash (Serverless Redis), CloudAMQP (Managed RabbitMQ)

## ⚙️ Arquitetura e Fluxo

1. O cliente envia uma requisição de compra de ingresso para o `OrderController`.
2. A aplicação verifica a disponibilidade no **Redis** para garantir resposta em milissegundos e evitar *overbooking*.
3. O pedido é salvo com status "Pendente" no **PostgreSQL**.
4. Uma mensagem é publicada no **RabbitMQ** para processamento assíncrono do pagamento e emissão do ticket.

## 📋 Pré-requisitos

Para rodar este projeto localmente, você precisará ter instalado:
*   [Java 17+](https://adoptium.net/)
*   [Docker](https://www.docker.com/) e Docker Compose
*   [Git](https://git-scm.com/)

# Clone o repositório
git clone [https://github.com/seu-usuario/ticket-mania.git](https://github.com/Rafael-Souza-De-Almeida/ticket-mania.git)

# Entre no diretório
cd ticket-mania

# Faça o build da aplicação via Gradle
./gradlew clean build -x test

# Suba os containers (API, Postgres, Redis, RabbitMQ)
docker-compose up -d

## 📚 Documentação da API (Swagger)

A API foi totalmente documentada utilizando o **SpringDoc OpenAPI (Swagger)**. Esta interface gráfica e interativa permite visualizar, testar e compreender o funcionamento de todos os endpoints disponíveis sem a necessidade de ferramentas externas (como Postman ou Insomnia).

<img width="1743" height="896" alt="image" src="https://github.com/user-attachments/assets/d11a1ab5-73f0-49fc-86b5-1f459a02cc11" />
<img width="1620" height="346" alt="image" src="https://github.com/user-attachments/assets/5383df72-c90c-498a-93f5-053bbdc2f558" />



**Como acessar:**
1. Certifique-se de que a aplicação está rodando localmente (via Docker ou Gradle).
2. Abra o seu navegador e acesse a seguinte URL:
   ```text
   http://localhost:8080/swagger-ui.html

## ⚙️ Integração Contínua (CI) com GitHub Actions

O projeto utiliza o **GitHub Actions** para garantir a integridade do código e automatizar a validação a cada nova atualização.

O fluxo de CI foi configurado para disparar automaticamente sempre que um novo código é enviado (`push` ou `pull request`) para a branch principal. O pipeline executa os seguintes passos:
1. Configuração do ambiente com a versão correta do Java (17+).
2. Resolução de dependências via Gradle.
3. Execução automatizada da suíte de testes da aplicação.
4. Validação do build do projeto.

Isso garante que nenhuma nova funcionalidade quebre os contratos da API ou a lógica de negócio já existente antes de ser integrada.

## 🧪 Testes de Carga e Performance (k6)

Como um sistema de venda de ingressos lida com picos de acessos severos (alta concorrência), a arquitetura foi validada utilizando o **Grafana k6**. 

Os testes foram desenhados para simular dezenas de usuários simultâneos tentando comprar o mesmo ingresso ao mesmo tempo, validando a eficácia das nossas ferramentas:
1. **Redisson (Distributed Locks):** Comprovamos que o cache impediu o *overbooking* (venda de ingressos além do limite disponível) durante os picos de concorrência.
2. **RabbitMQ:** Validamos que a fila de mensageria absorveu a carga de requisições de compra, impedindo que o banco de dados (PostgreSQL) fosse sobrecarregado pelas transações simultâneas.

**Como rodar os testes de carga localmente:**
Os scripts do k6 estão localizados no arquivo `benchmark.js`. Para executá-los, instale o k6 na sua máquina e rode:

```bash
k6 run benchmark.js
